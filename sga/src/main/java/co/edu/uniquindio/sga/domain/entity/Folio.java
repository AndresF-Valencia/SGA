package co.edu.uniquindio.sga.domain.entity;

import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import java.util.Collections;

/**
 * Raíz del agregado Folio: la cuenta de una reserva.
 * Acumula cargos y pagos, y de ellos determina el saldo.
 *
 * Invariantes que garantiza:
 * - el saldo es siempre la diferencia entre cargos y pagos (RN-15);
 * - se calcula, nunca se guarda, para que no pueda desincronizarse.
 * - un folio cerrado no admite pagos.
 * - un folio cerrado no admite cargos.
 * - un folio pertenece a una sola reserva y se abre con su cargo de
 * alojamiento.
 * - solo se cierra con saldo cero, o con una autorización registrada (RN-17);
 * - los cargos y pagos solo se consultan; nunca se modifican desde afuera
 * (RN-16).
 */

public class Folio {

    private final CodigoReserva reserva;
    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private AutorizacionCierre autorizacionCierre;
    private boolean cerrado;

    /**
     * Constructor del folio.
     * F-08: el folio se abre al crear la reserva, con su cargo de alojamiento
     * (7.7).
     */
    public Folio(CodigoReserva reserva, Cargo cargoAlojamiento) {
        if (reserva == null) {
            throw new ReglaDominioException("El folio debe estar asociado a una reserva");
        }
        if (cargoAlojamiento == null) {
            throw new ReglaDominioException("El folio debe iniciar con un cargo de alojamiento");
        }
        if (cargoAlojamiento.tipo() != TipoCargo.ALOJAMIENTO) {
            throw new ReglaDominioException("El folio solo puede abrirse con un cargo de tipo alojamiento");
        }
        this.reserva = reserva;
        this.cargos.add(cargoAlojamiento);
    }

    public CodigoReserva getReserva() {
        return reserva;
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public AutorizacionCierre getAutorizacionCierre() {
        return autorizacionCierre;
    }

    public List<Cargo> getCargos() {
        return Collections.unmodifiableList(cargos);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

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

    /** RN-17: el folio se cierra sin autorización solo si el saldo es cero. */
    public void cerrar() {
        if (this.cerrado) {
            throw new ReglaDominioException("El folio ya está cerrado");
        }
        if (!saldo().esCero()) {
            throw new ReglaDominioException("El folio no puede cerrarse con saldo distinto de cero");
        }
        this.cerrado = true;
    }

    /**
     * RN-17: con saldo distinto de cero, el cierre exige una autorización
     * registrada.
     */
    public void cerrarConAutorizacion(AutorizacionCierre autorizacion) {
        if (autorizacion == null) {
            throw new ReglaDominioException(
                    "Se requiere una autorización para cerrar el folio con saldo distinto de cero");
        }
        if (this.cerrado) {
            throw new ReglaDominioException("El folio ya está cerrado");
        }
        autorizacionCierre = autorizacion;
        this.cerrado = true;
    }

}
