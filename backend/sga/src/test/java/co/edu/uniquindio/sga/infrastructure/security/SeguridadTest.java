package co.edu.uniquindio.sga.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prueba de integración de la seguridad: levanta el servidor en un puerto al
 * azar y le hace peticiones HTTP de verdad, como lo haría Postman.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Seguridad: JWT y roles (7.2)")
class SeguridadTest {

    private static final String CLAVE = "Sanrocco123";   // clave de los usuarios de prueba

    @Autowired private Environment entorno;
    @Autowired private JwtServicio jwtServicio;
    @Autowired private UsuarioRepository usuarios;

    private final HttpClient http = HttpClient.newHttpClient();

    // ---------- Ayudas ----------

    private HttpResponse<String> enviar(String metodo, String ruta, String token, String json) throws Exception {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + entorno.getProperty("local.server.port") + ruta))
                .header("Content-Type", "application/json")
                .method(metodo, json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String iniciarSesion(String correo) throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + CLAVE + "\"}");
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        Matcher token = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(respuesta.body());
        assertTrue(token.find(), "La respuesta del login no trae token: " + respuesta.body());
        return token.group(1);
    }

    // ---------- Pruebas ----------

    @Test
    @DisplayName("Al arrancar existe un usuario de prueba por rol, con la contraseña cifrada")
    void usuariosDePrueba() {
        var admin = usuarios.buscarPorCorreo(new Correo("admin@sanrocco.co")).orElseThrow();
        assertEquals(Rol.ADMINISTRADOR, admin.getRol());
        assertTrue(admin.getContrasenaCifrada().startsWith("$2"), "Debe guardarse con BCrypt");
        assertTrue(usuarios.existePorCorreo(new Correo("recepcion@sanrocco.co")));
        assertTrue(usuarios.existePorCorreo(new Correo("servicio@sanrocco.co")));
        assertTrue(usuarios.existePorCorreo(new Correo("huesped@sanrocco.co")));
    }

    @Test
    @DisplayName("Login correcto da un token válido con el rol; un token alterado se rechaza")
    void loginYToken() throws Exception {
        String token = iniciarSesion("admin@sanrocco.co");

        var usuario = jwtServicio.validar(token).orElseThrow();
        assertEquals("admin@sanrocco.co", usuario.correo());
        assertEquals(Rol.ADMINISTRADOR, usuario.rol());
        assertFalse(jwtServicio.validar(token + "x").isPresent());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta: 401")
    void loginIncorrecto() throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null,
                "{\"correo\":\"admin@sanrocco.co\",\"contrasena\":\"Incorrecta1\"}");
        assertEquals(401, respuesta.statusCode());
    }

    @Test
    @DisplayName("Sin token, una ruta protegida responde 401 en JSON")
    void sinToken() throws Exception {
        HttpResponse<String> respuesta = enviar("PUT", "/api/reservas/RES-2026-00001/confirmar", null, null);
        assertEquals(401, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"status\":401"), respuesta.body());
    }

    @Test
    @DisplayName("Un huésped no puede tocar la configuración del alojamiento: 403")
    void huespedSinPermiso() throws Exception {
        String token = iniciarSesion("huesped@sanrocco.co");

        HttpResponse<String> respuesta = enviar("PUT", "/api/alojamiento/parametros", token, "{}");
        assertEquals(403, respuesta.statusCode());
    }

    @Test
    @DisplayName("El personal de servicio no puede crear usuarios: 403")
    void servicioSinPermiso() throws Exception {
        String token = iniciarSesion("servicio@sanrocco.co");

        HttpResponse<String> respuesta = enviar("POST", "/api/usuarios", token, "{}");
        assertEquals(403, respuesta.statusCode());
    }

    @Test
    @DisplayName("Registro público: crea un huésped (201) y no permite repetir el correo (409)")
    void registro() throws Exception {
        String json = "{\"nombre\":\"Pedro Pérez\",\"correo\":\"pedro.seguridad@mail.com\","
                + "\"contrasena\":\"Clave1234\",\"telefono\":\"3001112233\",\"documento\":\"55500011\","
                + "\"fechaNacimiento\":\"1990-05-01\"}";

        HttpResponse<String> primera = enviar("POST", "/api/auth/registro", null, json);
        assertEquals(201, primera.statusCode(), primera.body());
        assertTrue(primera.body().contains("\"rol\":\"HUESPED\""), primera.body());
        assertFalse(primera.body().contains("contrasena"), "Nunca se devuelve la contraseña");

        assertEquals(409, enviar("POST", "/api/auth/registro", null, json).statusCode());
    }

    @Test
    @DisplayName("Registro con contraseña débil: 400 por validación")
    void registroContrasenaDebil() throws Exception {
        String json = "{\"nombre\":\"Ana\",\"correo\":\"ana.debil@mail.com\",\"contrasena\":\"123\","
                + "\"telefono\":\"300\",\"documento\":\"777\",\"fechaNacimiento\":\"1990-05-01\"}";
        assertEquals(400, enviar("POST", "/api/auth/registro", null, json).statusCode());
    }

    @Test
    @DisplayName("El administrador crea un recepcionista (201)")
    void adminCreaUsuario() throws Exception {
        String token = iniciarSesion("admin@sanrocco.co");
        String json = "{\"nombre\":\"Recepción Noche\",\"correo\":\"noche@sanrocco.co\",\"contrasena\":\"Noche1234\","
                + "\"telefono\":\"3009998877\",\"documento\":\"1000000099\",\"fechaNacimiento\":\"1992-02-02\","
                + "\"rol\":\"RECEPCIONISTA\"}";

        HttpResponse<String> respuesta = enviar("POST", "/api/usuarios", token, json);
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("\"rol\":\"RECEPCIONISTA\""), respuesta.body());
    }
}
