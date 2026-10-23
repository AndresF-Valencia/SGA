package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("Cargo y AutorizacionCierre")
class CargoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 13, 10, 0);

    // ---------- Cargo ----------

    @Test
    @DisplayName("Conserva su valor, descripción, tipo y fecha")
    void conservaSusDatos() {
        Cargo cargo = new Cargo(Dinero.de(50_000), "Lavandería", TipoCargo.SERVICIO_ADICIONAL, AHORA);

        assertEquals(Dinero.de(50_000), cargo.valor());
        assertEquals("Lavandería", cargo.descripcion());
        assertEquals(TipoCargo.SERVICIO_ADICIONAL, cargo.tipo());
        assertEquals(AHORA, cargo.fecha());
    }

    @Test
    @DisplayName("Exige valor, descripción, tipo y fecha")
    void exigeSusDatos() {
        Dinero valor = Dinero.de(50_000);
        TipoCargo tipo = TipoCargo.SERVICIO_ADICIONAL;

        assertThrows(ReglaDominioException.class, () -> new Cargo(null, "x", tipo, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Cargo(Dinero.CERO, "x", tipo, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Cargo(valor, " ", tipo, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Cargo(valor, "x", null, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Cargo(valor, "x", tipo, null));
    }

    @Test
    @DisplayName("Solo un ajuste puede ser negativo")
    void soloAjusteNegativo() {
        Dinero negativo = Dinero.de(-400_000);

        assertDoesNotThrow(() -> new Cargo(negativo, "Acortó la estancia", TipoCargo.AJUSTE, AHORA));
        for (TipoCargo tipo : TipoCargo.values()) {
            if (tipo != TipoCargo.AJUSTE) {
                assertThrows(ReglaDominioException.class,
                        () -> new Cargo(negativo, "x", tipo, AHORA), "con tipo " + tipo);
            }
        }
    }

    @Test
    @DisplayName("Sin tipo, el error avisa que falta el tipo y no que el valor es negativo")
    void mensajeSinTipo() {
        ReglaDominioException error = assertThrows(ReglaDominioException.class,
                () -> new Cargo(Dinero.de(-1), "x", null, AHORA));

        assertEquals("El cargo debe indicar su tipo", error.getMessage());
    }

    // ---------- AutorizacionCierre ----------

    @Test
    @DisplayName("RN-17: la autorización exige autor, motivo y fecha")
    void autorizacionCompleta() {
        assertDoesNotThrow(() -> new AutorizacionCierre("administrador", "Cortesía", AHORA));
        assertThrows(ReglaDominioException.class, () -> new AutorizacionCierre(" ", "Cortesía", AHORA));
        assertThrows(ReglaDominioException.class, () -> new AutorizacionCierre("admin", null, AHORA));
        assertThrows(ReglaDominioException.class, () -> new AutorizacionCierre("admin", "Cortesía", null));
    }
}