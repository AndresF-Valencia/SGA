package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.dto.response.ChatResponse;
import co.edu.uniquindio.sga.application.exception.ChatUnavailableException;
import co.edu.uniquindio.sga.application.exception.InvalidChatRequestException;
import co.edu.uniquindio.sga.infrastructure.rest.controller.ChatController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Limitado a ChatController para no interferir con el manejo global de errores del SGA. */
@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatExceptionHandler.class);

    @ExceptionHandler(ChatUnavailableException.class)
    ResponseEntity<ChatResponse> unavailable(ChatUnavailableException e) {
        log.warn("Chatbot no disponible: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ChatResponse(
                "Ahora mismo no puedo responder, intenta de nuevo en unos minutos."));
    }

    @ExceptionHandler({InvalidChatRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ChatResponse> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(new ChatResponse("La pregunta no es valida."));
    }
}
