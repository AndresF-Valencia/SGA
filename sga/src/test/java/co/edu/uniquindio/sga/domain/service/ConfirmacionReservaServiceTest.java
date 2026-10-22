package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

@DisplayName("ConfirmacionReservaService (RP-03)")
class ConfirmacionReservaServiceTest {

    private final ConfirmacionReservaService servicio = new ConfirmacionReservaService();
    private Reserva reserva;   // vale 600.000
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reserva("RES-1", NOVIEMBRE);
        reserva.indicarHoraEstimadaLlegada(LocalTime.of(16, 0), RECEPCION);
        folio = folio(reserva);
    }

    @Test
    @DisplayName("Confirma cuando el folio registra el 50 % de anticipo")
    void confirmaConAnticipo() {
        folio.registrarPago(pago(300_000));

        servicio.autorizarConfirmacion(reserva, folio, alojamiento(), AHORA, RECEPCION);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("Rechaza con anticipo insuficiente y la reserva sigue PENDIENTE")
    void rechazaAnticipoInsuficiente() {
        folio.registrarPago(pago(299_999));

        assertThrows(ReglaDominioException.class,
                () -> servicio.autorizarConfirmacion(reserva, folio, alojamiento(), AHORA, RECEPCION));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("El porcentaje sale del alojamiento, no está fijo en 50")
    void usaElPorcentajeDelAlojamiento() {
        folio.registrarPago(pago(180_000));   // 30 % de 600.000

        servicio.autorizarConfirmacion(reserva, folio,
                alojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0), true, 30), AHORA, RECEPCION);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("Si el alojamiento no exige anticipo, confirma sin pagos")
    void sinAnticipoExigido() {
        servicio.autorizarConfirmacion(reserva, folio,
                alojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0), false, 0), AHORA, RECEPCION);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("Rechaza el folio de otra reserva y un autor vacío")
    void validaDatos() {
        Folio ajeno = folio(reserva("RES-2", NOVIEMBRE));

        assertThrows(ReglaDominioException.class,
                () -> servicio.autorizarConfirmacion(reserva, ajeno, alojamiento(), AHORA, RECEPCION));
        assertThrows(ReglaDominioException.class,
                () -> servicio.autorizarConfirmacion(reserva, folio, alojamiento(), AHORA, null));
    }

    @Test
    @DisplayName("El anticipo se redondea al peso, sin decimales")
    void redondeaElAnticipo() {
        assertEquals(Dinero.de(167), servicio.calcularAnticipoRequerido(Dinero.de(333), 50));
    }
}
