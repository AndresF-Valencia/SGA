package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdBloqueo(String valor) {
    public IdBloqueo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El bloqueo debe tener un identificador");
        }
        valor = valor.trim();
    }
}
