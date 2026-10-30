package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Servicio adicional del alojamiento con su valor (7.3). */
public record ServicioAdicionalRequest(

        @NotBlank(message = "El nombre del servicio adicional es obligatorio")
        String nombre,

        @NotNull(message = "El valor del servicio adicional es obligatorio")
        @PositiveOrZero(message = "El valor del servicio adicional no puede ser negativo")
        BigDecimal valor

) {}
