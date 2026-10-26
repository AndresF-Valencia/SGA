package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.AlojamientoPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AlojamientoRepositoryJpa implements AlojamientoRepository {

    private final AlojamientoJpaDataRepository jpa;

    public AlojamientoRepositoryJpa(AlojamientoJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Alojamiento alojamiento) {
        if (alojamiento == null) {
            throw new IllegalArgumentException("No se puede guardar un alojamiento nulo");
        }
        jpa.save(AlojamientoPersistenceMapper.aEntity(alojamiento));
    }

    @Override
    public Optional<Alojamiento> buscarPorId(IdentificacionAlojamiento id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.identificacion()).map(AlojamientoPersistenceMapper::aDominio);
    }

    /** F-13: el sistema administra un único alojamiento. */
    @Override
    public Optional<Alojamiento> obtenerUnico() {
        return jpa.findAll().stream().findFirst().map(AlojamientoPersistenceMapper::aDominio);
    }
}
