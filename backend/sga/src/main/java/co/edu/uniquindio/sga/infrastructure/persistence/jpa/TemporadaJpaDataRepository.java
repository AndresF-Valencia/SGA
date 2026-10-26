package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TemporadaJpaDataRepository extends JpaRepository<TemporadaEntity, String> {

    /**
     * Temporadas que tocan el rango [desde, hasta]:
     * SELECT * FROM temporada WHERE hasta >= ? AND desde <= ?
     * "PeriodoHasta" entra al campo hasta del embeddable periodo.
     */
    List<TemporadaEntity> findByPeriodoHastaGreaterThanEqualAndPeriodoDesdeLessThanEqual(
            LocalDate desde, LocalDate hasta);
}
