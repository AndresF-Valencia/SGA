package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: NoShowService
 * 7.5, RN-12, RN-13, RNP-04: declara el no-show y aplica su consecuencia.
 * Según la Ficha (A.4), la consecuencia es la retención del anticipo.
 * La reserva valida la hora límite y la notificación de retraso (RNP-04).
 */
public class NoShowService {

    /** @return el valor retenido */
    public Dinero declararNoShow(Reserva reserva, Folio folio, Alojamiento alojamiento,
            String autor, LocalDateTime ahora) {
        if (reserva == null || folio == null || alojamiento == null || ahora == null) {
            throw new ReglaDominioException(
                    "La reserva, el folio, el alojamiento y la marca temporal son obligatorios");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva");
        }
        if (folio.isCerrado()) {
            throw new ReglaDominioException("No se puede declarar no-show con el folio cerrado");
        }

        // 1. La reserva pasa a NO_SHOW (RN-12); si no procede, lanza y nada cambia
        reserva.declararNoShow(autor, ahora);

        // 2. Retención del anticipo (Ficha A.4)
        Dinero valorReserva = reserva.getValor().total();
        Dinero retencion = alojamiento.isExigeAnticipo()
                ? valorReserva.porcentual(alojamiento.getPorcentajeAnticipo())
                : Dinero.CERO;

        if (valorReserva.esPositivo()) {
            folio.registrarCargo(new Cargo(valorReserva.negado(),
                    "Reversión del alojamiento por no-show", TipoCargo.AJUSTE, ahora));
        }
        if (retencion.esPositivo()) {
            folio.registrarCargo(new Cargo(retencion,
                    "Retención del anticipo por no-show", TipoCargo.PENALIDAD_NO_SHOW, ahora));
        }
        return retencion;
    }
}