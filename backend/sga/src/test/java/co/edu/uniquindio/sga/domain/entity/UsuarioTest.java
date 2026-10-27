package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Usuario (7.2)")
class UsuarioTest {

    /** Cifrador falso: no usa BCrypt, solo marca el texto. Suficiente para probar el dominio. */
    static final CifradorContrasena CIFRADOR = new CifradorContrasena() {
        @Override
        public String cifrar(String plana) {
            return "cifrada:" + plana;
        }

        @Override
        public boolean coincide(String plana, String cifrada) {
            return cifrada.equals("cifrada:" + plana);
        }
    };

    static final LocalDate HOY = LocalDate.of(2026, 10, 27);

    static Usuario huesped(String contrasena) {
        return Usuario.registrarHuesped(new Correo("Laura@Mail.com"), "Laura Gómez", contrasena, "3001234567",
                new DocumentoIdentidad("1094000001"), LocalDate.of(1995, 3, 10), CIFRADOR, HOY);
    }

    @Test
    @DisplayName("El registro público crea un HUÉSPED activo, con la contraseña cifrada")
    void registraHuesped() {
        Usuario usuario = huesped("Secreta123");

        assertEquals(Rol.HUESPED, usuario.getRol());
        assertTrue(usuario.estaActivo());
        assertNotEquals("Secreta123", usuario.getContrasenaCifrada());
        assertEquals(new Correo("laura@mail.com"), usuario.getCorreo());
    }

    @Test
    @DisplayName("La contraseña debe tener 8 caracteres, una mayúscula y un número")
    void contrasenaSegura() {
        assertThrows(ReglaDominioException.class, () -> huesped("Corta1"));
        assertThrows(ReglaDominioException.class, () -> huesped("sinmayuscula1"));
        assertThrows(ReglaDominioException.class, () -> huesped("SinNumeroAqui"));
        assertThrows(ReglaDominioException.class, () -> huesped(null));
    }

    @Test
    @DisplayName("El correo debe tener formato válido")
    void correoValido() {
        assertThrows(ReglaDominioException.class, () -> new Correo("no-es-correo"));
        assertThrows(ReglaDominioException.class, () -> new Correo(" "));
    }

    @Test
    @DisplayName("La fecha de nacimiento no puede ser futura")
    void nacimientoFuturo() {
        assertThrows(ReglaDominioException.class, () -> Usuario.registrarHuesped(new Correo("a@b.co"), "Ana",
                "Secreta123", "300", new DocumentoIdentidad("1"), HOY.plusDays(1), CIFRADOR, HOY));
    }

    @Test
    @DisplayName("Inicia sesión solo con la contraseña correcta y si está activo")
    void iniciarSesion() {
        Usuario usuario = huesped("Secreta123");

        assertTrue(usuario.puedeIniciarSesion("Secreta123", CIFRADOR));
        assertFalse(usuario.puedeIniciarSesion("Otra12345", CIFRADOR));

        usuario.desactivar();
        assertFalse(usuario.puedeIniciarSesion("Secreta123", CIFRADOR));
    }
}
