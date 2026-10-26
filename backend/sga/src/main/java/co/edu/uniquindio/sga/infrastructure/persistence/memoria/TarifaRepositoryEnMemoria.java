package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;

import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link TarifaRepository}. */
// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class TarifaRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionTarifa, Tarifa>
        implements TarifaRepository {

    public TarifaRepositoryEnMemoria() {
        super(Tarifa::getIdentificacion);
    }

    @Override
    public void guardar(Tarifa tarifa) {
        almacenar(tarifa);
    }

    @Override
    public Optional<Tarifa> buscarPorId(IdentificacionTarifa id) {
        return porId(id);
    }

    @Override
    public List<Tarifa> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return filtrar(t -> t.getApartamento().equals(apartamento));
    }

    @Override
    public Optional<Tarifa> buscarPorApartamentoYTemporada(IdentificacionApartamento apartamento,
                                                           IdentificacionTemporada temporada) {
        return filtrar(t -> t.getApartamento().equals(apartamento) && t.getTemporada().equals(temporada))
                .stream().findFirst();
    }
}
