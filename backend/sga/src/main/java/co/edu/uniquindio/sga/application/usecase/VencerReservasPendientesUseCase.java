package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: Vencer automáticamente las reservas pendientes cuyo plazo de
 * confirmación ha expirado (RN-21).
 */
@Service
public class VencerReservasPendientesUseCase {

    private final ReservaRepository reservaRepository;
    private final PlazoConfirmacion plazoConfirmacion;

    public VencerReservasPendientesUseCase(ReservaRepository reservaRepository, PlazoConfirmacion plazoConfirmacion) {
        this.reservaRepository = reservaRepository;
        this.plazoConfirmacion = plazoConfirmacion;
    }

    /**
     * Revisa todas las reservas en estado PENDIENTE y vence aquellas que superen el
     * plazo establecido.
     * 
     * @param ahora Momento actual de ejecución del proceso automático (scheduler o
     *              tarea programada).
     * @return Lista de reservas que fueron vencidas y canceladas en este ciclo.
     */
    @Transactional
    public List<Reserva> ejecutar(LocalDateTime ahora) {
        if (ahora == null) {
            throw new ReglaDominioException(
                    "Se requiere la fecha y hora actual para procesar el vencimiento de reservas");
        }

        // 1. Obtener todas las reservas que actualmente se encuentran pendientes
        List<Reserva> reservasPendientes = reservaRepository.buscarPendientes();
        List<Reserva> reservasVencidas = new ArrayList<>();

        if (reservasPendientes == null || reservasPendientes.isEmpty()) {
            return reservasVencidas;
        }

        // 2. Iterar y evaluar cada reserva según el plazo de confirmación (RN-21)
        for (Reserva reserva : reservasPendientes) {
            
            if (plazoConfirmacion.venceAntesDe(reserva.getCreadaEn(), ahora)) {
                reserva.vencer(plazoConfirmacion, ahora);
                reservaRepository.guardar(reserva);
                reservasVencidas.add(reserva);
            }
        }

        return reservasVencidas;
    }
}