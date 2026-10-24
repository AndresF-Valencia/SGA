package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.ConfigurarParametrosAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

/**
 * Mapper relacionado con los DTOs ConfigurarParametrosAlojamientoRequest y
 * ParametrosAlojamientoResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlojamientoMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte la configuración del alojamiento en su respuesta plana.
    @Mapping(target = "identificacion", source = "identificacion.identificacion")
    @Mapping(target = "minutosPreparacion", source = "tiempoPreparacion")
    @Mapping(target = "horasPlazoConfirmacion", source = "plazoConfirmacion")
    @Mapping(target = "edadUmbralFacturable", source = "umbralEdadFacturable")
    @Mapping(target = "versionPoliticaVigente", source = "versionPoliticaVigente.numero")
    ParametrosAlojamientoResponse aParametrosAlojamiento(Alojamiento alojamiento);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte las horas de preparación del request en su objeto de valor.
    default TiempoPreparacion aTiempoPreparacion(ConfigurarParametrosAlojamientoRequest request) {
        return new TiempoPreparacion(request.horasPreparacion());
    }

    // Convierte el plazo de confirmación del request en su objeto de valor.
    default PlazoConfirmacion aPlazoConfirmacion(ConfigurarParametrosAlojamientoRequest request) {
        return new PlazoConfirmacion(request.horasPlazoConfirmacion());
    }

    // Convierte el umbral de edad del request en su objeto de valor.
    default UmbralEdadFacturable aUmbralEdadFacturable(ConfigurarParametrosAlojamientoRequest request) {
        return new UmbralEdadFacturable(request.edadUmbralFacturable());
    }

    /** ==================== CONVERSIONES DE OBJETOS DE VALOR ==================== */

    // El dominio almacena el tiempo en horas y la respuesta lo expone en minutos.
    default int aMinutos(TiempoPreparacion tiempoPreparacion) {
        return tiempoPreparacion == null ? 0 : tiempoPreparacion.tiempoPreparacion() * 60;
    }

    default int aHoras(PlazoConfirmacion plazoConfirmacion) {
        return plazoConfirmacion == null ? 0 : plazoConfirmacion.horas();
    }

    default int aAnios(UmbralEdadFacturable umbralEdadFacturable) {
        return umbralEdadFacturable == null ? 0 : umbralEdadFacturable.anios();
    }
}