package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Tiempo que necesita el personal para dejar listo un apartamento entre la
 * salida de un grupo y la entrada del siguiente (RN-20), en horas.
 *
 * Antes el constructor ignoraba el valor recibido y siempre guardaba 3; ahora
 * se respeta el valor configurado y el 3 queda como valor estándar.
 *
 * @param tiempoPreparacion horas de preparación (0 o más)
 */
public record TiempoPreparacion(int tiempoPreparacion) {

    public static final int HORAS_ESTANDAR = 3;

    public TiempoPreparacion {
        if (tiempoPreparacion < 0) {
            throw new ReglaDominioException("El tiempo de preparación no puede ser negativo");
        }
        if (tiempoPreparacion > 24) {
            throw new ReglaDominioException("El tiempo de preparación no puede superar las 24 horas");
        }
    }

    /** Tiempo de preparación estándar del alojamiento: 3 horas. */
    public static TiempoPreparacion estandar() {
        return new TiempoPreparacion(HORAS_ESTANDAR);
    }

    public Duration comoDuracion() {
        return Duration.ofHours(tiempoPreparacion);
    }

    /**
     * RN-20: si un grupo sale a la hora de salida, el apartamento queda listo
     * cuando termina la preparación. Indica si llega a tiempo para la hora de
     * entrada del siguiente grupo ese mismo día.
     */
    public boolean alcanzaEntre(LocalTime horaSalida, LocalTime horaEntrada) {
        if (horaSalida == null || horaEntrada == null) {
            throw new ReglaDominioException("Se requieren la hora de salida y la de entrada");
        }
        long minutosDisponibles = Duration.between(horaSalida, horaEntrada).toMinutes();
        return minutosDisponibles >= comoDuracion().toMinutes();
    }
}
