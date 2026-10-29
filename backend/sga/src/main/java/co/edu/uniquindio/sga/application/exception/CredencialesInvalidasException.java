package co.edu.uniquindio.sga.application.exception;

/**
 * El correo o la contraseña no coinciden, o el usuario está inactivo.
 * El mensaje es el mismo en todos los casos a propósito: así no se revela
 * si un correo está registrado o no.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
