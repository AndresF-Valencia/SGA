package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta que encapsula el estado financiero y el detalle del folio de una reserva.
 * Mapea a las respuestas de los endpoints:
 * - GET /api/reservas/{codigo}/folio (Consulta - CU-13)
 * - POST /api/reservas/{codigo}/folio/cargos (Registro de cargos - CU-08)
 * - POST /api/reservas/{codigo}/folio/pagos (Registro de pagos - CU-10)
 * - PUT /api/reservas/{codigo}/folio/movimientos/{id}/corregir (Corrección - CU-11)
 * - POST /api/reservas/{codigo}/folio/cerrar (Cierre - CU-12)
 * 
 * Nota de diseño: Todos los montos monetarios se expresan como Long (pesos colombianos sin decimales)
 * para mantener la consistencia financiera estricta del dominio.
 */
public record FolioResponse(

        String codigoReserva,
        String estadoFolio,
        Long subtotalCargos,
        Long totalPagos,
        Long saldoPendiente,
        List<CargoResumenDto> cargos,
        List<PagoResumenDto> pagos

) {
    public record CargoResumenDto(
            String id,
            String descripcion,
            Long monto,
            LocalDateTime fechaRegistro,
            String registradoPor
    ) {}

    public record PagoResumenDto(
            String id,
            String medioPago,
            Long monto,
            LocalDateTime fechaPago,
            String referencia
    ) {}
}