package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Tabla de la bitácora de canales (7.8). */
@Entity
@Table(name = "evento_canal")
@Getter
@Setter
@NoArgsConstructor
public class EventoCanalEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "canal", nullable = false, length = 100)
    private String canal;

    @Column(name = "operacion", nullable = false, length = 40)
    private String operacion;

    @Column(name = "codigo_externo", length = 100)
    private String codigoExterno;

    @Column(name = "exitoso", nullable = false)
    private boolean exitoso;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;
}
