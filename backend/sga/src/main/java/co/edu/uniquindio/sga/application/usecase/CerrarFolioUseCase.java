package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

/**
 * Caso de uso: Cierra el folio de una reserva. Si el saldo es cero se cierra directamente;
 * si el saldo es distinto de cero, requiere una autorización registrada (CU-14, RN-17).
 */
 
public class CerrarFolioUseCase {

    private final FolioRepository folioRepository;

    public CerrarFolioUseCase(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    /**
     * Cierra el folio asociado a una reserva.
     * 
     * @param codigoReserva       Código de la reserva propietaria del folio.
     * @param autorizacionCierre  Objeto de autorización (puede ser null si el saldo es cero).
     */
    @Transactional
    public Folio ejecutar(CodigoReserva codigoReserva, AutorizacionCierre autorizacionCierre) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio para cerrar el folio");
        }

        // 1. Buscar el folio a través del repositorio
        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el folio asociado a la reserva: " + codigoReserva));

        // 2. Aplicar la regla de cierre según el saldo (RN-17)
        if (folio.saldo().esCero() && autorizacionCierre == null) {
            // Si el saldo es cero, se cierra de forma estándar
            folio.cerrar();
        } else {
            // Si hay saldo pendiente (positivo o a favor), se exige la autorización del administrador
            folio.cerrarConAutorizacion(autorizacionCierre);
        }

        // 3. Persistir los cambios del folio cerrado
        folioRepository.guardar(folio);
        return folio;
    }
}