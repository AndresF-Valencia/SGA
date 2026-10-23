package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar la salida (check-out) de una reserva (CU-09).
 * Mapea al endpoint: PUT /api/reservas/{codigo}/registrar-salida.
 * 
 * Nota de diseño: Si el folio presenta saldo pendiente, el dominio valida 
 * que se haya otorgado la autorización del administrador para permitir el cierre.
 */
public record RegistrarSalidaRequest(

        @NotBlank String autor,
        @NotBlank @Size(min = 5, max = 300) String motivo,
        @Size(max = 300, message = "Las observaciones no pueden superar los 300 caracteres")
        String observaciones

) {}