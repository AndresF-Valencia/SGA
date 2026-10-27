package co.edu.uniquindio.sga.application.exception;

/**
 * El usuario inició sesión, pero el recurso no es suyo (7.2: un huésped nunca
 * accede a reservas ni folios de otros huéspedes).
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
