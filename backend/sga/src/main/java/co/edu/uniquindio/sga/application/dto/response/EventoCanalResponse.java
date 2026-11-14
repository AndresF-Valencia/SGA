package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de un evento de la bitácora de canales (7.8).
 * Mapea a la respuesta de GET /api/eventos-canal.
 */
public record EventoCanalResponse(
        String id,
        String canal,
        String operacion,
        String codigoExterno,
        String resultado,
        String detalle,
        LocalDateTime fecha
) {}
