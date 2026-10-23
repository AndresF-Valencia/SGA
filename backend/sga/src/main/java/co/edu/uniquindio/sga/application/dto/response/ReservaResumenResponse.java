package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/**
 * DTO de respuesta con el resumen de una reserva, para los listados (Guía 07, sección 5.5).
 * Mapea a los elementos de la respuesta de:
 * - GET /api/reservas (ConsultarReservasPorEstadoUseCase)
 *
 * Nota de diseño: en un listado no se envía el detalle completo (ocupantes, titular, folio);
 * para eso existe GET /api/reservas/{codigo} con ReservaResponse.
 */
public record ReservaResumenResponse(

        String codigoReserva,
        String apartamentoId,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String estadoReserva,
        String canalOrigen,
        String nombreTitular,
        Long valorTotal

) {}
