package co.edu.uniquindio.sga.application.exception;

/** 7.2: el código de recuperación no existe, ya se usó, venció o no coincide → 400. */
public class CodigoRecuperacionInvalidoException extends RuntimeException {
    public CodigoRecuperacionInvalidoException() {
        super("El código de recuperación no es válido o ya venció. Solicita uno nuevo.");
    }
}
