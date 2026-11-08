package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import java.time.LocalDate;

/**
 * DTO de entrada para registrar una nueva temporada (CU-18).
 * Mapea al endpoint: POST /api/temporadas.
 * 
 * Nota de diseño: Permite crear una temporada sin solaparse con otras, 
 * definiendo su nombre, rango de fechas y la estancia mínima requerida.
 */
public record RegistrarTemporadaRequest(

        @NotBlank(message = "El nombre de la temporada es obligatorio")
        String nombre,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @Min(value = 1, message = "La estancia mínima debe ser al menos de 1 noche")
        int estanciaMinima,

        // BASE / MEDIA / ALTA (si no se envía queda BASE)
        TipoTemporada tipo

) {}