package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para cancelar una reserva (CU-06).
 * Mapea a: CancelarReservaUseCase[cite: 2].
 *
 * Nota de diseño: No incluye montos de retención, devoluciones ni estado destino. 
 * La retención se calcula automáticamente utilizando la versión de la política 
 * congelada en la reserva (RN-13), y el estado terminal lo define estrictamente el dominio.
 */
public record CancelarReservaRequest(

        @NotBlank(message = "El motivo de la cancelación es obligatorio")
        @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
        String motivo

) {}