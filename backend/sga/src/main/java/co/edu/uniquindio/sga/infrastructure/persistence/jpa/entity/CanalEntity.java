package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "canal")
@Getter
@Setter
@NoArgsConstructor
public class CanalEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_canal", nullable = false, length = 15)
    private CanalOrigen tipoCanal;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "credencial_acceso", nullable = false, unique = true, length = 200)
    private String credencialAcceso;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
