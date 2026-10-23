package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para definir o reemplazar la tarifa de una combinación apartamento × temporada (CU-18).
 * Mapea al endpoint: PUT/POST /api/apartamentos/{identificacion}/tarifas/{temporadald}.
 */
public record RegistrarTarifaRequest(

        @NotNull(message = "El valor de la tarifa es obligatorio")
        @Min(value = 1, message = "El valor de la tarifa debe ser mayor a cero")
        Long valorTarifa

) {}