package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TarifaPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TarifaRepositoryJpa implements TarifaRepository {

    private final TarifaJpaDataRepository jpa;

    public TarifaRepositoryJpa(TarifaJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Tarifa tarifa) {
        if (tarifa == null) {
            throw new IllegalArgumentException("No se puede guardar una tarifa nula");
        }
        jpa.save(TarifaPersistenceMapper.aEntity(tarifa));
    }

    @Override
    public Optional<Tarifa> buscarPorId(IdentificacionTarifa id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.identificacion()).map(TarifaPersistenceMapper::aDominio);
    }

    @Override
    public List<Tarifa> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return jpa.findByApartamentoId(apartamento.valor()).stream()
                .map(TarifaPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public Optional<Tarifa> buscarPorApartamentoYTemporada(IdentificacionApartamento apartamento,
                                                           IdentificacionTemporada temporada) {
        return jpa.findFirstByApartamentoIdAndTemporadaId(apartamento.valor(), temporada.id())
                .map(TarifaPersistenceMapper::aDominio);
    }
}
