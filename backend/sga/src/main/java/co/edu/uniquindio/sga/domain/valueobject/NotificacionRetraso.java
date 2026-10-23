package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import java.time.LocalTime;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Notificación enviada por el huésped para informar un retraso en su llegada.
 * Impide la declaración de NO_SHOW cuando existe una causa informada.
 */
public record NotificacionRetraso(
        LocalDateTime fechaHoraNotificacion,
        LocalTime nuevaHoraEstimada,
        String motivo) {

    public NotificacionRetraso {
        if (fechaHoraNotificacion == null) {
            throw new ReglaDominioException("La fecha y hora de la notificación son obligatorias");
        }
        if (nuevaHoraEstimada == null) {
            throw new ReglaDominioException("Debe indicar la nueva hora estimada de llegada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("Debe registrar el motivo del retraso");
        }
    }

    public static NotificacionRetraso registrar(LocalTime nuevaHoraEstimada, String motivo, LocalDateTime ahora) {
        return new NotificacionRetraso(ahora, nuevaHoraEstimada, motivo);
    }
}