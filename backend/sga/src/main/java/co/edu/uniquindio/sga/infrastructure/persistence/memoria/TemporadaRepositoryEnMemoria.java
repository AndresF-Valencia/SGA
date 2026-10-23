package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador en memoria del puerto {@link TemporadaRepository}. */
@Repository
public class TemporadaRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionTemporada, Temporada>
        implements TemporadaRepository {

    public TemporadaRepositoryEnMemoria() {
        super(Temporada::getIdentificacion);
    }

    @Override
    public void guardar(Temporada temporada) {
        almacenar(temporada);
    }

    @Override
    public Optional<Temporada> buscarPorId(IdentificacionTemporada id) {
        return porId(id);
    }

    @Override
    public List<Temporada> buscarEntre(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Se requieren las dos fechas del rango");
        }
        return filtrar(t -> !t.getPeriodo().hasta().isBefore(desde) && !t.getPeriodo().desde().isAfter(hasta));
    }

    @Override
    public List<Temporada> listar() {
        return todos();
    }
}
