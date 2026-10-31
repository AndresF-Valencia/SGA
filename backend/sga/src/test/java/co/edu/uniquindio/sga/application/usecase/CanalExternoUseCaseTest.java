package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.application.exception.ConflictoCanalException;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.AlojamientoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ConflictoCanalRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.FolioRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.GeneradorCodigoReservaEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@DisplayName("Canal externo (10.3.1): aceptada, conflicto registrado y mensaje repetido")
class CanalExternoUseCaseTest {

    private static final Canal BOOKING = new Canal(new IdentificacionCanal("C-1"), CanalOrigen.EXTERNO,
            "Booking", "clave-booking");

    private ReservaRepositoryEnMemoria reservas;
    private ConflictoCanalRepositoryEnMemoria conflictos;
    private RecibirReservaExternaUseCase recibir;
    private ResolverConflictoCanalUseCase resolver;

    @BeforeEach
    void preparar() {
        var alojamientos = new AlojamientoRepositoryEnMemoria();
        alojamientos.guardar(alojamiento());
        var apartamentos = new ApartamentoRepositoryEnMemoria();
        apartamentos.guardar(apartamento(APTO_101));
        var temporadas = new TemporadaRepositoryEnMemoria();
        TEMPORADAS.forEach(temporadas::guardar);
        var tarifas = new TarifaRepositoryEnMemoria();
        TARIFAS.forEach(tarifas::guardar);
        reservas = new ReservaRepositoryEnMemoria();
        conflictos = new ConflictoCanalRepositoryEnMemoria();

        recibir = new RecibirReservaExternaUseCase(reservas, apartamentos, alojamientos, temporadas, tarifas,
                new BloqueoRepositoryEnMemoria(), new FolioRepositoryEnMemoria(), new GeneradorCodigoReservaEnMemoria(),
                new DisponibilidadApartamentoService(), new EstanciaMinimaTemporadaAltaService(),
                new CotizadorEstanciaService(), conflictos);
        resolver = new ResolverConflictoCanalUseCase(conflictos);
    }

    private Reserva enviar(String codigoExterno, LocalDate entrada, LocalDate salida, int ocupantes) {
        return recibir.ejecutar(codigoExterno, BOOKING, APTO_101, entrada, salida, LAURA,
                ocupantes == 2 ? List.of(LAURA, PEDRO) : List.of(LAURA, PEDRO, adulto("1", "A"), adulto("2", "B"), adulto("3", "C")),
                new CantidadMascota(0), AHORA);
    }

    @Test
    @DisplayName("1. reserva externa aceptada, ligada al canal y a su código externo")
    void aceptada() {
        Reserva reserva = enviar("BKG-1", NOVIEMBRE.fechaEntrada(), NOVIEMBRE.fechaSalida(), 2);

        assertEquals("BKG-1", reserva.getCodigoExterno());
        assertEquals(CanalOrigen.EXTERNO, reserva.getCanalOrigen());
        assertEquals(1, reservas.obtenerTodas().size());
    }

    @Test
    @DisplayName("2. si choca con una reserva vigente se rechaza, se registra y no toca la existente")
    void conflictoRegistrado() {
        Reserva vigente = reservaConfirmada("RES-1", NOVIEMBRE);
        reservas.guardar(vigente);

        ConflictoCanalException error = assertThrows(ConflictoCanalException.class,
                () -> enviar("BKG-2", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 5), 2));

        assertEquals(1, conflictos.listar().size());
        ConflictoCanal conflicto = conflictos.listar().get(0);
        assertEquals(error.getConflictoId(), conflicto.getId());
        assertEquals("Booking", conflicto.getCanal());
        assertEquals("BKG-2", conflicto.getCodigoExterno());
        assertEquals(1, reservas.obtenerTodas().size());                 // no se creó la externa
        assertEquals(NOVIEMBRE, reservas.obtenerTodas().get(0).getEstancia());   // la vigente intacta

        // el mismo mensaje otra vez no registra un segundo conflicto
        assertThrows(ConflictoCanalException.class,
                () -> enviar("BKG-2", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 5), 2));
        assertEquals(1, conflictos.listar().size());

        // el administrador lo resuelve
        resolver.resolver(conflicto.getId(), "Se reubicó en otro apartamento", "admin@sanrocco.co", AHORA);
        assertTrue(conflictos.buscarPorId(conflicto.getId()).orElseThrow().estaResuelto());
        assertEquals(0, resolver.listar(false).size());
    }

    @Test
    @DisplayName("3. el mensaje repetido devuelve la misma reserva y no la duplica")
    void mensajeRepetido() {
        Reserva primera = enviar("BKG-3", NOVIEMBRE.fechaEntrada(), NOVIEMBRE.fechaSalida(), 2);
        Reserva repetida = enviar("BKG-3", NOVIEMBRE.fechaEntrada(), NOVIEMBRE.fechaSalida(), 2);

        assertEquals(primera.getCodigo(), repetida.getCodigo());
        assertEquals(1, reservas.obtenerTodas().size());
    }

    @Test
    @DisplayName("si el grupo no cabe se rechaza, pero NO es un conflicto de canal")
    void capacidadNoEsConflicto() {
        ReglaDominioException error = assertThrows(ReglaDominioException.class,
                () -> enviar("BKG-4", NOVIEMBRE.fechaEntrada(), NOVIEMBRE.fechaSalida(), 5));
        assertTrue(!(error instanceof ConflictoCanalException));
        assertEquals(0, conflictos.listar().size());
    }
}
