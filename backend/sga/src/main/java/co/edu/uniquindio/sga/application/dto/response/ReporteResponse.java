package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta del reporte de gestión del alojamiento en un periodo.
 * Mapea a la respuesta de GET /api/reportes.
 *
 * Nota de diseño: el periodo incluye ambas fechas. La ocupación cuenta las noches de las
 * reservas CONFIRMADA, EN_CURSO y FINALIZADA sobre los apartamentos activos; los ingresos
 * son los pagos registrados en los folios dentro del periodo. Los montos van en pesos (Long).
 */
public record ReporteResponse(
        LocalDate desde,
        LocalDate hasta,
        int nochesDisponibles,
        int nochesOcupadas,
        int porcentajeOcupacion,
        Long ingresos,
        Long valorReservado,
        int totalReservas,
        List<ConteoDto> reservasPorEstado,
        List<ConteoDto> reservasPorCanal,
        List<ApartamentoDto> apartamentos
) {
    /** Cantidad de reservas de un estado o de un canal. */
    public record ConteoDto(
            String nombre,
            int cantidad
    ) {}

    /** Ocupación de un apartamento en el periodo. */
    public record ApartamentoDto(
            String identificacion,
            String nombre,
            int nochesOcupadas,
            int porcentajeOcupacion
    ) {}
}
