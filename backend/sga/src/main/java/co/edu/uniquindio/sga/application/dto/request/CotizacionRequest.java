package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para generar una cotización de alojamiento.
 * Mapea al endpoint: POST /api/cotizaciones (o equivalente de cálculo).
 * 
 * Nota de diseño: Permite evaluar disponibilidad, tarifas aplicables por temporada,
 * y costos totales basándose en el apartamento, fechas y la edad de los ocupantes.
 */
public record CotizacionRequest(

        @NotBlank(message = "La identificación del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser en el futuro")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotEmpty(message = "Debe incluir al menos un ocupante para la cotización")
        @Valid
        List<OcupanteCotizacionDto> ocupantes

) {
    public record OcupanteCotizacionDto(
            @NotNull(message = "La fecha de nacimiento del ocupante es obligatoria")
            LocalDate fechaNacimiento
    ) {}
}