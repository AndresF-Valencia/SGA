package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ContadorReservaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ContadorReservaJpaDataRepository extends JpaRepository<ContadorReservaEntity, Integer> {

    /**
     * Lee el contador del año y lo BLOQUEA hasta que termine la transacción:
     * si dos reservas se crean al mismo tiempo, la segunda espera a la primera
     * y nunca salen dos códigos iguales.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ContadorReservaEntity> findByAnio(int anio);
}
