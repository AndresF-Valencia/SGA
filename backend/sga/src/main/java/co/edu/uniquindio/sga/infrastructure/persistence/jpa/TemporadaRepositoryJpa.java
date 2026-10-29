package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TemporadaPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TemporadaRepositoryJpa implements TemporadaRepository {

    private final TemporadaJpaDataRepository jpa;

    public TemporadaRepositoryJpa(TemporadaJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Temporada temporada) {
        if (temporada == null) {
            throw new IllegalArgumentException("No se puede guardar una temporada nula");
        }
        jpa.save(TemporadaPersistenceMapper.aEntity(temporada));
    }

    @Override
    public Optional<Temporada> buscarPorId(IdentificacionTemporada id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.id()).map(TemporadaPersistenceMapper::aDominio);
    }

    @Override
    public List<Temporada> buscarEntre(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Se requieren las dos fechas del rango");
        }
        return jpa.findByPeriodoHastaGreaterThanEqualAndPeriodoDesdeLessThanEqual(desde, hasta).stream()
                .map(TemporadaPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public List<Temporada> listar() {
        return jpa.findAll().stream().map(TemporadaPersistenceMapper::aDominio).toList();
    }
}
