package co.edu.uniquindio.sga.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.EventoCanalRepositoryEnMemoria;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Bitácora de eventos de los canales (7.8)")
class RegistrarEventoCanalUseCaseTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 10, 0);

    private RegistrarEventoCanalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarEventoCanalUseCase(new EventoCanalRepositoryEnMemoria());
    }

    @Test
    @DisplayName("Guarda el evento y lo devuelve en la consulta")
    void guardaYLista() {
        useCase.registrar("Booking", EventoCanal.RESERVA_EXTERNA, "BKG-1", true, "AP001: reserva creada", AHORA);

        List<EventoCanal> eventos = useCase.listar();

        assertEquals(1, eventos.size());
        assertEquals("Booking", eventos.get(0).getCanal());
        assertEquals(EventoCanal.RESERVA_EXTERNA, eventos.get(0).getOperacion());
        assertEquals("BKG-1", eventos.get(0).getCodigoExterno());
        assertTrue(eventos.get(0).fueExitoso());
    }

    @Test
    @DisplayName("Lista del más reciente al más antiguo")
    void ordenaPorFecha() {
        useCase.registrar("Booking", EventoCanal.CONSULTA_DISPONIBILIDAD, null, true, "primera", AHORA);
        useCase.registrar("Booking", EventoCanal.CANCELACION_EXTERNA, "BKG-1", false, "segunda", AHORA.plusMinutes(5));

        List<EventoCanal> eventos = useCase.listar();

        assertEquals("segunda", eventos.get(0).getDetalle());
        assertFalse(eventos.get(0).fueExitoso());
        assertEquals("primera", eventos.get(1).getDetalle());
    }

    @Test
    @DisplayName("Una credencial que no es de ningún canal queda como 'Sin identificar'")
    void canalSinIdentificar() {
        EventoCanal evento = useCase.registrar(null, EventoCanal.RESERVA_EXTERNA, "BKG-9", false,
                "Credencial de canal inválida", AHORA);

        assertEquals(RegistrarEventoCanalUseCase.CANAL_SIN_IDENTIFICAR, evento.getCanal());
    }

    @Test
    @DisplayName("El detalle se recorta a 500 caracteres y la operación es obligatoria")
    void validaElEvento() {
        EventoCanal largo = useCase.registrar("Booking", EventoCanal.RESERVA_EXTERNA, "BKG-2", false,
                "x".repeat(900), AHORA);

        assertEquals(500, largo.getDetalle().length());
        assertThrows(ReglaDominioException.class,
                () -> useCase.registrar("Booking", " ", null, true, "sin operación", AHORA));
    }
}
