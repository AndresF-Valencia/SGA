package co.edu.uniquindio.sga.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.application.usecase.VerificarAccesoReservaUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 12.3: al iniciar, el sistema debe quedar con datos suficientes para operar y demostrar. */
@SpringBootTest
@DisplayName("Datos de prueba al iniciar (12.3)")
class DatosDePruebaTest {

    private static final List<String> APARTAMENTOS = List.of("AP001", "AP002", "AP003", "AP004", "AP005");

    @Autowired private AlojamientoRepository alojamientos;
    @Autowired private ApartamentoRepository apartamentos;
    @Autowired private TemporadaRepository temporadas;
    @Autowired private TarifaRepository tarifas;
    @Autowired private PoliticaCancelacionRepository politicas;
    @Autowired private BloqueoRepository bloqueos;
    @Autowired private ReservaRepository reservas;
    @Autowired private FolioRepository folios;
    @Autowired private CotizadorEstanciaService cotizador;
    @Autowired private VerificarAccesoReservaUseCase verificarAcceso;

    /** Solo las reservas de los datos de prueba (otras pruebas guardan reservas en otros apartamentos). */
    private List<Reserva> reservasDePrueba() {
        return reservas.obtenerTodas().stream()
                .filter(r -> APARTAMENTOS.contains(r.getApartamento().valor()))
                .toList();
    }

    @Test
    @DisplayName("Existe el alojamiento SAN ROCCO con la política versión 1")
    void alojamientoYPolitica() {
        var sanrocco = alojamientos.buscarPorId(DatosDePrueba.SANROCCO).orElseThrow();
        assertEquals(5, sanrocco.getUmbralEdadFacturable().anios());
        assertEquals(50, sanrocco.getPorcentajeAnticipo());
        assertTrue(politicas.buscarPorVersion(new VersionPolitica(1)).isPresent());
    }

    @Test
    @DisplayName("Están los 5 apartamentos de la Ficha, uno ocupado y uno fuera de servicio")
    void apartamentos() {
        for (String id : APARTAMENTOS) {
            assertTrue(apartamentos.buscarPorId(new IdentificacionApartamento(id)).isPresent(), id);
        }
        assertEquals(EstadoOperativo.FUERA_DE_SERVICIO,
                apartamentos.buscarPorId(new IdentificacionApartamento("AP003")).orElseThrow().getEstadoOperativo());
        assertEquals(EstadoOperativo.OCUPADO,
                apartamentos.buscarPorId(new IdentificacionApartamento("AP002")).orElseThrow().getEstadoOperativo());
    }

    @Test
    @DisplayName("Cada apartamento tiene tarifa en TODAS las temporadas, y se puede cotizar cualquier fecha del rango")
    void tarifasCompletas() {
        List<Temporada> deLaFicha = temporadas.listar().stream()
                .filter(t -> t.getIdentificacion().id().matches("T-(BASE|MEDIA|ALTA)-\\d{4}-\\d{2}-\\d{2}"))
                .toList();
        assertFalse(deLaFicha.isEmpty());
        for (String id : APARTAMENTOS) {
            assertEquals(deLaFicha.size(), tarifas.buscarPorApartamento(new IdentificacionApartamento(id)).size(), id);
        }

        // Una estancia cualquiera dentro de un año se puede cotizar: no hay huecos entre temporadas
        LocalDate entrada = LocalDate.now().plusDays(200);
        Estancia estancia = new Estancia(entrada, entrada.plusDays(4));
        Ocupante adulto = new Ocupante(new DocumentoIdentidad("123"), "Adulto", LocalDate.of(1990, 1, 1), LocalDate.now());
        IdentificacionApartamento ap005 = new IdentificacionApartamento("AP005");
        assertDoesNotThrow(() -> cotizador.cotizar(ap005, estancia, List.of(adulto), new co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable(5),
                temporadas.buscarEntre(entrada, entrada.plusDays(3)), tarifas.buscarPorApartamento(ap005)));
    }

    @Test
    @DisplayName("Hay reservas en TODOS los estados del ciclo de vida y de los tres canales")
    void reservasEnTodosLosEstadosYCanales() {
        Set<EstadoReserva> estados = reservasDePrueba().stream().map(Reserva::getEstado)
                .collect(Collectors.toSet());
        Set<CanalOrigen> canales = reservasDePrueba().stream().map(Reserva::getCanalOrigen)
                .collect(Collectors.toSet());

        assertEquals(EnumSet.allOf(EstadoReserva.class), estados);
        assertEquals(EnumSet.allOf(CanalOrigen.class), canales);
    }

    @Test
    @DisplayName("La reserva finalizada tiene el folio cerrado en cero")
    void folioFinalizada() {
        Reserva finalizada = reservasDePrueba().stream()
                .filter(r -> r.getEstado() == EstadoReserva.FINALIZADA).findFirst().orElseThrow();
        var folio = folios.buscarPorReserva(finalizada.getCodigo()).orElseThrow();

        assertTrue(folio.isCerrado());
        assertTrue(folio.saldo().esCero());
    }

    @Test
    @DisplayName("Hay al menos un bloqueo vigente")
    void bloqueoVigente() {
        assertFalse(bloqueos.buscarVigentesPorApartamento(new IdentificacionApartamento("AP004")).isEmpty());
        assertFalse(bloqueos.buscarVigentesPorApartamento(new IdentificacionApartamento("AP003")).isEmpty());
    }

    @Test
    @DisplayName("La huésped de prueba es la titular de la reserva en curso (puede verla como suya)")
    void huespedEsTitular() {
        Reserva enCurso = reservasDePrueba().stream()
                .filter(r -> r.getEstado() == EstadoReserva.EN_CURSO).findFirst().orElseThrow();

        assertDoesNotThrow(() -> verificarAcceso.verificarTitular(enCurso.getCodigo(),
                new DocumentoIdentidad("1094000001")));
    }
}
