package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdentificacionBloqueo(String valor) {
    public IdentificacionBloqueo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El bloqueo debe tener un identificador");
        }
        valor = valor.trim();
    }
}
