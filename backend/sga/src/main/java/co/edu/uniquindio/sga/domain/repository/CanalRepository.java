package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;

import java.util.List;
import java.util.Optional;

/** Puerto de salida de los canales de venta (7.8). */
public interface CanalRepository {

    void guardar(Canal canal);

    Optional<Canal> buscarPorId(IdentificacionCanal id);

    /** Para autenticar a un canal externo que envía una reserva por integración. */
    Optional<Canal> buscarPorCredencial(String credencialAcceso);

    List<Canal> listarActivos();
}
