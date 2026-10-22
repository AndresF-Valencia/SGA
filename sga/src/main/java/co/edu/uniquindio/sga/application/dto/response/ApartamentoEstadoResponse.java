package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/**
 * DTO de respuesta que encapsula el estado operativo actual de los apartamentos del hotel (CU-14, CU-16).
 * Mapea al endpoint: GET /api/apartamentos/estado.
 * 
 * Nota de diseño: Proporciona una vista detallada del estado de cada unidad (disponible,
 * ocupada, en mantenimiento, limpieza, etc.) para la gestión de recepción y operaciones.
 */
public record ApartamentoEstadoResponse(

        int totalApartamentos,
        List<ApartamentoEstadoDetalleDto> apartamentos

) {
    public record ApartamentoEstadoDetalleDto(
            String identificacionApartamento,
            String nombre,
            String estadoOperativo,
            String codigoReservaActual,
            String observaciones
    ) {}
}