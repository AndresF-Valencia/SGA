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
    @DisplayName("Sin hora indicada, la entrada es a las 14:00 y el no-show a las 16:00")
    void horaEstandar() {
        assertEquals(LocalTime.of(14, 0), HoraEstimadaLlegada.ESTANDAR.horaEntrada());
        assertEquals(LocalTime.of(16, 0), HoraEstimadaLlegada.ESTANDAR.horaLimiteNoShow());
    }

    @Test
    @DisplayName("Con hora estimada, el límite es esa hora más dos horas")
    void horaEstimadaMasTolerancia() {
        assertEquals(LocalTime.of(20, 30), HoraEstimadaLlegada.de(18, 30).horaLimiteNoShow());
        assertEquals(LocalTime.of(11, 0),
                HoraEstimadaLlegada.de(LocalTime.of(9, 0)).horaLimiteNoShow());
    }

    @Test
    @DisplayName("La tolerancia queda fija en dos horas")
    void toleranciaFija() {
        HoraEstimadaLlegada hora = new HoraEstimadaLlegada(null, LocalTime.of(15, 0), 5);

        assertEquals(2, hora.horasToleranciaNoShow());
        assertEquals(LocalTime.of(17, 0), hora.horaLimiteNoShow());
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
