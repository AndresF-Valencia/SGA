package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDateTime;

/**
 * Raíz del agregado Cotización: el valor de una estancia antes de reservar (7.4).
 *
 * Guarda el desglose noche por noche ({@link ValorCongelado}) que calcula el
 * {@code CotizadorEstanciaService} (RN-05), para que la reserva pueda nacer
 * con ese mismo valor. Es inmutable: si cambian los datos, se cotiza de nuevo.
 */
public class Cotizacion {

    private final IdentificacionCotizacion identificacion;
    private final IdentificacionApartamento identificacionApartamento;
    private final Estancia estancia;
    private final int ocupantesFacturables;
    private final CantidadMascota cantidadMascota;
    private final ValorCongelado valor;
    private final LocalDateTime creadaEn;

    public Cotizacion(IdentificacionCotizacion identificacion,
                      IdentificacionApartamento identificacionApartamento,
                      Estancia estancia,
                      int ocupantesFacturables,
                      CantidadMascota cantidadMascota,
                      ValorCongelado valor,
                      LocalDateTime creadaEn) {
        if (identificacion == null) {
            throw new ReglaDominioException("La cotización debe tener una identificación");
        }
        if (identificacionApartamento == null) {
            throw new ReglaDominioException("La cotización debe indicar el apartamento");
        }
        if (estancia == null) {
            throw new ReglaDominioException("La cotización debe tener una estancia definida");
        }
        if (ocupantesFacturables <= 0) {
            throw new ReglaDominioException("Debe haber al menos un ocupante facturable para cotizar");
        }
        if (valor == null) {
            throw new ReglaDominioException("La cotización debe tener su valor calculado");
        }
        if (valor.noches() != estancia.noches()) {
            throw new ReglaDominioException("El desglose del valor no corresponde a las noches de la estancia");
        }
        if (creadaEn == null) {
            throw new ReglaDominioException("La cotización debe indicar cuándo se hizo");
        }

        this.identificacion = identificacion;
        this.identificacionApartamento = identificacionApartamento;
        this.estancia = estancia;
        this.ocupantesFacturables = ocupantesFacturables;
        this.cantidadMascota = cantidadMascota == null ? new CantidadMascota(0) : cantidadMascota;
        this.valor = valor;
        this.creadaEn = creadaEn;
    }

    // ----- CONSULTAS (sin setters: la cotización es inmutable) -----

    public IdentificacionCotizacion getIdentificacion() {
        return identificacion;
    }

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

    public ValorCongelado getValor() {
        return valor;
    }

    /** Total de la cotización: la suma del desglose noche por noche. */
    public Dinero getMontoTotal() {
        return valor.total();
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cotizacion otra)) return false;
        return this.identificacion.equals(otra.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}
