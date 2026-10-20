package co.edu.uniquindio.sga.infrastructure.persistence;

import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PoliticaCancelacionRepositoryEnMemoria implements PoliticaCancelacionRepository {

    private final Map<VersionPolitica, PoliticaCancelacion> politicas = new HashMap<>();

    @Override
    public void guardar(VersionPolitica version, PoliticaCancelacion politica) {
        politicas.put(version, politica);
    }

    @Override
    public Optional<PoliticaCancelacion> buscarPorVersion(VersionPolitica version) {
        return Optional.ofNullable(politicas.get(version));
    }
}
