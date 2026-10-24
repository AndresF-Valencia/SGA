package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;

/**
 * Caso de uso: Registrar una novedad (daño, faltante o situación) 
 * reportada por el personal de servicio en un apartamento (CU-15).
 */
@Service
public class RegistrarNovedadUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public RegistrarNovedadUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    /**
     * Registra una nueva novedad en el apartamento utilizando el objeto de valor.
     * 
     * @param apartamentoId Identificador único del apartamento.
     * @param novedad       Objeto de valor con los datos validados de la novedad.
     */
    @Transactional
    public void ejecutar(IdentificacionApartamento apartamentoId, Novedad novedad) {
        if (apartamentoId == null) {
            throw new ReglaDominioException("El identificador del apartamento es obligatorio para registrar una novedad");
        }
        if (novedad == null) {
            throw new ReglaDominioException("La novedad a registrar es obligatoria");
        }

        // 1. Buscar el apartamento a través del repositorio
        Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el apartamento con identificador: " + apartamentoId));

        // 2. Delegar el registro de la novedad a la entidad de dominio
        apartamento.registrarNovedad(novedad);

        // 3. Persistir el agregado actualizado
        apartamentoRepository.guardar(apartamento);
    }
}