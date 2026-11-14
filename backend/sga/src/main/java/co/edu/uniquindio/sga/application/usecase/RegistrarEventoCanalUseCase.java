package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.domain.repository.EventoCanalRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: bitácora de eventos intercambiados con los canales externos (7.8).
 *
 * Cada evento se guarda en su propia transacción, después de terminar la operación del
 * canal. Así un mensaje rechazado (por ejemplo, una reserva externa que choca, RN-18)
 * también queda registrado aunque su operación se haya deshecho.
 */
public class RegistrarEventoCanalUseCase {

    /** Nombre con el que queda un mensaje cuya credencial no corresponde a ningún canal. */
    public static final String CANAL_SIN_IDENTIFICAR = "Sin identificar";

    private final EventoCanalRepository eventoCanalRepository;

    public RegistrarEventoCanalUseCase(EventoCanalRepository eventoCanalRepository) {
        this.eventoCanalRepository = eventoCanalRepository;
    }

    @Transactional
    public EventoCanal registrar(String canal, String operacion, String codigoExterno, boolean exitoso,
            String detalle, LocalDateTime ahora) {
        EventoCanal evento = EventoCanal.registrar(
                UUID.randomUUID().toString(),
                canal == null || canal.isBlank() ? CANAL_SIN_IDENTIFICAR : canal,
                operacion,
                codigoExterno,
                exitoso,
                detalle,
                ahora);
        eventoCanalRepository.guardar(evento);
        return evento;
    }

    /** Del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<EventoCanal> listar() {
        return eventoCanalRepository.listar();
    }
}
