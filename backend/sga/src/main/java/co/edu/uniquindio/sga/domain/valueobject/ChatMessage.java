package co.edu.uniquindio.sga.domain.valueobject;

public record ChatMessage(Role role, String content) {

    public enum Role { USER, ASSISTANT }

    public ChatMessage {
        if (role == null) {
            throw new IllegalArgumentException("El rol del mensaje es obligatorio");
        }
        content = content == null ? "" : content;
    }
}
