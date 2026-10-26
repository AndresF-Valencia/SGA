package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.PublicarPoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

/**
 * Mapper relacionado con los DTOs PublicarPoliticaCancelacionRequest y
 * PoliticaCancelacionResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PoliticaCancelacionMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte la política del dominio en su respuesta plana.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "tramos", source = "tramos")
    PoliticaCancelacionResponse aPolitica(PoliticaCancelacion politica);

    // Convierte un tramo del dominio, que almacena devolución, en retención.
    @Mapping(target = "diasMinimosAnticipacion", source = "diasMinimosAnticipacion")
    @Mapping(target = "diasMaximosAnticipacion", source = "diasMaximosAnticipacion")
    @Mapping(target = "porcentajeRetencion", expression = "java(aRetencion(tramo.porcentajeDevolucion()))")
    PoliticaCancelacionResponse.TramoPoliticaDto aTramo(TramoCancelacion tramo);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte la versión textual del request en su objeto de valor.
    default VersionPolitica aVersion(PublicarPoliticaCancelacionRequest request) {
        Objects.requireNonNull(request, "El request de política es obligatorio");
        return new VersionPolitica(Integer.parseInt(request.version()));
    }

    // Convierte todos los tramos del request en value objects del dominio.
    default List<TramoCancelacion> aTramos(PublicarPoliticaCancelacionRequest request) {
        Objects.requireNonNull(request, "El request de política es obligatorio");
        return request.tramos().stream()
                .map(this::aTramo)
                .toList();
    }

    // Convierte un tramo de retención del request en uno de devolución del dominio.
    default TramoCancelacion aTramo(PublicarPoliticaCancelacionRequest.TramoPoliticaDto tramo) {
        Objects.requireNonNull(tramo, "El tramo de política es obligatorio");
        return new TramoCancelacion(
                tramo.diasMinimo(),
                tramo.diasMaximo(),
                aDevolucion(tramo.porcentajeRetencion()));
    }

    /** ==================== CONVERSIONES DE OBJETOS DE VALOR ==================== */

    default int aDevolucion(int porcentajeRetencion) {
        return 100 - porcentajeRetencion;
    }

    default Double aRetencion(int porcentajeDevolucion) {
        return 100.0 - porcentajeDevolucion;
    }
}