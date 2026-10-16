package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdentificacionCanal(String identificacion) {
    public IdentificacionCanal {
        if (identificacion == null || identificacion.isBlank()) {
            throw new ReglaDominioException("La identificación del canal no puede ser nula o vacía");
        }
    }
}
