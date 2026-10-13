package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la identificación de una cotización.
 * Se utiliza para garantizar que cada cotización tenga una identificación única y válida.
 */

public record IndentificacionCotizacion(String valor) {

    public IndentificacionCotizacion {

        if (valor == null || valor.isBlank()) {

            throw new IllegalArgumentException("La cotización debe tener una identificación");

        }

        valor = valor.trim().toUpperCase();

    }

}
