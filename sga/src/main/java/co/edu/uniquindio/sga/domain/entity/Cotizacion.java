package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;

/**
 * Representa la cotización de una estancia antes de confirmar la reserva (7.4).
 * Calcula el valor noche por noche según tarifas y ocupantes facturables (RN-05).
 */
public class Cotizacion {

    private final IdentificacionApartamento identificacionApartamento;
    private final Estancia estancia;
    private final int ocupantesFacturables;
    private final CantidadMascota cantidadMascota;
    private final Dinero montoTotal;
    //private final List<DetalleCotizacionNoche> desglose;

    public Cotizacion(IdentificacionApartamento identificacionApartamento,
                      Estancia estancia,
                      int ocupantesFacturables,
                      CantidadMascota cantidadMascota,
                      Dinero montoTotal) {
        if (identificacionApartamento == null) {
            throw new ReglaDominioException("La cotización debe indicar el apartamento");
        }
        if (estancia == null) {
            throw new ReglaDominioException("La cotización debe tener una estancia definida");
        }
        if (ocupantesFacturables <= 0) {
            throw new ReglaDominioException("Debe haber al menos un ocupante facturable para cotizar");
        }
        if (montoTotal == null) {
            throw new ReglaDominioException("La cotización debe reflejar un monto total calculado");
        }

        this.identificacionApartamento = identificacionApartamento;
        this.estancia = estancia;
        this.ocupantesFacturables = ocupantesFacturables;
        this.cantidadMascota = cantidadMascota;
        this.montoTotal = montoTotal;
    }

    // ----- CONSULTAS (Inmutabilidad garantizada sin setters) -----

    public IdentificacionApartamento getIdentificacionApartamento() {
        return identificacionApartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public int getOcupantesFacturables() {
        return ocupantesFacturables;
    }

    public CantidadMascota getCantidadMascota() {
        return cantidadMascota;
    }

    public Dinero getMontoTotal() {
        return montoTotal;
    }
}