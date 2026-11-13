package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Caso de uso: Consultar el historial de versiones de las políticas de cancelación.
 */
public class ConsultarHistorialPoliticasCancelacionUseCase {

    private final PoliticaCancelacionRepository politicaCancelacionRepository;

    public ConsultarHistorialPoliticasCancelacionUseCase(PoliticaCancelacionRepository politicaCancelacionRepository) {
        this.politicaCancelacionRepository = politicaCancelacionRepository;
    }

    @Transactional(readOnly = true)
    public Map<VersionPolitica, PoliticaCancelacion> ejecutar() {
        return politicaCancelacionRepository.obtenerTodas();
    }
}