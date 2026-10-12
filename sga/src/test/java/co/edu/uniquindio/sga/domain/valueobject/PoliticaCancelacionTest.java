package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("PoliticaCancelacion y TramoCancelacion")
class PoliticaCancelacionTest {

    private static final Estancia ESTANCIA =
            new Estancia(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23));
    private static final Dinero VALOR = Dinero.de(1_000_000);

    private static final TramoCancelacion SIN_DEVOLUCION = new TramoCancelacion(0, 6, 0);
    private static final TramoCancelacion MITAD = new TramoCancelacion(7, 29, 50);
    private static final TramoCancelacion TOTAL = new TramoCancelacion(30, 365, 100);

    private PoliticaCancelacion politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaCancelacion("Estándar", List.of(SIN_DEVOLUCION, MITAD, TOTAL));
    }

    /** Momento de cancelación a cierta cantidad de días de la entrada. */
    private static LocalDateTime diasAntes(int dias) {
        return ESTANCIA.fechaEntrada().minusDays(dias).atTime(10, 0);
    }

    // ---------- TramoCancelacion ----------

    @Test
    @DisplayName("El tramo valida sus límites y su porcentaje")
    void validaTramo() {
        assertThrows(ReglaDominioException.class, () -> new TramoCancelacion(-1, 5, 50));
        assertThrows(ReglaDominioException.class, () -> new TramoCancelacion(10, 5, 50));
        assertThrows(ReglaDominioException.class, () -> new TramoCancelacion(0, 5, -1));
        assertThrows(ReglaDominioException.class, () -> new TramoCancelacion(0, 5, 101));
    }

    @Test
    @DisplayName("Los límites del tramo son inclusivos")
    void limitesInclusivos() {
        assertTrue(MITAD.aplicaPara(diasAntes(7), ESTANCIA));
        assertTrue(MITAD.aplicaPara(diasAntes(29), ESTANCIA));
        assertFalse(MITAD.aplicaPara(diasAntes(6), ESTANCIA));
        assertFalse(MITAD.aplicaPara(diasAntes(30), ESTANCIA));
    }

    @Test
    @DisplayName("Un tramo no aplica sin momento o sin estancia")
    void tramoSinDatos() {
        assertFalse(MITAD.aplicaPara(null, ESTANCIA));
        assertFalse(MITAD.aplicaPara(diasAntes(10), null));
    }

    // ---------- PoliticaCancelacion ----------

    @Test
    @DisplayName("La política exige nombre y al menos un tramo")
    void validaPolitica() {
        assertThrows(ReglaDominioException.class,
                () -> new PoliticaCancelacion(" ", List.of(TOTAL)));
        assertThrows(ReglaDominioException.class,
                () -> new PoliticaCancelacion("Estándar", List.of()));
        assertThrows(ReglaDominioException.class,
                () -> new PoliticaCancelacion("Estándar", null));
    }

    @Test
    @DisplayName("Con mucha anticipación se devuelve todo")
    void devolucionTotal() {
        ResultadoCancelacion resultado = politica.evaluar(diasAntes(40), ESTANCIA, VALOR);

        assertEquals(TOTAL, resultado.tramoAplicado());
        assertTrue(resultado.penalidad().esCero());
        assertEquals(VALOR, resultado.reembolso());
    }

    @Test
    @DisplayName("En el tramo intermedio se retiene la mitad")
    void devolucionParcial() {
        ResultadoCancelacion resultado = politica.evaluar(diasAntes(10), ESTANCIA, VALOR);

        assertEquals(MITAD, resultado.tramoAplicado());
        assertEquals(Dinero.de(500_000), resultado.penalidad());
        assertEquals(Dinero.de(500_000), resultado.reembolso());
    }

    @Test
    @DisplayName("Cerca de la entrada no se devuelve nada")
    void sinDevolucion() {
        ResultadoCancelacion resultado = politica.evaluar(diasAntes(2), ESTANCIA, VALOR);

        assertEquals(VALOR, resultado.penalidad());
        assertTrue(resultado.reembolso().esCero());
    }

    @Test
    @DisplayName("Penalidad más reembolso siempre dan el valor total")
    void penalidadMasReembolsoEsElTotal() {
        Dinero valorImpar = Dinero.de(333_333);
        for (int dias : new int[] {0, 6, 7, 15, 29, 30, 100}) {
            ResultadoCancelacion r = politica.evaluar(diasAntes(dias), ESTANCIA, valorImpar);
            assertEquals(valorImpar, r.penalidad().mas(r.reembolso()), "a " + dias + " días");
        }
    }

    @Test
    @DisplayName("Falla si ningún tramo cubre la anticipación")
    void sinTramoAplicable() {
        // Cancelar después de la fecha de entrada da anticipación negativa
        LocalDateTime despuesDeEntrar = ESTANCIA.fechaEntrada().plusDays(1).atTime(10, 0);

        assertThrows(ReglaDominioException.class,
                () -> politica.evaluar(despuesDeEntrar, ESTANCIA, VALOR));
    }

    @Test
    @DisplayName("La evaluación valida sus datos de entrada")
    void validaEvaluacion() {
        assertThrows(ReglaDominioException.class, () -> politica.evaluar(null, ESTANCIA, VALOR));
        assertThrows(ReglaDominioException.class, () -> politica.evaluar(diasAntes(10), null, VALOR));
        assertThrows(ReglaDominioException.class,
                () -> politica.evaluar(diasAntes(10), ESTANCIA, Dinero.de(-1)));
    }

    @Test
    @DisplayName("Los tramos no se pueden cambiar después de crear la política")
    void tramosInmutables() {
        List<TramoCancelacion> tramos = new ArrayList<>(List.of(TOTAL));
        PoliticaCancelacion p = new PoliticaCancelacion("Flexible", tramos);

        tramos.add(SIN_DEVOLUCION);

        assertEquals(1, p.tramos().size());
        assertThrows(UnsupportedOperationException.class, () -> p.tramos().add(MITAD));
    }
}
