package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO de entrada para publicar una nueva versión de la política de cancelación (CU-19).
 * Mapea al endpoint: PUT /api/politica-cancelacion/versiones.
 * 
 * Nota de diseño: Crea una nueva versión de la política y conserva las anteriores (F-07, RN-22).
 * El número de versión lo asigna el sistema: el cliente solo envía los tramos.
 */
public record PublicarPoliticaCancelacionRequest(

        @NotEmpty(message = "La política debe contener al menos un tramo de retención")
        @Valid
        List<TramoPoliticaDto> tramos

) {
    public record TramoPoliticaDto(
            @Min(value = 0, message = "Los días de anticipación no pueden ser negativos")
            int diasMinimo,

            @Min(value = 0, message = "Los días de anticipación no pueden ser negativos")
            int diasMaximo,

            @Min(value = 0, message = "El porcentaje de retención no puede ser negativo")
            int porcentajeRetencion
    ) {}
}