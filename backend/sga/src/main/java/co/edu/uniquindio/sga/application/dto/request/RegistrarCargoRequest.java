package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para registrar un cargo en el folio de la reserva (CU-11).
 * Mapea al endpoint: POST /api/reservas/{codigo}/folio/cargos.
 */
public record RegistrarCargoRequest(

        @NotBlank(message = "El concepto del cargo es obligatorio")
        String concepto,

        @NotNull(message = "El monto del cargo es obligatorio")
        @Min(value = 1, message = "El monto del cargo debe ser mayor a cero")
        Long monto

) {}