package co.edu.uniquindio.sga.infrastructure.persistence;

import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
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
