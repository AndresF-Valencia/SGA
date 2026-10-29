package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para recibir una reserva externa desde un Channel Manager (CU-03).
 * Mapea al endpoint: POST /api/reservas/externas.
 * 
 * Contiene el código externo necesario para cumplir con la regla de idempotencia (RN-19).
 */
public record RecibirReservaExternaRequest(

        @NotBlank(message = "El código externo de la reserva es obligatorio para la idempotencia")
        String codigoExterno,

        @NotBlank(message = "El canal de origen es obligatorio")
        String canalOrigen,

        @NotNull(message = "La hora estimada de llegada es obligatoria")
        LocalTime horaEstimadaLlegada,
        
        @NotBlank(message = "La identificación del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser en el futuro")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "Los datos del titular son obligatorios")
        @Valid
        TitularExternoDto titular,

        @NotEmpty(message = "La reserva debe incluir al menos un ocupante")
        @Valid
        List<OcupanteExternoDto> ocupantes,

        int cantidadMascotas

) {
    public record TitularExternoDto(
            @NotBlank(message = "El nombre del titular es obligatorio")
            String nombre,

            @NotBlank(message = "El documento del titular es obligatorio")
            String documento,

            @NotBlank(message = "El correo del titular es obligatorio")
            String email,

            @NotBlank(message = "El teléfono del titular es obligatorio")
            String telefono,

            @NotNull(message = "La fecha de nacimiento del titular es obligatoria")
            LocalDate fechaNacimiento
    ) {}

    public record OcupanteExternoDto(
            @NotBlank(message = "El nombre del ocupante es obligatorio")
            String nombre,

            @NotBlank(message = "El documento del ocupante es obligatorio")
            String documento,

            @NotNull(message = "La fecha de nacimiento del ocupante es obligatoria")
            LocalDate fechaNacimiento
    ) {}
}