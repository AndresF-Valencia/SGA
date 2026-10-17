package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@DisplayName("ModificacionReservaService (RN-14)")
class ModificacionReservaServiceTest {

    private final ModificacionReservaService servicio = new ModificacionReservaService(
            new DisponibilidadApartamentoService(),
            new EstanciaMinimaTemporadaAltaService(),
            new CotizadorEstanciaService());

    private Reserva reserva;   // 1 al 4 de noviembre, 600.000
    private Apartamento apartamento;
    private Folio folio;

    @BeforeEach
    void setUp() {
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        apartamento = apartamento(APTO_101);
        folio = folio(reserva);
    }

    private Dinero modificar(Estancia nueva, List<Reserva> reservas, LocalDateTime ahora) {
        return servicio.modificarEstancia(reserva, apartamento, folio, nueva, alojamiento(),
                reservas, List.of(), TEMPORADAS, TARIFAS, RECEPCION, ahora);
    }

    private static Estancia noviembre(int entrada, int salida) {
        return new Estancia(LocalDate.of(2026, 11, entrada), LocalDate.of(2026, 11, salida));
    }

    @Test
    @DisplayName("Alargar la estancia sube el valor y deja un ajuste positivo en el folio")
    void alargaEstancia() {
        Dinero ajuste = modificar(noviembre(1, 6), List.of(reserva), AHORA);

        assertEquals(Dinero.de(400_000), ajuste);                 // 2 noches más × 2 personas
        assertEquals(noviembre(1, 6), reserva.getEstancia());
        assertEquals(Dinero.de(1_000_000), reserva.getValor().total());
        assertEquals(TipoCargo.AJUSTE, folio.getCargos().get(1).tipo());
        assertEquals(Dinero.de(1_000_000), folio.saldo());
    }

    @Test
    @DisplayName("Acortar la estancia deja un ajuste negativo")
    void acortaEstancia() {
        Dinero ajuste = modificar(noviembre(1, 2), List.of(reserva), AHORA);

        assertEquals(Dinero.de(-400_000), ajuste);
        assertEquals(Dinero.de(200_000), folio.saldo());
    }

    @Test
    @DisplayName("Rechaza si la nueva estancia choca con otra reserva, y nada cambia")
    void rechazaChoque() {
        Reserva otra = reserva("RES-2", noviembre(5, 8));

        assertThrows(ReglaDominioException.class, () -> modificar(noviembre(1, 6), List.of(reserva, otra), AHORA));
        assertEquals(NOVIEMBRE, reserva.getEstancia());
        assertEquals(1, folio.getCargos().size());
    }

    @Test
    @DisplayName("RN-04: no se puede mover la reserva al pasado")
    void rechazaFechaPasada() {
        LocalDateTime despues = LocalDateTime.of(2026, 11, 2, 9, 0);

        assertThrows(ReglaDominioException.class, () -> modificar(noviembre(1, 3), List.of(reserva), despues));
    }

    @Test
    @DisplayName("RP-01: al pasar a temporada alta debe cumplir el mínimo de noches")
    void respetaEstanciaMinima() {
        Estancia diciembre = new Estancia(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 22));

        assertThrows(ReglaDominioException.class, () -> modificar(diciembre, List.of(reserva), AHORA));
    }
}
