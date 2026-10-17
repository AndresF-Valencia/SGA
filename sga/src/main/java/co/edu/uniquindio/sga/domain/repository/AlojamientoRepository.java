package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;

import java.util.Optional;

/** Puerto de salida del agregado Alojamiento (configuración del negocio, F-13). */
public interface AlojamientoRepository {

    void guardar(Alojamiento alojamiento);

    Optional<Alojamiento> buscarPorId(IdentificacionAlojamiento id);
}
