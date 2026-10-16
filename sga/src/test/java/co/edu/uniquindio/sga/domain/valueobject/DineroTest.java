package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("Dinero")
class DineroTest {

    @Test
    @DisplayName("No acepta un valor nulo")
    void noAceptaValorNulo() {
        assertThrows(ReglaDominioException.class, () -> new Dinero(null));
    }

    @Test
    @DisplayName("Redondea a pesos sin decimales (HALF_UP)")
    void redondeaSinDecimales() {
        assertEquals(Dinero.de(1501), new Dinero(new BigDecimal("1500.5")));
        assertEquals(Dinero.de(1500), new Dinero(new BigDecimal("1500.4")));
    }

    @Test
    @DisplayName("Dos valores iguales son el mismo Dinero aunque se escriban distinto")
    void igualdadPorValor() {
        assertEquals(Dinero.de(1000), new Dinero(new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("Suma, resta y multiplica")
    void operacionesBasicas() {
        Dinero cien = Dinero.de(100_000);

        assertEquals(Dinero.de(150_000), cien.mas(Dinero.de(50_000)));
        assertEquals(Dinero.de(70_000), cien.menos(Dinero.de(30_000)));
        assertEquals(Dinero.de(300_000), cien.por(3));
    }

    @Test
    @DisplayName("Las operaciones no modifican el valor original")
    void esInmutable() {
        Dinero original = Dinero.de(100_000);

        original.mas(Dinero.de(1));
        original.por(5);

        assertEquals(Dinero.de(100_000), original);
    }

    @Test
    @DisplayName("Admite resultados negativos (ajustes y saldos a favor)")
    void admiteNegativos() {
        Dinero ajuste = Dinero.de(100_000).menos(Dinero.de(150_000));

        assertEquals(Dinero.de(-50_000), ajuste);
        assertTrue(ajuste.esNegativo());
    }

    @Test
    @DisplayName("Reconoce el cero")
    void reconoceCero() {
        assertTrue(Dinero.CERO.esCero());
        assertFalse(Dinero.CERO.esNegativo());
        assertFalse(Dinero.de(1).esCero());
    }

    @Test
    @DisplayName("Calcula porcentajes")
    void calculaPorcentajes() {
        assertEquals(Dinero.de(50_000), Dinero.de(100_000).porcentual(50));
        assertEquals(Dinero.de(75_000), Dinero.de(250_000).porcentual(30));
        assertTrue(Dinero.de(250_000).porcentual(0).esCero());
        assertEquals(Dinero.de(250_000), Dinero.de(250_000).porcentual(100));
    }

    @Test
    @DisplayName("Rechaza porcentajes fuera de 0 a 100")
    void rechazaPorcentajesInvalidos() {
        Dinero valor = Dinero.de(100_000);

        assertThrows(ReglaDominioException.class, () -> valor.porcentual(-1));
        assertThrows(ReglaDominioException.class, () -> valor.porcentual(100.1));
    }
}
