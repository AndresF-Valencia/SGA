package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase.Filtros;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@DisplayName("Listado de reservas con filtros")
class ConsultarReservasUseCaseTest {

    private static final Estancia DICIEMBRE = new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4));

    private ConsultarReservasUseCase consultar;

    /** Reserva creada {@code dias} días después de AHORA, así el orden por creación es controlable. */
    private static Reserva reserva(String codigo, IdentificacionApartamento apto, Estancia estancia,
                                   Ocupante titular, CanalOrigen canal, int dias) {
        return Reserva.crear(codigo(codigo), apto, estancia, titular, List.of(titular), canal,
                valorBase(estancia, 1), new VersionPolitica(1), AHORA.plusDays(dias), new CantidadMascota(0));
    }

    private static Filtros sinFiltros() {
        return new Filtros(null, null, null, null, null, null);
    }

    private List<String> codigos(Filtros filtros) {
        return consultar.ejecutar(filtros).stream().map(r -> r.getCodigo().valor()).toList();
    }

    @BeforeEach
    void preparar() {
        var reservas = new ReservaRepositoryEnMemoria();
        reservas.guardar(reserva("RES-1", APTO_101, NOVIEMBRE, LAURA, CanalOrigen.PORTAL, 0));
        reservas.guardar(reserva("RES-2", APTO_102, DICIEMBRE, PEDRO, CanalOrigen.DIRECTO, 2));
        Reserva cancelada = reserva("RES-3", APTO_101, DICIEMBRE, LAURA, CanalOrigen.PORTAL, 1);
        cancelada.cancelar("cambio de planes", RECEPCION, AHORA.plusDays(1));
        reservas.guardar(cancelada);
        consultar = new ConsultarReservasUseCase(reservas);
    }

    @Test
    @DisplayName("ordena de la más reciente a la más antigua")
    void orden() {
        assertEquals(List.of("RES-2026-00002", "RES-2026-00003", "RES-2026-00001"), codigos(sinFiltros()));
    }

    @Test
    @DisplayName("filtra por estado")
    void porEstado() {
        assertEquals(List.of("RES-2026-00003"),
                codigos(new Filtros(EstadoReserva.CANCELADA, null, null, null, null, null)));
    }

    @Test
    @DisplayName("filtra por apartamento y por canal")
    void porApartamentoYCanal() {
        assertEquals(List.of("RES-2026-00002"),
                codigos(new Filtros(null, APTO_102, null, null, null, null)));
        assertEquals(List.of("RES-2026-00003", "RES-2026-00001"),
                codigos(new Filtros(null, null, CanalOrigen.PORTAL, null, null, null)));
    }

    @Test
    @DisplayName("filtra por rango de fecha de entrada, extremos incluidos")
    void porFechaEntrada() {
        assertEquals(List.of("RES-2026-00001"),
                codigos(new Filtros(null, null, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null)));
        assertEquals(List.of("RES-2026-00002", "RES-2026-00003"),
                codigos(new Filtros(null, null, null, LocalDate.of(2026, 12, 1), null, null)));
    }

    @Test
    @DisplayName("filtra por titular: es lo que usa 'mis reservas'")
    void porTitular() {
        assertEquals(List.of("RES-2026-00003", "RES-2026-00001"),
                codigos(new Filtros(null, null, null, null, null, LAURA.getDocumento())));
        assertEquals(List.of("RES-2026-00002"),
                codigos(new Filtros(null, null, null, null, null, PEDRO.getDocumento())));
    }
}
