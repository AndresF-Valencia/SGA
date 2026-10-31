package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.AlojamientoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Modificar reserva (7.5): ajuste en el folio y cambio de apartamento")
class ModificarReservaUseCaseTest {

    private ModificarReservaUseCase modificar;
    private ReservaRepositoryEnMemoria reservas;
    private FolioRepositoryEnMemoria folios;
    private Reserva reserva;   // 1 al 4 de noviembre en APTO-101, 2 personas: 600.000

    @BeforeEach
    void preparar() {
        var alojamientos = new AlojamientoRepositoryEnMemoria();
        alojamientos.guardar(alojamiento());
        var apartamentos = new ApartamentoRepositoryEnMemoria();
        apartamentos.guardar(apartamento(APTO_101));
        apartamentos.guardar(apartamento(APTO_102));
        var temporadas = new TemporadaRepositoryEnMemoria();
        TEMPORADAS.forEach(temporadas::guardar);
        var tarifas = new TarifaRepositoryEnMemoria();
        TARIFAS.forEach(tarifas::guardar);
        tarifas.guardar(new Tarifa(new IdentificacionTarifa("TF-3"), APTO_102, BASE.getIdentificacion(), Dinero.de(120_000)));
        tarifas.guardar(new Tarifa(new IdentificacionTarifa("TF-4"), APTO_102, ALTA.getIdentificacion(), Dinero.de(180_000)));

        reservas = new ReservaRepositoryEnMemoria();
        folios = new FolioRepositoryEnMemoria();
        reserva = reservaConfirmada("RES-1", NOVIEMBRE);
        reservas.guardar(reserva);
        folios.guardar(folio(reserva));

        modificar = new ModificarReservaUseCase(reservas, apartamentos, alojamientos, temporadas, tarifas,
                new BloqueoRepositoryEnMemoria(), new DisponibilidadApartamentoService(),
                new EstanciaMinimaTemporadaAltaService(), new CotizadorEstanciaService(), folios);
    }

    private static Estancia noviembre(int entrada, int salida) {
        return new Estancia(LocalDate.of(2026, 11, entrada), LocalDate.of(2026, 11, salida));
    }

    private Folio folioGuardado() {
        return folios.buscarPorReserva(reserva.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("alargar la estancia deja un AJUSTE positivo en el folio, sin mandar ocupantes")
    void alargarRegistraAjuste() {
        modificar.ejecutar(reserva.getCodigo(), SAN_ROCCO, APTO_101, noviembre(1, 6), null,
                new CantidadMascota(0), RECEPCION, AHORA);

        assertEquals(TipoCargo.AJUSTE, folioGuardado().getCargos().get(1).tipo());
        assertEquals(Dinero.de(400_000), folioGuardado().getCargos().get(1).valor());   // 2 noches × 2 personas
        assertEquals(Dinero.de(1_000_000), folioGuardado().saldo());
        assertEquals(2, reserva.getOcupantes().size());
    }

    @Test
    @DisplayName("cambiar de apartamento queda guardado y recotiza con la tarifa del nuevo")
    void cambiarApartamento() {
        modificar.ejecutar(reserva.getCodigo(), SAN_ROCCO, APTO_102, NOVIEMBRE, null,
                new CantidadMascota(0), RECEPCION, AHORA);

        assertEquals(APTO_102, reservas.buscarPorCodigo(reserva.getCodigo()).orElseThrow().getApartamento());
        assertEquals(Dinero.de(120_000), folioGuardado().getCargos().get(1).valor());   // 3 noches × 2 × 20.000 más
    }

    @Test
    @DisplayName("si nada cambia de valor, no se agrega ningún cargo")
    void sinCambioDeValor() {
        modificar.ejecutar(reserva.getCodigo(), SAN_ROCCO, APTO_101, noviembre(2, 5), null,
                new CantidadMascota(0), RECEPCION, AHORA);

        assertEquals(1, folioGuardado().getCargos().size());
    }

    @Test
    @DisplayName("una reserva que no existe responde 'no encontrada' (404), no 409")
    void reservaInexistente() {
        assertThrows(ReservaNoEncontradaException.class, () -> modificar.ejecutar(codigo("RES-99"), SAN_ROCCO,
                APTO_101, NOVIEMBRE, null, new CantidadMascota(0), RECEPCION, AHORA));
    }
}
