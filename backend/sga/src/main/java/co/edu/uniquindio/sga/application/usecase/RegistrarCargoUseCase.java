package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

/**
 * Caso de uso: Registrar un nuevo cargo (adicional o de consumo) en el folio de una reserva.
 */
 
public class RegistrarCargoUseCase {

    private final FolioRepository folioRepository;

    public RegistrarCargoUseCase(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    /**
     * Registra un cargo en el folio asociado a una reserva.
     * 
     * @param codigoReserva Código de la reserva propietaria del folio.
     * @param cargo         Objeto de valor que representa el cargo a registrar (tipo, valor, descripción).
     */
    @Transactional
    public Folio ejecutar(CodigoReserva codigoReserva, Cargo cargo) {
        if (codigoReserva == null) {
            throw new ReglaDominioException("El código de la reserva es obligatorio para registrar un cargo");
        }
        if (cargo == null) {
            throw new ReglaDominioException("El cargo a registrar es obligatorio");
        }

        // 1. Buscar el folio correspondiente a la reserva a través del repositorio
        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el folio asociado a la reserva: " + codigoReserva));

        // 2. Delegar la adición del cargo a la entidad de dominio (Valida si está cerrado - RN-16)
        folio.registrarCargo(cargo);

        // 3. Persistir los cambios actualizados del folio
        folioRepository.guardar(folio);
        return folio;
    }
}