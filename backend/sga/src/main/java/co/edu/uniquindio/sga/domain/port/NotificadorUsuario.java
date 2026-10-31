package co.edu.uniquindio.sga.domain.port;

import co.edu.uniquindio.sga.domain.entity.Usuario;

/**
 * Puerto de salida para avisarle a un usuario sobre su cuenta (7.2).
 * Hoy solo envía el código de recuperación de contraseña al correo registrado.
 *
 * Igual que NotificadorReserva (I-04, I-05): nunca lanza excepciones; si el envío
 * falla queda en el log.
 */
public interface NotificadorUsuario {

    void codigoRecuperacion(Usuario usuario, String codigo, int minutosVigencia);
}
