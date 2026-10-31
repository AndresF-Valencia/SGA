package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.application.exception.CodigoRecuperacionInvalidoException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.port.NotificadorUsuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.UsuarioRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DisplayName("Usuarios 7.2: perfil, contraseña, recuperación, roles y eliminación")
class GestionUsuariosUseCaseTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 27);
    private static final LocalDateTime AHORA_ = LocalDateTime.of(2026, 10, 27, 10, 0);
    private static final CifradorContrasena CIFRADOR = new CifradorContrasena() {
        @Override
        public String cifrar(String plana) {
            return "cifrada:" + plana;
        }

        @Override
        public boolean coincide(String plana, String cifrada) {
            return cifrada.equals("cifrada:" + plana);
        }
    };
    private static final Correo LAURA_CORREO = new Correo("laura@example.com");
    private static final Correo ADMIN_CORREO = new Correo("admin@sanrocco.co");

    private UsuarioRepositoryEnMemoria usuarios;
    private ReservaRepositoryEnMemoria reservas;
    private GestionarPerfilUseCase perfil;
    private RecuperarContrasenaUseCase recuperar;
    private AdministrarUsuariosUseCase administrar;
    private final List<String> codigosEnviados = new ArrayList<>();

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEnMemoria();
        reservas = new ReservaRepositoryEnMemoria();
        // Laura tiene el mismo documento que la titular de las reservas del Escenario
        usuarios.guardar(Usuario.registrarHuesped(LAURA_CORREO, "Laura Gómez", "Clave1234", "3001234567",
                new DocumentoIdentidad("1094000001"), LocalDate.of(1990, 5, 1), CIFRADOR, HOY));
        usuarios.guardar(Usuario.registrar(ADMIN_CORREO, "Admin", "Clave1234", "3000000000",
                new DocumentoIdentidad("1094999999"), LocalDate.of(1985, 1, 1), Rol.ADMINISTRADOR, CIFRADOR, HOY));
        NotificadorUsuario notificador = (usuario, codigo, minutos) -> codigosEnviados.add(codigo);
        perfil = new GestionarPerfilUseCase(usuarios, CIFRADOR);
        recuperar = new RecuperarContrasenaUseCase(usuarios, CIFRADOR, notificador, () -> "123456");
        administrar = new AdministrarUsuariosUseCase(usuarios, reservas);
    }

    private Usuario laura() {
        return usuarios.buscarPorCorreo(LAURA_CORREO).orElseThrow();
    }

    // ---------- Perfil y contraseña ----------

    @Test
    @DisplayName("actualiza nombre, teléfono y foto; la foto debe ser una URL")
    void actualizarPerfil() {
        perfil.actualizar(LAURA_CORREO, "Laura G.", "3110000000", "https://img.example.com/laura.jpg");
        assertEquals("Laura G.", laura().getNombre());
        assertEquals("https://img.example.com/laura.jpg", laura().getFotoPerfil());

        assertThrows(ReglaDominioException.class,
                () -> perfil.actualizar(LAURA_CORREO, "Laura", "311", "foto.jpg"));
    }

    @Test
    @DisplayName("cambiar la contraseña exige la actual correcta y una nueva segura")
    void cambiarContrasena() {
        assertThrows(ReglaDominioException.class, () -> perfil.cambiarContrasena(LAURA_CORREO, "Otra1234", "Nueva1234"));
        assertThrows(ReglaDominioException.class, () -> perfil.cambiarContrasena(LAURA_CORREO, "Clave1234", "debil"));

        perfil.cambiarContrasena(LAURA_CORREO, "Clave1234", "Nueva1234");
        assertTrue(laura().puedeIniciarSesion("Nueva1234", CIFRADOR));
        assertFalse(laura().puedeIniciarSesion("Clave1234", CIFRADOR));
    }

    // ---------- Recuperación ----------

    @Test
    @DisplayName("el código llega al correo y sirve una sola vez")
    void recuperarUnSoloUso() {
        recuperar.solicitar(LAURA_CORREO, AHORA_);
        assertEquals(List.of("123456"), codigosEnviados);
        assertTrue(laura().getCodigoRecuperacionCifrado() != null);

        recuperar.restablecer(LAURA_CORREO, "123456", "Nueva1234", AHORA_.plusMinutes(5));
        assertTrue(laura().puedeIniciarSesion("Nueva1234", CIFRADOR));
        assertNull(laura().getCodigoRecuperacionCifrado());

        assertThrows(CodigoRecuperacionInvalidoException.class,
                () -> recuperar.restablecer(LAURA_CORREO, "123456", "Otra12345", AHORA_.plusMinutes(6)));
    }

    @Test
    @DisplayName("el código vence a los 15 minutos")
    void codigoVencido() {
        recuperar.solicitar(LAURA_CORREO, AHORA_);
        assertThrows(CodigoRecuperacionInvalidoException.class,
                () -> recuperar.restablecer(LAURA_CORREO, "123456", "Nueva1234", AHORA_.plusMinutes(16)));
        assertTrue(laura().puedeIniciarSesion("Clave1234", CIFRADOR));
    }

    @Test
    @DisplayName("tras 5 intentos fallidos el código se anula")
    void intentosFallidos() {
        recuperar.solicitar(LAURA_CORREO, AHORA_);
        for (int i = 0; i < 5; i++) {
            assertThrows(CodigoRecuperacionInvalidoException.class,
                    () -> recuperar.restablecer(LAURA_CORREO, "000000", "Nueva1234", AHORA_.plusMinutes(1)));
        }
        assertThrows(CodigoRecuperacionInvalidoException.class,
                () -> recuperar.restablecer(LAURA_CORREO, "123456", "Nueva1234", AHORA_.plusMinutes(2)));
    }

    @Test
    @DisplayName("con un correo que no existe no se envía nada ni se revela el error")
    void correoInexistente() {
        recuperar.solicitar(new Correo("nadie@example.com"), AHORA_);
        assertTrue(codigosEnviados.isEmpty());
    }

    // ---------- Administración ----------

    @Test
    @DisplayName("lista con filtros por rol y cambia el rol")
    void listarYCambiarRol() {
        assertEquals(1, administrar.listar(Rol.HUESPED, true).size());
        administrar.cambiarRol(LAURA_CORREO, Rol.RECEPCIONISTA);
        assertEquals(Rol.RECEPCIONISTA, laura().getRol());
        assertEquals(0, administrar.listar(Rol.HUESPED, null).size());
    }

    @Test
    @DisplayName("nunca se queda el sistema sin administrador activo")
    void ultimoAdministrador() {
        assertThrows(ReglaDominioException.class, () -> administrar.cambiarRol(ADMIN_CORREO, Rol.HUESPED));
        assertThrows(ReglaDominioException.class, () -> administrar.eliminar(ADMIN_CORREO));
    }

    @Test
    @DisplayName("no se elimina al titular de reservas activas; sin ellas, la eliminación es lógica")
    void eliminarConReservasActivas() {
        Reserva activa = reservaConfirmada("RES-1", NOVIEMBRE);   // titular: Laura
        reservas.guardar(activa);
        assertThrows(ReglaDominioException.class, () -> administrar.eliminar(LAURA_CORREO));

        activa.cancelar("sin viaje", RECEPCION, AHORA);
        reservas.guardar(activa);
        administrar.eliminar(LAURA_CORREO);
        assertFalse(laura().estaActivo());
        assertFalse(laura().puedeIniciarSesion("Clave1234", CIFRADOR));
        assertEquals(1, reservas.obtenerTodas().size());   // la reserva histórica se conserva
    }
}
