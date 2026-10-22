package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para resolver un conflicto de canal (CU-21).
 * Mapea al endpoint: PUT /api/conflictos-canal/{id}/resolver.
 * 
 * Nota de diseño: Operación de negocio sobre un conflicto registrado; nunca sobrescribe la reserva vigente (RN-18).
 */
public record ResolverConflictoCanalRequest(

        @NotBlank(message = "La resolución o motivo es obligatorio")
        @Size(min = 5, max = 300, message = "La resolución debe tener entre 5 y 300 caracteres")
        String resolucion

) {}