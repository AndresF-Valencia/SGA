package co.edu.uniquindio.sga.application.exception;

public class ChatUnavailableException extends RuntimeException {

    public ChatUnavailableException(String message) {
        super(message);
    }

    public ChatUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
