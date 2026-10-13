package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la identificación de un apartamento.
 * Se utiliza para garantizar que cada apartamento tenga una identificación única y válida.
 */


import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdentificacionApartamento(String valor) {

    public IdentificacionApartamento {

        if (valor == null || valor.isBlank()) {

            throw new ReglaDominioException("El apartamento debe tener una identificación");

        }

        valor = valor.trim().toUpperCase();

    }

}
