package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Periodo (desde y hasta, ambos incluidos) de un bloqueo o una temporada. */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PeriodoEmbeddable {

    @Column(name = "desde", nullable = false)
    private LocalDate desde;

    @Column(name = "hasta", nullable = false)
    private LocalDate hasta;
}
