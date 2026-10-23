package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que encapsula el detalle de una novedad o incidencia registrada en un apartamento (CU-15).
 * Mapea a las respuestas de los endpoints:
 * - POST /api/apartamentos/{id}/novedades (Registro de novedad)
 * - GET /api/apartamentos/{id}/novedades (Consulta de historial de novedades)
 * 
 * Nota de diseño: Permite llevar una trazabilidad clara de daños, mantenimientos correctivos,
 * preventivos o reportes operativos asociados a una unidad de alojamiento.
 */
public record NovedadResponse(

        String id,
        String identificacionApartamento,
        String gravedad,
        String descripcion,
        String estado,
        LocalDateTime fechaRegistro,
        String reportadoPor

) {}