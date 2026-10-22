package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: RegistroSalidaService (check-out)
 * RN-08, RN-12, RN-17 y 7.6: el grupo entrega el apartamento.
 *
 * Coordina tres agregados:
 * - el Folio se cierra: con saldo cero, o con autorización del administrador
 *   si queda saldo (7.6: "el cierre del folio es requisito para completar la
 *   salida, salvo autorización del administrador");
 * - la Reserva pasa de EN_CURSO a FINALIZADA y libera sus noches (RN-12);
 * - el Apartamento pasa de OCUPADO a PENDIENTE_PREPARACION.
 *
 * Todo se valida antes de cambiar algo, para no dejar el folio cerrado con
 * una reserva que no pudo finalizar.
 */
public class RegistroSalidaService {

    /**
     * @param autorizacion autorización del administrador; solo se usa si el
     *                     folio tiene saldo distinto de cero. Puede ser null.
     */
    public void registrarSalida(Reserva reserva, Apartamento apartamento, Folio folio,
                                AutorizacionCierre autorizacion, String autor, LocalDateTime ahora) {
        if (reserva == null || apartamento == null || folio == null || ahora == null) {
            throw new ReglaDominioException(
                    "La reserva, el apartamento, el folio y la marca temporal son obligatorios");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("Debe indicarse quién registra la salida");
        }
        if (!reserva.getApartamento().equals(apartamento.getIdentificacion())) {
            throw new ReglaDominioException("La reserva no corresponde a este apartamento");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva");
        }

        // 1. Validar todo antes de tocar nada
        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException(
                    "Solo se registra la salida de una reserva EN_CURSO; esta está " + reserva.getEstado());
        }
        if (apartamento.getEstadoOperativo() != EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException(
                    "El apartamento debería estar OCUPADO y está " + apartamento.getEstadoOperativo());
        }
        boolean necesitaAutorizacion = !folio.isCerrado() && !folio.saldo().esCero();
        if (necesitaAutorizacion && autorizacion == null) {
            throw new ReglaDominioException(String.format(
                    "El folio tiene un saldo de %s: se necesita la autorización del administrador para cerrarlo (RN-17)",
                    folio.saldo().valor()));
        }

        // 2. Cerrar el folio (RN-17)
        if (!folio.isCerrado()) {
            if (necesitaAutorizacion) {
                folio.cerrarConAutorizacion(autorizacion);
            } else {
                folio.cerrar();
            }
        }

        // 3. EN_CURSO → FINALIZADA y OCUPADO → PENDIENTE_PREPARACION
        reserva.registrarSalida(autor, ahora);
        apartamento.marcarPendientePreparacion();
    }
}
