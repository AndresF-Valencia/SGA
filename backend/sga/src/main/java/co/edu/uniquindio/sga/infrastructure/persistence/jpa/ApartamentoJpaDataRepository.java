package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApartamentoJpaDataRepository extends JpaRepository<ApartamentoEntity, String> {

    // SELECT * FROM apartamento WHERE activo = true
    List<ApartamentoEntity> findByActivoTrue();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApartamentoEntity a WHERE a.id = :id")
    Optional<ApartamentoEntity> findByIdWithLock(@Param("id") String id);
}
