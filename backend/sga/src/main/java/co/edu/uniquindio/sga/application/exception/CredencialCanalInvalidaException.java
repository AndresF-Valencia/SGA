package co.edu.uniquindio.sga.application.exception;

/** El canal externo no mandó su credencial, no existe o está inactivo (10.3.1). */
public class CredencialCanalInvalidaException extends RuntimeException {
    public CredencialCanalInvalidaException() {
        super("Credencial de canal inválida: envía el encabezado X-Canal-Credencial de un canal activo");
    }
}
