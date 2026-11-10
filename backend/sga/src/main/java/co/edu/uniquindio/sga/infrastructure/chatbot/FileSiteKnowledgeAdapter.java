package co.edu.uniquindio.sga.infrastructure.chatbot;

import co.edu.uniquindio.sga.application.port.SiteKnowledgePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Component
public class FileSiteKnowledgeAdapter implements SiteKnowledgePort {

    private final String content;

    public FileSiteKnowledgeAdapter(
            @Value("${chatbot.knowledge-file:classpath:chatbot/knowledge.md}") Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            this.content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo de conocimiento del chatbot", e);
        }
    }

    @Override
    public String loadKnowledge() {
        return content;
    }
}
