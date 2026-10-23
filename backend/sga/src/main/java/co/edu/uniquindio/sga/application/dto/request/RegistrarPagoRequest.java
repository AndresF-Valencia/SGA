package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para registrar un pago en el folio de la reserva (CU-10).
 * Mapea al endpoint: POST /api/reservas/{codigo}/folio/pagos.
 * 
 * Nota de diseño: El monto se maneja como un entero largo (Long) representando 
 * los pesos colombianos sin decimales, cumpliendo con la regla monetaria del dominio.
 */
public record RegistrarPagoRequest(

        @NotNull(message = "El monto del pago es obligatorio")
        @Min(value = 1, message = "El monto del pago debe ser mayor a cero")
        Long monto,

        @NotBlank(message = "El medio de pago es obligatorio")
        String medioPago,

        @NotNull(message = "La fecha del pago es obligatoria")
        LocalDate fechaPago

) {}