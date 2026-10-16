package co.edu.uniquindio.sga.domain.valueobject;

public record IdentificacionCanal(String identificacion) {
    public IdentificacionCanal {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("La identificación del alojamiento no puede ser nula o vacía");
        }
    }
}
