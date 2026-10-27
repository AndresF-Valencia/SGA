package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/login */
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {}
