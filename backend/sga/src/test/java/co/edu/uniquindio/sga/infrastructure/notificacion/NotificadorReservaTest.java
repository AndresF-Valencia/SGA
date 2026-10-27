package co.edu.uniquindio.sga.infrastructure.notificacion;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Pruebas del notificador SIN servidor de correo real (I-08): el MailSender es
 * falso y el envío corre en el mismo hilo para poder revisarlo enseguida.
 */
@DisplayName("Notificaciones al titular (10.3.2)")
class NotificadorReservaTest {

    private static final Executor MISMO_HILO = Runnable::run;

    /** MailSender falso que guarda los correos en una lista, o falla si se le pide. */
    private static class BuzonFalso implements MailSender {
        final List<SimpleMailMessage> enviados = new ArrayList<>();
        boolean fallar;

        @Override
        public void send(SimpleMailMessage mensaje) {
            if (fallar) {
                throw new IllegalStateException("Servidor de correo caído");
            }
            enviados.add(mensaje);
        }

        @Override
        public void send(SimpleMailMessage... mensajes) {
            for (SimpleMailMessage mensaje : mensajes) {
                send(mensaje);
            }
        }
    }

    private static Reserva reservaConCorreo() {
        Reserva reserva = reserva("RES-7", NOVIEMBRE);
        reserva.registrarCorreoContacto(new Correo("laura@mail.com"));
        return reserva;
    }

    @Test
    @DisplayName("Envía el correo al titular con el código, las fechas y el valor")
    void enviaCorreo() {
        BuzonFalso buzon = new BuzonFalso();
        var notificador = new NotificadorReservaCorreo(buzon, "reservas@sanrocco.co", MISMO_HILO);

        notificador.reservaCreada(reservaConCorreo());

        assertEquals(1, buzon.enviados.size());
        SimpleMailMessage correo = buzon.enviados.get(0);
        assertEquals("laura@mail.com", correo.getTo()[0]);
        assertEquals("reservas@sanrocco.co", correo.getFrom());
        assertTrue(correo.getSubject().contains("RES-2026-00007"), correo.getSubject());
        assertTrue(correo.getText().contains("PENDIENTE"), correo.getText());
        assertTrue(correo.getText().contains("Noches:       3"), correo.getText());
    }

    @Test
    @DisplayName("Confirmada y cancelada también se notifican, cada una con su asunto")
    void confirmadaYCancelada() {
        BuzonFalso buzon = new BuzonFalso();
        var notificador = new NotificadorReservaCorreo(buzon, "reservas@sanrocco.co", MISMO_HILO);

        notificador.reservaConfirmada(reservaConCorreo());
        notificador.reservaCancelada(reservaConCorreo());

        assertTrue(buzon.enviados.get(0).getSubject().contains("confirmada"));
        assertTrue(buzon.enviados.get(1).getSubject().contains("cancelada"));
    }

    @Test
    @DisplayName("I-05: si el servidor de correo falla, NO lanza excepción (la reserva sigue)")
    void fallaSinRomper() {
        BuzonFalso buzon = new BuzonFalso();
        buzon.fallar = true;
        var notificador = new NotificadorReservaCorreo(buzon, "reservas@sanrocco.co", MISMO_HILO);

        assertDoesNotThrow(() -> notificador.reservaCreada(reservaConCorreo()));
    }

    @Test
    @DisplayName("Si la reserva no tiene correo de contacto, no se envía nada")
    void sinCorreo() {
        BuzonFalso buzon = new BuzonFalso();
        var notificador = new NotificadorReservaCorreo(buzon, "reservas@sanrocco.co", MISMO_HILO);

        notificador.reservaCreada(reserva("RES-8", NOVIEMBRE));

        assertTrue(buzon.enviados.isEmpty());
    }

    @Test
    @DisplayName("I-02: el modo consola funciona sin internet y nunca falla")
    void modoConsola() {
        var consola = new NotificadorReservaConsola();

        assertDoesNotThrow(() -> consola.reservaCreada(reservaConCorreo()));
        assertDoesNotThrow(() -> consola.reservaCancelada(reserva("RES-9", NOVIEMBRE)));
    }

    @Test
    @DisplayName("El correo de contacto se guarda en la base y vuelve igual; nulo no se permite")
    void correoEnLaReserva() {
        Reserva copia = ReservaPersistenceMapper.aDominio(ReservaPersistenceMapper.aEntity(reservaConCorreo()));

        assertEquals(new Correo("laura@mail.com"), copia.getCorreoContacto());
        assertThrows(ReglaDominioException.class, () -> reserva("RES-10", NOVIEMBRE).registrarCorreoContacto(null));
    }
}
