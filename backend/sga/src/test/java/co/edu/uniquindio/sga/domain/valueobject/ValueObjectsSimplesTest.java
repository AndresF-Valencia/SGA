package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Value objects pequeños: identificadores, enums y configuraciones simples.
 */
@DisplayName("Value objects simples")
class ValueObjectsSimplesTest {

    // ---------- CodigoReserva ----------

    @Test
    @DisplayName("CodigoReserva acepta el formato RES-YYYY-NNNNN")
    void codigoValido() {
        assertDoesNotThrow(() -> new CodigoReserva("RES-2026-00001"));
    }

    @Test
    @DisplayName("CodigoReserva rechaza formatos distintos")
    void codigoInvalido() {
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva(null));
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva(" "));
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva("RES-26-00001"));
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva("res-2026-00001"));
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva("RES-2026-000001"));
        assertThrows(ReglaDominioException.class, () -> new CodigoReserva("RES-2026-0001A"));
    }

    // ---------- DocumentoIdentidad ----------

    @Test
    @DisplayName("DocumentoIdentidad es obligatorio y se guarda sin espacios")
    void documentoIdentidad() {
        assertEquals("1094123456", new DocumentoIdentidad("  1094123456 ").numero());
        assertEquals(new DocumentoIdentidad("123"), new DocumentoIdentidad(" 123"));
        assertThrows(ReglaDominioException.class, () -> new DocumentoIdentidad(null));
        assertThrows(ReglaDominioException.class, () -> new DocumentoIdentidad("  "));
    }

    // ---------- IdentificacionApartamento ----------

    @Test
    @DisplayName("IdentificacionApartamento se normaliza a mayúsculas y sin espacios")
    void identificacionApartamento() {
        assertEquals("APTO-101", new IdentificacionApartamento(" apto-101 ").valor());
        assertEquals(new IdentificacionApartamento("APTO-101"),
                new IdentificacionApartamento("apto-101"));
        assertThrows(ReglaDominioException.class, () -> new IdentificacionApartamento(""));
    }

    // ---------- VersionPolitica ----------

    @Test
    @DisplayName("VersionPolitica empieza en 1")
    void versionPolitica() {
        assertDoesNotThrow(() -> new VersionPolitica(1));
        assertThrows(ReglaDominioException.class, () -> new VersionPolitica(0));
    }

    // ---------- UmbralEdadFacturable ----------

    @Test
    @DisplayName("UmbralEdadFacturable: factura desde la edad configurada")
    void umbralEdad() {
        UmbralEdadFacturable umbral = new UmbralEdadFacturable(12);

        assertTrue(umbral.esFacturable(12));
        assertTrue(umbral.esFacturable(30));
        assertFalse(umbral.esFacturable(11));
        assertThrows(ReglaDominioException.class, () -> new UmbralEdadFacturable(-1));
    }

    // ---------- CanalOrigen ----------

    @Test
    @DisplayName("RN-19: solo el canal EXTERNO exige identificador externo")
    void canalOrigen() {
        assertTrue(CanalOrigen.EXTERNO.exigeIdentificadorExterno());
        assertFalse(CanalOrigen.PORTAL.exigeIdentificadorExterno());
        assertFalse(CanalOrigen.DIRECTO.exigeIdentificadorExterno());
    }

    // ---------- EstadoOperativo ----------

    @Test
    @DisplayName("RN-11: solo un apartamento PREPARADO permite registro")
    void estadoOperativo() {
        for (EstadoOperativo estado : EstadoOperativo.values()) {
            assertEquals(estado == EstadoOperativo.PREPARADO, estado.permiteRegistro(),
                    "permiteRegistro para " + estado);
        }
    }

    // ---------- EventoReserva ----------

    @Test
    @DisplayName("EventoReserva exige momento, acción y autor")
    void eventoReserva() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 12, 10, 0);
        EstadoReserva p = EstadoReserva.PENDIENTE;

        assertDoesNotThrow(() -> new EventoReserva(ahora, "CREACION", "Ana", null, p, null));
        assertThrows(ReglaDominioException.class,
                () -> new EventoReserva(null, "CREACION", "Ana", null, p, null));
        assertThrows(ReglaDominioException.class,
                () -> new EventoReserva(ahora, " ", "Ana", null, p, null));
        assertThrows(ReglaDominioException.class,
                () -> new EventoReserva(ahora, "CREACION", "", null, p, null));
    }
}
