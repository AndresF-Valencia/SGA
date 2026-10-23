package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para cerrar el folio de la reserva (CU-14).
 * Mapea al endpoint: PUT /api/reservas/{codigo}/folio/cerrar.
 * Con saldo distinto de cero exige la autorización del administrador en el cuerpo (RN-17)
 */
public record CerrarFolioRequest(

        @NotBlank String autor,
        @NotBlank @Size(min = 5, max = 300) String motivo,

        @Size(max = 300, message = "Las observaciones no pueden superar los 300 caracteres")
        String observaciones

) {}