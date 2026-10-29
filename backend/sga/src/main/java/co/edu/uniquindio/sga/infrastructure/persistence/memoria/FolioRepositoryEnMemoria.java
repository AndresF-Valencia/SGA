package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.Optional;

/** Adaptador en memoria del puerto {@link FolioRepository}: un folio por reserva. */
// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class FolioRepositoryEnMemoria extends AlmacenEnMemoria<CodigoReserva, Folio>
        implements FolioRepository {

    public FolioRepositoryEnMemoria() {
        super(Folio::getReserva);
    }

    @Override
    public void guardar(Folio folio) {
        almacenar(folio);
    }

    @Override
    public Optional<Folio> buscarPorReserva(CodigoReserva reserva) {
        return porId(reserva);
    }
}
