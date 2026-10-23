package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link ApartamentoRepository}. */
@Repository
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
    public List<Apartamento> listarActivos() {
        return filtrar(Apartamento::estaActivo);
    }

    @Override
    public List<Apartamento> listar() {
        return todos();
    }
}
