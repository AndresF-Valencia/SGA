package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.domain.repository.EventoCanalRepository;

import java.util.Comparator;
import java.util.List;

// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class EventoCanalRepositoryEnMemoria extends AlmacenEnMemoria<String, EventoCanal>
        implements EventoCanalRepository {

    public EventoCanalRepositoryEnMemoria() {
        super(EventoCanal::getId);
    }

    @Override
    public void guardar(EventoCanal evento) {
        almacenar(evento);
    }

    @Override
    public List<EventoCanal> listar() {
        return todos().stream().sorted(Comparator.comparing(EventoCanal::getOcurridoEn).reversed()).limit(500).toList();
    }
}
