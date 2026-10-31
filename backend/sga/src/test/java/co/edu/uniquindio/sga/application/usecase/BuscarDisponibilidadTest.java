package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase.Criterios;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase.Resultado;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.AlojamientoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ApartamentoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.BloqueoRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TarifaRepositoryEnMemoria;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.TemporadaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Búsqueda de disponibilidad con filtros")
class BuscarDisponibilidadTest {

    /** Sin tarifa registrada: se lista, pero sin precio. */
    private static final IdentificacionApartamento APTO_103 = new IdentificacionApartamento("APTO-103");

    private ConsultarDisponibilidadUseCase disponibilidad;

    @BeforeEach
    void preparar() {
        var alojamientos = new AlojamientoRepositoryEnMemoria();
        alojamientos.guardar(alojamiento());

        var apartamentos = new ApartamentoRepositoryEnMemoria();
        apartamentos.guardar(apartamento(APTO_101));   // 4 personas, Wifi y Balcón
        apartamentos.guardar(new Apartamento(APTO_102, "Suite", TiempoPreparacion.estandar(), SAN_ROCCO,
                new Dormitorio(1), new CapacidadOcupantes(2), new LimiteMascota(0),
                List.of("Wifi", "Jacuzzi"), List.of()));
        apartamentos.guardar(apartamento(APTO_103));

        var temporadas = new TemporadaRepositoryEnMemoria();
        TEMPORADAS.forEach(temporadas::guardar);

        var tarifas = new TarifaRepositoryEnMemoria();
        TARIFAS.forEach(tarifas::guardar);   // APTO-101: 100.000 por persona y noche en temporada base
        tarifas.guardar(new Tarifa(new IdentificacionTarifa("TF-9"), APTO_102, BASE.getIdentificacion(),
                Dinero.de(200_000)));

        disponibilidad = new ConsultarDisponibilidadUseCase(apartamentos, alojamientos,
                new ReservaRepositoryEnMemoria(), new BloqueoRepositoryEnMemoria(),
                new DisponibilidadApartamentoService(), temporadas, tarifas, new CotizadorEstanciaService());
    }

    private List<Resultado> buscar(int personas, Integer capacidadMinima, Long precioMin, Long precioMax,
                                   List<String> caracteristicas) {
        return disponibilidad.buscar(new Criterios(NOVIEMBRE.fechaEntrada(), NOVIEMBRE.fechaSalida(),
                personas, new CantidadMascota(0), capacidadMinima, precioMin, precioMax, caracteristicas));
    }

    private static List<String> ids(List<Resultado> resultados) {
        return resultados.stream().map(r -> r.apartamento().getIdentificacion().valor()).toList();
    }

    @Test
    @DisplayName("sin filtros lista los disponibles, con la tarifa estimada por noche del grupo")
    void sinFiltros() {
        List<Resultado> resultados = buscar(2, null, null, null, List.of());

        assertEquals(List.of("APTO-101", "APTO-102", "APTO-103"), ids(resultados));
        assertEquals(200_000L, resultados.get(0).tarifaPorNoche());   // 2 personas x 100.000
        assertEquals(400_000L, resultados.get(1).tarifaPorNoche());   // 2 personas x 200.000
        assertEquals(null, resultados.get(2).tarifaPorNoche());
    }

    @Test
    @DisplayName("el tamaño del grupo descarta los apartamentos que no lo admiten")
    void porPersonas() {
        assertEquals(List.of("APTO-101", "APTO-103"), ids(buscar(3, null, null, null, List.of())));
    }

    @Test
    @DisplayName("capacidad mínima")
    void porCapacidad() {
        assertEquals(List.of("APTO-101", "APTO-103"), ids(buscar(2, 4, null, null, List.of())));
    }

    @Test
    @DisplayName("rango de precio por noche; sin tarifa no cumple un filtro de precio")
    void porPrecio() {
        assertEquals(List.of("APTO-101"), ids(buscar(2, null, null, 300_000L, List.of())));
        assertEquals(List.of("APTO-102"), ids(buscar(2, null, 300_000L, null, List.of())));
    }

    @Test
    @DisplayName("rechaza un grupo sin personas y un rango de precio invertido")
    void parametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> buscar(0, null, null, null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> buscar(-1, null, null, null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> buscar(2, null, 300_000L, 100_000L, List.of()));
    }

    @Test
    @DisplayName("características: debe tenerlas todas, sin distinguir mayúsculas")
    void porCaracteristicas() {
        assertEquals(List.of("APTO-102"), ids(buscar(2, null, null, null, List.of("jacuzzi"))));
        assertEquals(List.of("APTO-101", "APTO-102", "APTO-103"),
                ids(buscar(2, null, null, null, List.of("WIFI"))));
        assertEquals(List.of(), ids(buscar(2, null, null, null, List.of("wifi", "jacuzzi", "balcón"))));
    }
}
