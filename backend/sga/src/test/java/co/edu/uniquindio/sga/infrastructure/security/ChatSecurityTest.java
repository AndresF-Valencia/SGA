package co.edu.uniquindio.sga.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Verifica que el chatbot FAQ solo esté disponible para usuarios con sesión iniciada.
 * Se fuerza la clave de Gemini vacía para que la llamada autenticada termine en 503
 * (mensaje amable) sin salir a Internet.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "chatbot.api-key=")
@DisplayName("Chatbot: solo usuarios autenticados")
class ChatSecurityTest {

    private static final String CLAVE = "Sanrocco123";
    private static final String PREGUNTA =
            "{\"messages\":[{\"role\":\"user\",\"content\":\"Como me registro?\"}]}";

    @Autowired
    private Environment entorno;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> enviar(String ruta, String token, String json) throws Exception {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + entorno.getProperty("local.server.port") + ruta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String iniciarSesion() throws Exception {
        HttpResponse<String> respuesta = enviar("/api/auth/login", null,
                "{\"correo\":\"huesped@sanrocco.co\",\"contrasena\":\"" + CLAVE + "\"}");
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        Matcher token = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(respuesta.body());
        assertTrue(token.find(), "La respuesta del login no trae token: " + respuesta.body());
        return token.group(1);
    }

    @Test
    @DisplayName("Sin token, POST /api/chat responde 401")
    void sinToken() throws Exception {
        HttpResponse<String> respuesta = enviar("/api/chat", null, PREGUNTA);
        assertEquals(401, respuesta.statusCode(), respuesta.body());
    }

    @Test
    @DisplayName("Con sesión y sin clave de Gemini responde 503 con mensaje amable")
    void conSesionSinClave() throws Exception {
        String token = iniciarSesion();

        HttpResponse<String> respuesta = enviar("/api/chat", token, PREGUNTA);

        assertEquals(503, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("Ahora mismo no puedo responder"), respuesta.body());
    }
}
