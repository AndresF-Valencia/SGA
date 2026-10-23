package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import java.time.LocalDateTime;

/**
 * Concepto que suma al folio: alojamiento, servicio, ajuste o penalidad (7.7).
 * RN-16: es inmutable; nunca se modifica ni se elimina.
 * Solo un ajuste puede ser negativo, cuando una modificación baja el valor de
 * la estancia.
 */

public record Cargo(Dinero valor, String descripcion, TipoCargo tipo, LocalDateTime fecha) {
    public Cargo {
        if (valor == null || valor.esCero()) {
            throw new ReglaDominioException("El cargo debe indicar su valor");
        }
        if (tipo == null) {
            throw new ReglaDominioException("El cargo debe indicar su tipo");
        }
        if (valor.esNegativo() && tipo != TipoCargo.AJUSTE) {
            throw new ReglaDominioException("Solo un ajuste puede tener valor negativo");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El cargo debe indicar su descripción");
        }
        if (fecha == null) {
            throw new ReglaDominioException("El cargo debe indicar la fecha del cargo");
        }
    }

}
