package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotizacion")
@Getter
@Setter
@NoArgsConstructor
public class CotizacionEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "apartamento_id", nullable = false, length = 40)
    private String apartamentoId;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Column(name = "ocupantes_facturables", nullable = false)
    private int ocupantesFacturables;

    @Column(name = "cantidad_mascotas", nullable = false)
    private int cantidadMascotas;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    // Reutiliza el mismo embeddable de la reserva: el desglose por noche es igual
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cotizacion_cargo_noche", joinColumns = @JoinColumn(name = "cotizacion_id"))
    @OrderColumn(name = "orden")
    private List<CargoNocheEmbeddable> cargosPorNoche = new ArrayList<>();
}
