package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.PoliticaCancelacionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Map<VersionPolitica, PoliticaCancelacion> obtenerTodas() {
        return jpa.findAll().stream()
                .collect(Collectors.toMap(
                        entity -> new VersionPolitica(entity.getVersion()), // Clave: VersionPolitica
                        PoliticaCancelacionPersistenceMapper::aDominio,     // Valor: PoliticaCancelacion mapeada
                        (existente, reemplazo) -> existente,              // Manejo de colisiones (por seguridad)
                        LinkedHashMap::new                                // Preservar el orden de inserción/versiones
                ));
    }

}
