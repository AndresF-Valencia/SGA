package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Plazo para confirmar una reserva PENDIENTE (RN-21).
 */
public record PlazoConfirmacion(int horas) {

    public PlazoConfirmacion {
        if (horas < 1) {
            throw new ReglaDominioException("El plazo de confirmación debe ser de al menos 1 hora");
        }
    }

    /**
     * Instancia por defecto con la regla de 1 hora.
     */
    public static PlazoConfirmacion unaHora() {
        return new PlazoConfirmacion(1);
    }

    public boolean venceAntesDe(LocalDateTime creacion, LocalDateTime momento) {
        return momento.isAfter(creacion.plusHours(horas));
    }
}