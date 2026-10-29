package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Una novedad reportada sobre un apartamento (7.6). */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NovedadEmbeddable {

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "gravedad", nullable = false, length = 30)
    private String gravedad;

    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
