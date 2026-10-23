package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Dominio: EstanciaMinimaTemporadaAltaService
 * RP-01 (Regla propia 1, Anexo A.6): rechaza la solicitud si una estancia que
 * toca una temporada alta no cumple el mínimo de noches de esa temporada.
 *
 * El mínimo ya no llega como un número suelto: cada temporada guarda el suyo
 * ({@link Temporada#getEstanciaMinima()}), así que el servicio le pregunta a
 * cada temporada que toque la estancia ({@link Temporada#cumpleEstanciaMinima(int)}).
 * Así la regla vive en un solo lugar.
 */
public class EstanciaMinimaTemporadaAltaService {

    /**
     * Lanza una ReglaDominioException si alguna temporada alta que toca la
     * estancia exige más noches de las que tiene.
     */
    public void verificarEstanciaMinima(Estancia estancia, List<Temporada> temporadasDisponibles) {
        if (estancia == null || temporadasDisponibles == null) {
            throw new ReglaDominioException("La estancia y las temporadas son obligatorias para verificar la estancia mínima");
        }

        for (Temporada temporada : temporadasQueToca(estancia, temporadasDisponibles)) {
            if (!temporada.cumpleEstanciaMinima(estancia.noches())) {
                throw new ReglaDominioException(String.format(
                        "La temporada %s exige un mínimo de %d noches de estancia. La solicitud cuenta con %d noches.",
                        temporada.getNombre(), temporada.getEstanciaMinima().noches(), estancia.noches()));
            }
        }
    }

    /** Indica si alguna de las noches de la estancia cae en una temporada alta. */
    public boolean esEstanciaDeTemporadaAlta(Estancia estancia, List<Temporada> temporadasDisponibles) {
        if (estancia == null || temporadasDisponibles == null) {
            return false;
        }
        return temporadasQueToca(estancia, temporadasDisponibles).stream().anyMatch(Temporada::esAlta);
    }

    private List<Temporada> temporadasQueToca(Estancia estancia, List<Temporada> temporadas) {
        List<LocalDate> noches = estancia.nochesOcupadas();
        return temporadas.stream()
                .filter(t -> noches.stream().anyMatch(t::cubre))
                .toList();
    }
}
