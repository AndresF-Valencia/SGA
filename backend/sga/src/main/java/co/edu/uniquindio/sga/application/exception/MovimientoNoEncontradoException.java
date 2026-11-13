package co.edu.uniquindio.sga.application.exception;

public class MovimientoNoEncontradoException extends RuntimeException{
    public MovimientoNoEncontradoException (String id){
        super("No se encontro el movimiento:" + id);
    }
}
