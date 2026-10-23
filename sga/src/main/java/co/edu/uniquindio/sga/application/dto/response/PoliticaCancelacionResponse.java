package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta que encapsula el detalle de una política de cancelación y sus reglas de retención (CU-19).
 * Mapea a las respuestas de los endpoints:
 * - POST /api/politicas-cancelacion (Creación de política)
 * - GET /api/politicas-cancelacion (Listado y consulta de políticas)
 * 
 * Nota de diseño: Define los tramos o reglas de penalización según la anticipación con la que 
 * se cancela una reserva, permitiendo calcular retenciones porcentuales o cargos fijos.
 */
public record PoliticaCancelacionResponse(

        String id,
        String nombre,
        String descripcion,
        boolean activa,
        LocalDateTime fechaCreacion,
        List<TramoPoliticaDto> tramos

) {
    public record TramoPoliticaDto(
            int diasMinimosAnticipacion,
            int diasMaximosAnticipacion,
            Double porcentajeRetencion
    ) {}
}