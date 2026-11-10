package co.edu.uniquindio.sga.infrastructure.chatbot;

import co.edu.uniquindio.sga.application.exception.ChatUnavailableException;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiChatAdapterTest {

    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private MockRestServiceServer server;
    private GeminiChatAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new GeminiChatAdapter(builder, "test-key", "gemini-2.5-flash");
    }

    @Test
    void mapeaRolesYExtraeElTexto() {
        server.expect(requestTo(URL))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text").value("sistema"))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(jsonPath("$.contents[1].role").value("model"))
                .andExpect(jsonPath("$.contents[2].parts[0].text").value("y los horarios?"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":" Atendemos de lunes a viernes. "}]}}]}
                        """, MediaType.APPLICATION_JSON));

        String reply = adapter.complete("sistema", List.of(
                new ChatMessage(Role.USER, "hola"),
                new ChatMessage(Role.ASSISTANT, "en que te ayudo?"),
                new ChatMessage(Role.USER, "y los horarios?")));

        assertEquals("Atendemos de lunes a viernes.", reply);
        server.verify();
    }

    @Test
    void traduceErrorDeCuotaAChatUnavailable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(ChatUnavailableException.class,
                () -> adapter.complete("sistema", List.of(new ChatMessage(Role.USER, "hola"))));
    }

    @Test
    void respuestaSinTextoLanzaChatUnavailable() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(ChatUnavailableException.class,
                () -> adapter.complete("sistema", List.of(new ChatMessage(Role.USER, "hola"))));
    }
}
