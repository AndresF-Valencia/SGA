package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.ResultadoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: CancelacionReservaService
 * RN-12, RN-13, RN-16: cancela una reserva y deja la cuenta correcta en el
 * folio.
 *
 * La reserva solo guarda el número de versión de su política (RN-13); por eso
 * quien llama debe buscar esa versión y pasarla aquí como
 * {@code politicaCongelada}, nunca la política vigente.
 *
 * En el folio el alojamiento ya está cobrado completo. Como los cargos no se
 * borran (RN-16), se reversa con un AJUSTE negativo y se agrega la penalidad
 * que diga la política. Así el saldo queda en "penalidad - pagos": si es
 * negativo, es lo que se le devuelve al huésped.
 */
public class CancelacionReservaService {

    /**
     * @param folio puede ser null si la reserva todavía no tiene folio
     * @return el tramo aplicado, la penalidad y el reembolso
     */
    public ResultadoCancelacion cancelar(Reserva reserva, Folio folio, PoliticaCancelacion politicaCongelada,
            Alojamiento alojamiento, String motivo, String autor, LocalDateTime ahora) {
        if (reserva == null || politicaCongelada == null || alojamiento == null || ahora == null) {
            throw new ReglaDominioException(
                    "La reserva, su política, el alojamiento y la marca temporal son obligatorias");
        }
        if (folio != null && !folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva");
        }
        if (folio != null && folio.isCerrado()) {
            throw new ReglaDominioException("No se puede cancelar una reserva con el folio cerrado");
        }
        // Validar antes de tocar el folio; los demás detalles los valida la reserva
        if (!reserva.getEstado().puedeTransicionarA(EstadoReserva.CANCELADA)) {
            throw new ReglaDominioException("Una reserva " + reserva.getEstado() + " no se puede cancelar");
        }

        // 1. Calcular retención y devolución con la política congelada (RN-13).
        // Según el glosario del equipo, el tramo define la devolución del ANTICIPO.
        Dinero valorReserva = reserva.getValor().total();
        Dinero anticipo = alojamiento.isExigeAnticipo()
                ? valorReserva.porcentual(alojamiento.getPorcentajeAnticipo())
                : Dinero.CERO;
        ResultadoCancelacion resultado = politicaCongelada.evaluar(ahora, reserva.getEstancia(), anticipo);

        // 2. Cancelar la reserva: libera las noches de inmediato (RN-12)
        reserva.cancelar(motivo, autor, ahora);

        // 3. Dejar la cuenta en el folio (RN-16: movimientos nuevos, nada se borra)
        if (folio != null) {
            if (valorReserva.esPositivo()) {
                folio.registrarCargo(new Cargo(valorReserva.negado(),
                        "Reversión del alojamiento por cancelación", TipoCargo.AJUSTE, ahora));
            }
            if (resultado.penalidad().esPositivo()) {
                folio.registrarCargo(new Cargo(resultado.penalidad(),
                        "Penalidad por cancelación (" + resultado.tramoAplicado().porcentajeDevolucion()
                                + " % de devolución)",
                        TipoCargo.PENALIDAD_POR_CANCELACION, ahora));
            }
        }
        return resultado;
    }
}
