package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para cerrar el folio de la reserva (CU-14).
 * Mapea al endpoint: PUT /api/reservas/{codigo}/folio/cerrar.
 * Con saldo distinto de cero exige la autorización del administrador en el cuerpo (RN-17)
 */
public record CerrarFolioRequest(

        @NotNull(message = "La autorización del administrador es obligatoria")
        Boolean autorizacionAdministrador,

        @Size(max = 300, message = "Las observaciones no pueden superar los 300 caracteres")
        String observaciones

) {}