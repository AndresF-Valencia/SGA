package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Referencia a la versión de la política de cancelación vigente al crear la reserva.
 */

public record VersionPolitica(int numero) {

    public VersionPolitica {

        if (numero < 1) {

            throw new ReglaDominioException("La versión de la política debe ser mayor que cero");

        }

    }

}
