package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * DTO de entrada para configurar y reemplazar los parámetros generales del alojamiento (CU-20).
 * Mapea al endpoint: PUT /api/alojamiento/parametros.
 * 
 * Nota de diseño: Reemplaza todos los parámetros de configuración global del único establecimiento (F-13, sección 6).
 */
public record ConfigurarParametrosAlojamientoRequest(

        @NotNull(message = "La hora de check-in es obligatoria")
        LocalTime horaEntrada,

        @NotNull(message = "La hora de check-out es obligatoria")
        LocalTime horaSalida,

        LocalTime horaLimiteNoShow,

        @NotNull(message = "El tiempo de preparación es obligatorio")
        Integer minutosPreparacion,

        @NotNull(message = "El plazo de confirmación es obligatorio")
        Integer horasPlazoConfirmacion,

        @NotNull(message = "El umbral de edad es obligatorio")
        Integer edadUmbralFacturable,

        boolean exigeAnticipo,

        @Min(value = 0, message = "El porcentaje de anticipo no puede ser negativo")
        @Max(value = 100, message = "El porcentaje de anticipo no puede superar el 100%")
        int porcentajeAnticipo

) {}