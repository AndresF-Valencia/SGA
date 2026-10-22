package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link CanalRepository}. */
@Repository
public class CanalRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionCanal, Canal>
        implements CanalRepository {

    public CanalRepositoryEnMemoria() {
        super(Canal::getIdentificacion);
    }

    @Override
    public void guardar(Canal canal) {
        almacenar(canal);
    }

    @Override
    public Optional<Canal> buscarPorId(IdentificacionCanal id) {
        return porId(id);
    }

    @Override
    public Optional<Canal> buscarPorCredencial(String credencialAcceso) {
        if (credencialAcceso == null) {
            return Optional.empty();
        }
        return filtrar(c -> c.getCredencialAcceso().equals(credencialAcceso)).stream().findFirst();
    }

    @Override
    public List<Canal> listarActivos() {
        return filtrar(Canal::isActivo);
    }
}
