package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import co.edu.uniquindio.sga.domain.port.NotificadorUsuario;
import co.edu.uniquindio.sga.infrastructure.notificacion.NotificadorReservaConsola;
import co.edu.uniquindio.sga.infrastructure.notificacion.NotificadorReservaCorreo;
import co.edu.uniquindio.sga.infrastructure.notificacion.NotificadorUsuarioConsola;
import co.edu.uniquindio.sga.infrastructure.notificacion.NotificadorUsuarioCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.util.concurrent.Executors;

/**
 * Escoge el adaptador de notificaciones con UNA línea de configuración, sin tocar código:
 *
 *   sga.notificaciones.modo=consola   → solo escribe en el log (por defecto, sin internet)
 *   sga.notificaciones.modo=correo    → envía correos reales por SMTP
 *
 * Si se pide "correo" pero no hay servidor configurado (spring.mail.host), no se
 * cae la aplicación: avisa en el log y usa la consola.
 */
@Configuration
public class NotificacionConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConfig.class);

    @Bean
    public NotificadorReserva notificadorReserva(
            @Value("${sga.notificaciones.modo:consola}") String modo,
            @Value("${sga.notificaciones.remitente:no-responder@sanrocco.co}") String remitente,
            ObjectProvider<MailSender> mailSender) {
        if ("correo".equalsIgnoreCase(modo)) {
            MailSender sender = mailSender.getIfAvailable();
            if (sender != null) {
                log.info("Notificaciones: se enviarán correos reales desde {}", remitente);
                // Un hilo aparte y "daemon": no bloquea la petición ni impide apagar la aplicación
                return new NotificadorReservaCorreo(sender, remitente, Executors.newSingleThreadExecutor(tarea -> {
                    Thread hilo = new Thread(tarea, "sga-correo");
                    hilo.setDaemon(true);
                    return hilo;
                }));
            }
            log.warn("sga.notificaciones.modo=correo pero no hay servidor de correo (spring.mail.host). "
                    + "Se usará el modo consola.");
        }
        log.info("Notificaciones: modo consola (los correos solo se escriben en el log)");
        return new NotificadorReservaConsola();
    }

    /** Mismo modo que los avisos de reserva, para el código de recuperación de contraseña (7.2). */
    @Bean
    public NotificadorUsuario notificadorUsuario(
            @Value("${sga.notificaciones.modo:consola}") String modo,
            @Value("${sga.notificaciones.remitente:no-responder@sanrocco.co}") String remitente,
            ObjectProvider<MailSender> mailSender) {
        MailSender sender = mailSender.getIfAvailable();
        if ("correo".equalsIgnoreCase(modo) && sender != null) {
            return new NotificadorUsuarioCorreo(sender, remitente, Executors.newSingleThreadExecutor(tarea -> {
                Thread hilo = new Thread(tarea, "sga-correo-usuario");
                hilo.setDaemon(true);
                return hilo;
            }));
        }
        return new NotificadorUsuarioConsola();
    }
}
