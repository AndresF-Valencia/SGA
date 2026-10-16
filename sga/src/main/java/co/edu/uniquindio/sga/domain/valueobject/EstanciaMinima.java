package co.edu.uniquindio.sga.domain.valueobject;

/**
 * representa la estancia minima de una temporada
 * EstanciaMinima
 * 
 * @param noches
 */

public record EstanciaMinima(int noches) {
    public EstanciaMinima {
        if (noches < 1) {
            throw new IllegalArgumentException("La estancia mínima no puede ser menor a 1s");
        }
    }
}