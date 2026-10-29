package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temporada")
@Getter
@Setter
@NoArgsConstructor
public class TemporadaEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Embedded
    private PeriodoEmbeddable periodo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private TipoTemporada tipo;

    /** null = la temporada no exige estancia mínima. Por eso es Integer y no int. */
    @Column(name = "estancia_minima_noches")
    private Integer estanciaMinimaNoches;
}
