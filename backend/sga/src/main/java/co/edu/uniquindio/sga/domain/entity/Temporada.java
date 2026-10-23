package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;

import java.time.LocalDate;

/**
 * Raíz del agregado Temporada: periodo del calendario con tarifas y reglas
 * propias (7.4, RP-01).
 *
 * El periodo es un {@link Periodo} y no una {@link co.edu.uniquindio.sga.domain.valueobject.Estancia}:
 * una temporada del 1 al 31 de diciembre incluye la noche del 31. Con Estancia
 * esa última noche quedaba sin temporada y la cotización fallaba.
 */
public class Temporada {

    private final IdentificacionTemporada identificacion;
    private final Periodo periodo;
    private final String nombre;
    private final TipoTemporada tipo;
    private final EstanciaMinima estanciaMinima; // null = sin mínimo de noches

    public Temporada(IdentificacionTemporada identificacion, Periodo periodo, String nombre, TipoTemporada tipo,
            EstanciaMinima estanciaMinima) {
        if (identificacion == null) {
            throw new ReglaDominioException("La temporada debe tener una identificación");
        }
        if (periodo == null) {
            throw new ReglaDominioException("La temporada debe tener un periodo definido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener un nombre");
        }
        if (tipo == null) {
            throw new ReglaDominioException("La temporada debe indicar su tipo");
        }

        this.identificacion = identificacion;
        this.periodo = periodo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estanciaMinima = estanciaMinima;
    }

    /** Indica si la noche de esa fecha pertenece a esta temporada (ambos extremos incluidos). */
    public boolean cubre(LocalDate fecha) {
        return periodo.incluye(fecha);
    }

    /** Dos temporadas no deberían cubrir la misma noche; sirve para validarlo al crearlas. */
    public boolean seSolapaCon(Temporada otra) {
        return !this.periodo.hasta().isBefore(otra.periodo.desde())
                && !otra.periodo.hasta().isBefore(this.periodo.desde());
    }

    /**
     * RP-01: indica si una estancia de esa cantidad de noches cumple el mínimo
     * de la temporada. Si la temporada es base o no tiene mínimo, siempre cumple.
     * (Antes rechazaba cualquier estancia de una sola noche, aunque fuera en
     * temporada base.)
     */
    public boolean cumpleEstanciaMinima(int cantidadNoches) {
        if (cantidadNoches < 1) {
            throw new ReglaDominioException("Una estancia tiene al menos una noche");
        }
        if (!exigeEstanciaMinima()) {
            return true;
        }
        return cantidadNoches >= estanciaMinima.noches();
    }

    public boolean exigeEstanciaMinima() {
        return tipo == TipoTemporada.ALTA && estanciaMinima != null;
    }

    public boolean esAlta() {
        return tipo == TipoTemporada.ALTA;
    }

    // ----- CONSULTAS ----- //

    public IdentificacionTemporada getIdentificacion() {
        return identificacion;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoTemporada getTipo() {
        return tipo;
    }

    public EstanciaMinima getEstanciaMinima() {
        return estanciaMinima;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Temporada otra)) return false;
        return this.identificacion.equals(otra.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}
