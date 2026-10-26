package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartamentoJpaDataRepository extends JpaRepository<ApartamentoEntity, String> {

    // SELECT * FROM apartamento WHERE activo = true
    List<ApartamentoEntity> findByActivoTrue();
}
