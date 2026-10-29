package co.edu.uniquindio.sga.application.usecase;


import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.entity.Pago;

/**
 * Caso de uso: Corregir un movimiento (cargo o pago) del folio mediante la creación 
 * de un movimiento inverso, sin alterar ni eliminar el original (CU-13, RN-16).
 */
 
public class CorregirMovimientoUseCase {

    private final FolioRepository folioRepository;

    public CorregirMovimientoUseCase(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    /**
     * Corrige un cargo previo agregando un cargo compensatorio o inverso al folio.
     * 
     * @param codigoReserva Código de la reserva propietaria del folio.
     * @param cargoInverso  Objeto Cargo con valor negativo o concepto de reversión.
     */
    @Transactional
    public void corregirCargo(CodigoReserva codigoReserva, Cargo cargoInverso) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio para corregir un movimiento");
        }
        if (cargoInverso == null) {
            throw new ReglaDominioException("El cargo de corrección (inverso) es obligatorio");
        }

        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el folio asociado a la reserva: " + codigoReserva));

        // Se registra el movimiento inverso en el folio abierto (Respeta RN-16)
        folio.registrarCargo(cargoInverso);

        folioRepository.guardar(folio);
    }

    /**
     * Corrige un pago previo registrando un pago o contra-movimiento compensatorio.
     * 
     * @param codigoReserva Código de la reserva propietaria del folio.
     * @param pagoInverso   Objeto Pago que compensa o revierte el pago erróneo.
     */
    @Transactional
    public void corregirPago(CodigoReserva codigoReserva, Pago pagoInverso) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio para corregir un pago");
        }
        if (pagoInverso == null) {
            throw new ReglaDominioException("El pago de corrección (inverso) es obligatorio");
        }

        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el folio asociado a la reserva: " + codigoReserva));

        // Se registra el pago inverso en el folio abierto (Respeta RN-16)
        folio.registrarPago(pagoInverso);

        folioRepository.guardar(folio);
    }
}