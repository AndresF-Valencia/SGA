package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Un tramo de la política: según los días de anticipación, cuánto se devuelve. */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TramoCancelacionEmbeddable {

    @Column(name = "dias_minimos", nullable = false)
    private int diasMinimosAnticipacion;

    @Column(name = "dias_maximos", nullable = false)
    private int diasMaximosAnticipacion;

    @Column(name = "porcentaje_devolucion", nullable = false)
    private int porcentajeDevolucion;
}
