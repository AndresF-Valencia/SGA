package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.Optional;

/**
 * Puerto de salida del agregado Folio.
 * Cada reserva tiene un solo folio, así que se busca por el código de la reserva.
 */
public interface FolioRepository {

    void guardar(Folio folio);

    Optional<Folio> buscarPorReserva(CodigoReserva reserva);
}
