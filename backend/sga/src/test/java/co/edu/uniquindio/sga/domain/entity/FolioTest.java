package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

@DisplayName("Folio")
class FolioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 13, 10, 0);
    private static final CodigoReserva RESERVA = new CodigoReserva("RES-2026-00001");

    private Folio folio;

    @BeforeEach
    void setUp() {
        folio = new Folio(RESERVA, cargo(600_000, TipoCargo.ALOJAMIENTO));
    }

    // ---------- Ayudantes ----------

    private static Cargo cargo(long valor, TipoCargo tipo) {
        return new Cargo(Dinero.de(valor), "Cargo de prueba", tipo, AHORA);
    }

    private static Pago pago(long monto) {
        return new Pago(Dinero.de(monto), MedioPago.EFECTIVO, AHORA);
    }

    private static AutorizacionCierre autorizacion() {
        return new AutorizacionCierre("administrador", "Cortesía al huésped", AHORA);
    }

    // ---------- Apertura (F-08) ----------

    @Test
    @DisplayName("F-08: se abre con el cargo de alojamiento y el saldo es lo que debe el huésped")
    void abreConCargoDeAlojamiento() {
        assertEquals(RESERVA, folio.getReserva());
        assertEquals(1, folio.getCargos().size());
        assertEquals(Dinero.de(600_000), folio.saldo());
        assertTrue(folio.getPagos().isEmpty());
        assertFalse(folio.isCerrado());
        assertNull(folio.getAutorizacionCierre());
    }

    @Test
    @DisplayName("F-08: no se abre sin reserva ni sin cargo de alojamiento")
    void exigeReservaYCargo() {
        Cargo alojamiento = cargo(600_000, TipoCargo.ALOJAMIENTO);

        assertThrows(ReglaDominioException.class, () -> new Folio(null, alojamiento));
        assertThrows(ReglaDominioException.class, () -> new Folio(RESERVA, null));
    }

    @Test
    @DisplayName("F-08: solo se abre con un cargo de tipo alojamiento")
    void soloAbreConAlojamiento() {
        assertThrows(ReglaDominioException.class,
                () -> new Folio(RESERVA, cargo(50_000, TipoCargo.SERVICIO_ADICIONAL)));
        assertThrows(ReglaDominioException.class,
                () -> new Folio(RESERVA, cargo(100_000, TipoCargo.PENALIDAD_NO_SHOW)));
    }

    // ---------- Saldo y pagos (RN-15) ----------

    @Test
    @DisplayName("RN-15: un pago baja el saldo")
    void pagoBajaElSaldo() {
        folio.registrarPago(pago(200_000));

        assertEquals(Dinero.de(400_000), folio.saldo());
        assertEquals(1, folio.getPagos().size());
    }

    @Test
    @DisplayName("RN-15: admite varios pagos parciales con medios distintos")
    void variosPagosParciales() {
        folio.registrarPago(new Pago(Dinero.de(300_000), MedioPago.TARJETA_CREDITO, AHORA));
        folio.registrarPago(new Pago(Dinero.de(300_000), MedioPago.EFECTIVO, AHORA.plusDays(3)));

        assertTrue(folio.saldo().esCero());
        assertEquals(2, folio.getPagos().size());
    }

    @Test
    @DisplayName("RN-15: si paga de más, el huésped queda con saldo a favor")
    void saldoAFavor() {
        folio.registrarPago(pago(650_000));

        assertEquals(Dinero.de(-50_000), folio.saldo());
        assertTrue(folio.saldo().esNegativo());
    }

    @Test
    @DisplayName("RN-15: un servicio adicional sube el saldo")
    void cargoSubeElSaldo() {
        folio.registrarCargo(cargo(50_000, TipoCargo.SERVICIO_ADICIONAL));

        assertEquals(Dinero.de(650_000), folio.saldo());
        assertEquals(2, folio.getCargos().size());
    }

    @Test
    @DisplayName("RN-14: un ajuste negativo baja la cuenta cuando se acorta la estancia")
    void ajusteNegativo() {
        folio.registrarCargo(cargo(-400_000, TipoCargo.AJUSTE));

        assertEquals(Dinero.de(200_000), folio.saldo());
    }

    @Test
    @DisplayName("No registra pagos ni cargos vacíos")
    void rechazaMovimientosNulos() {
        assertThrows(ReglaDominioException.class, () -> folio.registrarPago(null));
        assertThrows(ReglaDominioException.class, () -> folio.registrarCargo(null));
    }

    // ---------- Inmutabilidad de los movimientos (RN-16) ----------

    @Test
    @DisplayName("RN-16: los cargos y pagos no se pueden alterar desde afuera")
    void listasDeSoloLectura() {
        folio.registrarPago(pago(100_000));

        assertThrows(UnsupportedOperationException.class,
                () -> folio.getPagos().add(pago(1)));
        assertThrows(UnsupportedOperationException.class,
                () -> folio.getPagos().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> folio.getCargos().remove(0));
        assertEquals(Dinero.de(500_000), folio.saldo(), "el saldo no cambió");
    }

    // ---------- Cierre (RN-17) ----------

    @Test
    @DisplayName("RN-17: con saldo cero se cierra sin autorización")
    void cierraConSaldoCero() {
        folio.registrarPago(pago(600_000));

        folio.cerrar();

        assertTrue(folio.isCerrado());
        assertNull(folio.getAutorizacionCierre());
    }

    @Test
    @DisplayName("RN-17: con saldo pendiente no se cierra sin autorización")
    void noCierraConSaldoPendiente() {
        folio.registrarPago(pago(580_000));

        assertThrows(ReglaDominioException.class, () -> folio.cerrar());
        assertFalse(folio.isCerrado());
    }

    @Test
    @DisplayName("RN-17: con saldo a favor tampoco se cierra sin autorización")
    void noCierraConSaldoAFavor() {
        folio.registrarPago(pago(700_000));

        assertThrows(ReglaDominioException.class, () -> folio.cerrar());
    }

    @Test
    @DisplayName("RN-17: con autorización se cierra y la autorización queda registrada")
    void cierraConAutorizacion() {
        folio.registrarPago(pago(580_000));
        AutorizacionCierre autorizacion = autorizacion();

        folio.cerrarConAutorizacion(autorizacion);

        assertTrue(folio.isCerrado());
        assertEquals(autorizacion, folio.getAutorizacionCierre());
        assertEquals("administrador", folio.getAutorizacionCierre().autor());
    }

    @Test
    @DisplayName("RN-17: el cierre con autorización exige la autorización")
    void cierreSinAutorizacion() {
        assertThrows(ReglaDominioException.class, () -> folio.cerrarConAutorizacion(null));
        assertFalse(folio.isCerrado());
    }

    @Test
    @DisplayName("Un folio no se cierra dos veces")
    void noCierraDosVeces() {
        folio.registrarPago(pago(600_000));
        folio.cerrar();

        assertThrows(ReglaDominioException.class, () -> folio.cerrar());
        assertThrows(ReglaDominioException.class, () -> folio.cerrarConAutorizacion(autorizacion()));
    }

    @Test
    @DisplayName("Un folio cerrado no admite pagos ni cargos")
    void cerradoNoAdmiteMovimientos() {
        folio.registrarPago(pago(600_000));
        folio.cerrar();

        assertThrows(ReglaDominioException.class, () -> folio.registrarPago(pago(10_000)));
        assertThrows(ReglaDominioException.class,
                () -> folio.registrarCargo(cargo(10_000, TipoCargo.SERVICIO_ADICIONAL)));
        assertTrue(folio.saldo().esCero(), "el saldo quedó igual");
    }
}