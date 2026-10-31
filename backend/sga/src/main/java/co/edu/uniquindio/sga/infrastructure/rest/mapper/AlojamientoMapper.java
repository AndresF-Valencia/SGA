package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import co.edu.uniquindio.sga.application.dto.request.ConfigurarParametrosAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.request.ServicioAdicionalRequest;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.ServicioAdicionalResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.NormasConvivencia;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
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
    @Mapping(target = "normasConvivencia", source = "normasConvivencia.texto")
    @Mapping(target = "versionPoliticaVigente", source = "versionPoliticaVigente.numero")
    ParametrosAlojamientoResponse aParametrosAlojamiento(Alojamiento alojamiento);

    @Mapping(target = "valor", source = "valor.valor")
    ServicioAdicionalResponse aServicioAdicionalResponse(ServicioAdicional servicio);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte el texto de normas del request en su objeto de valor.
    default NormasConvivencia aNormasConvivencia(ConfigurarParametrosAlojamientoRequest request) {
        return new NormasConvivencia(request.normasConvivencia());
    }

    // Convierte los servicios adicionales del request en objetos de valor (lista vacía si no hay).
    default List<ServicioAdicional> aServiciosAdicionales(ConfigurarParametrosAlojamientoRequest request) {
        if (request.serviciosAdicionales() == null) {
            return List.of();
        }
        return request.serviciosAdicionales().stream()
                .map((ServicioAdicionalRequest s) -> new ServicioAdicional(s.nombre(), new Dinero(s.valor())))
                .toList();
    }

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