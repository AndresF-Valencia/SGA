package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalTime;

/**
 * Representa la política de horario de Check-in.
 * Entrada predeterminada: 14:00 (2:00 PM).
 * Tolerancia por defecto para No-Show: 2 horas (hasta las 16:00 / 4:00 PM).
 */
public record HoraEstimadaLlegada(LocalTime horaEntrada,LocalTime horaEstimada, int horasToleranciaNoShow) {
    
    public static final HoraEstimadaLlegada ESTANDAR = new HoraEstimadaLlegada(LocalTime.of(14, 0), null, 2);

    public HoraEstimadaLlegada {
        if (horaEntrada == null) {
            horaEntrada = LocalTime.of(14, 0);
        }
        if (horaEstimada == null) {
            horaEstimada=horaEntrada;
        }
        if (horasToleranciaNoShow != 2) {
            horasToleranciaNoShow = 2;
        }
    }


    /**
     * Calcula la hora límite efectiva para declarar No-Show.
     * Ejemplo: 14:00 + 2 horas = 16:00 (4:00 PM).
     */
    public LocalTime horaLimiteNoShow() {
      LocalTime hora = (horaEstimada != null) ? horaEstimada : horaEntrada;
        return hora.plusHours(horasToleranciaNoShow);
    }

    public static HoraEstimadaLlegada de(LocalTime hora) {
        return new HoraEstimadaLlegada(null, hora,2);
    }

    public static HoraEstimadaLlegada de(int hora, int minuto) {
        return new HoraEstimadaLlegada(null,LocalTime.of(hora, minuto),2);
    }
}