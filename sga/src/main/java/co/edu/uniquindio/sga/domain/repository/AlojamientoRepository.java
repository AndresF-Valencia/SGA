package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;

import java.util.Optional;

/** 
 * Puerto de salida del agregado Alojamiento (configuración del negocio, F-13).
 * El dominio define QUÉ necesita consultar; la infraestructura decide CÓMO.
 */
public interface AlojamientoRepository {

    void guardar(Alojamiento alojamiento);

    Optional<Alojamiento> buscarPorId(IdentificacionAlojamiento id);

    /**
     * Permite obtener la configuración única del establecimiento (Sanrocco) 
     * sin necesidad de conocer su identificador exacto de antemano.
     */
    Optional<Alojamiento> obtenerUnico();

}