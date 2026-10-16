package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Representa la cantidad de mascotas que un huésped puede tener
 * al momento de realizar una reserva o cotización.
 */

public record CantidadMascota(int cantidad) {
    public CantidadMascota {
        if (cantidad < 0) {
            throw new ReglaDominioException("La cantidad de mascotas no puede ser negativa");
        }
    }
}