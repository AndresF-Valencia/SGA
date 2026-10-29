package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.CotizacionRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de integración: arranca Spring con H2 y guarda cada agregado DE VERDAD.
 * Los identificadores empiezan por "JPA-" porque la base se comparte con las
 * demás pruebas de la misma ejecución.
 */
@SpringBootTest
@DisplayName("Repositorios JPA (H2)")
class RepositoriosJpaTest {

    @Autowired private AlojamientoRepository alojamientos;
    @Autowired private ApartamentoRepository apartamentos;
    @Autowired private BloqueoRepository bloqueos;
    @Autowired private CanalRepository canales;
    @Autowired private CotizacionRepository cotizaciones;
    @Autowired private FolioRepository folios;
    @Autowired private TarifaRepository tarifas;
    @Autowired private TemporadaRepository temporadas;
    @Autowired private PoliticaCancelacionRepository politicas;
    @Autowired private GeneradorCodigoReserva generador;

    @Test
    @DisplayName("Spring entrega los adaptadores JPA, no los de memoria")
    void usaLosAdaptadoresJpa() {
        assertInstanceOf(AlojamientoRepositoryJpa.class, alojamientos);
        assertInstanceOf(ApartamentoRepositoryJpa.class, apartamentos);
        assertInstanceOf(BloqueoRepositoryJpa.class, bloqueos);
        assertInstanceOf(CanalRepositoryJpa.class, canales);
        assertInstanceOf(CotizacionRepositoryJpa.class, cotizaciones);
        assertInstanceOf(FolioRepositoryJpa.class, folios);
        assertInstanceOf(TarifaRepositoryJpa.class, tarifas);
        assertInstanceOf(TemporadaRepositoryJpa.class, temporadas);
        assertInstanceOf(PoliticaCancelacionRepositoryJpa.class, politicas);
        assertInstanceOf(GeneradorCodigoReservaJpa.class, generador);
    }

    @Test
    @DisplayName("Alojamiento: se guarda, se lee y se actualiza")
    void alojamientoPersistido() {
        var alojamiento = alojamiento();
        alojamientos.guardar(alojamiento);

        var leido = alojamientos.buscarPorId(alojamiento.getIdentificacion()).orElseThrow();
        leido.publicarNuevaVersionPolitica();
        alojamientos.guardar(leido);

        assertEquals(new VersionPolitica(2),
                alojamientos.buscarPorId(alojamiento.getIdentificacion()).orElseThrow().getVersionPoliticaVigente());
        assertTrue(alojamientos.obtenerUnico().isPresent());
    }

    @Test
    @DisplayName("Apartamento: guarda listas y novedades, y listarActivos excluye los inactivos")
    void apartamentoPersistido() {
        Apartamento activo = apartamento(new IdentificacionApartamento("JPA-APTO-1"));
        activo.agregarCaracteristica("Balcón");
        activo.registrarNovedad(Novedad.de("Bombillo fundido", "BAJA", RECEPCION, AHORA));
        activo.marcarOcupado();
        Apartamento inactivo = apartamento(new IdentificacionApartamento("JPA-APTO-2"));
        inactivo.desactivar();
        apartamentos.guardar(activo);
        apartamentos.guardar(inactivo);

        Apartamento leido = apartamentos.buscarPorId(activo.getIdentificacion()).orElseThrow();
        assertEquals(EstadoOperativo.OCUPADO, leido.getEstadoOperativo());
        assertEquals(activo.getCaracteristicas(), leido.getCaracteristicas());
        assertEquals(activo.getNovedades(), leido.getNovedades());

        List<IdentificacionApartamento> activos = apartamentos.listarActivos().stream()
                .map(Apartamento::getIdentificacion).toList();
        assertTrue(activos.contains(activo.getIdentificacion()));
        assertFalse(activos.contains(inactivo.getIdentificacion()));
    }

    @Test
    @DisplayName("Bloqueo: separa los vigentes de los levantados")
    void bloqueoPersistido() {
        IdentificacionApartamento apto = new IdentificacionApartamento("JPA-APTO-B");
        Periodo periodo = new Periodo(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));
        Bloqueo vigente = new Bloqueo(new IdentificacionBloqueo("JPA-B-1"), apto, periodo, MotivoBloqueo.MANTENIMIENTO);
        Bloqueo levantado = new Bloqueo(new IdentificacionBloqueo("JPA-B-2"), apto, periodo, MotivoBloqueo.REMODELACION);
        levantado.levantar();
        bloqueos.guardar(vigente);
        bloqueos.guardar(levantado);

