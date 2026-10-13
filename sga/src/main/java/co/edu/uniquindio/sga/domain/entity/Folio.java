package co.edu.uniquindio.sga.domain.entity;

import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

/**
 * Raíz del agregado Folio: la cuenta de una reserva.
 * Acumula cargos y pagos, y de ellos determina el saldo.
 *
 * Invariantes que garantiza:
 * - el saldo es siempre la diferencia entre cargos y pagos (RN-15);
 * - se calcula, nunca se guarda, para que no pueda desincronizarse.
 * - un folio cerrado no admite pagos.
 * - un folio cerrado no admite cargos.
 */

public class Folio {

    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

    private boolean cerrado;

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

    /**
     * RN-15: todo pago se registra contra un folio abierto, con su medio y su
     * fecha.
     */
    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new ReglaDominioException("El pago a registrar es obligatorio");
        }
        if (this.cerrado) {
            throw new ReglaDominioException("No se puede registrar un pago en un folio cerrado");
        }
        pagos.add(pago);
    }

    /**
     * RN-16: el cargo solo se agrega a un folio abierto, y nunca se modifica ni se
     * elimina
     */
    public void registrarCargo(Cargo cargo) {
        if (cargo == null) {
            throw new ReglaDominioException("El cargo a registrar es obligatorio");
        }
        if (this.cerrado) {
            throw new ReglaDominioException("No se puede registrar un cargo en un folio cerrado");
        }
        cargos.add(cargo);
    }

}
