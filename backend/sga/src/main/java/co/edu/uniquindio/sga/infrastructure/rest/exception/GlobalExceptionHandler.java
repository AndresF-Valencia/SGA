package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.dto.response.ErrorResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.BloqueoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.CodigoRecuperacionInvalidoException;
import co.edu.uniquindio.sga.application.exception.ConflictoCanalNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.CredencialCanalInvalidaException;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.MovimientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a respuestas HTTP con significado de negocio (12.4):
 * 400 datos mal enviados, 401 sin sesión válida, 403 recurso ajeno,
 * 404 no existe, 405 método no permitido, 409 regla de negocio o integridad, 500 error interno.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 404: el recurso o ruta no existe ----------

    @ExceptionHandler({
            ReservaNoEncontradaException.class,
            ApartamentoNoEncontradoException.class,
            FolioNoEncontradoException.class,
            AlojamientoNoEncontradoException.class,
            PoliticaCancelacionNoEncontradaException.class,
            UsuarioNoEncontradoException.class,
            ConflictoCanalNoEncontradoException.class,
            BloqueoNoEncontradoException.class,
            MovimientoNoEncontradoException.class
    })
    ResponseEntity<ErrorResponse> noEncontrado(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    /** 404 para rutas o endpoints que no existen en la API */
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "La ruta o recurso solicitado no existe.", request, List.of());
    }

    // ---------- 409: una regla del negocio no se cumple o conflicto de integridad ----------

    @ExceptionHandler(ReglaDominioException.class)
    ResponseEntity<ErrorResponse> reglaDominio(ReglaDominioException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    /** 409 para violaciones de integridad en base de datos (ej. duplicados en campos únicos o llaves foráneas) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflictoIntegridad(DataIntegrityViolationException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "La operación viola restricciones de integridad en los datos.", request, List.of());
    }

    // ---------- 400: datos mal enviados, sintaxis JSON o tipos incorrectos ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> argumentoNoValido(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> detalles = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", request, detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErrorResponse> errorDeSintaxisOFormato(Exception exception, HttpServletRequest request) {
        String mensaje = "El cuerpo de la solicitud JSON está mal formado o hay un parámetro con tipo incorrecto.";
        if (exception instanceof MethodArgumentTypeMismatchException ex) {
            mensaje = "El parámetro '" + ex.getName() + "' tiene un valor con formato inválido.";
        }
        return error(HttpStatus.BAD_REQUEST, mensaje, request, List.of());
    }

    @ExceptionHandler(CodigoRecuperacionInvalidoException.class)
    ResponseEntity<ErrorResponse> codigoInvalido(CodigoRecuperacionInvalidoException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> argumentoIlegal(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    // ---------- 401 y 403: seguridad ----------

    @ExceptionHandler({CredencialesInvalidasException.class, CredencialCanalInvalidaException.class})
    ResponseEntity<ErrorResponse> credencialesInvalidas(
            RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), request, List.of());
    }

    // ---------- 405: Método HTTP no permitido ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> metodoNoSoportado(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        String mensaje = "El método HTTP " + exception.getMethod() + " no está soportado para este endpoint.";
        return error(HttpStatus.METHOD_NOT_ALLOWED, mensaje, request, List.of());
    }

    // ---------- 500: Error genérico del servidor ----------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> errorGenerico(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno inesperado en el servidor.", request, List.of());
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