package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.transaction.annotation.Transactional;

/** Consulta el alojamiento único del sistema (F-13) con todos sus parámetros (7.3). */
public class ConsultarAlojamientoUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public ConsultarAlojamientoUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional(readOnly = true)
    public Alojamiento ejecutar() {
        return alojamientoRepository.obtenerUnico().orElseThrow(AlojamientoNoEncontradoException::new);
    }
}
