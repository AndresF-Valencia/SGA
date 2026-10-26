package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;

import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link BloqueoRepository}. */
// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class BloqueoRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionBloqueo, Bloqueo>
        implements BloqueoRepository {

    public BloqueoRepositoryEnMemoria() {
        super(Bloqueo::getIdentificacionBloqueo);
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        almacenar(bloqueo);
    }

    @Override
    public Optional<Bloqueo> buscarPorId(IdentificacionBloqueo id) {
        return porId(id);
    }

    @Override
    public List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento) {
        return filtrar(b -> b.getApartamento().equals(apartamento) && b.getEstado() == EstadoBloqueo.VIGENTE);
    }

    @Override
    public List<Bloqueo> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return filtrar(b -> b.getApartamento().equals(apartamento));
    }

    @Override
    public List<Bloqueo> obtenerTodos() {
        return todos();
    }
}