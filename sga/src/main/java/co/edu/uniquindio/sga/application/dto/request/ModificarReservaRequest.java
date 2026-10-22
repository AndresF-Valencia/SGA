package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para modificar una reserva existente (CU-05).
 * Mapea al endpoint: PUT /api/reservas/{codigo}.
 * 
 * Nota de diseño: Permite actualizar datos clave de la reserva como fechas,
 * apartamento, titular, lista de ocupantes y cantidad de mascotas.
 */
public record ModificarReservaRequest(

        @NotBlank(message = "El identificador del apartamento es obligatorio")
        String apartamentoId,

        @NotNull(message = "La nueva fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser en el futuro")
        LocalDate fechaEntrada,

        @NotNull(message = "La nueva fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "Los datos del titular son obligatorios")
        @Valid
        TitularModificacionDto titular,

        @NotEmpty(message = "La reserva debe incluir al menos un ocupante")
        @Valid
        List<OcupanteModificacionDto> ocupantes,

        int cantidadMascotas

) {
    public record TitularModificacionDto(
            @NotBlank(message = "El nombre del titular es obligatorio")
            String nombre,

            @NotBlank(message = "El documento del titular es obligatorio")
            String documento,

            @NotBlank(message = "El correo del titular es obligatorio")
            String email,

            @NotBlank(message = "El teléfono del titular es obligatorio")
            String telefono
    ) {}

    public record OcupanteModificacionDto(
            @NotBlank(message = "El nombre del ocupante es obligatorio")
            String nombre,

            @NotBlank(message = "El documento del ocupante es obligatorio")
            String documento
    ) {}
}