package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;

import java.util.List;

public interface AnswerQuestionUseCase {

    /** Responde la ultima pregunta del usuario dentro de la conversacion recibida. */
    String answer(List<ChatMessage> conversation);
}
