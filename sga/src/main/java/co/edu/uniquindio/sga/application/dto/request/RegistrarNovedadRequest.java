package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar una novedad en un apartamento (CU-15).
 * Mapea al sub-recurso: POST /api/apartamentos/{identificacion}/novedades.
 */
public record RegistrarNovedadRequest(

        @NotBlank(message = "La descripción de la novedad es obligatoria")
        @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
        String descripcion,

        @NotBlank(message = "El tipo de novedad es obligatorio")
        String tipo

) {}