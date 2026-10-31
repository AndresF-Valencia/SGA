package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tabla de conflictos de canal (7.8). */
@Entity
@Table(name = "conflicto_canal")
@Getter
@Setter
@NoArgsConstructor
public class ConflictoCanalEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "canal", nullable = false, length = 100)
    private String canal;

    @Column(name = "codigo_externo", nullable = false, length = 100)
    private String codigoExterno;

    @Column(name = "apartamento", length = 40)
    private String apartamento;

    @Column(name = "fecha_entrada")
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "titular_nombre", length = 150)
    private String titularNombre;

    @Column(name = "titular_documento", length = 20)
    private String titularDocumento;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "recibido_en", nullable = false)
    private LocalDateTime recibidoEn;

    @Column(name = "resuelto", nullable = false)
    private boolean resuelto;

    @Column(name = "resolucion", length = 300)
    private String resolucion;

    @Column(name = "resuelto_por", length = 150)
    private String resueltoPor;

    @Column(name = "resuelto_en")
    private LocalDateTime resueltoEn;
}
