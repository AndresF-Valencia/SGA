package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta que contiene el cálculo de la cotización de alojamiento.
 * Mapea al resultado de: POST /api/cotizaciones.
 * 
 * Nota de diseño: Los montos monetarios se expresan en Long (pesos colombianos sin decimales).
 */
public record CotizacionResponse(

        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int cantidadNoches,
        Long valorTotal,
        Long valorSubtotal,
        List<DetalleTarifaNocheDto> desgloseNoches

) {
    /** 7.4: desglose noche por noche: fecha, temporada, tarifa aplicada, ocupantes facturables y subtotal. */
    public record DetalleTarifaNocheDto(
            LocalDate fecha,
            Long tarifaAplicada,
            String nombreTemporada,
            int ocupantesFacturables,
            Long subtotal
    ) {}
}