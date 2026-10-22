package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

/**
 * Caso de uso: Actualiza el estado operativo de un apartamento 
 * (por ejemplo, a PREPARADO al terminar la preparación) (CU-16).
 */
public class CambiarEstadoOperativoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public CambiarEstadoOperativoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    /**
     * Cambia el estado operativo de un apartamento aplicando las reglas de transición del dominio.
     * 
     * @param apartamentoId     Identificador único del apartamento.
     * @param nuevoEstado       El estado operativo al que se desea transicionar.
     */
    public void ejecutar(IdentificacionApartamento apartamentoId, EstadoOperativo nuevoEstado) {
        if (apartamentoId == null) {
            throw new ReglaDominioException("El identificador del apartamento es obligatorio");
        }
        if (nuevoEstado == null) {
            throw new ReglaDominioException("El nuevo estado operativo es obligatorio");
        }

        // 1. Buscar el apartamento a través del repositorio
        Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el apartamento con identificador: " + apartamentoId));

        // 2. Delegar el cambio de estado a la entidad 
        apartamento.cambiarEstado(nuevoEstado);

        // 3. Persistir el agregado actualizado
        apartamentoRepository.guardar(apartamento);
    }
}