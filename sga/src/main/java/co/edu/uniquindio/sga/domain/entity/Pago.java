package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import java.time.LocalDateTime;

/**
 * Entidad interna del agregado Folio: un abono registrado contra la cuenta.
 * RN-15: todo pago tiene monto, medio y fecha.
 * RN-16: es inmutable; una corrección se hace con un movimiento inverso, nunca
 * editándolo.
 */

public class Pago {

    private final Dinero monto;
    private final MedioPago medioPago;
    private final LocalDateTime horaPago;

    public Pago(Dinero monto, MedioPago medioPago, LocalDateTime horaPago) {
        if (monto == null || monto.esCero()) {
            throw new ReglaDominioException("El pago debe indicar su monto");
        }
        if (monto.esNegativo()) {
            throw new ReglaDominioException("El pago no puede ser negativo");
        }
        if (medioPago == null) {
            throw new ReglaDominioException("El pago debe indicar su medio de pago");
        }
        if (horaPago == null) {
            throw new ReglaDominioException("El pago debe indicar la hora del pago");
        }
        this.monto = monto;
        this.medioPago = medioPago;
        this.horaPago = horaPago;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public LocalDateTime getHoraPago() {
        return horaPago;
    }

    public Dinero getMonto() {
        return monto;
    }
}