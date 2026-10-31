package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ApartamentoPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ApartamentoRepositoryJpa implements ApartamentoRepository {

    private final ApartamentoJpaDataRepository jpa;

    public ApartamentoRepositoryJpa(ApartamentoJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("No se puede guardar un apartamento nulo");
        }
        jpa.save(ApartamentoPersistenceMapper.aEntity(apartamento));
    }

    @Override
    public Optional<Apartamento> buscarPorId(IdentificacionApartamento id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.valor()).map(ApartamentoPersistenceMapper::aDominio);
    }

    @Override
    public boolean existePorId(IdentificacionApartamento id) {
        return id != null && jpa.existsById(id.valor());
    }

    @Override
    public List<Apartamento> listarActivos() {
        return jpa.findByActivoTrue().stream().map(ApartamentoPersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Apartamento> listar() {
        return jpa.findAll().stream().map(ApartamentoPersistenceMapper::aDominio).toList();
    }
}
