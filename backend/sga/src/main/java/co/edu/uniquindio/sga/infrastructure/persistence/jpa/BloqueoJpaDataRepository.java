package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloqueoJpaDataRepository extends JpaRepository<BloqueoEntity, String> {

    // SELECT * FROM bloqueo WHERE apartamento_id = ? AND estado = ?
    List<BloqueoEntity> findByApartamentoIdAndEstado(String apartamentoId, EstadoBloqueo estado);

    // SELECT * FROM bloqueo WHERE apartamento_id = ?
    List<BloqueoEntity> findByApartamentoId(String apartamentoId);
}
