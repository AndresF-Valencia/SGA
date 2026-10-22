package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Temporada")
class TemporadaTest {

    private static final Periodo DICIEMBRE = new Periodo(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

    private static Temporada alta() {
        return new Temporada(new IdentificacionTemporada("T-1"), DICIEMBRE, "Diciembre",
                TipoTemporada.ALTA, new EstanciaMinima(3));
    }

    @Test
    @DisplayName("Cubre desde el primer hasta el último día del periodo, ambos incluidos")
    void cubreAmbosExtremos() {
        Temporada temporada = alta();

        assertTrue(temporada.cubre(LocalDate.of(2026, 12, 1)));
        assertTrue(temporada.cubre(LocalDate.of(2026, 12, 31)));
        assertFalse(temporada.cubre(LocalDate.of(2027, 1, 1)));
    }

    @Test
    @DisplayName("RP-01: la temporada alta exige su mínimo; la base acepta una sola noche")
    void estanciaMinima() {
        Temporada base = new Temporada(new IdentificacionTemporada("T-2"), DICIEMBRE, "Base",
                TipoTemporada.BASE, null);

        assertFalse(alta().cumpleEstanciaMinima(2));
        assertTrue(alta().cumpleEstanciaMinima(3));
        assertTrue(base.cumpleEstanciaMinima(1));
        assertThrows(ReglaDominioException.class, () -> base.cumpleEstanciaMinima(0));
    }

    @Test
    @DisplayName("Detecta si dos temporadas se cruzan")
    void detectaCruce() {
        Temporada enero = new Temporada(new IdentificacionTemporada("T-3"),
                new Periodo(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 15)), "Enero", TipoTemporada.ALTA, null);
        Temporada febrero = new Temporada(new IdentificacionTemporada("T-4"),
                new Periodo(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28)), "Febrero", TipoTemporada.BASE, null);

        assertTrue(alta().seSolapaCon(enero));
        assertFalse(alta().seSolapaCon(febrero));
    }
}