        assertEquals(List.of(vigente.getIdentificacionBloqueo()),
                bloqueos.buscarVigentesPorApartamento(apto).stream().map(Bloqueo::getIdentificacionBloqueo).toList());
        assertEquals(2, bloqueos.buscarPorApartamento(apto).size());
    }

    @Test
    @DisplayName("Canal: se encuentra por su credencial")
    void canalPersistido() {
        Canal canal = new Canal(new IdentificacionCanal("JPA-C-1"), CanalOrigen.EXTERNO, "Airbnb", "clave-jpa-airbnb");
        canales.guardar(canal);

        assertEquals(canal.getIdentificacion(),
                canales.buscarPorCredencial("clave-jpa-airbnb").orElseThrow().getIdentificacion());
        assertTrue(canales.buscarPorCredencial("clave-que-no-existe").isEmpty());
    }

    @Test
    @DisplayName("Cotización: se guarda y se lee con su desglose")
    void cotizacionPersistido() {
        Cotizacion cotizacion = new Cotizacion(new IdentificacionCotizacion("JPA-COT-1"), APTO_101, NOVIEMBRE, 2,
                new CantidadMascota(0), valorBase(NOVIEMBRE, 2), AHORA);
        cotizaciones.guardar(cotizacion);

        Cotizacion leida = cotizaciones.buscarPorId(cotizacion.getIdentificacion()).orElseThrow();
        assertEquals(cotizacion.getValor(), leida.getValor());
        assertEquals(cotizacion.getMontoTotal(), leida.getMontoTotal());
    }

    @Test
    @DisplayName("Folio: guarda cargos y pagos, y al guardar de nuevo agrega los nuevos")
    void folioPersistido() {
        Folio folio = folio(reserva("RES-801", NOVIEMBRE));
        folios.guardar(folio);

        Folio leido = folios.buscarPorReserva(folio.getReserva()).orElseThrow();
        leido.registrarPago(pago(600_000));
        leido.cerrar();
        folios.guardar(leido);

        Folio otraVez = folios.buscarPorReserva(folio.getReserva()).orElseThrow();
        assertEquals(1, otraVez.getPagos().size());
        assertTrue(otraVez.saldo().esCero());
        assertTrue(otraVez.isCerrado());
        assertTrue(folios.buscarPorReserva(new CodigoReserva("RES-2026-99999")).isEmpty());
    }

    @Test
    @DisplayName("Temporada y tarifa: búsqueda por rango y por apartamento y temporada")
    void temporadaYTarifaPersistido() {
        Temporada media = new Temporada(new IdentificacionTemporada("JPA-T-MEDIA"),
                new Periodo(LocalDate.of(2030, 6, 1), LocalDate.of(2030, 6, 30)),
                "Media de mitad de año", TipoTemporada.ALTA, new EstanciaMinima(2));
        temporadas.guardar(media);
        IdentificacionApartamento apto = new IdentificacionApartamento("JPA-APTO-T");
        tarifas.guardar(new Tarifa(new IdentificacionTarifa("JPA-TF-1"), apto, media.getIdentificacion(),
                Dinero.de(120_000)));

        assertEquals(List.of(media.getIdentificacion()),
                temporadas.buscarEntre(LocalDate.of(2030, 6, 30), LocalDate.of(2030, 7, 3)).stream()
                        .map(Temporada::getIdentificacion).toList());
        assertTrue(temporadas.buscarEntre(LocalDate.of(2030, 7, 1), LocalDate.of(2030, 7, 3)).isEmpty());
        assertEquals(new EstanciaMinima(2),
                temporadas.buscarPorId(media.getIdentificacion()).orElseThrow().getEstanciaMinima());

        assertEquals(Dinero.de(120_000), tarifas.buscarPorApartamentoYTemporada(apto, media.getIdentificacion())
                .orElseThrow().getValorPorOcupanteNoche());
        assertEquals(1, tarifas.buscarPorApartamento(apto).size());
    }

    @Test
    @DisplayName("Política de cancelación: cada versión se guarda aparte")
    void politicaPersistido() {
        politicas.guardar(new VersionPolitica(901), POLITICA);

        assertEquals(POLITICA, politicas.buscarPorVersion(new VersionPolitica(901)).orElseThrow());
        assertTrue(politicas.buscarPorVersion(new VersionPolitica(902)).isEmpty());
    }

    @Test
    @DisplayName("Generador: da códigos consecutivos por año guardando el contador en la base")
    void generadorPersistido() {
        assertEquals(new CodigoReserva("RES-2099-00001"), generador.siguiente(2099));
        assertEquals(new CodigoReserva("RES-2099-00002"), generador.siguiente(2099));
        assertEquals(new CodigoReserva("RES-2098-00001"), generador.siguiente(2098));
    }
}
