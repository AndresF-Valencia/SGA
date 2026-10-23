package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import java.time.LocalDate;

/**
 * Rango de fechas cerrado en ambos extremos: [desde, hasta]. Lo usan el Bloqueo
 * y la Temporada.
 * Distinto de Estancia, que no incluye el día de salida: confundirlos produce
 * errores de una noche.
 */

public record Periodo(LocalDate desde, LocalDate hasta) {
    public Periodo {
        if (desde == null || hasta == null) {
            throw new ReglaDominioException("El periodo debe tener fechas de inicio y fin");
        }
        if (desde.isAfter(hasta)) {
            throw new ReglaDominioException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    public boolean incluye(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaDominioException("Debe indicarse la fecha a verificar");
        }
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }
}
