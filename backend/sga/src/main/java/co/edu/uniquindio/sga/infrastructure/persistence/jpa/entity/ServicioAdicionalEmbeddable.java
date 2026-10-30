package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ServicioAdicionalEmbeddable {

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "valor", nullable = false, precision = 14, scale = 0)
    private BigDecimal valor;
}
