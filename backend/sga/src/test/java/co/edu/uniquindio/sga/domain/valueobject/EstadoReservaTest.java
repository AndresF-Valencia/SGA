package co.edu.uniquindio.sga.domain.valueobject;

import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.CANCELADA;
import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.CONFIRMADA;
import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.EN_CURSO;
import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.FINALIZADA;
import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.NO_SHOW;
import static co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.PENDIENTE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EstadoReserva")
class EstadoReservaTest {

    @Test
    @DisplayName("PENDIENTE, CONFIRMADA y EN_CURSO son activas y retienen disponibilidad")
    void estadosActivos() {
        for (EstadoReserva estado : List.of(PENDIENTE, CONFIRMADA, EN_CURSO)) {
            assertTrue(estado.esActiva(), estado + " debería ser activa");
            assertTrue(estado.retieneDisponibilidad(), estado + " debería retener noches");
            assertFalse(estado.esTerminal(), estado + " no debería ser terminal");
        }
    }

    @Test
    @DisplayName("RN-12: FINALIZADA, CANCELADA y NO_SHOW son terminales y liberan las noches")
    void estadosTerminales() {
        for (EstadoReserva estado : List.of(FINALIZADA, CANCELADA, NO_SHOW)) {
            assertTrue(estado.esTerminal(), estado + " debería ser terminal");
            assertFalse(estado.esActiva(), estado + " no debería ser activa");
            assertFalse(estado.retieneDisponibilidad(), estado + " no debería retener noches");
        }
    }

    @Test
    @DisplayName("RN-08: transiciones permitidas")
    void transicionesPermitidas() {
        assertTrue(PENDIENTE.puedeTransicionarA(CONFIRMADA));
        assertTrue(PENDIENTE.puedeTransicionarA(CANCELADA));
        assertTrue(CONFIRMADA.puedeTransicionarA(EN_CURSO));
        assertTrue(CONFIRMADA.puedeTransicionarA(CANCELADA));
        assertTrue(CONFIRMADA.puedeTransicionarA(NO_SHOW));
        assertTrue(EN_CURSO.puedeTransicionarA(FINALIZADA));
    }

    @Test
    @DisplayName("RN-08: transiciones no permitidas")
    void transicionesNoPermitidas() {
        assertFalse(PENDIENTE.puedeTransicionarA(EN_CURSO), "no se salta la confirmación");
        assertFalse(PENDIENTE.puedeTransicionarA(NO_SHOW), "no hay no-show sin confirmar");
        assertFalse(CONFIRMADA.puedeTransicionarA(PENDIENTE), "no se devuelve a pendiente");
        assertFalse(CONFIRMADA.puedeTransicionarA(FINALIZADA), "no se finaliza sin llegada");
        assertFalse(EN_CURSO.puedeTransicionarA(CANCELADA), "una estancia en curso no se cancela");
        assertFalse(PENDIENTE.puedeTransicionarA(PENDIENTE), "no hay transición a sí mismo");
    }

    @Test
    @DisplayName("Desde un estado terminal no se puede pasar a ningún otro")
    void terminalesNoTransicionan() {
        for (EstadoReserva terminal : List.of(FINALIZADA, CANCELADA, NO_SHOW)) {
            for (EstadoReserva siguiente : EstadoReserva.values()) {
                assertFalse(terminal.puedeTransicionarA(siguiente),
                        terminal + " no debería poder pasar a " + siguiente);
            }
        }
    }
}
