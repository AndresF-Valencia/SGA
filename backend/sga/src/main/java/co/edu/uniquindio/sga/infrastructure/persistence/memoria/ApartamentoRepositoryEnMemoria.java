package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link ApartamentoRepository}. */
// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class ApartamentoRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionApartamento, Apartamento>
        implements ApartamentoRepository {

    public ApartamentoRepositoryEnMemoria() {
        super(Apartamento::getIdentificacion);
    }

    @Override
    public void guardar(Apartamento apartamento) {
        almacenar(apartamento);
    }

    @Override
    public Optional<Apartamento> buscarPorId(IdentificacionApartamento id) {
        return porId(id);
    }

    @Override
    public boolean existePorId(IdentificacionApartamento id) {
        return porId(id).isPresent();
    }

    @Override
    public List<Apartamento> listarActivos() {
        return filtrar(Apartamento::estaActivo);
    }

    @Override
    public List<Apartamento> listar() {
        return todos();
    }
}
