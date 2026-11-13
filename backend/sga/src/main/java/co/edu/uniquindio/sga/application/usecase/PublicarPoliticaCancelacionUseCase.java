package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository; // Asegúrate de tener este repositorio
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

/**
 * Caso de uso: Publicar política de cancelación (CU-19).
 * El sistema asigna la nueva versión a través del Alojamiento y la congela.
 */
public class PublicarPoliticaCancelacionUseCase {

    private final AlojamientoRepository alojamientoRepository;
    private final PoliticaCancelacionRepository politicaCancelacionRepository;

    public PublicarPoliticaCancelacionUseCase(AlojamientoRepository alojamientoRepository,
                                              PoliticaCancelacionRepository politicaCancelacionRepository) {
        this.alojamientoRepository = alojamientoRepository;
        this.politicaCancelacionRepository = politicaCancelacionRepository;
    }

    /**
     * Publica una nueva versión de la política de cancelación asignada automáticamente por el sistema.
     * 
     * @param politica Objeto de valor PoliticaCancelacion que contiene las reglas y tramos.
     * @return La versión que el sistema asignó y entró en vigor.
     */
    @Transactional
    public VersionPolitica ejecutar(PoliticaCancelacion politica) {
        if (politica == null) {
            throw new ReglaDominioException("La política de cancelación es obligatoria");
        }

        // 1. Obtener el agregado Alojamiento para gestionar la versión vigente
        Alojamiento alojamiento = alojamientoRepository.obtenerUnico()
                .orElseThrow(() -> new AlojamientoNoEncontradoException());

        // 2. El sistema asigna la nueva versión llamando al método del dominio
        VersionPolitica nuevaVersion = alojamiento.publicarNuevaVersionPolitica();

        // 3. Persistir la política ligada a la nueva versión y actualizar el alojamiento
        politicaCancelacionRepository.guardar(nuevaVersion, politica);
        alojamientoRepository.guardar(alojamiento);

        return nuevaVersion;
    }
}