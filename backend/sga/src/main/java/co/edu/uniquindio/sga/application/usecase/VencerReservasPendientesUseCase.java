package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: Vencer automáticamente las reservas pendientes cuyo plazo de
 * confirmación ha expirado (RN-21).
 * Lee el plazo directamente de la configuración vigente del Alojamiento.
 */
public class VencerReservasPendientesUseCase {

    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;

    public VencerReservasPendientesUseCase(ReservaRepository reservaRepository,
                                           AlojamientoRepository alojamientoRepository) {
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
    }

    /**
     * Revisa todas las reservas en estado PENDIENTE y vence aquellas que superen el
     * plazo configurado en el Alojamiento.
     * 
     * @param ahora Momento actual de ejecución del proceso automático (scheduler).
     * @return Lista de reservas que fueron vencidas y canceladas en este ciclo.
     */
    @Transactional
    public List<Reserva> ejecutar(LocalDateTime ahora) {
        if (ahora == null) {
            throw new ReglaDominioException(
                    "Se requiere la fecha y hora actual para procesar el vencimiento de reservas");
        }

        // 1. Obtener la configuración del alojamiento para extraer el plazo vigente
        Alojamiento alojamiento = alojamientoRepository.obtenerUnico()
                .orElseThrow(() -> new ReglaDominioException("No se encontró la configuración del alojamiento"));
        
        PlazoConfirmacion plazoConfirmacion = alojamiento.getPlazoConfirmacion(); // Asegúrate de tener este getter en Alojamiento

        // 2. Obtener todas las reservas pendientes
        List<Reserva> reservasPendientes = reservaRepository.buscarPendientes();
        List<Reserva> reservasVencidas = new ArrayList<>();

        if (reservasPendientes == null || reservasPendientes.isEmpty()) {
            return reservasVencidas;
        }

        // 3. Iterar y evaluar cada reserva según el plazo dinámico del alojamiento
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