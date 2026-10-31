package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class ConflictoCanalRepositoryEnMemoria extends AlmacenEnMemoria<String, ConflictoCanal>
        implements ConflictoCanalRepository {

    public ConflictoCanalRepositoryEnMemoria() {
        super(ConflictoCanal::getId);
    }

    @Override
    public void guardar(ConflictoCanal conflicto) {
        almacenar(conflicto);
    }

    @Override
    public Optional<ConflictoCanal> buscarPorId(String id) {
        return porId(id);
    }

    @Override
    public Optional<ConflictoCanal> buscarPendientePorCodigoExterno(String codigoExterno) {
        return filtrar(c -> !c.estaResuelto() && c.getCodigoExterno().equals(codigoExterno)).stream().findFirst();
    }

    @Override
    public List<ConflictoCanal> listar() {
        return todos().stream().sorted(Comparator.comparing(ConflictoCanal::getRecibidoEn).reversed()).toList();
    }
}
