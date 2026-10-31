package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tabla de usuarios. La llave es el correo, que es único y no se edita (7.2). */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity {

    @Id
    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /** Hash BCrypt, nunca la contraseña en texto plano. */
    @Column(name = "contrasena_cifrada", nullable = false, length = 100)
    private String contrasenaCifrada;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "documento", nullable = false, length = 20)
    private String documento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    /** Código de recuperación de contraseña, cifrado como la contraseña (7.2). */
    @Column(name = "codigo_recuperacion", length = 100)
    private String codigoRecuperacionCifrado;

    @Column(name = "codigo_recuperacion_expira")
    private LocalDateTime codigoRecuperacionExpira;

    @Column(name = "intentos_codigo_fallidos", nullable = false)
    private int intentosCodigoFallidos;
}
