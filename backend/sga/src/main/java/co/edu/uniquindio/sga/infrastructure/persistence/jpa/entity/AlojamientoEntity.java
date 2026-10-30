package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Tabla del alojamiento (F-13: hay uno solo) con sus parámetros configurables (sección 6). */
@Entity
@Table(name = "alojamiento")
@Getter
@Setter
@NoArgsConstructor
public class AlojamientoEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "normas_convivencia", length = 2000)
    private String normasConvivencia;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alojamiento_servicio_adicional", joinColumns = @JoinColumn(name = "alojamiento_id"))
    @OrderColumn(name = "orden")
    private List<ServicioAdicionalEmbeddable> serviciosAdicionales = new ArrayList<>();

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "hora_limite_no_show")
    private LocalTime horaLimiteNoShow;

    @Column(name = "umbral_edad_facturable", nullable = false)
    private int umbralEdadFacturable;

    @Column(name = "tiempo_preparacion_horas", nullable = false)
    private int tiempoPreparacionHoras;

    @Column(name = "plazo_confirmacion_horas", nullable = false)
    private int plazoConfirmacionHoras;

    @Column(name = "exige_anticipo", nullable = false)
    private boolean exigeAnticipo;

    @Column(name = "porcentaje_anticipo", nullable = false)
    private int porcentajeAnticipo;

    @Column(name = "version_politica_vigente", nullable = false)
    private int versionPoliticaVigente;
}
