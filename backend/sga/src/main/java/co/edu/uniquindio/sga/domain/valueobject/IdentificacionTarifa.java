package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdentificacionTarifa(String identificacion) {

    public IdentificacionTarifa {
         if (identificacion == null || identificacion.isBlank()) {
            throw new ReglaDominioException("La identificación de la tarifa no puede ser nula o vacía");
        }
    }


} 
