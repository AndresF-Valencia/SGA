package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que encapsula el detalle de un bloqueo de apartamento (CU-17).
 * Mapea a las respuestas de los endpoints:
 * - POST /api/bloqueos (Creación de bloqueo)
 * - GET /api/bloqueos (Consulta general de bloqueos)
 * - GET /api/apartamentos/{id}/bloqueos (Consulta por unidad)
 * 
 * Nota de diseño: Permite bloquear unidades por mantenimiento preventivo, correctivo 
 * o razones administrativas para impedir su asignación en rangos de fechas específicos.
 */
public record BloqueoResponse(

        String id,
        String identificacionApartamento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String motivo,
        String registradoPor,
        LocalDateTime fechaCreacion

) {}