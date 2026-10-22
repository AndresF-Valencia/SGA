package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@DisplayName("CotizadorEstanciaService")
class CotizadorEstanciaServiceTest {

    private static final UmbralEdadFacturable TRES_ANIOS = new UmbralEdadFacturable(3);

    private final CotizadorEstanciaService servicio = new CotizadorEstanciaService();

    @Test
    @DisplayName("RN-05: 3 noches de temporada base para 2 adultos")
    void cotizaTemporadaBase() {
        ValorCongelado valor = servicio.cotizar(APTO_101, NOVIEMBRE, List.of(LAURA, PEDRO),
                TRES_ANIOS, TEMPORADAS, TARIFAS);

        assertEquals(3, valor.noches());
        assertEquals(Dinero.de(600_000), valor.total());   // 100.000 × 2 × 3
    }

    @Test
    @DisplayName("RN-06: un niño por debajo del umbral ocupa cupo pero no paga")
    void ninoNoPaga() {
        ValorCongelado valor = servicio.cotizar(APTO_101, NOVIEMBRE,
                List.of(LAURA, PEDRO, nino("1094000003", "Sofía Gómez")), TRES_ANIOS, TEMPORADAS, TARIFAS);

        assertEquals(Dinero.de(600_000), valor.total());
    }

    @Test
    @DisplayName("RN-05: si la estancia cruza temporadas, cada noche usa su tarifa")
    void cruzaTemporadas() {
        // 13 y 14 de diciembre en base, 15 y 16 en alta
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 17));

        ValorCongelado valor = servicio.cotizar(APTO_101, estancia, List.of(LAURA, PEDRO),
                TRES_ANIOS, TEMPORADAS, TARIFAS);

        List<CargoNoche> detalle = valor.detalle();
        assertEquals(Dinero.de(200_000), detalle.get(1).subtotal());   // 14: base
        assertEquals(Dinero.de(300_000), detalle.get(2).subtotal());   // 15: alta
        assertEquals(Dinero.de(1_000_000), valor.total());
    }

    @Test
    @DisplayName("El último día de una temporada sí tiene tarifa")
    void ultimoDiaDeLaTemporadaCuenta() {
        // La temporada base termina el 14 de diciembre; esa noche es base
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 15));

        ValorCongelado valor = servicio.cotizar(APTO_101, estancia, List.of(LAURA), TRES_ANIOS, TEMPORADAS, TARIFAS);

        assertEquals(Dinero.de(100_000), valor.total());
    }

    @Test
    @DisplayName("Rechaza noches sin temporada o sin tarifa configurada")
    void rechazaSinTemporadaOTarifa() {
        Estancia sinTemporada = new Estancia(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 3));

        assertThrows(ReglaDominioException.class, () -> servicio.cotizar(APTO_101, sinTemporada,
                List.of(LAURA), TRES_ANIOS, TEMPORADAS, TARIFAS));
        assertThrows(ReglaDominioException.class, () -> servicio.cotizar(APTO_102, NOVIEMBRE,
                List.of(LAURA), TRES_ANIOS, TEMPORADAS, TARIFAS));
    }

    @Test
    @DisplayName("Rechaza una estancia sin ningún ocupante facturable")
    void rechazaSinFacturables() {
        assertThrows(ReglaDominioException.class, () -> servicio.cotizar(APTO_101, NOVIEMBRE,
                List.of(nino("1094000003", "Sofía Gómez")), TRES_ANIOS, TEMPORADAS, TARIFAS));
    }
}
