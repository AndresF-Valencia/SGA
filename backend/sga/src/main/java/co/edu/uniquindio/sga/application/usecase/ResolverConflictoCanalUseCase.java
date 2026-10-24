package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.LocalDateTime;

/**
 * Caso de uso: Resolver conflicto de canal (CU-21).
 * Gestiona y resuelve las sobre-reservas o colisiones generadas por canales externos (Channel Manager).
 */
@Service
public class ResolverConflictoCanalUseCase {

    private final ReservaRepository reservaRepository;

    public ResolverConflictoCanalUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /**
     * Rechaza o cancela la reserva en conflicto recibida desde un canal externo.
     * 
     * @param codigoReserva Código único de la reserva en conflicto.
     * @param motivo        Descripción del motivo de la resolución (ej. "Sobre-reserva por bloqueo previo").
     */
    @Transactional
    public Reserva rechazarReservaCanal(CodigoReserva codigoReserva, String motivo) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El motivo de resolución del conflicto es obligatorio");
        }

        // 1. Obtener la reserva en conflicto
        Reserva reserva = reservaRepository.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró la reserva con código: " + codigoReserva));

        // 2. Invocar el método de negocio cancelar existente en la entidad Reserva
        reserva.cancelar(motivo, "CHANNEL_MANAGER", LocalDateTime.now());

        // 3. Persistir el nuevo estado de la reserva
        reservaRepository.guardar(reserva);
        return reserva;
    }
}