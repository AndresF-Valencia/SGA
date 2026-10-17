package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Reserva.
 *
 * El dominio dice QUÉ necesita guardar y consultar; la capa de infraestructura
 * decide CÓMO (en memoria, con JPA y H2, etc.). Por eso aquí no hay nada de
 * Spring ni de base de datos.
 */
public interface ReservaRepository {

    /** Guarda una reserva nueva o reemplaza la que tenga el mismo código. */
    void guardar(Reserva reserva);

    Optional<Reserva> buscarPorCodigo(CodigoReserva codigo);

    /**
     * Reservas que retienen noches del apartamento (PENDIENTE, CONFIRMADA y
     * EN_CURSO). Es lo que necesitan la disponibilidad (RN-01), el bloqueo
     * (7.3) y la modificación (RN-14).
     */
    List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento);

    /** Reservas PENDIENTES, para vencer las que superen el plazo (RN-21). */
    List<Reserva> buscarPendientes();

    List<Reserva> listar();
}
