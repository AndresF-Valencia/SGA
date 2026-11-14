package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.domain.repository.EventoCanalRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.EventoCanalPersistenceMapper;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventoCanalRepositoryJpa implements EventoCanalRepository {

    private final EventoCanalJpaDataRepository jpa;

    public EventoCanalRepositoryJpa(EventoCanalJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(EventoCanal evento) {
        if (evento == null) {
            throw new IllegalArgumentException("No se puede guardar un evento de canal nulo");
        }
        jpa.save(EventoCanalPersistenceMapper.aEntity(evento));
    }

    @Override
    public List<EventoCanal> listar() {
        return jpa.findTop500ByOrderByOcurridoEnDesc().stream().map(EventoCanalPersistenceMapper::aDominio).toList();
    }
}
