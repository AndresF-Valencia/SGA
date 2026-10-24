package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;

import java.util.List;

/**
 * Caso de uso: Gestionar Temporadas y Tarifas (Módulo de Administración).
 * Permite registrar nuevas temporadas comerciales validando solapamientos y configurar sus tarifas.
 */
@Service
public class GestionarTemporadasTarifasUseCase {

    private final TemporadaRepository temporadaRepository;
    private final TarifaRepository tarifaRepository;

    public GestionarTemporadasTarifasUseCase(TemporadaRepository temporadaRepository, 
                                           TarifaRepository tarifaRepository) {
        this.temporadaRepository = temporadaRepository;
        this.tarifaRepository = tarifaRepository;
    }

    /**
     * Registra una nueva temporada comercial asegurando que no se solape con periodos existentes.
     */
    @Transactional
    public Temporada registrarTemporada(IdentificacionTemporada identificacion,
                                   Periodo periodo, 
                                   String nombre, 
                                   TipoTemporada tipo, 
                                   EstanciaMinima estanciaMinima) {
        if (identificacion == null) {
            throw new ReglaDominioException("La identificación de la temporada es obligatoria");
        }
        if (periodo == null) {
            throw new ReglaDominioException("El periodo de la temporada es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre de la temporada es obligatorio");
        }
        if (tipo == null) {
            throw new ReglaDominioException("El tipo de temporada es obligatorio");
        }

        // 1. Instanciar la nueva temporada (validará sus propias invariantes en el constructor)
        Temporada nuevaTemporada = new Temporada(identificacion, periodo, nombre, tipo, estanciaMinima);

        // 2. Obtener las temporadas existentes para validar solapamientos
        List<Temporada> temporadasExistentes = temporadaRepository.listar();
        for (Temporada existente : temporadasExistentes) {
            if (existente.seSolapaCon(nuevaTemporada)) {
                throw new ReglaDominioException(
                    String.format("La temporada '%s' se solapa con el periodo de la temporada existente '%s'", 
                        nombre, existente.getNombre())
                );
            }
        }

        // 3. Persistir la nueva temporada si pasa todas las validaciones
        temporadaRepository.guardar(nuevaTemporada);
        return nuevaTemporada;
    }

    /**
     * Configura o actualiza una tarifa en el sistema.
     */
    @Transactional
    public void configurarTarifa(Tarifa tarifa) {
        if (tarifa == null) {
            throw new ReglaDominioException("La tarifa a configurar es obligatoria");
        }
        tarifaRepository.guardar(tarifa);
    }
}