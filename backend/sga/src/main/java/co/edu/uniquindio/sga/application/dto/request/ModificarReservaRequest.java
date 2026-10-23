package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para modificar la estancia y detalles de una reserva existente (CU-05).
 * Mapea al endpoint: PUT /api/reservas/{codigo}.
 * 
 * Nota de diseño: Permite actualizar las fechas de la estancia, el apartamento asignado 
 * y la cantidad de mascotas para recalcular los recargos correspondientes (RNP-02).
 */
public record ModificarReservaRequest(

        @NotBlank(message = "El identificador del apartamento es obligatorio")
        String apartamentoId,

        @NotNull(message = "La nueva fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser en el futuro")
        LocalDate fechaEntrada,

        @NotNull(message = "La nueva fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @Min(value = 0, message = "La cantidad de mascotas no puede ser negativa")
        int cantidadMascotas

) {}