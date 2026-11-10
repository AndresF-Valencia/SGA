package co.edu.uniquindio.sga.application.dto.request;

import java.util.List;

public record ChatRequest(List<ChatMessageDto> messages) {
}
