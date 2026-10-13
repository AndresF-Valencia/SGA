package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;

public class Temporada {

    private final IdentificacionTemporada identificacion;

    private Estancia periodo;

    private String nombre;

    private TipoTemporada tipo;

    private EstanciaMinima estanciaMinima;

    public Temporada(IdentificacionTemporada identificacion, Estancia periodo, String nombre, TipoTemporada tipo,
            EstanciaMinima estanciaMinima) {
        this.identificacion = identificacion;
        this.periodo = periodo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estanciaMinima = estanciaMinima;
    }

    /**
     * Verifica si la cantidad de días cumple con la estancia mínima de la temporada
     * 
     * @param cantidadDias cantidad de días a verificar
     * @return true si cumple con la estancia mínima, false en caso contrario
     */
    public boolean cumpleEstanciaMinima(int cantidadNoches) {
        if (cantidadNoches < 1) {

            throw new ReglaDominioException("La cantidad de días no puede ser negativa");
        }

        /**
         * regla de negocio propia RNP-01: Una estancia correspondiente a una Temporada
         * Alta
         * debe cumplir con el número mínimo de noches establecido por el alojamiento
         */

        if (tipo == TipoTemporada.BASE) {
            throw new ReglaDominioException("La temporada base no tiene estancia mínima");
        }

        return cantidadNoches >= estanciaMinima.noches();
    }

    // ----- CONSULTAS ----- //

    public IdentificacionTemporada getIdentificacion() {
        return identificacion;
    }

    public Estancia getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Estancia periodo) {
        this.periodo = periodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoTemporada getTipo() {
        return tipo;
    }

    public void setTipo(TipoTemporada tipo) {
        this.tipo = tipo;
    }

    public EstanciaMinima getEstanciaMinima() {
        return estanciaMinima;
    }

    public void setEstanciaMinima(EstanciaMinima estanciaMinima) {
        this.estanciaMinima = estanciaMinima;
    }
}