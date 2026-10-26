package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Una versión publicada de la política de cancelación (RN-13, RN-22). Nunca se modifica. */
@Entity
@Table(name = "politica_cancelacion")
@Getter
@Setter
@NoArgsConstructor
public class PoliticaCancelacionEntity {

    @Id
    @Column(name = "version")
    private int version;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "politica_tramo", joinColumns = @JoinColumn(name = "version"))
    @OrderColumn(name = "orden")
    private List<TramoCancelacionEmbeddable> tramos = new ArrayList<>();
}
