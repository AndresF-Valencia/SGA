package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;

import java.util.List;
import java.util.Optional;

/** Puerto de salida de los conflictos de canal (7.8). */
public interface ConflictoCanalRepository {

    void guardar(ConflictoCanal conflicto);

    Optional<ConflictoCanal> buscarPorId(String id);

    /** Conflicto aún sin resolver para ese mensaje del canal (para no registrarlo dos veces). */
    Optional<ConflictoCanal> buscarPendientePorCodigoExterno(String codigoExterno);

    /** Todos, del más reciente al más antiguo. */
    List<ConflictoCanal> listar();
}
