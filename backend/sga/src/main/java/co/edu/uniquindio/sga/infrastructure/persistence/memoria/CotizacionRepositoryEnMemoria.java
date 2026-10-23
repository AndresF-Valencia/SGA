package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.repository.CotizacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Adaptador en memoria del puerto {@link CotizacionRepository}. */
@Repository
public class CotizacionRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionCotizacion, Cotizacion>
        implements CotizacionRepository {

    public CotizacionRepositoryEnMemoria() {
        super(Cotizacion::getIdentificacion);
    }

    @Override
    public void guardar(Cotizacion cotizacion) {
        almacenar(cotizacion);
    }

    @Override
    public Optional<Cotizacion> buscarPorId(IdentificacionCotizacion id) {
        return porId(id);
    }
}
