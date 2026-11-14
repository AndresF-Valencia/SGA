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
    default PoliticaCancelacionResponse aPolitica(VersionPolitica version, PoliticaCancelacion politica) {
        Objects.requireNonNull(version, "La versión de la política es obligatoria");
        Objects.requireNonNull(politica, "La política es obligatoria");
        List<PoliticaCancelacionResponse.TramoPoliticaDto> tramos = politica.tramos().stream()
                .map(tramo -> aTramo(tramo))
                .toList();
        return new PoliticaCancelacionResponse(version.numero(), politica.nombre(), tramos);
    }

    // Convierte un tramo del dominio, que almacena devolución, en retención.
    @Mapping(target = "diasMinimosAnticipacion", source = "diasMinimosAnticipacion")
    @Mapping(target = "diasMaximosAnticipacion", source = "diasMaximosAnticipacion")
    @Mapping(target = "porcentajeRetencion", expression = "java(aRetencion(tramo.porcentajeDevolucion()))")
    PoliticaCancelacionResponse.TramoPoliticaDto aTramo(TramoCancelacion tramo);

    /** ==================== CONVERSIONES DE REQUEST ==================== */


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