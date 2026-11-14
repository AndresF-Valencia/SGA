package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO de respuesta que encapsula el detalle completo de una reserva.
 * Mapea a las respuestas de los endpoints: 
 * - POST /api/reservas (Creación - CU-01)
 * - PUT /api/reservas/{codigo} (Modificación - CU-05)
 * - GET /api/reservas/{codigo} (Consulta)
 * 
 * Nota de diseño: Los montos monetarios se expresan como Long (pesos colombianos sin decimales)
 * para mantener la consistencia financiera estricta del dominio.
 */
public record ReservaResponse(

        String codigoReserva,
        String codigoExterno,
        String estadoReserva,
        String canalOrigen,
        String apartamentoId,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        LocalDateTime fechaCreacion,
        LocalTime horaEstimadaLlegada,
        RetrasoDto retraso,
        Long valorTotal,
        TitularResumenDto titular,
        List<OcupanteResumenDto> ocupantes,
        int cantidadMascotas

) {
    public record TitularResumenDto(
            String nombre,
            String documento,
            String email
    ) {}

    /** RNP-04: aviso de retraso del titular; nulo si no ha avisado. */
    public record RetrasoDto(
            LocalTime nuevaHoraEstimada,
            String motivo,
            LocalDateTime fechaNotificacion
    ) {}

    public record OcupanteResumenDto(
            String nombre,
            String documento,
            LocalDate fechaNacimiento
    ) {}
}