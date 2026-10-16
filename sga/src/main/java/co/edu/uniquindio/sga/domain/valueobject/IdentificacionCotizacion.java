package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Representa la identificación de una cotización.
 * Se utiliza para garantizar que cada cotización tenga una identificación única y válida.
 */

public record IdentificacionCotizacion(String valor) {

    public IdentificacionCotizacion {

        if (valor == null || valor.isBlank()) {

            throw new ReglaDominioException("La cotización debe tener una identificación");

        }

        valor = valor.trim().toUpperCase();

    }

}