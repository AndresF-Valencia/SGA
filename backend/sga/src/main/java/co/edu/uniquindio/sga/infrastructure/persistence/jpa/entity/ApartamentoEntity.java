package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apartamento")
@Getter
@Setter
@NoArgsConstructor
public class ApartamentoEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "alojamiento_id", nullable = false, length = 40)
    private String alojamientoId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "dormitorios", nullable = false)
    private int dormitorios;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 30)
    private EstadoOperativo estadoOperativo;

    @Column(name = "limite_mascotas", nullable = false)
    private int limiteMascotas;

    @Column(name = "tiempo_preparacion_horas", nullable = false)
    private int tiempoPreparacionHoras;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    // Listas de texto: cada una en su tabla, una fila por elemento
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartamento_caracteristica", joinColumns = @JoinColumn(name = "apartamento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "caracteristica", nullable = false, length = 200)
    private List<String> caracteristicas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartamento_imagen", joinColumns = @JoinColumn(name = "apartamento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "url", nullable = false, length = 500)
    private List<String> imagenes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartamento_novedad", joinColumns = @JoinColumn(name = "apartamento_id"))
    @OrderColumn(name = "orden")
    private List<NovedadEmbeddable> novedades = new ArrayList<>();
}
