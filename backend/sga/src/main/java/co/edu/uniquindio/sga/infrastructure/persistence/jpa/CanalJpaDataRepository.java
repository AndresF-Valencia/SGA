package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanalJpaDataRepository extends JpaRepository<CanalEntity, String> {

    // SELECT * FROM canal WHERE credencial_acceso = ?
    Optional<CanalEntity> findByCredencialAcceso(String credencialAcceso);

    // SELECT * FROM canal WHERE activo = true
    List<CanalEntity> findByActivoTrue();
}
