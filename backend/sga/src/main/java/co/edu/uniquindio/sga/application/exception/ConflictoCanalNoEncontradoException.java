package co.edu.uniquindio.sga.application.exception;

/** No existe un conflicto de canal con ese id → 404. */
public class ConflictoCanalNoEncontradoException extends RuntimeException {
    public ConflictoCanalNoEncontradoException(String id) {
        super("No se encontró el conflicto de canal: " + id);
    }
}
