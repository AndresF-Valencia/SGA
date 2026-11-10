package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.port.AnswerQuestionUseCase;
import co.edu.uniquindio.sga.application.port.ChatModelPort;
import co.edu.uniquindio.sga.application.port.SiteKnowledgePort;
import co.edu.uniquindio.sga.application.usecase.AnswerQuestionService;
import co.edu.uniquindio.sga.infrastructure.security.ChatRateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ChatbotConfig {

    /**
     * Spring Boot 4 no expone RestClient.Builder con el starter webmvc; se crea aquí
     * para que GeminiChatAdapter lo reciba por constructor. Los tiempos máximos evitan
     * que una llamada colgada al proveedor bloquee el hilo del servidor.
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    AnswerQuestionUseCase answerQuestionUseCase(ChatModelPort model, SiteKnowledgePort knowledge) {
        return new AnswerQuestionService(model, knowledge);
    }

    @Bean
    FilterRegistrationBean<ChatRateLimitFilter> chatRateLimitFilter(
            @Value("${chatbot.rate-limit.per-minute:10}") int perMinute,
            @Value("${chatbot.rate-limit.trust-forwarded-header:false}") boolean trustForwardedHeader) {
        var registration = new FilterRegistrationBean<>(new ChatRateLimitFilter(perMinute, trustForwardedHeader));
        registration.addUrlPatterns("/api/chat");
        registration.setOrder(-150); // antes de la cadena de Spring Security (orden -100)
        return registration;
    }
}
