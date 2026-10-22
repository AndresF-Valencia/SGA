package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del agregado Temporada. */
public interface TemporadaRepository {

    void guardar(Temporada temporada);

    Optional<Temporada> buscarPorId(IdentificacionTemporada id);

    /**
     * Temporadas que tocan al menos un día entre {@code desde} y {@code hasta}
     * (ambos incluidos). Es lo que necesitan el cotizador (RN-05) y la
     * estancia mínima (RP-01) para una estancia.
     */
    List<Temporada> buscarEntre(LocalDate desde, LocalDate hasta);

    List<Temporada> listar();
}
