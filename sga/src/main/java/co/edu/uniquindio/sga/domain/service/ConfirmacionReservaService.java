package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: ConfirmacionReservaService
 * RN-08, RN-09, RP-03: valida que el folio de la reserva registre el anticipo
 * exigido por el alojamiento antes de confirmarla.
 *
 * El porcentaje ya no está fijo en 50: lo da el Alojamiento
 * ({@link Alojamiento#getPorcentajeAnticipo()}), y si el alojamiento no exige
 * anticipo, no se pide.
 */
public class ConfirmacionReservaService {

    /**
     * Autoriza la confirmación de la reserva validando el anticipo del folio
     * (RP-03); la reserva valida por su cuenta la hora de llegada (RN-09) y la
     * transición de estado (RN-08).
     */
    public void autorizarConfirmacion(Reserva reserva, Folio folio, Alojamiento alojamiento,
                                      LocalDateTime ahora, String autor) {
        if (reserva == null || folio == null || alojamiento == null || ahora == null) {
            throw new ReglaDominioException(
                    "La reserva, el folio, el alojamiento y la marca temporal son obligatorios para confirmar");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("Debe indicarse quién confirma la reserva");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva que se quiere confirmar");
        }

        // 1. Solo se confirman reservas PENDIENTES (RN-08)
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden confirmar reservas que se encuentran en estado PENDIENTE");
        }

        // 2. Verificar el anticipo, si el alojamiento lo exige (RP-03)
        if (alojamiento.isExigeAnticipo()) {
            Dinero anticipoRequerido = calcularAnticipoRequerido(
                    reserva.getValor().total(), alojamiento.getPorcentajeAnticipo());
            verificarAnticipoRegistrado(folio, anticipoRequerido);
        }

        // 3. La reserva protege su propio cambio de estado
        reserva.confirmar(autor, ahora);
    }

    /** Calcula el anticipo exigido sobre el valor total de la reserva. */
    public Dinero calcularAnticipoRequerido(Dinero valorTotal, int porcentajeAnticipo) {
        if (valorTotal == null) {
            throw new ReglaDominioException("El valor total de la reserva es obligatorio para calcular el anticipo");
        }
        return valorTotal.porcentual(porcentajeAnticipo);
    }

    /** Valida que lo pagado en el folio cubra el anticipo mínimo. */
    public void verificarAnticipoRegistrado(Folio folio, Dinero anticipoRequerido) {
        if (folio == null || anticipoRequerido == null) {
            throw new ReglaDominioException("El folio y el anticipo requerido son obligatorios para la validación");
        }
        Dinero totalPagado = folio.totalPagado();
        if (totalPagado.esMenorQue(anticipoRequerido)) {
            throw new ReglaDominioException(String.format(
                    "Anticipo insuficiente. Se requiere un mínimo de %s, pero el folio registra un total pagado de %s",
                    anticipoRequerido.valor(), totalPagado.valor()));
        }
    }
}
