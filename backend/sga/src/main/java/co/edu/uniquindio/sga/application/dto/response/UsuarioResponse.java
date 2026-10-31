package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Datos públicos de un usuario. Nunca incluye la contraseña. */
public record UsuarioResponse(
        String correo,
        String nombre,
        String telefono,
        String documento,
        LocalDate fechaNacimiento,
        String rol,
        boolean activo,
        String fotoPerfil
) {}
