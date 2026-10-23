package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;

import java.util.Optional;

/** Puerto de salida del agregado Cotización. */
public interface CotizacionRepository {

    void guardar(Cotizacion cotizacion);

    Optional<Cotizacion> buscarPorId(IdentificacionCotizacion id);
}
