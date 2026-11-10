package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.InvalidChatRequestException;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage.Role;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AnswerQuestionServiceTest {

    private final AtomicReference<String> capturedSystem = new AtomicReference<>();
    private final AtomicReference<List<ChatMessage>> capturedConversation = new AtomicReference<>();

    private final AnswerQuestionService service = new AnswerQuestionService(
            (system, conversation) -> {
                capturedSystem.set(system);
                capturedConversation.set(conversation);
                return "respuesta";
            },
            () -> "INFO-DEL-SITIO");

    @Test
    void incluyeElConocimientoEnElSystemPrompt() {
        String reply = service.answer(List.of(new ChatMessage(Role.USER, "Hola")));

        assertEquals("respuesta", reply);
        assertTrue(capturedSystem.get().contains("INFO-DEL-SITIO"));
    }

    @Test
    void conservaSoloLosUltimosDiezMensajes() {
        List<ChatMessage> conversation = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            conversation.add(new ChatMessage(i % 2 == 0 ? Role.USER : Role.ASSISTANT, "m" + i));
        }
        conversation.add(new ChatMessage(Role.USER, "ultima"));

        service.answer(conversation);

        List<ChatMessage> sent = capturedConversation.get();
        assertTrue(sent.size() <= AnswerQuestionService.MAX_MESSAGES);
        assertEquals("ultima", sent.get(sent.size() - 1).content());
        assertEquals(Role.USER, sent.get(0).role());
    }

    @Test
    void truncaCadaMensaje() {
        service.answer(List.of(new ChatMessage(Role.USER, "x".repeat(2000))));

        assertEquals(AnswerQuestionService.MAX_CHARS, capturedConversation.get().get(0).content().length());
    }

    @Test
    void rechazaConversacionVaciaOUltimoMensajeDelAsistente() {
        assertThrows(InvalidChatRequestException.class, () -> service.answer(List.of()));
        assertThrows(InvalidChatRequestException.class,
                () -> service.answer(List.of(new ChatMessage(Role.ASSISTANT, "hola"))));
        assertThrows(InvalidChatRequestException.class,
                () -> service.answer(List.of(new ChatMessage(Role.USER, "   "))));
    }
}
