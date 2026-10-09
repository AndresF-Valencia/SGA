package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record TramoCancelacion(
        int diasMinimosAnticipacion,
        int diasMaximosAnticipacion,
        double porcentajeDevolucion) {

    public TramoCancelacion {
        if (diasMinimosAnticipacion < 0 || diasMaximosAnticipacion < 0) {
            throw new ReglaDominioException("Los días de anticipación no pueden ser negativos");
        }
        if (diasMinimosAnticipacion > diasMaximosAnticipacion) {
            throw new ReglaDominioException("El límite mínimo no puede superar el límite máximo del tramo");
        }
        if (porcentajeDevolucion < 0.0 || porcentajeDevolucion > 100.0) {
            throw new ReglaDominioException("El porcentaje de devolución debe estar entre 0 y 100");
        }
    }

    public boolean aplicaPara(LocalDateTime momentoCancelacion, Estancia estancia) {
        if (momentoCancelacion == null || estancia == null) {
            return false;
        }

        long diasAnticipacion = estancia.diasAnticipacionDesde(momentoCancelacion.toLocalDate());
        return diasAnticipacion >= diasMinimosAnticipacion && diasAnticipacion <= diasMaximosAnticipacion;
    }
}