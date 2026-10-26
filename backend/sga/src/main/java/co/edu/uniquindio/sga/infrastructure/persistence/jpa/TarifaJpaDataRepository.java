package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarifaJpaDataRepository extends JpaRepository<TarifaEntity, String> {

    // SELECT * FROM tarifa WHERE apartamento_id = ?
    List<TarifaEntity> findByApartamentoId(String apartamentoId);

    // SELECT * FROM tarifa WHERE apartamento_id = ? AND temporada_id = ? LIMIT 1
    Optional<TarifaEntity> findFirstByApartamentoIdAndTemporadaId(String apartamentoId, String temporadaId);
}
