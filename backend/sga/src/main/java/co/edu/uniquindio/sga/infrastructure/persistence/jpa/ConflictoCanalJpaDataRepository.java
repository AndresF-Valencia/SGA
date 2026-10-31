package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConflictoCanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConflictoCanalJpaDataRepository extends JpaRepository<ConflictoCanalEntity, String> {

    // SELECT * FROM conflicto_canal WHERE codigo_externo = ? AND resuelto = false
    Optional<ConflictoCanalEntity> findFirstByCodigoExternoAndResueltoFalse(String codigoExterno);

    // SELECT * FROM conflicto_canal ORDER BY recibido_en DESC
    List<ConflictoCanalEntity> findAllByOrderByRecibidoEnDesc();
}
