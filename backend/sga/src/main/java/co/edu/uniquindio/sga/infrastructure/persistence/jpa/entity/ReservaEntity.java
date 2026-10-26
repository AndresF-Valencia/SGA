package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservaEntity {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "apartamento_id", nullable = false, length = 20)
    private String apartamentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoReserva estado;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 15)
    private CanalOrigen canalOrigen;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "documento_titular", nullable = false, length = 20)
    private String documentoTitular;

    @Column(name = "version_politica", nullable = false)
    private int versionPolitica;

    @Column(name = "hora_estimada_llegada")
    private LocalTime horaEstimadaLlegada;

    @Column(name = "cantidad_mascotas", nullable = false)
    private int cantidadMascotas;

    // --- NotificacionRetraso (RNP-04): las 3 quedan vacías si no hubo aviso ---
    @Column(name = "fecha_notificacion_retraso")
    private LocalDateTime fechaNotificacionRetraso;

    @Column(name = "nueva_hora_estimada")
    private LocalTime nuevaHoraEstimada;

    @Column(name = "motivo_retraso", length = 300)
    private String motivoRetraso;

    // --- Listas: cada una se guarda en su propia tabla ---

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_ocupante", joinColumns = @JoinColumn(name = "reserva_codigo"))
    @OrderColumn(name = "orden")
    private List<OcupanteEmbeddable> ocupantes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_cargo_noche", joinColumns = @JoinColumn(name = "reserva_codigo"))
    @OrderColumn(name = "orden")
    private List<CargoNocheEmbeddable> cargosPorNoche = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_historial", joinColumns = @JoinColumn(name = "reserva_codigo"))
    @OrderColumn(name = "orden")
    private List<EventoReservaEmbeddable> historial = new ArrayList<>();
}