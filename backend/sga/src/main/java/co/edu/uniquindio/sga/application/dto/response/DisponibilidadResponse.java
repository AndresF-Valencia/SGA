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
        List<ApartamentoDisponibleDto> apartamentosDisponibles
) {
    public record ApartamentoDisponibleDto(
            String identificacionApartamento,
            String nombre,
            int capacidadMaxima,
            List<String> caracteristicas,
            List<String> imagenes,
            Long tarifaEstimadaPorNoche
    ) {}
}