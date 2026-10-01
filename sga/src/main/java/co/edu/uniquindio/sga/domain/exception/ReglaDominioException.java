package co.edu.uniquindio.sga.domain.exception;

/**

 * Se lanza cuando una operación viola una regla del negocio.

 */

public class ReglaDominioException extends RuntimeException {

    public ReglaDominioException(String mensaje) {

        super(mensaje);

    }

}
