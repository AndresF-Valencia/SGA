package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.port.EmisorToken;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.UsuarioRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

@DisplayName("Casos de uso de usuarios y sesión")
class UsuariosUseCaseTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 27);
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
    /** Emisor falso: el token es "token-" + correo. */
    private static final EmisorToken EMISOR = usuario ->
            new EmisorToken.TokenAcceso("token-" + usuario.getCorreo().valor(), Instant.EPOCH);

    private UsuarioRepositoryEnMemoria usuarios;
    private RegistrarHuespedUseCase registrar;
    private CrearUsuarioInternoUseCase crearInterno;
    private IniciarSesionUseCase iniciarSesion;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEnMemoria();
        registrar = new RegistrarHuespedUseCase(usuarios, CIFRADOR);
        crearInterno = new CrearUsuarioInternoUseCase(usuarios, CIFRADOR);
        iniciarSesion = new IniciarSesionUseCase(usuarios, CIFRADOR, EMISOR);
    }

    private Usuario registrarLaura() {
        return registrar.ejecutar(new Correo("laura@mail.com"), "Laura Gómez", "Secreta123", "3001234567",
                new DocumentoIdentidad("1094000001"), LocalDate.of(1995, 3, 10), HOY);
    }

    @Test
    @DisplayName("Registrar: guarda un huésped y no permite repetir el correo")
    void registrarHuesped() {
        registrarLaura();

        assertTrue(usuarios.existePorCorreo(new Correo("laura@mail.com")));
        assertThrows(ReglaDominioException.class, this::registrarLaura);
    }

    @Test
    @DisplayName("Crear usuario interno: el administrador asigna el rol")
    void crearInterno() {
        Usuario recepcion = crearInterno.ejecutar(new Correo("recepcion@sanrocco.co"), "Recepción",
                "Recepcion123", "3000000000", new DocumentoIdentidad("2000"), LocalDate.of(1990, 1, 1),
                Rol.RECEPCIONISTA, HOY);

        assertEquals(Rol.RECEPCIONISTA, recepcion.getRol());
    }

    @Test
    @DisplayName("Iniciar sesión: con datos correctos devuelve el token")
    void iniciarSesionCorrecto() {
        registrarLaura();

        IniciarSesionUseCase.Sesion sesion = iniciarSesion.ejecutar(new Correo("LAURA@mail.com"), "Secreta123");

        assertEquals("token-laura@mail.com", sesion.token().valor());
        assertEquals(Rol.HUESPED, sesion.usuario().getRol());
    }

    @Test
    @DisplayName("Iniciar sesión: correo inexistente, mala contraseña o usuario inactivo dan el mismo error")
    void iniciarSesionIncorrecto() {
        Usuario laura = registrarLaura();

        assertThrows(CredencialesInvalidasException.class,
                () -> iniciarSesion.ejecutar(new Correo("nadie@mail.com"), "Secreta123"));
        assertThrows(CredencialesInvalidasException.class,
                () -> iniciarSesion.ejecutar(new Correo("laura@mail.com"), "Incorrecta1"));

        laura.desactivar();
        usuarios.guardar(laura);
        assertThrows(CredencialesInvalidasException.class,
                () -> iniciarSesion.ejecutar(new Correo("laura@mail.com"), "Secreta123"));
    }

    @Test
    @DisplayName("Acceso a reserva: el titular pasa, otro huésped recibe acceso denegado")
    void accesoReserva() {
        ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
        Reserva reserva = reserva("RES-1", NOVIEMBRE);   // titular: LAURA
        reservas.guardar(reserva);
        VerificarAccesoReservaUseCase verificar = new VerificarAccesoReservaUseCase(reservas);

        assertDoesNotThrow(() -> verificar.verificarTitular(reserva.getCodigo(), LAURA.getDocumento()));
        assertThrows(AccesoDenegadoException.class,
                () -> verificar.verificarTitular(reserva.getCodigo(), new DocumentoIdentidad("999")));
    }
}
