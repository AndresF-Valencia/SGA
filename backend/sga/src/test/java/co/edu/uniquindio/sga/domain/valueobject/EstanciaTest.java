package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("Estancia")
class EstanciaTest {

    private static Estancia del(int diaEntrada, int diaSalida) {
        return new Estancia(LocalDate.of(2026, 11, diaEntrada), LocalDate.of(2026, 11, diaSalida));
    }

    @Test
    @DisplayName("Exige fecha de entrada y de salida")
    void exigeFechas() {
        LocalDate hoy = LocalDate.of(2026, 11, 10);

        assertThrows(ReglaDominioException.class, () -> new Estancia(null, hoy));
        assertThrows(ReglaDominioException.class, () -> new Estancia(hoy, null));
    }

    @Test
    @DisplayName("RN-03: la salida debe ser posterior a la entrada")
    void salidaPosteriorAEntrada() {
        assertThrows(ReglaDominioException.class, () -> del(10, 10));
        assertThrows(ReglaDominioException.class, () -> del(12, 10));
    }

    @Test
    @DisplayName("Del 10 al 12 son dos noches")
    void cuentaNoches() {
        assertEquals(2, del(10, 12).noches());
        assertEquals(1, del(10, 11).noches());
    }

    @Test
    @DisplayName("Incluye la noche de entrada pero no la de salida")
    void incluyeNoches() {
        Estancia estancia = del(10, 12);

        assertTrue(estancia.incluye(LocalDate.of(2026, 11, 10)));
        assertTrue(estancia.incluye(LocalDate.of(2026, 11, 11)));
        assertFalse(estancia.incluye(LocalDate.of(2026, 11, 12)));
        assertFalse(estancia.incluye(LocalDate.of(2026, 11, 9)));
    }

    @Test
    @DisplayName("Lista las noches efectivamente ocupadas")
    void nochesOcupadas() {
        assertEquals(
                List.of(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11)),
                del(10, 12).nochesOcupadas());
    }

    @Test
    @DisplayName("RN-01: dos estancias que comparten una noche se solapan")
    void detectaSolapamiento() {
        assertTrue(del(10, 13).seSolapaCon(del(12, 15)));
        assertTrue(del(12, 15).seSolapaCon(del(10, 13)), "el solapamiento debe ser simétrico");
        assertTrue(del(10, 20).seSolapaCon(del(12, 14)), "una estancia contenida en otra se solapa");
    }

    @Test
    @DisplayName("RN-01: si una sale el mismo día que la otra entra, no se solapan")
    void estanciasConsecutivasNoSeSolapan() {
        assertFalse(del(10, 12).seSolapaCon(del(12, 14)));
        assertFalse(del(12, 14).seSolapaCon(del(10, 12)));
    }

    @Test
    @DisplayName("Calcula los días de anticipación hasta la entrada")
    void diasDeAnticipacion() {
        Estancia estancia = del(20, 22);

        assertEquals(10, estancia.diasAnticipacionDesde(LocalDate.of(2026, 11, 10)));
        assertEquals(0, estancia.diasAnticipacionDesde(LocalDate.of(2026, 11, 20)));
        assertThrows(ReglaDominioException.class, () -> estancia.diasAnticipacionDesde(null));
    }
}
