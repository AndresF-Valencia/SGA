package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalTime;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Representa la hora estimada de llegada informada para la reserva.
 */
public record HoraEstimadaLlegada(LocalTime horaEstimada) {
    
    public HoraEstimadaLlegada {
        if (horaEstimada == null) {
            throw new ReglaDominioException("La hora estimada de llegada es obligatoria");
        }
    }

    public static HoraEstimadaLlegada de(LocalTime hora) {
        return new HoraEstimadaLlegada(hora);
    }

    public static HoraEstimadaLlegada de(int hora, int minuto) {
        return new HoraEstimadaLlegada(LocalTime.of(hora, minuto));
    }
}