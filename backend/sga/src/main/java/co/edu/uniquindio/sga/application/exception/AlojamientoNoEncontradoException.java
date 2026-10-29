package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;

public class AlojamientoNoEncontradoException extends RuntimeException {
    public AlojamientoNoEncontradoException(IdentificacionAlojamiento identificacionAlojamiento) {
        super("No se encontró un alojamiento con la identificación: " + identificacionAlojamiento);
    }

    public AlojamientoNoEncontradoException() {
        super("No se encontró un alojamiento ");
    }
}