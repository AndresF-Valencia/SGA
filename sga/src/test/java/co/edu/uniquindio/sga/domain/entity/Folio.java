package co.edu.uniquindio.sga.domain.entity;

import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;

public class Folio {

    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

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
