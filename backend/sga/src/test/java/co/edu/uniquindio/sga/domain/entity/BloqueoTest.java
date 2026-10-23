package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

@DisplayName("Bloqueo")
class BloqueoTest {

    private static final IdentificacionBloqueo ID = new IdentificacionBloqueo("BLQ-001");
    private static final IdentificacionApartamento APTO = new IdentificacionApartamento("APTO-101");

    /** El 101 se bloquea del 15 al 20 de noviembre, ambos días incluidos. */
    private static final Periodo DEL_15_AL_20 = new Periodo(dia(15), dia(20));

    private Bloqueo bloqueo;

    @BeforeEach
    void setUp() {
        bloqueo = new Bloqueo(ID, APTO, DEL_15_AL_20, MotivoBloqueo.MANTENIMIENTO);
    }

    // ---------- Ayudantes ----------

    private static LocalDate dia(int dia) {
        return LocalDate.of(2026, 11, dia);
    }

    /** Estancia en noviembre: entra el día indicado y sale el día indicado. */
    private static Estancia estancia(int entrada, int salida) {
        return new Estancia(dia(entrada), dia(salida));
    }

    // ---------- Creación ----------

    @Test
    @DisplayName("Nace VIGENTE y conserva sus datos")
    void naceVigente() {
        assertEquals(EstadoBloqueo.VIGENTE, bloqueo.getEstado());
        assertEquals(ID, bloqueo.getIdentificacionBloqueo());
        assertEquals(APTO, bloqueo.getApartamento());
        assertEquals(DEL_15_AL_20, bloqueo.getPeriodo());
        assertEquals(MotivoBloqueo.MANTENIMIENTO, bloqueo.getMotivo());
    }

    @Test
    @DisplayName("Exige identificador, apartamento, periodo y motivo")
    void exigeSusDatos() {
        MotivoBloqueo motivo = MotivoBloqueo.REMODELACION;

        assertThrows(ReglaDominioException.class, () -> new Bloqueo(null, APTO, DEL_15_AL_20, motivo));
        assertThrows(ReglaDominioException.class, () -> new Bloqueo(ID, null, DEL_15_AL_20, motivo));
        assertThrows(ReglaDominioException.class, () -> new Bloqueo(ID, APTO, null, motivo));
        assertThrows(ReglaDominioException.class, () -> new Bloqueo(ID, APTO, DEL_15_AL_20, null));
    }

    // ---------- RN-07: noches bloqueadas ----------

    @Test
    @DisplayName("RN-07: un bloqueo vigente impide vender las noches de su periodo, incluidos los extremos")
    void bloqueaSuPeriodo() {
        assertTrue(bloqueo.bloquea(dia(15)), "primer día");
        assertTrue(bloqueo.bloquea(dia(17)), "día intermedio");
        assertTrue(bloqueo.bloquea(dia(20)), "último día: el periodo lo incluye");
    }

    @Test
    @DisplayName("RN-07: las noches fuera del periodo siguen disponibles")
    void noBloqueaFueraDelPeriodo() {
        assertFalse(bloqueo.bloquea(dia(14)));
        assertFalse(bloqueo.bloquea(dia(21)));
    }

    @Test
    @DisplayName("RN-07: una estancia que pasa por el periodo queda afectada")
    void afectaEstanciasQueSeCruzan() {
        assertTrue(bloqueo.afecta(estancia(16, 18)), "dentro del periodo");
        assertTrue(bloqueo.afecta(estancia(12, 16)), "entra antes y sale dentro");
        assertTrue(bloqueo.afecta(estancia(19, 23)), "entra dentro y sale después");
        assertTrue(bloqueo.afecta(estancia(10, 25)), "cubre todo el periodo");
        assertTrue(bloqueo.afecta(estancia(20, 22)), "entra el último día bloqueado");
    }

    @Test
    @DisplayName("RN-07: si el huésped sale el día que empieza el bloqueo, no hay choque")
    void salidaElPrimerDiaDelBloqueo() {
        // Duerme del 10 al 14 y se va el 15 por la mañana: la noche del 15 no es suya.
        assertFalse(bloqueo.afecta(estancia(10, 15)));
    }

    @Test
    @DisplayName("RN-07: una estancia que empieza después del bloqueo no queda afectada")
    void estanciaDespuesDelBloqueo() {
        assertFalse(bloqueo.afecta(estancia(21, 24)));
        assertFalse(bloqueo.afecta(estancia(1, 5)));
    }

    // ---------- Levantar (eliminación lógica) ----------

    @Test
    @DisplayName("Al levantarlo deja de bloquear, pero no desaparece")
    void levantar() {
        bloqueo.levantar();

        assertEquals(EstadoBloqueo.LEVANTADO, bloqueo.getEstado());
        assertFalse(bloqueo.bloquea(dia(17)));
        assertFalse(bloqueo.afecta(estancia(16, 18)));
        assertEquals(DEL_15_AL_20, bloqueo.getPeriodo(), "conserva su historia");
        assertEquals(MotivoBloqueo.MANTENIMIENTO, bloqueo.getMotivo());
    }

    @Test
    @DisplayName("No se puede levantar dos veces")
    void noLevantarDosVeces() {
        bloqueo.levantar();

        assertThrows(ReglaDominioException.class, () -> bloqueo.levantar());
    }

    // ---------- Identidad ----------

    @Test
    @DisplayName("Dos bloqueos son el mismo si tienen el mismo identificador")
    void igualdadPorId() {
        Bloqueo mismoId = new Bloqueo(new IdentificacionBloqueo(" BLQ-001 "), new IdentificacionApartamento("APTO-202"),
                new Periodo(dia(1), dia(2)), MotivoBloqueo.REMODELACION);
        Bloqueo otro = new Bloqueo(new IdentificacionBloqueo("BLQ-002"), APTO, DEL_15_AL_20, MotivoBloqueo.MANTENIMIENTO);

        assertEquals(bloqueo, mismoId);
        assertEquals(bloqueo.hashCode(), mismoId.hashCode());
        assertNotEquals(bloqueo, otro);
    }
}