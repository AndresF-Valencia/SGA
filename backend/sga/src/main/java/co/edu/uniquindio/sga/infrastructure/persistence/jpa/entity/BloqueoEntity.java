package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bloqueo")
@Getter
@Setter
@NoArgsConstructor
public class BloqueoEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "apartamento_id", nullable = false, length = 40)
    private String apartamentoId;

    @Embedded
    private PeriodoEmbeddable periodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoBloqueo motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoBloqueo estado;
}
