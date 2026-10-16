package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: ConfirmacionReservaService
 * RN-08, RN-09, RP-03: Valida que el folio asociado registre el anticipo requerido (50%)
 * y que la reserva cumpla con las condiciones para autorizar su confirmación.
 */
public class ConfirmacionReservaService {

    /**
     * Autoriza la confirmación de la reserva validando el anticipo del folio (RP-03)
     * y el cumplimiento de los plazos (RN-09).
     */
    public void autorizarConfirmacion(Reserva reserva, Folio folio, LocalDateTime ahora, String autor) {
        if (reserva == null || folio == null || ahora == null || autor.isBlank()) {
            throw new ReglaDominioException("La reserva,el autor, el folio y la marca temporal son obligatorios para confirmar");
        }

        // 1. Validar que la reserva esté en estado PENDIENTE (RN-08)
        if (reserva.getEstado()!= EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden confirmar reservas que se encuentran en estado PENDIENTE");
        }

        // 2. Calcular el anticipo requerido (50% del valor total de la reserva)
        Dinero valorTotal = reserva.getValor().total(); 
        Dinero anticipoRequerido = calcularAnticipoRequerido(valorTotal);

        // 3. Verificar que el folio ya registre al menos ese 50% pagado (Regla propia 3 - RP-03)
        verificarAnticipoRegistrado(folio, anticipoRequerido);

        // 4. Delegar en la reserva la transición de PENDIENTE a CONFIRMADA (RN-08)
        // La reserva protege su propio comportamiento de cambio de estado.
        reserva.confirmar(autor,ahora);
    }

    /**
     * Calcula el 50% de anticipo exigido sobre el valor total.
     */
    public Dinero calcularAnticipoRequerido(Dinero valorTotal) {
        if (valorTotal == null) {
            throw new ReglaDominioException("El valor total de la reserva es obligatorio para calcular el anticipo");
        }
        // Utiliza el método porcentual que ya definiste en tu objeto de valor Dinero
        return valorTotal.porcentual(50.0);
    }

    /**
     * Valida que el saldo abonado en el folio cumpla con el anticipo mínimo.
     */
    public void verificarAnticipoRegistrado(Folio folio, Dinero anticipoRequerido) {
        if (folio == null || anticipoRequerido == null) {
            throw new ReglaDominioException("El folio y el anticipo requerido son obligatorios para la validación");
        }

        // Sumamos los montos de todos los pagos registrados en el folio
        Dinero totalPagado = folio.getPagos().stream()
                .map(Pago::getMonto)
                .reduce(Dinero.CERO, Dinero::mas);
        
        // Comparamos el valor BigDecimal del total pagado frente al anticipo requerido
        if (totalPagado.valor().compareTo(anticipoRequerido.valor()) < 0) {
            throw new ReglaDominioException(
                String.format("Anticipo insuficiente. Se requiere un mínimo del 50%% (%s), pero el folio registra un total pagado de %s",
                    anticipoRequerido.valor(), totalPagado.valor())
            );
        }
    }
}