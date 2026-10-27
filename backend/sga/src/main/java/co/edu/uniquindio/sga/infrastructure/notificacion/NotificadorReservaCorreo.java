package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.concurrent.Executor;

/**
 * Implementación REAL del puerto: envía el aviso por correo con SMTP (10.3.2, Opción A).
 *
 * Cómo cumple los requisitos de la 10.3.3:
 *  - I-03 tiempo máximo: lo fijan las propiedades spring.mail.properties[mail.smtp.*timeout].
 *  - I-04 y I-05 ante fallo: el envío corre en OTRO hilo, así la reserva responde enseguida
 *    aunque el servidor de correo esté caído o lento. Si falla, queda en el log como error
 *    y la reserva sigue creada (el usuario no ve ningún error).
 *  - I-06 credenciales: usuario y contraseña de correo vienen de variables de entorno.
 */
public class NotificadorReservaCorreo implements NotificadorReserva {

    private static final Logger log = LoggerFactory.getLogger(NotificadorReservaCorreo.class);

    private final MailSender mailSender;
    private final String remitente;
    private final Executor hilo;

    public NotificadorReservaCorreo(MailSender mailSender, String remitente, Executor hilo) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.hilo = hilo;
    }

    @Override
    public void reservaCreada(Reserva reserva) {
        enviar(MensajeReserva.creada(reserva));
    }

    @Override
    public void reservaConfirmada(Reserva reserva) {
        enviar(MensajeReserva.confirmada(reserva));
    }

    @Override
    public void reservaCancelada(Reserva reserva) {
        enviar(MensajeReserva.cancelada(reserva));
    }

    private void enviar(MensajeReserva mensaje) {
        if (mensaje.destinatario() == null) {
            log.info("Aviso no enviado: la reserva no tiene correo de contacto ({})", mensaje.asunto());
            return;
        }
        try {
            hilo.execute(() -> enviarAhora(mensaje));
        } catch (RuntimeException e) {
            // Ni siquiera se pudo programar el envío: igual no se interrumpe la operación de negocio
            log.error("No se pudo programar el correo '{}': {}", mensaje.asunto(), e.getMessage());
        }
    }

    private void enviarAhora(MensajeReserva mensaje) {
        try {
            SimpleMailMessage correo = new SimpleMailMessage();
            correo.setFrom(remitente);
            correo.setTo(mensaje.destinatario());
            correo.setSubject(mensaje.asunto());
            correo.setText(mensaje.cuerpo());
            mailSender.send(correo);
            log.info("Correo enviado a {}: {}", mensaje.destinatario(), mensaje.asunto());
        } catch (RuntimeException e) {
            log.error("Falló el envío del correo a {} ('{}'): {}. La reserva NO se afecta.",
                    mensaje.destinatario(), mensaje.asunto(), e.getMessage());
        }
    }
}
