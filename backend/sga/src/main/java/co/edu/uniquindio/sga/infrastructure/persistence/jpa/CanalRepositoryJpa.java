package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CanalPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CanalRepositoryJpa implements CanalRepository {

    private final CanalJpaDataRepository jpa;

    public CanalRepositoryJpa(CanalJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Canal canal) {
        if (canal == null) {
            throw new IllegalArgumentException("No se puede guardar un canal nulo");
        }
        jpa.save(CanalPersistenceMapper.aEntity(canal));
    }

    @Override
    public Optional<Canal> buscarPorId(IdentificacionCanal id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.identificacion()).map(CanalPersistenceMapper::aDominio);
    }

    @Override
    public Optional<Canal> buscarPorCredencial(String credencialAcceso) {
        if (credencialAcceso == null) {
            return Optional.empty();
        }
        return jpa.findByCredencialAcceso(credencialAcceso).map(CanalPersistenceMapper::aDominio);
    }

    @Override
    public List<Canal> listarActivos() {
        return jpa.findByActivoTrue().stream().map(CanalPersistenceMapper::aDominio).toList();
    }
}
