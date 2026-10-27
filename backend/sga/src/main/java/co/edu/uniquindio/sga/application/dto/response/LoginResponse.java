package co.edu.uniquindio.sga.application.dto.response;

import java.time.Instant;

/**
 * Respuesta del inicio de sesión. El cliente guarda el token y lo manda en
 * cada petición: Authorization: Bearer <token>
 */
public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        UsuarioResponse usuario
) {}
