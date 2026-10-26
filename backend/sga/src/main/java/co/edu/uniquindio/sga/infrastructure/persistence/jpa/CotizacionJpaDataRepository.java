package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CotizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionJpaDataRepository extends JpaRepository<CotizacionEntity, String> {
}
