package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.NotificadorUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.concurrent.Executor;

/**
 * Implementación REAL: envía el código de recuperación por SMTP, en otro hilo
 * (la petición responde enseguida aunque el correo tarde o falle).
 */
public class NotificadorUsuarioCorreo implements NotificadorUsuario {

    private static final Logger log = LoggerFactory.getLogger(NotificadorUsuarioCorreo.class);

    private final MailSender mailSender;
    private final String remitente;
    private final Executor hilo;

    public NotificadorUsuarioCorreo(MailSender mailSender, String remitente, Executor hilo) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.hilo = hilo;
    }

    @Override
    public void codigoRecuperacion(Usuario usuario, String codigo, int minutosVigencia) {
        String destino = usuario.getCorreo().valor();
        String cuerpo = MensajeUsuario.cuerpoRecuperacion(usuario, codigo, minutosVigencia);
        try {
            hilo.execute(() -> {
                try {
                    SimpleMailMessage correo = new SimpleMailMessage();
                    correo.setFrom(remitente);
                    correo.setTo(destino);
                    correo.setSubject(MensajeUsuario.ASUNTO_RECUPERACION);
                    correo.setText(cuerpo);
                    mailSender.send(correo);
                    log.info("Código de recuperación enviado a {}", destino);
                } catch (RuntimeException e) {
                    log.error("Falló el envío del código de recuperación a {}: {}", destino, e.getMessage());
                }
            });
        } catch (RuntimeException e) {
            log.error("No se pudo programar el correo de recuperación a {}: {}", destino, e.getMessage());
        }
    }
}
