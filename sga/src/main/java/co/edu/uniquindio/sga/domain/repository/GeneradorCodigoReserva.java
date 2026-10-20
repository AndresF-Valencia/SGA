package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

/**
 * Puerto que entrega el siguiente código de reserva (Guía 06), formato
 * RES-AAAA-NNNNN.
 */
public interface GeneradorCodigoReserva {

    CodigoReserva siguiente(int anio);
}