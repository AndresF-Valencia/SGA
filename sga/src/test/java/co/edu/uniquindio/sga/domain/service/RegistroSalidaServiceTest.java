package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@DisplayName("RegistroSalidaService (check-out)")
class RegistroSalidaServiceTest {

    private static final LocalDateTime DIA_DE_SALIDA = NOVIEMBRE.fechaSalida().atTime(11, 0);

    private final RegistroSalidaService servicio = new RegistroSalidaService();
    private Reserva reserva; // vale 600.000
    private Apartamento apartamento;
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        apartamento = apartamento(APTO_101);
        folio = folio(reserva);
        folio.registrarPago(pago(600_000)); // RNP-03: se paga todo al llegar
        new RegistroLlegadaService().registrarLlegada(reserva, apartamento, folio, RECEPCION,
                NOVIEMBRE.fechaEntrada().atTime(15, 0));
    }

    /**
     * Un desayuno de 20.000 consumido durante la estancia: deja saldo pendiente.
     */
    private static Cargo desayuno() {
        return new Cargo(Dinero.de(20_000), "Desayuno", TipoCargo.SERVICIO_ADICIONAL, AHORA);
    }

    @Test
    @DisplayName("Con saldo cero: cierra el folio, finaliza la reserva y deja el apartamento por preparar")
    void salidaConSaldoCero() {
        servicio.registrarSalida(reserva, apartamento, folio, null, RECEPCION, DIA_DE_SALIDA);

        assertTrue(folio.isCerrado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-17: con saldo pendiente y sin autorización no sale, y nada cambia")
    void rechazaSaldoSinAutorizacion() {
        folio.registrarCargo(desayuno());

        assertThrows(ReglaDominioException.class,
                () -> servicio.registrarSalida(reserva, apartamento, folio, null, RECEPCION, DIA_DE_SALIDA));
        assertFalse(folio.isCerrado());
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-17: con autorización del administrador sale aunque quede saldo")
    void salidaConAutorizacion() {
        folio.registrarCargo(desayuno());
        AutorizacionCierre autorizacion = new AutorizacionCierre("administrador",
                "Pagará el saldo por transferencia", DIA_DE_SALIDA);

        servicio.registrarSalida(reserva, apartamento, folio, autorizacion, RECEPCION, DIA_DE_SALIDA);

        assertTrue(folio.isCerrado());
        assertEquals(autorizacion, folio.getAutorizacionCierre());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    @DisplayName("Solo sale una reserva EN_CURSO")
    void rechazaReservaQueNoEstaEnCurso() {
        Reserva confirmada = reservaConfirmada("RES-2", NOVIEMBRE);
        Folio otroFolio = folio(confirmada);
        otroFolio.registrarPago(pago(600_000));

        assertThrows(ReglaDominioException.class, () -> servicio.registrarSalida(confirmada, apartamento,
                otroFolio, null, RECEPCION, DIA_DE_SALIDA));
        assertFalse(otroFolio.isCerrado());
    }
}