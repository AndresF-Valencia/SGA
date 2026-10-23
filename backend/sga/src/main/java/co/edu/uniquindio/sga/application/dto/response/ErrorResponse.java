package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta estándar para encapsular errores de la API (tanto de negocio como técnicos o de validación).
 * 
 * Nota de diseño: Permite que los clientes (frontend o integraciones externas) interpreten 
 * de forma unificada el código de estado, el mensaje descriptivo y la ruta donde ocurrió la falla.
 */
public record ErrorResponse(

        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detallesValidacion

) {}