package co.edu.uniquindio.sga.infrastructure.chatbot;

import co.edu.uniquindio.sga.application.exception.ChatUnavailableException;
import co.edu.uniquindio.sga.application.port.ChatModelPort;
import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiChatAdapter implements ChatModelPort {

    static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private final RestClient client;
    private final String apiKey;
    private final String model;

    public GeminiChatAdapter(RestClient.Builder builder,
                             @Value("${chatbot.api-key:}") String apiKey,
                             @Value("${chatbot.model:gemini-3.5-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = builder
                .baseUrl(BASE_URL)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    @Override
    public String complete(String systemPrompt, List<ChatMessage> conversation) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatUnavailableException("Falta configurar la variable de entorno GEMINI_API_KEY");
        }

        List<Map<String, Object>> contents = conversation.stream()
                .map(m -> Map.<String, Object>of(
                        "role", m.role() == ChatMessage.Role.ASSISTANT ? "model" : "user",
                        "parts", List.of(Map.of("text", m.content()))))
                .toList();

        // Los modelos "lite" no admiten thinkingConfig (Gemini responde 400).
        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("maxOutputTokens", 500);
        if (!model.contains("lite")) {
            generationConfig.put("thinkingConfig", Map.of("thinkingBudget", 0));
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents,
                "generationConfig", generationConfig);

        JsonNode response;
        try {
            response = client.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException | HttpMessageConversionException e) {
            // Incluye 429 (cuota gratuita agotada), 5xx, fallos de red y respuestas ilegibles.
            throw new ChatUnavailableException("El proveedor del modelo no esta disponible", e);
        }

        JsonNode textNode = response == null ? null
                : response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        String text = textNode == null || textNode.isMissingNode() || textNode.isNull()
                ? ""
                : textNode.asString();
        if (text == null || text.isBlank()) {
            throw new ChatUnavailableException("El modelo no devolvio una respuesta");
        }
        return text.trim();
    }
}
