package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Referencia a la versión de la política de cancelación vigente al crear la reserva.
 */

public record VersionPolitica(int numero) {

    public VersionPolitica {
        if (numero <= 0) {
            throw new ReglaDominioException("El número de versión debe ser un entero positivo");
        }
    }

    public VersionPolitica siguiente() {
        return new VersionPolitica(this.numero + 1);
    }
}
