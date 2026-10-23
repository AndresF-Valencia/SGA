package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/**
 * DTO de respuesta que encapsula el detalle de una temporada tarifaria (CU-18).
 * Mapea a las respuestas de los endpoints:
 * - POST /api/temporadas (Creación de temporada)
 * - GET /api/temporadas (Listado de temporadas)
 * 
 * Nota de diseño: Permite definir periodos especiales del año para aplicar
 * factores de ajuste o tarifas diferenciadas en el motor de cotizaciones.
 */
public record TemporadaResponse(

        String id,
        String nombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoTemporada,
        int estanciaMinima

) {}