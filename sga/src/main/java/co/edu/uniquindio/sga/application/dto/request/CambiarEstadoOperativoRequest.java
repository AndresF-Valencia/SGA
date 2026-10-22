package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para cambiar el estado operativo de un apartamento (CU-16).
 * Mapea al endpoint: POST /api/apartamentos/{identificacion}/estado-operativo.
 */
public record CambiarEstadoOperativoRequest(

        @NotNull(message = "El nuevo estado operativo es obligatorio")
        EstadoOperativo nuevoEstado

) {}