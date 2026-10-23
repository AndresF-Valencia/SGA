package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

/**
 * Las pruebas usan las interfaces del dominio como tipo: así se comprueba que
 * el adaptador cumple el contrato del puerto, que es lo que verán los servicios.
 */
@DisplayName("Repositorios en memoria")
class RepositoriosEnMemoriaTest {

    @Test
    @DisplayName("Reserva: guarda, busca por código y reemplaza al guardar de nuevo")
    void reservaGuardaYBusca() {
        ReservaRepository repositorio = new ReservaRepositoryEnMemoria();
        Reserva reserva = reserva("RES-1", NOVIEMBRE);

        repositorio.guardar(reserva);
        repositorio.guardar(reserva);

        assertSame(reserva, repositorio.buscarPorCodigo(reserva.getCodigo()).orElseThrow());
        assertEquals(1, repositorio.obtenerTodas().size());
        assertTrue(repositorio.buscarPorCodigo(codigo("RES-99")).isEmpty());
        assertTrue(repositorio.buscarPorCodigo(null).isEmpty());
    }

    @Test
    @DisplayName("Reserva: las activas son las del apartamento que retienen noches")
    void reservaActivasYPendientes() {
        ReservaRepository repositorio = new ReservaRepositoryEnMemoria();
        Reserva pendiente = reserva("RES-1", NOVIEMBRE);
        Reserva confirmada = reservaConfirmada("RES-2", new Estancia(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12)));
        Reserva cancelada = reserva("RES-3", NOVIEMBRE);
        cancelada.cancelar("Desistió", RECEPCION, AHORA);
        Reserva otroApartamento = reserva("RES-4", APTO_102, NOVIEMBRE);
        List.of(pendiente, confirmada, cancelada, otroApartamento).forEach(repositorio::guardar);

        assertEquals(List.of(pendiente, confirmada), repositorio.buscarActivasPorApartamento(APTO_101));
        assertEquals(List.of(pendiente, otroApartamento), repositorio.buscarPendientes());
    }

    @Test
    @DisplayName("Apartamento: listarActivos no incluye los desactivados")
    void apartamentosActivos() {
        ApartamentoRepository repositorio = new ApartamentoRepositoryEnMemoria();
        Apartamento activo = apartamento(APTO_101);
        Apartamento inactivo = apartamento(APTO_102);
        inactivo.desactivar();
        repositorio.guardar(activo);
        repositorio.guardar(inactivo);

        assertEquals(List.of(activo), repositorio.listarActivos());
        assertEquals(2, repositorio.listar().size());
    }

    @Test
    @DisplayName("Folio: se busca por el código de su reserva")
    void folioPorReserva() {
        FolioRepository repositorio = new FolioRepositoryEnMemoria();
        Folio folio = folio(reserva("RES-1", NOVIEMBRE));
        repositorio.guardar(folio);

        assertSame(folio, repositorio.buscarPorReserva(codigo("RES-1")).orElseThrow());
        assertTrue(repositorio.buscarPorReserva(codigo("RES-2")).isEmpty());
    }

    @Test
    @DisplayName("Bloqueo: separa los vigentes de los levantados")
    void bloqueosVigentes() {
        BloqueoRepository repositorio = new BloqueoRepositoryEnMemoria();
        Periodo periodo = new Periodo(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));
        Bloqueo vigente = new Bloqueo(new IdentificacionBloqueo("B-1"), APTO_101, periodo, MotivoBloqueo.MANTENIMIENTO);
        Bloqueo levantado = new Bloqueo(new IdentificacionBloqueo("B-2"), APTO_101, periodo, MotivoBloqueo.REMODELACION);
        levantado.levantar();
        repositorio.guardar(vigente);
        repositorio.guardar(levantado);

        assertEquals(List.of(vigente), repositorio.buscarVigentesPorApartamento(APTO_101));
        assertEquals(2, repositorio.buscarPorApartamento(APTO_101).size());
        assertTrue(repositorio.buscarPorApartamento(APTO_102).isEmpty());
    }

    @Test
    @DisplayName("Temporada: busca las que tocan un rango de fechas")
    void temporadasEntreFechas() {
        TemporadaRepository repositorio = new TemporadaRepositoryEnMemoria();
        TEMPORADAS.forEach(repositorio::guardar);

        assertEquals(List.of(BASE), repositorio.buscarEntre(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4)));
        assertEquals(TEMPORADAS, repositorio.buscarEntre(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 15)));
        assertTrue(repositorio.buscarEntre(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 5)).isEmpty());
    }

    @Test
    @DisplayName("Tarifa: busca por apartamento y por apartamento y temporada")
    void tarifas() {
        TarifaRepository repositorio = new TarifaRepositoryEnMemoria();
        TARIFAS.forEach(repositorio::guardar);

        assertEquals(2, repositorio.buscarPorApartamento(APTO_101).size());
        Tarifa alta = repositorio.buscarPorApartamentoYTemporada(APTO_101, ALTA.getIdentificacion()).orElseThrow();
        assertEquals(ALTA.getIdentificacion(), alta.getTemporada());
        assertTrue(repositorio.buscarPorApartamento(APTO_102).isEmpty());
    }

    @Test
    @DisplayName("Canal: se encuentra por su credencial y se filtran los inactivos")
    void canales() {
        CanalRepository repositorio = new CanalRepositoryEnMemoria();
        Canal booking = new Canal(new IdentificacionCanal("C-1"), CanalOrigen.EXTERNO, "Booking", "clave-booking");
        Canal inactivo = new Canal(new IdentificacionCanal("C-2"), CanalOrigen.EXTERNO, "Otro", "clave-otro");
        inactivo.desactivar();
        repositorio.guardar(booking);
        repositorio.guardar(inactivo);

        assertSame(booking, repositorio.buscarPorCredencial("clave-booking").orElseThrow());
        assertFalse(repositorio.buscarPorCredencial("clave-falsa").isPresent());
        assertEquals(List.of(booking), repositorio.listarActivos());
    }

    @Test
    @DisplayName("No se puede guardar un valor nulo")
    void rechazaNulos() {
        assertThrows(IllegalArgumentException.class, () -> new ReservaRepositoryEnMemoria().guardar(null));
    }
}
