package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO de entrada para crear un apartamento (7.3).
 * Mapea al endpoint: POST /api/apartamentos. Nace inactivo hasta tener tarifas completas.
 */
public record CrearApartamentoRequest(

        @NotBlank(message = "La identificación es obligatoria")
        String identificacion,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Min(value = 1, message = "Debe tener al menos un dormitorio")
        int dormitorios,

        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        int capacidadMaxima,

        @Min(value = 0, message = "El límite de mascotas no puede ser negativo")
        int limiteMascotas,

        @Min(value = 0, message = "El tiempo de preparación no puede ser negativo")
        @Max(value = 24, message = "El tiempo de preparación no puede superar las 24 horas")
        int tiempoPreparacionHoras,

        @NotNull(message = "Las características son obligatorias")
        List<String> caracteristicas,

        @NotNull(message = "Las imágenes son obligatorias")
        List<String> imagenes

) {}
