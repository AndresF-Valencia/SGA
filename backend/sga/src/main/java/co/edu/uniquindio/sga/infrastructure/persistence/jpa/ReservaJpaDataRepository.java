package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaJpaDataRepository extends JpaRepository<ReservaEntity, String> {

    // SELECT * FROM reserva WHERE apartamento_id = ? AND estado IN (?, ?, ?)
    List<ReservaEntity> findByApartamentoIdAndEstadoIn(String apartamentoId,
            Collection<EstadoReserva> estados);

    // SELECT * FROM reserva WHERE estado = ?
    List<ReservaEntity> findByEstado(EstadoReserva estado);

    // SELECT * FROM reserva WHERE canal_origen = ? LIMIT 1
    Optional<ReservaEntity> findFirstByCanalOrigen(CanalOrigen canalOrigen);
}
