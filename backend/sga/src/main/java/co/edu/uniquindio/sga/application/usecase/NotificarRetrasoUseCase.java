package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class NotificarRetrasoUseCase {

    private final ReservaRepository reservaRepository;

    public NotificarRetrasoUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigoReserva, NotificacionRetraso notificacion, String autor, LocalDateTime ahora) {
        Reserva reserva = reservaRepository.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigoReserva));
        
        reserva.notificarRetraso(notificacion, autor, ahora);
        reservaRepository.guardar(reserva);
        return reserva;
    }
}