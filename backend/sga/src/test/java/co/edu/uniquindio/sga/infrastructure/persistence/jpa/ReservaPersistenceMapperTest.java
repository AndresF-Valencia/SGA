package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

/**
 * Ida y vuelta: Reserva → ReservaEntity → Reserva debe dar la misma reserva.
 * Si un campo se pierde en el camino, se perdería también en la base de datos.
 */
@DisplayName("ReservaPersistenceMapper")
class ReservaPersistenceMapperTest {

    @Test
    @DisplayName("Una reserva recién creada vuelve igual, sin hora ni retraso")
    void reservaPendiente() {
        Reserva original = reserva("RES-1", NOVIEMBRE);

        Reserva copia = idaYVuelta(original);

        mismosDatos(original, copia);
        assertNull(copia.getHoraEstimadaLlegada());
        assertNull(copia.getNotificacionRetraso());
    }

    @Test
    @DisplayName("Una reserva confirmada con retraso conserva estado, hora, retraso e historial")
    void reservaConfirmadaConRetraso() {
        Reserva original = reservaConfirmada("RES-2", NOVIEMBRE);
        original.notificarRetraso(
                NotificacionRetraso.registrar(LocalTime.of(19, 30), "Vuelo demorado", AHORA),
                RECEPCION, AHORA);

        Reserva copia = idaYVuelta(original);

        mismosDatos(original, copia);
        assertEquals(original.getHoraEstimadaLlegada(), copia.getHoraEstimadaLlegada());
        assertEquals(original.getNotificacionRetraso(), copia.getNotificacionRetraso());
    }

    @Test
    @DisplayName("Una reserva cancelada vuelve cancelada y con el motivo en su historial")
    void reservaCancelada() {
        Reserva original = reserva("RES-3", NOVIEMBRE);
        original.cancelar("Cambio de planes", RECEPCION, AHORA);

        mismosDatos(original, idaYVuelta(original));
    }

    private static Reserva idaYVuelta(Reserva reserva) {
        ReservaEntity entity = ReservaPersistenceMapper.aEntity(reserva);
        return ReservaPersistenceMapper.aDominio(entity);
    }

    private static void mismosDatos(Reserva esperada, Reserva real) {
        assertEquals(esperada.getCodigo(), real.getCodigo());
        assertEquals(esperada.getApartamento(), real.getApartamento());
        assertEquals(esperada.getEstancia(), real.getEstancia());
        assertEquals(esperada.getEstado(), real.getEstado());
        assertEquals(esperada.getCanalOrigen(), real.getCanalOrigen());
        assertEquals(esperada.getCreadaEn(), real.getCreadaEn());
        assertEquals(esperada.getTitular().getDocumento(), real.getTitular().getDocumento());
        assertEquals(esperada.getOcupantes(), real.getOcupantes());
        assertEquals(esperada.getValor(), real.getValor());
        assertEquals(esperada.getPolitica(), real.getPolitica());
        assertEquals(esperada.getCantidadMascotas(), real.getCantidadMascotas());
        assertEquals(esperada.obtenerHistorial(), real.obtenerHistorial());
    }
}
