package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Mínimo de noches que exige una temporada alta (RP-01).
 *
 * @param noches número mínimo de noches, al menos 1
 */
public record EstanciaMinima(int noches) {
    public EstanciaMinima {
        if (noches < 1) {
            throw new ReglaDominioException("La estancia mínima debe ser de al menos 1 noche");
        }
    }
}
