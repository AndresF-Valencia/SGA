package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del agregado Bloqueo. */
public interface BloqueoRepository {

    void guardar(Bloqueo bloqueo);

    Optional<Bloqueo> buscarPorId(IdentificacionBloqueo id);

    /** Bloqueos VIGENTES del apartamento: los que quitan disponibilidad (RN-07). */
    List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento);

    /** Todos los bloqueos del apartamento, también los levantados (historial). */
    List<Bloqueo> buscarPorApartamento(IdentificacionApartamento apartamento);
}
