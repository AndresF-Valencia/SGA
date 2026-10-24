package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

/**
 * Caso de uso: Publicar política de cancelación (CU-19).
 * Publica y congela una nueva versión de la política de cancelación.
 */
@Service
public class PublicarPoliticaCancelacionUseCase {

    private final PoliticaCancelacionRepository politicaCancelacionRepository;

    public PublicarPoliticaCancelacionUseCase(PoliticaCancelacionRepository politicaCancelacionRepository) {
        this.politicaCancelacionRepository = politicaCancelacionRepository;
    }

    /**
     * Publica una versión específica de la política de cancelación para ser utilizada por las futuras reservas.
     * 
     * @param version  Versión de la política que se desea publicar y congelar.
     * @param politica Objeto de valor PoliticaCancelacion que contiene las reglas y tramos.
     */
    @Transactional
    public PoliticaCancelacion ejecutar(VersionPolitica version, PoliticaCancelacion politica) {
        if (version == null) {
            throw new ReglaDominioException("La versión de la política es obligatoria");
        }
        if (politica == null) {
            throw new ReglaDominioException("La política de cancelación es obligatoria");
        }

        // Persistir la versión y sus reglas correspondientes a través del repositorio
        politicaCancelacionRepository.guardar(version, politica);
        return politica;
    }
}