package co.edu.uniquindio.sga.domain.port;

import co.edu.uniquindio.sga.domain.entity.Reserva;

/**
 * Puerto de salida para avisarle al titular de una reserva (7.8 y 10.3.2, Opción A).
 * El dominio no sabe si el aviso sale por correo, por mensajería o solo por consola.
 *
 * Regla para TODA implementación (I-04, I-05): nunca lanza excepciones. Si el aviso
 * falla, se registra en el log y la operación de negocio sigue: una reserva no deja
 * de crearse, confirmarse ni cancelarse porque el correo no salió.
 * Si la reserva no tiene correo de contacto, simplemente no se envía nada.
 */
public interface NotificadorReserva {

    void reservaCreada(Reserva reserva);

    void reservaConfirmada(Reserva reserva);

    void reservaCancelada(Reserva reserva);
}
