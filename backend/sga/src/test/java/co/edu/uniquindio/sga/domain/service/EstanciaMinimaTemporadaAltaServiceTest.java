package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("EstanciaMinimaTemporadaAltaService (RP-01)")
class EstanciaMinimaTemporadaAltaServiceTest {

    private final EstanciaMinimaTemporadaAltaService servicio = new EstanciaMinimaTemporadaAltaService();

    private static Estancia diciembre(int entrada, int salida) {
        return new Estancia(LocalDate.of(2026, 12, entrada), LocalDate.of(2026, 12, salida));
    }

    @Test
    @DisplayName("En temporada base se puede reservar una sola noche")
    void unaNocheEnBase() {
        Estancia unaNoche = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 2));

        assertDoesNotThrow(() -> servicio.verificarEstanciaMinima(unaNoche, TEMPORADAS));
        assertFalse(servicio.esEstanciaDeTemporadaAlta(unaNoche, TEMPORADAS));
    }

    @Test
    @DisplayName("En temporada alta exige el mínimo de 3 noches")
    void exigeMinimoEnAlta() {
        assertThrows(ReglaDominioException.class,
                () -> servicio.verificarEstanciaMinima(diciembre(20, 22), TEMPORADAS));
        assertDoesNotThrow(() -> servicio.verificarEstanciaMinima(diciembre(20, 23), TEMPORADAS));
    }

    @Test
    @DisplayName("Una estancia que apenas toca la temporada alta también debe cumplir el mínimo")
    void estanciaQueEntraEnAlta() {
        Estancia cruza = diciembre(14, 16);   // 14 base, 15 alta: 2 noches

        assertTrue(servicio.esEstanciaDeTemporadaAlta(cruza, TEMPORADAS));
        assertThrows(ReglaDominioException.class, () -> servicio.verificarEstanciaMinima(cruza, TEMPORADAS));
    }
}
