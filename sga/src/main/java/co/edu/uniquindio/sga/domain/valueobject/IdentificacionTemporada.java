package co.edu.uniquindio.sga.domain.valueobject;

/**
 * representa la identificacion de una temporada
 * IdentificacionTemporada
 * 
 * @param id
 */

public record IdentificacionTemporada(String id) {
    public IdentificacionTemporada {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la temporada no puede ser nulo o vacío");
        }
    }
}
