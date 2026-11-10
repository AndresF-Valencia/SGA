package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.domain.valueobject.ChatMessage;

import java.util.List;

public interface ChatModelPort {

    /**
     * @throws co.edu.uniquindio.sga.application.exception.ChatUnavailableException
     *         si el proveedor falla o se agota la cuota.
     */
    String complete(String systemPrompt, List<ChatMessage> conversation);
}
