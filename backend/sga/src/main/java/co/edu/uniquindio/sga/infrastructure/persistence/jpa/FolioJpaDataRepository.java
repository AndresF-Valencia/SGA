package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** La llave es el código de la reserva, así que buscar el folio de una reserva es findById. */
public interface FolioJpaDataRepository extends JpaRepository<FolioEntity, String> {
}
