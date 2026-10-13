package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.valueobject.Dinero;

public class Pago {

    private final Dinero monto;

    public Pago(Dinero monto) {
        this.monto = monto;
    }

    public Dinero getMonto() {
        return monto;
    }
}