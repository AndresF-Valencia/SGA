package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("HoraEstimadaLlegada, NotificacionRetraso y PlazoConfirmacion")
class LlegadaYPlazosTest {

    // ---------- HoraEstimadaLlegada ----------

    @Test
    @DisplayName("Se puede crear una hora estimada de llegada válida")
    void crearHoraEstimada() {
        HoraEstimadaLlegada hel = HoraEstimadaLlegada.de(LocalTime.of(15, 30));
        assertEquals(LocalTime.of(15, 30), hel.horaEstimada());
    }

    @Test
    @DisplayName("La hora estimada de llegada no puede ser nula")
    void horaEstimadaNula() {
        assertThrows(ReglaDominioException.class, () -> HoraEstimadaLlegada.de(null));
    }

    // ---------- NotificacionRetraso ----------

    @Test
    @DisplayName("La notificación de retraso exige momento, nueva hora y motivo")
    void validaNotificacion() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 1, 15, 0);
        LocalTime hora = LocalTime.of(20, 0);

        assertThrows(ReglaDominioException.class, () -> new NotificacionRetraso(null, hora, "Vuelo"));
        assertThrows(ReglaDominioException.class, () -> new NotificacionRetraso(ahora, null, "Vuelo"));
        assertThrows(ReglaDominioException.class, () -> new NotificacionRetraso(ahora, hora, " "));
    }

    @Test
    @DisplayName("registrar() crea la notificación con los datos dados")
    void registrarNotificacion() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 1, 15, 0);

        NotificacionRetraso n = NotificacionRetraso.registrar(LocalTime.of(20, 0), "Vuelo retrasado", ahora);

        assertEquals(ahora, n.fechaHoraNotificacion());
        assertEquals(LocalTime.of(20, 0), n.nuevaHoraEstimada());
        assertEquals("Vuelo retrasado", n.motivo());
    }

    // ---------- PlazoConfirmacion ----------

    @Test
    @DisplayName("RN-21: el plazo es de al menos una hora")
    void plazoMinimo() {
        assertThrows(ReglaDominioException.class, () -> new PlazoConfirmacion(0));
        assertEquals(1, PlazoConfirmacion.unaHora().horas());
    }

    @Test
    @DisplayName("RN-21: vence solo cuando se supera el plazo")
    void venceAlSuperarElPlazo() {
        PlazoConfirmacion plazo = PlazoConfirmacion.unaHora();
        LocalDateTime creada = LocalDateTime.of(2026, 10, 12, 10, 0);

        assertFalse(plazo.venceAntesDe(creada, creada.plusMinutes(30)));
        assertFalse(plazo.venceAntesDe(creada, creada.plusHours(1)), "justo en el límite aún no vence");
        assertTrue(plazo.venceAntesDe(creada, creada.plusHours(1).plusMinutes(1)));
    }
}