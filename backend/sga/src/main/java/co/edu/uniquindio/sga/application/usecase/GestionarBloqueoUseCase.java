package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;

/**
 * Caso de uso: Gestionar bloqueo de apartamento (Crear y Levantar Bloqueo - CU-17).
 */
@Service
public class GestionarBloqueoUseCase {

    private final BloqueoApartamentoService bloqueoApartamentoService;
    private final BloqueoRepository bloqueoRepository;
    private final ReservaRepository reservaRepository;

    public GestionarBloqueoUseCase(BloqueoApartamentoService bloqueoApartamentoService,
                                 BloqueoRepository bloqueoRepository,
                                 ReservaRepository reservaRepository) {
        this.bloqueoApartamentoService = bloqueoApartamentoService;
        this.bloqueoRepository = bloqueoRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Crea un nuevo bloqueo administrativo validando que no existan reservas activas (RN-07, 7.3).
     */
    @Transactional
    public Bloqueo crearBloqueo(IdentificacionBloqueo idBloqueo,
                             IdentificacionApartamento apartamentoId,
                             Periodo periodo,
                             MotivoBloqueo motivo) {
        if (idBloqueo == null || apartamentoId == null || periodo == null || motivo == null) {
            throw new ReglaDominioException("Todos los parámetros para crear el bloqueo son obligatorios");
        }

        // 1. Obtener las reservas del apartamento para que el servicio de dominio evalúe el cruce (7.3)
        List<Reserva> reservasExistentes = reservaRepository.buscarActivasPorApartamento(apartamentoId);

        // 2. Invocar al servicio de dominio para que aplique las reglas y cree el agregado
        Bloqueo bloqueo = bloqueoApartamentoService.crearBloqueo(idBloqueo, apartamentoId, periodo, motivo, reservasExistentes);

        // 3. Persistir el nuevo bloqueo creado
        bloqueoRepository.guardar(bloqueo);
        return bloqueo;
    }

    /**
     * Levanta un bloqueo existente de forma lógica (12.4).
     */
    @Transactional
    public void levantarBloqueo(IdentificacionBloqueo idBloqueo) {
        if (idBloqueo == null) {
            throw new ReglaDominioException("El identificador del bloqueo es obligatorio para levantarlo");
        }

        // 1. Buscar el bloqueo a través del repositorio
        Bloqueo bloqueo = bloqueoRepository.buscarPorId(idBloqueo)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el bloqueo con identificador: " + idBloqueo));

        // 2. Delegar la acción de levantamiento a la entidad (eliminación lógica, 12.4)
        bloqueo.levantar();

        // 3. Persistir los cambios del bloqueo actualizado
        bloqueoRepository.guardar(bloqueo);
    }
}