package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;

import java.util.List;
import java.util.Optional;

/** Puerto de salida de las tarifas (valor por ocupante y noche, 7.4). */
public interface TarifaRepository {

    void guardar(Tarifa tarifa);

    Optional<Tarifa> buscarPorId(IdentificacionTarifa id);

    /** Las tarifas del apartamento en todas las temporadas: las usa el cotizador. */
    List<Tarifa> buscarPorApartamento(IdentificacionApartamento apartamento);

    Optional<Tarifa> buscarPorApartamentoYTemporada(IdentificacionApartamento apartamento,
                                                    IdentificacionTemporada temporada);
}
