package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ContadorReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContadorReservaJpaDataRepository extends JpaRepository<ContadorReservaEntity, Integer> {

    /**
     * Lee el contador del año y lo BLOQUEA hasta que termine la transacción:
     * si dos reservas se crean al mismo tiempo, la segunda espera a la primera
     * y nunca salen dos códigos iguales.
     */
    Optional<ContadorReservaEntity> findByAnio(int anio);
}
