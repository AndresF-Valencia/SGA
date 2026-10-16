package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Dominio: EstanciaMinimaTemporadaAltaService
 * RP-01 (Regla propia 1, Anexo A.6): Rechaza la solicitud si una estancia
 * que abarca Temporada Alta no cumple con el número mínimo de noches exigido.
 */
public class EstanciaMinimaTemporadaAltaService {

    /**
     * Verifica que si la estancia incluye noches de temporada alta, cumpla con el mínimo de noches exigido (RP-01).
     * Lanza una ReglaDominioException si no cumple con la condición.
     */
    public void verificarEstanciaMinima(Estancia estancia, List<Temporada> temporadasDisponibles, int nochesMinimasTemporadaAlta) {
        if (estancia == null || temporadasDisponibles == null) {
            throw new ReglaDominioException("La estancia y las temporadas son obligatorias para verificar la estancia mínima");
        }

        // 1. Verificar si la estancia abarca o es de temporada alta
        if (esEstanciaDeTemporadaAlta(estancia, temporadasDisponibles)) {
            
            // 2. Validar que la cantidad de noches cumpla con el mínimo exigido
            if (estancia.noches() < nochesMinimasTemporadaAlta) {
                throw new ReglaDominioException(
                    String.format("La regla de temporada alta exige un mínimo de %d noches de estancia. La solicitud cuenta con %d noches.", 
                        nochesMinimasTemporadaAlta, estancia.noches())
                );
            }
        }
    }

    /**
     * Determina si alguna de las noches ocupadas de la estancia cae dentro de una Temporada Alta.
     */
    public boolean esEstanciaDeTemporadaAlta(Estancia estancia, List<Temporada> temporadasDisponibles) {
        if (estancia == null || temporadasDisponibles == null) {
            return false;
        }

        List<LocalDate> noches = estancia.nochesOcupadas();
        for (LocalDate noche : noches) {
            for (Temporada temporada : temporadasDisponibles) {
                // Verifica si la temporada cubre la noche y es de tipo alta 
                // (puedes ajustar el método según tengas implementado en tu entidad Temporada, ej: temporada.esAlta())
                if (temporada.cubre(noche) && temporada.esAlta()) {
                    return true;
                }
            }
        }
        return false;
    }
}