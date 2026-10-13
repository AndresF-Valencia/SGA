package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la disponibilidad de un recurso o servicio.
 * Se utiliza para indicar si un recurso está disponible o no.
 */

public record Disponibilidad(boolean disponible) {
    public Disponibilidad {
        if (!disponible) {
            throw new IllegalArgumentException("La cotizacion no esta disponible");
        }
    }
}
