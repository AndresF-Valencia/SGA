package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta que encapsula el resultado de la consulta de disponibilidad de apartamentos (CU-02).
 * Mapea al endpoint: GET /api/disponibilidad .
 * 
 * Nota de diseño: Retorna el rango evaluado y la lista de apartamentos libres 
 * junto con sus características clave y tarifas estimadas (en Long, pesos colombianos).
 */
public record DisponibilidadResponse(
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int totalDisponibles,
        List<ApartamentoDisponibleDto> apartamentosDisponibles,
        int pagina,
        int tamano,
        int totalPaginas
) {
    /** Respuesta sin paginar (canal externo): una sola página con todos los resultados. */
    public DisponibilidadResponse(LocalDate fechaEntrada, LocalDate fechaSalida, int totalDisponibles,
            List<ApartamentoDisponibleDto> apartamentosDisponibles) {
        this(fechaEntrada, fechaSalida, totalDisponibles, apartamentosDisponibles, 0,
                apartamentosDisponibles.size(), 1);
    }

    public record ApartamentoDisponibleDto(
            String identificacionApartamento,
            String nombre,
            int capacidadMaxima,
            List<String> caracteristicas,
            List<String> imagenes,
            Long tarifaEstimadaPorNoche
    ) {}
}