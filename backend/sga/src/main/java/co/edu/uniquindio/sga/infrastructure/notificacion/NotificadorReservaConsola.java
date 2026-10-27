package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación LOCAL del puerto (I-02): no necesita internet ni contraseñas,
 * solo escribe el mensaje en el log. Es la que se usa por defecto, en las
 * pruebas automáticas (I-08) y para sustentar sin conexión.
 */
public class NotificadorReservaConsola implements NotificadorReserva {

    private static final Logger log = LoggerFactory.getLogger(NotificadorReservaConsola.class);

    @Override
    public void reservaCreada(Reserva reserva) {
        escribir(MensajeReserva.creada(reserva));
    }

    @Override
    public void reservaConfirmada(Reserva reserva) {
        escribir(MensajeReserva.confirmada(reserva));
    }

    @Override
    public void reservaCancelada(Reserva reserva) {
        escribir(MensajeReserva.cancelada(reserva));
    }

    private void escribir(MensajeReserva mensaje) {
        if (mensaje.destinatario() == null) {
            log.info("Aviso no enviado: la reserva no tiene correo de contacto ({})", mensaje.asunto());
            return;
        }
        log.info("\n===== CORREO (modo consola) =====\nPara: {}\nAsunto: {}\n\n{}\n=================================",
                mensaje.destinatario(), mensaje.asunto(), mensaje.cuerpo());
    }
}
