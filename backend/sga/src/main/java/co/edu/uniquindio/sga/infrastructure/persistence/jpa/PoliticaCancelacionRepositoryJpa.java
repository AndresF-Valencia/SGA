package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.PoliticaCancelacionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PoliticaCancelacionRepositoryJpa implements PoliticaCancelacionRepository {

    private final PoliticaCancelacionJpaDataRepository jpa;

    public PoliticaCancelacionRepositoryJpa(PoliticaCancelacionJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(VersionPolitica version, PoliticaCancelacion politica) {
        if (version == null || politica == null) {
            throw new IllegalArgumentException("Se requieren la versión y la política");
        }
        jpa.save(PoliticaCancelacionPersistenceMapper.aEntity(version, politica));
    }

    @Override
    public Optional<PoliticaCancelacion> buscarPorVersion(VersionPolitica version) {
        if (version == null) {
            return Optional.empty();
        }
        return jpa.findById(version.numero()).map(PoliticaCancelacionPersistenceMapper::aDominio);
    }
}
