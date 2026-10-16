package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("Periodo, IdBloqueo y EstadoBloqueo")
class PeriodoTest {

    private static LocalDate dia(int dia) {
        return LocalDate.of(2026, 11, dia);
    }

    // ---------- Periodo ----------

    @Test
    @DisplayName("Exige fecha de inicio y de fin")
    void exigeFechas() {
        assertThrows(ReglaDominioException.class, () -> new Periodo(null, dia(5)));
        assertThrows(ReglaDominioException.class, () -> new Periodo(dia(5), null));
    }

    @Test
    @DisplayName("El fin no puede ser anterior al inicio")
    void finAnteriorAlInicio() {
        assertThrows(ReglaDominioException.class, () -> new Periodo(dia(10), dia(9)));
    }

    @Test
    @DisplayName("A diferencia de la estancia, admite un solo día")
    void unSoloDia() {
        assertDoesNotThrow(() -> new Periodo(dia(10), dia(10)));
        assertTrue(new Periodo(dia(10), dia(10)).incluye(dia(10)));
    }

    @Test
    @DisplayName("Incluye los dos extremos y los días intermedios")
    void incluyeExtremos() {
        Periodo periodo = new Periodo(dia(10), dia(15));

        assertTrue(periodo.incluye(dia(10)));
        assertTrue(periodo.incluye(dia(12)));
        assertTrue(periodo.incluye(dia(15)), "el último día sí cuenta");
        assertFalse(periodo.incluye(dia(9)));
        assertFalse(periodo.incluye(dia(16)));
    }

    @Test
    @DisplayName("Del 10 al 12: el periodo incluye el 12 y la estancia no")
    void diferenciaConEstancia() {
        Periodo periodo = new Periodo(dia(10), dia(12));
        Estancia estancia = new Estancia(dia(10), dia(12));

        assertTrue(periodo.incluye(dia(12)));
        assertFalse(estancia.incluye(dia(12)));
    }

    @Test
    @DisplayName("Exige la fecha a verificar")
    void fechaAVerificar() {
        assertThrows(ReglaDominioException.class, () -> new Periodo(dia(1), dia(2)).incluye(null));
    }

    // ---------- IdBloqueo ----------

    @Test
    @DisplayName("IdBloqueo es obligatorio y se guarda sin espacios")
    void idBloqueo() {
        assertEquals("BLQ-001", new IdBloqueo("  BLQ-001 ").valor());
        assertEquals(new IdBloqueo("BLQ-001"), new IdBloqueo(" BLQ-001"));
        assertThrows(ReglaDominioException.class, () -> new IdBloqueo(null));
        assertThrows(ReglaDominioException.class, () -> new IdBloqueo("   "));
    }

    // ---------- EstadoBloqueo ----------

    @Test
    @DisplayName("RN-07: solo un bloqueo VIGENTE impide vender")
    void estadoBloqueo() {
        assertTrue(EstadoBloqueo.VIGENTE.impideVenta());
        assertFalse(EstadoBloqueo.LEVANTADO.impideVenta());
    }
}