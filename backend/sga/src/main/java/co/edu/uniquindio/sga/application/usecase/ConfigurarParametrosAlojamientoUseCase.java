package co.edu.uniquindio.sga.application.usecase;


import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.NormasConvivencia;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Caso de uso: Configurar parámetros del alojamiento (CU-20).
 * Permite actualizar todos los parámetros operativos y comerciales del Alojamiento.
 */
 
public class ConfigurarParametrosAlojamientoUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public ConfigurarParametrosAlojamientoUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    /**
     * Actualiza la totalidad de los parámetros configurables del alojamiento.
     */
    @Transactional
    public Alojamiento ejecutar(IdentificacionAlojamiento alojamientoId,
                         LocalTime horaEntrada,
                         LocalTime horaSalida,
                         LocalTime horaLimiteNoShow,
                         TiempoPreparacion tiempoPreparacion,
                         PlazoConfirmacion plazoConfirmacion,
                         UmbralEdadFacturable umbralEdadFacturable,
                         boolean exigeAnticipo,
                         int porcentajeAnticipo,
                         String direccion,
                         BigDecimal latitud,
                         BigDecimal longitud,
                         NormasConvivencia normasConvivencia,
                         List<ServicioAdicional> serviciosAdicionales) {

        if (alojamientoId == null) {
            throw new ReglaDominioException("El identificador del alojamiento es obligatorio");
        }

        // 1. Obtener la entidad Alojamiento
        Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el alojamiento con identificador: " + alojamientoId));

        // 2. Delegar la actualización de los diferentes bloques de parámetros
        alojamiento.actualizarParametrosOperativos(horaEntrada, horaSalida, tiempoPreparacion, plazoConfirmacion);
        alojamiento.actualizarPoliticasGenerales(horaLimiteNoShow, umbralEdadFacturable);
        alojamiento.actualizarConfiguracionAnticipo(exigeAnticipo, porcentajeAnticipo);
        alojamiento.actualizarFichaDescriptiva(direccion, latitud, longitud, normasConvivencia, serviciosAdicionales);

        // 3. Persistir los cambios
        alojamientoRepository.guardar(alojamiento);
        return alojamiento;
    }
}