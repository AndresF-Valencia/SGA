package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO de entrada para configurar y reemplazar los parámetros generales del alojamiento (CU-20).
 * Mapea al endpoint: PUT /api/alojamiento/parametros.
 * 
 * Nota de diseño: Reemplaza todos los parámetros de configuración global del único establecimiento (F-13, sección 6).
 */
public record ConfigurarParametrosAlojamientoRequest(

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
        BigDecimal latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
        BigDecimal longitud,

        @Size(max = 2000, message = "Las normas de convivencia no pueden superar los 2000 caracteres")
        String normasConvivencia,

        @Valid
        List<ServicioAdicionalRequest> serviciosAdicionales,

        @NotNull(message = "La hora de check-in es obligatoria")
        LocalTime horaEntrada,

        @NotNull(message = "La hora de check-out es obligatoria")
        LocalTime horaSalida,

        LocalTime horaLimiteNoShow,

        @NotNull(message = "El tiempo de preparación es obligatorio")
        Integer horasPreparacion,

        @NotNull(message = "El plazo de confirmación es obligatorio")
        Integer horasPlazoConfirmacion,

        @NotNull(message = "El umbral de edad es obligatorio")
        Integer edadUmbralFacturable,

        boolean exigeAnticipo,

        @Min(value = 0, message = "El porcentaje de anticipo no puede ser negativo")
        @Max(value = 100, message = "El porcentaje de anticipo no puede superar el 100%")
        int porcentajeAnticipo

) {}