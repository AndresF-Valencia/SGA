package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifa")
@Getter
@Setter
@NoArgsConstructor
public class TarifaEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "apartamento_id", nullable = false, length = 40)
    private String apartamentoId;

    @Column(name = "temporada_id", nullable = false, length = 40)
    private String temporadaId;

    @Column(name = "valor_por_ocupante_noche", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPorOcupanteNoche;
}
