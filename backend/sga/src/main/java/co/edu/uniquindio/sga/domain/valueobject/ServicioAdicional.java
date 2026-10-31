package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Servicio adicional que ofrece el alojamiento (7.3) con su valor. */
public record ServicioAdicional(String nombre, Dinero valor) {

    public ServicioAdicional {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El servicio adicional debe tener un nombre");
        }
        if (valor == null || valor.esNegativo()) {
            throw new ReglaDominioException("El valor del servicio adicional no puede ser negativo");
        }
    }
}
