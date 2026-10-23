package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

@DisplayName("Ocupante")
class OcupanteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 12);
    private static final DocumentoIdentidad DOC = new DocumentoIdentidad("1094000001");
    private static final UmbralEdadFacturable DOCE_ANIOS = new UmbralEdadFacturable(12);

    private static Ocupante nacidoEl(LocalDate fechaNacimiento) {
        return new Ocupante(DOC, "Laura Gómez", fechaNacimiento, HOY);
    }

    @Test
    @DisplayName("Exige documento, nombre y fecha de nacimiento")
    void validaDatos() {
        LocalDate nacimiento = LocalDate.of(1990, 5, 1);

        assertThrows(ReglaDominioException.class, () -> new Ocupante(null, "Laura", nacimiento, HOY));
        assertThrows(ReglaDominioException.class, () -> new Ocupante(DOC, " ", nacimiento, HOY));
        assertThrows(ReglaDominioException.class, () -> new Ocupante(DOC, "Laura", null, HOY));
    }

    @Test
    @DisplayName("La fecha de nacimiento no puede ser futura")
    void nacimientoFuturo() {
        assertThrows(ReglaDominioException.class, () -> nacidoEl(HOY.plusDays(1)));
    }

    @Test
    @DisplayName("Calcula la edad a una fecha dada")
    void calculaEdad() {
        Ocupante ocupante = nacidoEl(LocalDate.of(2000, 3, 15));

        assertEquals(26, ocupante.edadA(LocalDate.of(2026, 3, 15)));
        assertEquals(25, ocupante.edadA(LocalDate.of(2026, 3, 14)), "un día antes del cumpleaños");
    }

    @Test
    @DisplayName("RN-06: es facturable si alcanza el umbral a la fecha de entrada")
    void facturablePorEdad() {
        Estancia estancia = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4));

        assertTrue(nacidoEl(LocalDate.of(1990, 1, 1)).esFacturableEn(estancia, DOCE_ANIOS));
        assertTrue(nacidoEl(LocalDate.of(2014, 11, 1)).esFacturableEn(estancia, DOCE_ANIOS),
                "cumple 12 justo el día de entrada");
        assertFalse(nacidoEl(LocalDate.of(2020, 6, 1)).esFacturableEn(estancia, DOCE_ANIOS));
    }

    @Test
    @DisplayName("RN-06: quien cumple años durante la estancia no cambia de condición")
    void cumpleaniosDuranteLaEstancia() {
        Estancia estancia = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));
        Ocupante cumpleDoceEnLaEstancia = nacidoEl(LocalDate.of(2014, 11, 3));

        assertFalse(cumpleDoceEnLaEstancia.esFacturableEn(estancia, DOCE_ANIOS));
    }

    @Test
    @DisplayName("Puede cambiar de nombre, pero no a uno vacío")
    void actualizaNombre() {
        Ocupante ocupante = nacidoEl(LocalDate.of(1990, 1, 1));

        ocupante.actualizarNombre("Laura María Gómez");

        assertEquals("Laura María Gómez", ocupante.getNombre());
        assertThrows(ReglaDominioException.class, () -> ocupante.actualizarNombre(" "));
    }

    @Test
    @DisplayName("Dos ocupantes son el mismo si tienen el mismo documento")
    void igualdadPorDocumento() {
        LocalDate nacimiento = LocalDate.of(1990, 1, 1);
        Ocupante a = new Ocupante(new DocumentoIdentidad("123"), "Ana", nacimiento, HOY);
        Ocupante mismoDocumento = new Ocupante(new DocumentoIdentidad("123"), "Ana María", nacimiento, HOY);
        Ocupante otro = new Ocupante(new DocumentoIdentidad("456"), "Ana", nacimiento, HOY);

        assertEquals(a, mismoDocumento);
        assertEquals(a.hashCode(), mismoDocumento.hashCode());
        assertNotEquals(a, otro);
    }
}
