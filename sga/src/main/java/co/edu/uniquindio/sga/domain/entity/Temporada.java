package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import java.time.LocalDate;

/**
 * Raíz o entidad del agregado Temporada: periodo del calendario con tarifas y reglas propias (7.4, RNP-01).
 */
public class Temporada {

    private final IdentificacionTemporada identificacion;
    private final Estancia periodo;
    private final String nombre;
    private final TipoTemporada tipo;
    private final EstanciaMinima estanciaMinima; // Puede ser nula o tener un valor base si no aplica

    public Temporada(IdentificacionTemporada identificacion, Estancia periodo, String nombre, TipoTemporada tipo,
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

    /**
     * Verifica si una fecha específica cae dentro del periodo de esta temporada.
     */
    public boolean cubre(LocalDate fecha) {
        // Asumiendo que Estancia o Periodo permite verificar si incluye una fecha
        // (o si el periodo se maneja con las reglas de fecha de entrada/salida)
        return !fecha.isBefore(periodo.fechaEntrada()) && fecha.isBefore(periodo.fechaSalida());
    }

    /**
     * RNP-01: Verifica si la cantidad de noches cumple con la estancia mínima de la temporada.
     */
    public boolean cumpleEstanciaMinima(int cantidadNoches) {
        if (cantidadNoches < 2) {
            throw new ReglaDominioException("La cantidad de noches debe ser al menos 2");
        }

        // Si es temporada base o no tiene restricción de mínimo configurada, cumple por defecto
        if (tipo == TipoTemporada.BASE || estanciaMinima == null) {
            return true;
        }

        return cantidadNoches >= estanciaMinima.noches();
    }

    // ----- CONSULTAS (Inmutables) ----- //

    public IdentificacionTemporada getIdentificacion() {
        return identificacion;
    }

    public boolean esAlta(){
        if(tipo== TipoTemporada.ALTA){
            return true;
        }else{
            return false;
        }
    }

    public Estancia getPeriodo() {
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