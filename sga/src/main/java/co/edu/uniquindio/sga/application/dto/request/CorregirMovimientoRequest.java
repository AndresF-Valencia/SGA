package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para corregir un movimiento del folio mediante un movimiento inverso (CU-13).
 * Mapea al endpoint: POST /api/reservas/{codigo}/folio/correcciones.
 */
public record CorregirMovimientoRequest(

        @NotBlank(message = "El identificador del movimiento original es obligatorio")
        String movimientoIdOriginal,

        @NotBlank(message = "El motivo de la corrección es obligatorio")
        @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
        String motivo

) {}