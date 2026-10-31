package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.NotificadorUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación LOCAL: el correo con el código solo se escribe en el log.
 * Sirve para probar la recuperación de contraseña sin servidor de correo.
 */
public class NotificadorUsuarioConsola implements NotificadorUsuario {

    private static final Logger log = LoggerFactory.getLogger(NotificadorUsuarioConsola.class);

    @Override
    public void codigoRecuperacion(Usuario usuario, String codigo, int minutosVigencia) {
        log.info("\n===== CORREO (modo consola) =====\nPara: {}\nAsunto: {}\n\n{}\n=================================",
                usuario.getCorreo().valor(), MensajeUsuario.ASUNTO_RECUPERACION,
                MensajeUsuario.cuerpoRecuperacion(usuario, codigo, minutosVigencia));
    }
}
