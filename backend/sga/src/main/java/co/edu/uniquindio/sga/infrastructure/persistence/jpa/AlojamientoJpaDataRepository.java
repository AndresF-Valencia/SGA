package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlojamientoJpaDataRepository extends JpaRepository<AlojamientoEntity, String> {
}
