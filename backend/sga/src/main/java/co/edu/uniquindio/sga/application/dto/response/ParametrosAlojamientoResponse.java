package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO de respuesta que encapsula la configuración y los parámetros generales del alojamiento (CU-20).
 * Mapea a las respuestas de los endpoints:
 * - GET /api/alojamiento/parametros (Consulta de parámetros de alojamiento)
 * - PUT /api/alojamiento/parametros (Actualización / reconfiguración de parámetros)
 * 
 * Nota de diseño: Refleja fielmente el estado del agregado Alojamiento y el request de configuración,
 * exponiendo de forma plana los valores primitivos de los objetos de valor (tiempo de preparación,
 * plazos y umbrales) para facilitar su consumo en la capa de presentación.
 */
public record ParametrosAlojamientoResponse(

        String identificacion,
        String nombre,
        String ciudad,
        String descripcion,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        String normasConvivencia,
        List<ServicioAdicionalResponse> serviciosAdicionales,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        LocalTime horaLimiteNoShow,
        int minutosPreparacion,
        int horasPlazoConfirmacion,
        int edadUmbralFacturable,
        boolean exigeAnticipo,
        int porcentajeAnticipo,
        int versionPoliticaVigente

) {}