package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: recordarle al titular que su llegada es mañana.
 * Solo avisa a las reservas CONFIRMADA: una pendiente todavía puede vencerse.
 * El notificador nunca lanza excepciones (I-04, I-05), así que un correo que falle
 * no impide avisar a los demás.
 */
public class EnviarRecordatoriosLlegadaUseCase {

    private final ReservaRepository reservaRepository;
    private final NotificadorReserva notificador;

    public EnviarRecordatoriosLlegadaUseCase(ReservaRepository reservaRepository, NotificadorReserva notificador) {
        this.reservaRepository = reservaRepository;
        this.notificador = notificador;
    }

    /**
     * @param hoy fecha en la que corre el proceso; se avisa a quienes llegan al día siguiente.
     * @return las reservas a las que se les envió el recordatorio.
     */
    @Transactional(readOnly = true)
    public List<Reserva> ejecutar(LocalDate hoy) {
        if (hoy == null) {
            throw new IllegalArgumentException("Se requiere la fecha actual para enviar los recordatorios");
        }
        LocalDate manana = hoy.plusDays(1);
        List<Reserva> avisadas = new ArrayList<>();
        for (Reserva reserva : reservaRepository.obtenerTodas()) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA
                    && reserva.getEstancia().fechaEntrada().equals(manana)) {
                notificador.recordatorioLlegada(reserva);
                avisadas.add(reserva);
            }
        }
        return avisadas;
    }
}
