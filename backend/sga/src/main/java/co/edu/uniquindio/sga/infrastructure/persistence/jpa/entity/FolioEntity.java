package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tabla del folio. Su llave es el código de la reserva: hay un folio por reserva. */
@Entity
@Table(name = "folio")
@Getter
@Setter
@NoArgsConstructor
public class FolioEntity {

    @Id
    @Column(name = "reserva_codigo", length = 20)
    private String reservaCodigo;

    @Column(name = "cerrado", nullable = false)
    private boolean cerrado;

    // --- AutorizacionCierre (RN-17): las 3 quedan vacías si se cerró con saldo cero ---
    @Column(name = "autorizacion_autor", length = 100)
    private String autorizacionAutor;

    @Column(name = "autorizacion_motivo", length = 300)
    private String autorizacionMotivo;

    @Column(name = "autorizacion_fecha")
    private LocalDateTime autorizacionFecha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_cargo", joinColumns = @JoinColumn(name = "reserva_codigo"))
    @OrderColumn(name = "orden")
    private List<CargoFolioEmbeddable> cargos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_pago", joinColumns = @JoinColumn(name = "reserva_codigo"))
    @OrderColumn(name = "orden")
    private List<PagoEmbeddable> pagos = new ArrayList<>();
}
