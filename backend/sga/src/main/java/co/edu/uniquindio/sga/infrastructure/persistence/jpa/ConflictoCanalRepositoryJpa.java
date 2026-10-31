package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ConflictoCanalPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConflictoCanalRepositoryJpa implements ConflictoCanalRepository {

    private final ConflictoCanalJpaDataRepository jpa;

    public ConflictoCanalRepositoryJpa(ConflictoCanalJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(ConflictoCanal conflicto) {
        if (conflicto == null) {
            throw new IllegalArgumentException("No se puede guardar un conflicto nulo");
        }
        jpa.save(ConflictoCanalPersistenceMapper.aEntity(conflicto));
    }

    @Override
    public Optional<ConflictoCanal> buscarPorId(String id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(ConflictoCanalPersistenceMapper::aDominio);
    }

    @Override
    public Optional<ConflictoCanal> buscarPendientePorCodigoExterno(String codigoExterno) {
        return codigoExterno == null ? Optional.empty()
                : jpa.findFirstByCodigoExternoAndResueltoFalse(codigoExterno).map(ConflictoCanalPersistenceMapper::aDominio);
    }

    @Override
    public List<ConflictoCanal> listar() {
        return jpa.findAllByOrderByRecibidoEnDesc().stream().map(ConflictoCanalPersistenceMapper::aDominio).toList();
    }
}
