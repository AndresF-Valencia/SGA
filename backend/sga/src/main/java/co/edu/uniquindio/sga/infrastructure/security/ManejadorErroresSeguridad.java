package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Responde 401 y 403 con el mismo JSON que el resto de errores de la API
 * (ErrorResponse), en vez de la página de error por defecto de Spring.
 * Estos errores pasan ANTES de llegar al controller, por eso no los atrapa
 * el GlobalExceptionHandler.
 */
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    /** 401: no mandó token, o el token es inválido o ya venció. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excepcion) throws IOException {
        escribir(response, request, 401, "Unauthorized",
                "Debes iniciar sesión: envía el encabezado Authorization: Bearer <token>");
    }

    /** 403: inició sesión, pero su rol no tiene permiso para esta operación. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excepcion) throws IOException {
        escribir(response, request, 403, "Forbidden", "Tu rol no tiene permiso para esta operación");
    }

    private static void escribir(HttpServletResponse response, HttpServletRequest request,
                                 int estado, String error, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + estado + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + json(mensaje) + "\","
                + "\"path\":\"" + json(request.getRequestURI()) + "\","
                + "\"detallesValidacion\":[]}");
    }

    private static String json(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
