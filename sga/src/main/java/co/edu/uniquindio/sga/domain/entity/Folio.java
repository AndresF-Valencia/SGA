package co.edu.uniquindio.sga.domain.entity;

import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;

/**
 * Raíz del agregado Folio: la cuenta de una reserva.
 * Acumula cargos y pagos, y de ellos determina el saldo.
 *
 * Invariantes que garantiza:
 * - el saldo es siempre la diferencia entre cargos y pagos (RN-15);
 * se calcula, nunca se guarda, para que no pueda desincronizarse.
 */

public class Folio {

    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

    /**
     * RN-15: calcula el saldo del folio, que es la diferencia entre cargos y pagos.
     * Si es positivo, el huésped le debe al alojamiento; si es cero, la cuenta está
     * saldada;
     * si es negativo, el huésped tiene saldo a favor.
     */
    public Dinero saldo() {
        Dinero totalCargos = Dinero.CERO;

        for (Cargo cargo : cargos) {
            totalCargos = totalCargos.mas(cargo.valor());
        }

        Dinero totalPagos = Dinero.CERO;
        for (Pago pago : pagos) {
            totalPagos = totalPagos.mas(pago.getMonto());
        }
        return totalCargos.menos(totalPagos);
    }

}
