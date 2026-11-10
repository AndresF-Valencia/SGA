package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.exception.ChatUnavailableException;
import co.edu.uniquindio.sga.application.port.AnswerQuestionUseCase;
import co.edu.uniquindio.sga.infrastructure.rest.exception.ChatExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ChatExceptionHandler.class)
class ChatControllerTest {

    private static final String VALID = """
            {"messages":[{"role":"user","content":"Como me registro?"}]}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AnswerQuestionUseCase useCase;

    @Test
    void devuelve200ConLaRespuesta() throws Exception {
        when(useCase.answer(anyList())).thenReturn("Entra a la pagina de registro.");

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Entra a la pagina de registro."));
    }

    @Test
    void devuelve400ConPayloadInvalido() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"messages\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devuelve503CuandoElProveedorFalla() throws Exception {
        when(useCase.answer(anyList())).thenThrow(new ChatUnavailableException("cuota"));

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.reply").exists());
    }
}
