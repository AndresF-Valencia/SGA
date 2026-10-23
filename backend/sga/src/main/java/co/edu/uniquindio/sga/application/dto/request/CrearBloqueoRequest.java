package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO de entrada para crear un bloqueo de apartamento (CU-17).
 * Mapea al endpoint: POST /api/bloqueos.
 * 
 * Nota de diseño: Crea un bloqueo con apartamento, rango de fechas y motivo (RN-07).
 * El apartamento se referencia por su identificador único.
 */
public record CrearBloqueoRequest(

        @NotBlank(message = "La identificación del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotBlank(message = "El motivo del bloqueo es obligatorio")
        @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
        String motivo

) {}