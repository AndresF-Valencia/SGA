package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ConflictoCanalNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 7.8 / CU-21: el administrador revisa los conflictos de canal y los marca resueltos.
 * La reserva externa en conflicto nunca se creó (RN-18), así que resolver es dejar por
 * escrito qué se hizo con ese huésped (reubicarlo, avisarle al canal, etc.).
 */
public class ResolverConflictoCanalUseCase {

    private final ConflictoCanalRepository conflictoRepository;

    public ResolverConflictoCanalUseCase(ConflictoCanalRepository conflictoRepository) {
        this.conflictoRepository = conflictoRepository;
    }

    /** resuelto = null → todos; true/false → solo resueltos o solo pendientes. */
    @Transactional(readOnly = true)
    public List<ConflictoCanal> listar(Boolean resuelto) {
        return conflictoRepository.listar().stream()
                .filter(c -> resuelto == null || c.estaResuelto() == resuelto)
                .toList();
    }

    @Transactional
    public ConflictoCanal resolver(String id, String resolucion, String autor, LocalDateTime ahora) {
        ConflictoCanal conflicto = conflictoRepository.buscarPorId(id)
                .orElseThrow(() -> new ConflictoCanalNoEncontradoException(id));
        conflicto.resolver(resolucion, autor, ahora);
        conflictoRepository.guardar(conflicto);
        return conflicto;
    }
}
