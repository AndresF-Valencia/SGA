package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

@DisplayName("NoShowService (7.5, RNP-04)")
class NoShowServiceTest {

    /**
     * Llegada indicada a las 15:00 + 2 horas de tolerancia: el límite es a las
     * 17:00.
     */
    private static final LocalDateTime DESPUES_DEL_LIMITE = NOVIEMBRE.fechaEntrada().atTime(18, 0);

    private final NoShowService servicio = new NoShowService();
    private Reserva reserva; // vale 600.000
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        folio = folio(reserva);
        folio.registrarPago(pago(300_000)); // anticipo
    }

    @Test
    @DisplayName("Retiene el anticipo: el huésped pierde lo pagado y no debe nada más")
    void retieneElAnticipo() {
        Dinero retenido = servicio.declararNoShow(reserva, folio, alojamiento(), RECEPCION, DESPUES_DEL_LIMITE);

        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        assertEquals(Dinero.de(300_000), retenido);
        assertEquals(TipoCargo.PENALIDAD_NO_SHOW, folio.getCargos().get(2).tipo());
        assertEquals(Dinero.CERO, folio.saldo());
    }

    @Test
    @DisplayName("Antes de la hora límite se rechaza y el folio no cambia")
    void rechazaAntesDelLimite() {
        assertThrows(ReglaDominioException.class, () -> servicio.declararNoShow(reserva, folio, alojamiento(),
                RECEPCION, NOVIEMBRE.fechaEntrada().atTime(16, 0)));
        assertEquals(1, folio.getCargos().size());
    }

    @Test
    @DisplayName("RNP-04: con notificación de retraso no procede el no-show")
    void respetaNotificacionDeRetraso() {
        reserva.notificarRetraso(NotificacionRetraso.registrar(LocalTime.of(21, 0), "Vuelo retrasado",
                NOVIEMBRE.fechaEntrada().atTime(14, 0)), RECEPCION, NOVIEMBRE.fechaEntrada().atTime(14, 0));

        assertThrows(ReglaDominioException.class, () -> servicio.declararNoShow(reserva, folio, alojamiento(),
                RECEPCION, DESPUES_DEL_LIMITE));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }
}