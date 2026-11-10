package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ChatMessageDto;
import co.edu.uniquindio.sga.application.dto.request.ChatRequest;
import co.edu.uniquindio.sga.application.dto.response.ChatResponse;
import co.edu.uniquindio.sga.application.exception.InvalidChatRequestException;
import co.edu.uniquindio.sga.application.port.AnswerQuestionUseCase;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AnswerQuestionUseCase useCase;

    public ChatController(AnswerQuestionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            throw new InvalidChatRequestException("Debes enviar al menos un mensaje");
        }
        List<ChatMessage> conversation = request.messages().stream().map(this::toDomain).toList();
        return new ChatResponse(useCase.answer(conversation));
    }

    private ChatMessage toDomain(ChatMessageDto dto) {
        ChatMessage.Role role = "assistant".equalsIgnoreCase(dto.role())
                ? ChatMessage.Role.ASSISTANT
                : ChatMessage.Role.USER;
        return new ChatMessage(role, dto.content());
    }
}
