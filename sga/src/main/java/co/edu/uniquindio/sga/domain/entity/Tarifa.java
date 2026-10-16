package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;

/**
 * Entidad Tarifa: define el valor por ocupante facturable, por noche,
 * para un apartamento en una temporada específica (7.4).
 */
public class Tarifa {

    private final IdentificacionTarifa identificacion;
    private final IdentificacionApartamento apartamento;
    private final IdentificacionTemporada temporada;
    private final Dinero valorPorOcupanteNoche;

    public Tarifa(IdentificacionTarifa identificacion, IdentificacionApartamento apartamento,
                  IdentificacionTemporada temporada, Dinero valorPorOcupanteNoche) {
        if (identificacion == null) {
            throw new ReglaDominioException("La tarifa debe tener un identificador");
        }
        if (apartamento == null) {
            throw new ReglaDominioException("La tarifa debe estar asociada a un apartamento");
        }
        if (temporada == null) {
            throw new ReglaDominioException("La tarifa debe estar asociada a una temporada");
        }
        if (valorPorOcupanteNoche == null || valorPorOcupanteNoche.esNegativo() || valorPorOcupanteNoche.esCero()) {
            throw new ReglaDominioException("El valor de la tarifa por ocupante y noche debe ser mayor a cero");
        }

        this.identificacion = identificacion;
        this.apartamento = apartamento;
        this.temporada = temporada;
        this.valorPorOcupanteNoche = valorPorOcupanteNoche;
    }

    public IdentificacionTarifa getIdentificacion() {
        return identificacion;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public IdentificacionTemporada getTemporada() {
        return temporada;
    }

    public Dinero getValorPorOcupanteNoche() {
        return valorPorOcupanteNoche;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tarifa otra)) return false;
        return this.identificacion.equals(otra.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}