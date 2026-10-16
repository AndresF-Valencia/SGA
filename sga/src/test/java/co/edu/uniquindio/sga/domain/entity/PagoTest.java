package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;

@DisplayName("Pago")
class PagoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 13, 10, 0);

    @Test
    @DisplayName("RN-15: conserva su monto, medio y fecha")
    void conservaSusDatos() {
        Pago pago = new Pago(Dinero.de(300_000), MedioPago.TARJETA_DEBITO, AHORA);

        assertEquals(Dinero.de(300_000), pago.getMonto());
        assertEquals(MedioPago.TARJETA_DEBITO, pago.getMedioPago());
        assertEquals(AHORA, pago.getHoraPago());
    }

    @Test
    @DisplayName("RN-15: exige monto, medio y fecha")
    void exigeSusDatos() {
        Dinero monto = Dinero.de(100_000);

        assertThrows(ReglaDominioException.class, () -> new Pago(null, MedioPago.EFECTIVO, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Pago(monto, null, AHORA));
        assertThrows(ReglaDominioException.class, () -> new Pago(monto, MedioPago.EFECTIVO, null));
    }

    @Test
    @DisplayName("El monto debe ser mayor que cero")
    void montoPositivo() {
        assertThrows(ReglaDominioException.class,
                () -> new Pago(Dinero.CERO, MedioPago.EFECTIVO, AHORA));
        assertThrows(ReglaDominioException.class,
                () -> new Pago(Dinero.de(-50_000), MedioPago.EFECTIVO, AHORA));
    }
}