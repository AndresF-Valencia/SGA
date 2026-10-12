package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

@DisplayName("Apartamento")
class ApartamentoTest {

    private static final IdentificacionApartamento APTO_101 = new IdentificacionApartamento("APTO-101");

    private Apartamento apartamento;

    @BeforeEach
    void setUp() {
        apartamento = new Apartamento(APTO_101, "Vista al lago", 2, 4);
    }

    @Test
    @DisplayName("Valida identificación, nombre, dormitorios y capacidad")
    void validaDatos() {
        assertThrows(ReglaDominioException.class, () -> new Apartamento(null, "A", 1, 1));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(APTO_101, " ", 1, 1));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(APTO_101, "A", 0, 1));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(APTO_101, "A", 1, 0));
    }

    @Test
    @DisplayName("Nace activo y PREPARADO")
    void estadoInicial() {
        assertTrue(apartamento.estaActivo());
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-02: la capacidad es un tope rígido")
    void respetaCapacidad() {
        assertTrue(apartamento.admite(1));
        assertTrue(apartamento.admite(4), "admite exactamente su capacidad");
        assertFalse(apartamento.admite(5), "no admite ni una persona más");
        assertFalse(apartamento.admite(0), "una reserva sin ocupantes no tiene sentido");
    }

    @Test
    @DisplayName("RN-11: un apartamento activo y PREPARADO puede recibir un grupo")
    void puedeRecibirGrupo() {
        assertTrue(apartamento.puedeRecibirGrupo());
    }

    @Test
    @DisplayName("Un apartamento desactivado ya no recibe grupos")
    void desactivado() {
        apartamento.desactivar();

        assertFalse(apartamento.estaActivo());
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    @DisplayName("Dos apartamentos son el mismo si tienen la misma identificación")
    void igualdadPorIdentificacion() {
        Apartamento mismo = new Apartamento(new IdentificacionApartamento("apto-101"), "Otro nombre", 3, 6);
        Apartamento otro = new Apartamento(new IdentificacionApartamento("APTO-202"), "Vista al lago", 2, 4);

        assertEquals(apartamento, mismo);
        assertEquals(apartamento.hashCode(), mismo.hashCode());
        assertNotEquals(apartamento, otro);
    }
}
