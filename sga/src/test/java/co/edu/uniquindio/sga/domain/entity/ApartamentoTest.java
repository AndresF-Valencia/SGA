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
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

@DisplayName("Apartamento")
class ApartamentoTest {

    private static final IdentificacionApartamento APTO_101 = new IdentificacionApartamento("APTO-101");
    private static final IdentificacionAlojamiento ALOJAMIENTO = new IdentificacionAlojamiento("SAN-ROCCO");
    private static final TiempoPreparacion PREPARACION = new TiempoPreparacion(3);
    private static final Dormitorio DOS_DORMITORIOS = new Dormitorio(2);
    private static final CapacidadOcupantes CUATRO_PERSONAS = new CapacidadOcupantes(4);
    private static final LimiteMascota DOS_MASCOTAS = new LimiteMascota(2);

    private Apartamento apartamento;

    @BeforeEach
    void setUp() {
        apartamento = crear(APTO_101, "Vista al lago");
    }

    /** Crea un apartamento válido: 2 dormitorios, 4 personas y hasta 2 mascotas. */
    private static Apartamento crear(IdentificacionApartamento identificacion, String nombre) {
        return new Apartamento(identificacion, nombre, PREPARACION, ALOJAMIENTO,
                DOS_DORMITORIOS, CUATRO_PERSONAS, DOS_MASCOTAS);
    }

    // ---------- Creación ----------

    @Test
    @DisplayName("Exige identificación, alojamiento, nombre y tiempo de preparación")
    void validaDatos() {
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                null, "A", PREPARACION, ALOJAMIENTO, DOS_DORMITORIOS, CUATRO_PERSONAS, DOS_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, " ", PREPARACION, ALOJAMIENTO, DOS_DORMITORIOS, CUATRO_PERSONAS, DOS_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, "A", PREPARACION, null, DOS_DORMITORIOS, CUATRO_PERSONAS, DOS_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, "A", null, ALOJAMIENTO, DOS_DORMITORIOS, CUATRO_PERSONAS, DOS_MASCOTAS));
    }

    @Test
    @DisplayName("Exige dormitorios, capacidad y límite de mascotas, con un error del negocio")
    void exigeCantidades() {
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, "A", PREPARACION, ALOJAMIENTO, null, CUATRO_PERSONAS, DOS_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, "A", PREPARACION, ALOJAMIENTO, DOS_DORMITORIOS, null, DOS_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> new Apartamento(
                APTO_101, "A", PREPARACION, ALOJAMIENTO, DOS_DORMITORIOS, CUATRO_PERSONAS, null));
    }

    @Test
    @DisplayName("Necesita al menos un dormitorio, capacidad para una persona y límite de mascotas no negativo")
    void validaCantidades() {
        // TODO: Dormitorio y CapacidadOcupantes lanzan hoy IllegalArgumentException.
        // Cuando pasen a ReglaDominioException, cambiar RuntimeException por
        // ReglaDominioException.
        assertThrows(RuntimeException.class, () -> new Dormitorio(0));
        assertThrows(RuntimeException.class, () -> new CapacidadOcupantes(0));
        assertThrows(ReglaDominioException.class, () -> new LimiteMascota(-1));
    }

    @Test
    @DisplayName("Nace activo y PREPARADO")
    void estadoInicial() {
        assertTrue(apartamento.estaActivo());
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }

    // ---------- Capacidad y mascotas ----------

    @Test
    @DisplayName("RN-02: la capacidad es un tope rígido")
    void respetaCapacidad() {
        assertTrue(apartamento.admite(1));
        assertTrue(apartamento.admite(4), "admite exactamente su capacidad");
        assertFalse(apartamento.admite(5), "no admite ni una persona más");
        assertFalse(apartamento.admite(0), "una reserva sin ocupantes no tiene sentido");
    }

    @Test
    @DisplayName("Admite mascotas hasta su límite")
    void respetaLimiteMascotas() {
        assertTrue(apartamento.admiteMascotas(0));
        assertTrue(apartamento.admiteMascotas(2), "admite exactamente su límite");
        assertFalse(apartamento.admiteMascotas(3), "no admite ni una mascota más");
        assertThrows(ReglaDominioException.class, () -> apartamento.admiteMascotas(-1));
    }

    @Test
    @DisplayName("El administrador puede cambiar el límite de mascotas")
    void cambiaLimiteMascotas() {
        apartamento.cambiarLimiteMascotas(new LimiteMascota(0));

        assertFalse(apartamento.admiteMascotas(1), "ya no admite mascotas");
        assertThrows(ReglaDominioException.class, () -> apartamento.cambiarLimiteMascotas(null));
    }

    // ---------- Estado operativo (7.6) ----------

    @Test
    @DisplayName("7.6: ciclo completo preparado → ocupado → pendiente → en preparación → preparado")
    void cicloCompleto() {
        apartamento.marcarOcupado();
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());

        apartamento.marcarPendientePreparacion();
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION, apartamento.getEstadoOperativo());

        apartamento.marcarEnPreparacion();
        assertEquals(EstadoOperativo.EN_PREPARACION, apartamento.getEstadoOperativo());

        apartamento.marcarPreparado();
        assertEquals(EstadoOperativo.PREPARADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("RN-11: un apartamento a mitad de preparación no recibe un grupo")
    void noOcupaEnPreparacion() {
        apartamento.marcarOcupado();
        apartamento.marcarPendientePreparacion();
        apartamento.marcarEnPreparacion();

        assertFalse(apartamento.puedeRecibirGrupo());
        assertThrows(ReglaDominioException.class, () -> apartamento.marcarOcupado());
        assertEquals(EstadoOperativo.EN_PREPARACION, apartamento.getEstadoOperativo(), "no cambió");
    }

    @Test
    @DisplayName("7.6: un apartamento ocupado no se puede declarar fuera de servicio")
    void ocupadoNoPasaAFueraDeServicio() {
        apartamento.marcarOcupado();

        assertThrows(ReglaDominioException.class, () -> apartamento.marcarFueraDeServicio());
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());
    }

    @Test
    @DisplayName("7.6: fuera de servicio no vuelve directo a preparado; pasa por la preparación")
    void fueraDeServicioVuelvePorPreparacion() {
        apartamento.marcarFueraDeServicio();

        assertFalse(apartamento.puedeRecibirGrupo());
        assertThrows(ReglaDominioException.class, () -> apartamento.marcarPreparado());

        apartamento.marcarPendientePreparacion();
        apartamento.marcarEnPreparacion();
        apartamento.marcarPreparado();
        assertTrue(apartamento.puedeRecibirGrupo());
    }

    @Test
    @DisplayName("Un apartamento preparado no vuelve a prepararse")
    void preparadoNoSeVuelveAPreparar() {
        assertThrows(ReglaDominioException.class, () -> apartamento.marcarPendientePreparacion());
        assertThrows(ReglaDominioException.class, () -> apartamento.marcarEnPreparacion());
    }

    // ---------- Activo ----------

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

    // ---------- Identidad ----------

    @Test
    @DisplayName("Dos apartamentos son el mismo si tienen la misma identificación")
    void igualdadPorIdentificacion() {
        Apartamento mismo = crear(new IdentificacionApartamento("apto-101"), "Otro nombre");
        Apartamento otro = crear(new IdentificacionApartamento("APTO-202"), "Vista al lago");

        assertEquals(apartamento, mismo);
        assertEquals(apartamento.hashCode(), mismo.hashCode());
        assertNotEquals(apartamento, otro);
    }
}