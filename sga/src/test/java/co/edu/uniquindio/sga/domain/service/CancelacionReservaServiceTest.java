package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ResultadoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

@DisplayName("CancelacionReservaService (RN-12, RN-13)")
class CancelacionReservaServiceTest {

    private static final String MOTIVO = "El huésped desistió del viaje";

    private final CancelacionReservaService servicio = new CancelacionReservaService();
    private Reserva reserva;    // 1 de noviembre, vale 600.000
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        folio = folio(reserva);
        folio.registrarPago(pago(300_000));
    }

    @Test
    @DisplayName("Entre 7 y 29 días antes retiene el 50 %: el saldo queda a favor del huésped por la diferencia")
    void cancelacionConPenalidadParcial() {
        // 12 de octubre → 1 de noviembre: 20 días de anticipación
        ResultadoCancelacion resultado = servicio.cancelar(reserva, folio, POLITICA, MOTIVO, RECEPCION, AHORA);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Dinero.de(300_000), resultado.penalidad());
        assertEquals(Dinero.de(300_000), resultado.reembolso());
        // Cargos: 600.000 − 600.000 (reversión) + 300.000 (penalidad); pagos: 300.000
        assertEquals(Dinero.CERO, folio.saldo());
        assertEquals(TipoCargo.PENALIDAD_POR_CANCELACION, folio.getCargos().get(2).tipo());
    }

    @Test
    @DisplayName("Con 30 días o más no hay penalidad: se devuelve todo lo pagado")
    void cancelacionSinPenalidad() {
        Reserva lejana = reservaConfirmada("RES-2",
                new Estancia(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22)));
        Folio folioLejano = folio(lejana);
        folioLejano.registrarPago(pago(200_000));

        ResultadoCancelacion resultado = servicio.cancelar(lejana, folioLejano, POLITICA, MOTIVO, RECEPCION, AHORA);

        assertEquals(Dinero.CERO, resultado.penalidad());
        assertEquals(Dinero.de(-200_000), folioLejano.saldo());   // se le devuelven los 200.000
        assertEquals(2, folioLejano.getCargos().size());          // alojamiento + reversión
    }

    @Test
    @DisplayName("A menos de 7 días se retiene todo el valor")
    void cancelacionSinDevolucion() {
        LocalDateTime cuatroDiasAntes = LocalDateTime.of(2026, 10, 28, 9, 0);

        ResultadoCancelacion resultado = servicio.cancelar(reserva, folio, POLITICA, MOTIVO, RECEPCION, cuatroDiasAntes);

        assertEquals(Dinero.de(600_000), resultado.penalidad());
        assertEquals(Dinero.de(300_000), folio.saldo());   // aún debe la otra mitad
    }

    @Test
    @DisplayName("Una reserva EN_CURSO no se cancela y el folio queda intacto")
    void rechazaReservaEnCurso() {
        new RegistroLlegadaService().registrarLlegada(reserva, apartamento(APTO_101), RECEPCION,
                NOVIEMBRE.fechaEntrada().atTime(15, 0));

        assertThrows(ReglaDominioException.class,
                () -> servicio.cancelar(reserva, folio, POLITICA, MOTIVO, RECEPCION, AHORA));
        assertEquals(1, folio.getCargos().size());
    }

    @Test
    @DisplayName("Funciona aunque la reserva todavía no tenga folio")
    void cancelaSinFolio() {
        Reserva pendiente = reserva("RES-3", NOVIEMBRE);

        servicio.cancelar(pendiente, null, POLITICA, MOTIVO, RECEPCION, AHORA);

        assertEquals(EstadoReserva.CANCELADA, pendiente.getEstado());
    }
}
