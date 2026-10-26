package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReservaJpaDataRepository extends JpaRepository<ReservaEntity, String> {

    // SELECT * FROM reserva WHERE apartamento_id = ? AND estado IN (?, ?, ?)
    List<ReservaEntity> findByApartamentoIdAndEstadoIn(String apartamentoId,
            Collection<EstadoReserva> estados);
}