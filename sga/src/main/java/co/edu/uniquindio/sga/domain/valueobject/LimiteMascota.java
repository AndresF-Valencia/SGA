package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Representa el límite de mascotas permitido en un apartamento.
 * Se utiliza para indicar la cantidad máxima de mascotas que se pueden alojar en un apartamento.
 */

public record LimiteMascota(int limite) {
    public LimiteMascota {
        if (limite < 0) {
            throw new ReglaDominioException("El límite de mascotas no puede ser negativo");
        }
    }
}
