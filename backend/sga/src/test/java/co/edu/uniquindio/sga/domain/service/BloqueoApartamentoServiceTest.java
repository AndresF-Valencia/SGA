package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@DisplayName("BloqueoApartamentoService (7.3)")
class BloqueoApartamentoServiceTest {

    private static final IdentificacionBloqueo ID = new IdentificacionBloqueo("B-1");

    private final BloqueoApartamentoService servicio = new BloqueoApartamentoService();

    private static Periodo noviembre(int desde, int hasta) {
        return new Periodo(LocalDate.of(2026, 11, desde), LocalDate.of(2026, 11, hasta));
    }

    @Test
    @DisplayName("Crea el bloqueo VIGENTE si no hay reservas")
    void creaBloqueo() {
        Bloqueo bloqueo = servicio.crearBloqueo(ID, APTO_101, noviembre(1, 5), MotivoBloqueo.MANTENIMIENTO, List.of());

        assertEquals(EstadoBloqueo.VIGENTE, bloqueo.getEstado());
    }

    @Test
    @DisplayName("No bloquea noches de una reserva activa")
    void rechazaConReservaActiva() {
        Reserva activa = reserva("RES-1", NOVIEMBRE);

        assertThrows(ReglaDominioException.class, () -> servicio.crearBloqueo(ID, APTO_101,
                noviembre(3, 10), MotivoBloqueo.REMODELACION, List.of(activa)));
    }

    @Test
    @DisplayName("Sí bloquea si la reserva está cancelada, es de otro apartamento o no toca el periodo")
    void permiteCuandoNoHayChoque() {
        Reserva cancelada = reserva("RES-1", NOVIEMBRE);
        cancelada.cancelar("Desistió", RECEPCION, AHORA);
        Reserva otroApartamento = reserva("RES-2", APTO_102, NOVIEMBRE);
        Reserva activa = reserva("RES-3", NOVIEMBRE);

        // El 4 de noviembre es el día de salida: esa noche no está ocupada
        Bloqueo bloqueo = servicio.crearBloqueo(ID, APTO_101, noviembre(4, 8), MotivoBloqueo.MANTENIMIENTO,
                List.of(cancelada, otroApartamento, activa));

        assertEquals(EstadoBloqueo.VIGENTE, bloqueo.getEstado());
    }
}
