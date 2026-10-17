package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@DisplayName("RegistroLlegadaService (check-in)")
class RegistroLlegadaServiceTest {

    /** El día de entrada, a las 3 de la tarde. */
    private static final LocalDateTime DIA_DE_ENTRADA = NOVIEMBRE.fechaEntrada().atTime(15, 0);

    private final RegistroLlegadaService servicio = new RegistroLlegadaService();
    private Reserva reserva;
    private Apartamento apartamento;
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        apartamento = apartamento(APTO_101);
        folio = folio(reserva);
        folio.registrarPago(pago(600_000)); // RNP-03: pagado completo al llegar
    }

    @Test
    @DisplayName("La reserva pasa a EN_CURSO y el apartamento a OCUPADO")
    void registraLlegada() {
        servicio.registrarLlegada(reserva, apartamento, folio, RECEPCION, DIA_DE_ENTRADA);

        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-11: no entrega un apartamento que no está PREPARADO, y la reserva no cambia")
    void rechazaApartamentoNoPreparado() {
        apartamento.marcarOcupado();
        apartamento.marcarPendientePreparacion();

        assertThrows(ReglaDominioException.class,
                () -> servicio.registrarLlegada(reserva, apartamento, folio, RECEPCION, DIA_DE_ENTRADA));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("RN-08: una reserva PENDIENTE no hace check-in, y el apartamento no cambia")
    void rechazaReservaPendiente() {
        Reserva pendiente = reserva("RES-2", NOVIEMBRE);
        Folio folioPendiente = folio(pendiente);
        folioPendiente.registrarPago(pago(600_000));

        assertThrows(ReglaDominioException.class,
                () -> servicio.registrarLlegada(pendiente, apartamento, folioPendiente, RECEPCION, DIA_DE_ENTRADA));
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-10: no registra la llegada antes de la fecha de entrada")
    void rechazaAntesDeLaFecha() {
        assertThrows(ReglaDominioException.class,
                () -> servicio.registrarLlegada(reserva, apartamento, folio, RECEPCION, DIA_DE_ENTRADA.minusDays(1)));
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("Rechaza un apartamento distinto al de la reserva")
    void rechazaOtroApartamento() {
        assertThrows(ReglaDominioException.class,
                () -> servicio.registrarLlegada(reserva, apartamento(APTO_102), folio, RECEPCION, DIA_DE_ENTRADA));
    }

    @Test
    @DisplayName("RNP-03: no registra la llegada si falta pagar el 50 % restante")
    void rechazaSaldoPendiente() {
        Reserva reservaConSaldo = reservaConfirmada("RES-3", NOVIEMBRE);
        Folio folioConSaldo = folio(reservaConSaldo);
        folioConSaldo.registrarPago(pago(300_000)); // solo el anticipo

        assertThrows(ReglaDominioException.class, () -> servicio.registrarLlegada(reservaConSaldo, apartamento,
                folioConSaldo, RECEPCION, DIA_DE_ENTRADA));
        assertEquals(EstadoReserva.CONFIRMADA, reservaConSaldo.getEstado());
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }
}