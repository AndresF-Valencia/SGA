package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de entrada para crear una nueva reserva directa o interno (CU-01).
 * Mapea al endpoint: POST /api/reservas.
 * 
 * Nota de diseño: Contiene toda la información inicial necesaria para instanciar 
 * una reserva, incluyendo apartamento, fechas, canal, titular, ocupantes y fecha de nacimiento.
 */
public record CrearReservaRequest(

        @NotBlank(message = "El identificador del apartamento es obligatorio")
        String apartamentoId,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser en el futuro")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotBlank(message = "El canal de origen es obligatorio")
        String canalOrigen,

        @NotNull(message = "Los datos del titular son obligatorios")
        @Valid
        TitularDto titular,

        @NotEmpty(message = "La reserva debe incluir al menos un ocupante")
        @Valid
        List<OcupanteDto> ocupantes,

        int cantidadMascotas

) {
    public record TitularDto(
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

    public record OcupanteDto(
            @NotBlank(message = "El nombre del ocupante es obligatorio")
            String nombre,

            @NotBlank(message = "El documento del ocupante es obligatorio")
            String documento,

            @NotNull(message = "La fecha de nacimiento del ocupante es obligatoria")
            LocalDate fechaNacimiento
    ) {}
}