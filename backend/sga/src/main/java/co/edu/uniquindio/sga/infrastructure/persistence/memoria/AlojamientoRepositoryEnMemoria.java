package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Adaptador en memoria del puerto {@link AlojamientoRepository}. */
@Repository
public class AlojamientoRepositoryEnMemoria extends AlmacenEnMemoria<IdentificacionAlojamiento, Alojamiento>
        implements AlojamientoRepository {

    public AlojamientoRepositoryEnMemoria() {
        super(Alojamiento::getIdentificacion);
    }

    @Override
    public void guardar(Alojamiento alojamiento) {
        almacenar(alojamiento);
    }

    @Override
    public Optional<Alojamiento> buscarPorId(IdentificacionAlojamiento id) {
        return porId(id);
    }

    @Override
    public Optional<Alojamiento> obtenerUnico() {
        // Al tratarse del establecimiento principal (Sanrocco), 
        // retorna el primer alojamiento registrado en el almacén en memoria.
        return todos().stream().findFirst();
    }

  
}