package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaDataRepository extends JpaRepository<UsuarioEntity, String> {

    // SELECT COUNT(*) > 0 FROM usuario WHERE documento = ?
    boolean existsByDocumento(String documento);
}
