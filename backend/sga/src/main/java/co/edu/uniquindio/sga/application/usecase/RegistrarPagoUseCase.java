package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.entity.Pago;

/**
 * Caso de uso: Registrar un pago con su medio y fecha contra el folio de una reserva (CU-12, RN-15).
 */
 
public class RegistrarPagoUseCase {

    private final FolioRepository folioRepository;

    public RegistrarPagoUseCase(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    /**
     * Registra un nuevo pago en el folio asociado a la reserva y actualiza su saldo.
     * 
     * @param codigoReserva Código de la reserva propietaria del folio.
     * @param pago          Objeto que encapsula los detalles del pago (monto, medio de pago, fecha, etc.).
     */
    @Transactional
    public Folio ejecutar(CodigoReserva codigoReserva, Pago pago) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio para registrar un pago");
        }
        if (pago == null) {
            throw new ReglaDominioException("El pago a registrar es obligatorio");
        }

        // 1. Buscar el folio correspondiente a la reserva a través del puerto repositorio
        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el folio asociado a la reserva: " + codigoReserva));

        // 2. Delegar la operación a la entidad de dominio Folio 
        // (La entidad valida internamente que el folio no esté cerrado)
        folio.registrarPago(pago);

        // 3. Persistir el agregado actualizado
        folioRepository.guardar(folio);
        return folio;
    }
}