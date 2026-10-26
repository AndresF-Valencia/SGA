package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Último consecutivo usado por año, para generar RES-2026-00001, RES-2026-00002... */
@Entity
@Table(name = "contador_reserva")
@Getter
@Setter
@NoArgsConstructor
public class ContadorReservaEntity {

    @Id
    @Column(name = "anio")
    private int anio;

    @Column(name = "ultimo", nullable = false)
    private int ultimo;
}
