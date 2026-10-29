package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Reserva (Domain-Driven Design).
 *
 * El dominio define QUÉ necesita guardar y consultar; la capa de infraestructura
 * decide CÓMO (actualmente en memoria, en el futuro mediante JPA / PostgreSQL / H2).
 * Por esto la interfaz permanece totalmente pura y libre de dependencias de Spring o JPA.
 */
public interface ReservaRepository {

    /** 
     * Guarda una reserva nueva o actualiza el estado de una existente. 
     */
    void guardar(Reserva reserva);


     /**
     * Busca una reserva por su identificador EXTERNO dentro del dominio.
     */
    Optional<Reserva> buscarPorCodigoExterno(String codigoExterno);

    /**
     * Busca una reserva por su identificador único dentro del dominio.
     */
    Optional<Reserva> buscarPorCodigo(CodigoReserva codigo);


    /**
     * RN-01, RN-07, RN-14:
     * Retorna únicamente las reservas en estado activo (PENDIENTE, CONFIRMADA y EN_CURSO)
     * que actualmente retienen disponibilidad sobre un apartamento específico.
     */
    List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento);

    /**
     * RN-21:
     * Retorna todas las reservas que se encuentran en estado PENDIENTE, 
     * utilizado por el caso de uso de vencimiento automático de reservas.
     */
    List<Reserva> buscarPendientes();

    /**
     * Retorna la totalidad de las reservas registradas en el sistema.
     * Útil para evaluaciones globales de disponibilidad, trazabilidad y reportes.
     */
    List<Reserva> obtenerTodas();
}