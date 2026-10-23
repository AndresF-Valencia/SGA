package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

@DisplayName("TiempoPreparacion (RN-20)")
class TiempoPreparacionTest {

    @Test
    @DisplayName("Respeta el valor configurado (antes siempre quedaba en 3)")
    void respetaElValor() {
        assertEquals(5, new TiempoPreparacion(5).tiempoPreparacion());
        assertEquals(3, TiempoPreparacion.estandar().tiempoPreparacion());
    }

    @Test
    @DisplayName("Rechaza valores negativos o de más de un día")
    void rechazaValoresInvalidos() {
        assertThrows(ReglaDominioException.class, () -> new TiempoPreparacion(-1));
        assertThrows(ReglaDominioException.class, () -> new TiempoPreparacion(25));
    }

    @Test
    @DisplayName("Indica si la preparación cabe entre la salida y la entrada")
    void alcanzaEntreSalidaYEntrada() {
        TiempoPreparacion tresHoras = TiempoPreparacion.estandar();

        assertTrue(tresHoras.alcanzaEntre(LocalTime.of(11, 0), LocalTime.of(15, 0)));
        assertTrue(tresHoras.alcanzaEntre(LocalTime.of(12, 0), LocalTime.of(15, 0)));
        assertFalse(tresHoras.alcanzaEntre(LocalTime.of(13, 0), LocalTime.of(15, 0)));
    }
}
