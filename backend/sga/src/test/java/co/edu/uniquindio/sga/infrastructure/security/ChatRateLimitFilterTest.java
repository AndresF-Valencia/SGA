package co.edu.uniquindio.sga.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ChatRateLimitFilterTest {

    @Test
    @DisplayName("Permite 10 peticiones por minuto y bloquea la 11 con 429")
    void limitaPorIp() throws Exception {
        ChatRateLimitFilter filtro = new ChatRateLimitFilter(10, false);

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse respuesta = ejecutar(filtro, "1.2.3.4", null);
            assertEquals(200, respuesta.getStatus());
        }

        MockHttpServletResponse bloqueada = ejecutar(filtro, "1.2.3.4", null);
        assertEquals(429, bloqueada.getStatus());
        assertTrue(bloqueada.getContentAsString().contains("Demasiadas preguntas"));
    }

    @Test
    @DisplayName("Con trust-forwarded-header usa la primera IP de X-Forwarded-For")
    void usaXForwardedFor() throws Exception {
        ChatRateLimitFilter filtro = new ChatRateLimitFilter(1, true);

        assertEquals(200, ejecutar(filtro, "10.0.0.1", "9.9.9.9, 10.0.0.2").getStatus());
        assertEquals(429, ejecutar(filtro, "10.0.0.1", "9.9.9.9").getStatus());
        assertEquals(200, ejecutar(filtro, "10.0.0.1", "8.8.8.8").getStatus());
    }

    private MockHttpServletResponse ejecutar(ChatRateLimitFilter filtro, String ip, String forwarded)
            throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/chat");
        peticion.setRemoteAddr(ip);
        if (forwarded != null) {
            peticion.addHeader("X-Forwarded-For", forwarded);
        }
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(peticion, respuesta, cadena);

        if (respuesta.getStatus() == 200) {
            assertNotNull(cadena.getRequest(), "La cadena debe ejecutarse cuando hay cupo");
        } else {
            assertNull(cadena.getRequest(), "La cadena no debe ejecutarse cuando se bloquea");
        }
        return respuesta;
    }
}
