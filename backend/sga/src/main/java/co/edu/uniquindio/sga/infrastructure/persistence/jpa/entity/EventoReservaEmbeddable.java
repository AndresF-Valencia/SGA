package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

@Embeddable
public class EventoReservaEmbeddable {

    @Column(name = "momento", nullable = false)
    private LocalDateTime momento;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 15)
    private EstadoReserva estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 15)
    private EstadoReserva estadoNuevo;

    @Column(name = "observacion", length = 300)
    private String observacion;

    protected EventoReservaEmbeddable() {
    }

    public EventoReservaEmbeddable(LocalDateTime momento, String accion, String autor,
            EstadoReserva estadoAnterior, EstadoReserva estadoNuevo,
            String observacion) {
        this.momento = momento;
        this.accion = accion;
        this.autor = autor;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public String getAccion() {
        return accion;
    }

    public String getAutor() {
        return autor;
    }

    public EstadoReserva getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoReserva getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getObservacion() {
        return observacion;
    }
}
