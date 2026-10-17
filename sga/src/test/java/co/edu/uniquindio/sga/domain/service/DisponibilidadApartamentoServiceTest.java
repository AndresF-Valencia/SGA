package co.edu.uniquindio.sga.domain.service;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@DisplayName("DisponibilidadApartamentoService")
class DisponibilidadApartamentoServiceTest {

    private static final CantidadMascota SIN_MASCOTAS = new CantidadMascota(0);

    private final DisponibilidadApartamentoService servicio = new DisponibilidadApartamentoService();
    private Apartamento apartamento;
    private Alojamiento alojamiento;

    @BeforeEach
    void setUp() {
        apartamento = apartamento(APTO_101);
        alojamiento = alojamiento();
    }

    private void verificar(Estancia estancia, int ocupantes, CantidadMascota mascotas,
                           List<Reserva> reservas, List<Bloqueo> bloqueos) {
        servicio.verificarDisponibilidad(apartamento, estancia, ocupantes, mascotas, null,
                reservas, bloqueos, alojamiento);
    }

    private static Estancia del(int diaEntrada, int diaSalida) {
        return new Estancia(LocalDate.of(2026, 11, diaEntrada), LocalDate.of(2026, 11, diaSalida));
    }

    @Test
    @DisplayName("Sin reservas ni bloqueos, el apartamento está disponible")
    void disponibleSinReservas() {
        assertDoesNotThrow(() -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(), List.of()));
        assertTrue(servicio.estaDisponible(apartamento, NOVIEMBRE, 2, SIN_MASCOTAS, List.of(), List.of(), alojamiento));
    }

    @Test
    @DisplayName("RN-01: rechaza una estancia que se solapa con una reserva activa")
    void rechazaSolapamiento() {
        Reserva existente = reserva("RES-1", del(3, 6));

        assertThrows(ReglaDominioException.class,
                () -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(existente), List.of()));
        assertFalse(servicio.estaDisponible(apartamento, NOVIEMBRE, 2, SIN_MASCOTAS,
                List.of(existente), List.of(), alojamiento));
    }

    @Test
    @DisplayName("RN-12: una reserva cancelada ya no ocupa las noches")
    void ignoraReservaCancelada() {
        Reserva cancelada = reserva("RES-1", NOVIEMBRE);
        cancelada.cancelar("El huésped desistió", RECEPCION, AHORA);

        assertDoesNotThrow(() -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(cancelada), List.of()));
    }

    @Test
    @DisplayName("Las reservas de otro apartamento no cuentan")
    void ignoraOtroApartamento() {
        Reserva otra = reserva("RES-1", APTO_102, NOVIEMBRE);

        assertDoesNotThrow(() -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(otra), List.of()));
    }

    @Test
    @DisplayName("Al modificar, la reserva no choca consigo misma")
    void excluyeLaReservaQueSeModifica() {
        Reserva propia = reserva("RES-1", NOVIEMBRE);

        assertDoesNotThrow(() -> servicio.verificarDisponibilidad(apartamento, del(2, 5), 2, SIN_MASCOTAS,
                propia.getCodigo(), List.of(propia), List.of(), alojamiento));
    }

    @Test
    @DisplayName("RN-02: rechaza más ocupantes que la capacidad")
    void rechazaCapacidad() {
        assertThrows(ReglaDominioException.class,
                () -> verificar(NOVIEMBRE, 5, SIN_MASCOTAS, List.of(), List.of()));
    }

    @Test
    @DisplayName("Rechaza más mascotas que el límite del apartamento")
    void rechazaMascotas() {
        assertDoesNotThrow(() -> verificar(NOVIEMBRE, 2, new CantidadMascota(1), List.of(), List.of()));
        assertThrows(ReglaDominioException.class,
                () -> verificar(NOVIEMBRE, 2, new CantidadMascota(2), List.of(), List.of()));
    }

    @Test
    @DisplayName("Un apartamento desactivado no está disponible")
    void rechazaApartamentoInactivo() {
        apartamento.desactivar();

        assertThrows(ReglaDominioException.class,
                () -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(), List.of()));
    }

    @Test
    @DisplayName("RN-07: un bloqueo vigente impide la estancia; uno levantado no")
    void respetaBloqueos() {
        Bloqueo bloqueo = new Bloqueo(new IdentificacionBloqueo("B-1"), APTO_101,
                new Periodo(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3)), MotivoBloqueo.MANTENIMIENTO);

        assertThrows(ReglaDominioException.class,
                () -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(), List.of(bloqueo)));

        bloqueo.levantar();
        assertDoesNotThrow(() -> verificar(NOVIEMBRE, 2, SIN_MASCOTAS, List.of(), List.of(bloqueo)));
    }

    @Test
    @DisplayName("RN-20: se puede entrar el día que sale otro grupo si cabe la preparación")
    void permiteEntradaElDiaDeSalidaSiAlcanzaLaPreparacion() {
        // Sale a las 11:00, entra a las 15:00: caben las 3 horas de preparación
        Reserva anterior = reserva("RES-1", del(1, 4));

        assertDoesNotThrow(() -> verificar(del(4, 6), 2, SIN_MASCOTAS, List.of(anterior), List.of()));
    }

    @Test
    @DisplayName("RN-20: rechaza la entrada el día de salida si no alcanza la preparación")
    void rechazaEntradaSinTiempoDePreparacion() {
        // Sale a las 13:00 y entra a las 15:00: solo hay 2 horas para 3 de preparación
        alojamiento = alojamiento(LocalTime.of(15, 0), LocalTime.of(13, 0), true, 50);
        Reserva anterior = reserva("RES-1", del(1, 4));
        Reserva siguiente = reserva("RES-2", del(8, 10));

        assertThrows(ReglaDominioException.class,
                () -> verificar(del(4, 6), 2, SIN_MASCOTAS, List.of(anterior), List.of()));
        // También aplica al revés: salir el mismo día en que entra el siguiente grupo
        assertThrows(ReglaDominioException.class,
                () -> verificar(del(6, 8), 2, SIN_MASCOTAS, List.of(siguiente), List.of()));
        // Con un día de por medio no hay problema
        assertDoesNotThrow(() -> verificar(del(5, 7), 2, SIN_MASCOTAS, List.of(anterior, siguiente), List.of()));
    }
}
