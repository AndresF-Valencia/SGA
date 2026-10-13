package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la identificación de un alojamiento.s
 * IdentificacionAlojamiento
 * 
 * @param identificacion
 */

public record IdentificacionAlojamiento(String identificacion) {
    public IdentificacionAlojamiento {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("La identificación del alojamiento no puede ser nula o vacía");
        }
    }
}