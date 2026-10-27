package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.dto.response.ErrorResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones a respuestas HTTP con significado de negocio (12.4):
 * 400 datos mal enviados, 401 sin sesión válida, 403 recurso ajeno,
 * 404 no existe, 409 regla de negocio violada.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 404: el recurso no existe ----------

    @ExceptionHandler({
            ReservaNoEncontradaException.class,
            ApartamentoNoEncontradoException.class,
            FolioNoEncontradoException.class,
            AlojamientoNoEncontradoException.class,
            PoliticaCancelacionNoEncontradaException.class,
            UsuarioNoEncontradoException.class
    })
    ResponseEntity<ErrorResponse> noEncontrado(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    // ---------- 409: una regla del negocio no se cumple ----------

    @ExceptionHandler(ReglaDominioException.class)
    ResponseEntity<ErrorResponse> reglaDominio(ReglaDominioException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    // ---------- 400: datos mal enviados ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> argumentoNoValido(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> detalles = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", request, detalles);
    }

    /** Un valor que no existe en un enum (canal, medio de pago, motivo, rol...) */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> argumentoIlegal(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    // ---------- 401 y 403: seguridad ----------

    @ExceptionHandler(CredencialesInvalidasException.class)
    ResponseEntity<ErrorResponse> credencialesInvalidas(
            CredencialesInvalidasException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), request, List.of());
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status, String message, HttpServletRequest request, List<String> detalles) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                detalles);
        return ResponseEntity.status(status).body(response);
    }
}
