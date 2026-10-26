package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

/**
 * Mapper relacionado con los DTOs RegistrarTemporadaRequest y TemporadaResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TemporadaMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte una temporada del dominio en la respuesta de temporada.
    @Mapping(target = "id", source = "identificacion.id")
    @Mapping(target = "fechaInicio", source = "periodo.desde")
    @Mapping(target = "fechaFin", source = "periodo.hasta")
    @Mapping(target = "tipoTemporada", source = "tipo")
    @Mapping(target = "estanciaMinima", source = "estanciaMinima.noches")
    TemporadaResponse aTemporada(Temporada temporada);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte las fechas del request en el periodo cerrado del dominio.
    default Periodo aPeriodo(RegistrarTemporadaRequest request) {
        Objects.requireNonNull(request, "El request de temporada es obligatorio");
        return new Periodo(request.fechaInicio(), request.fechaFin());
    }

    // Convierte la estancia mínima del request en su objeto de valor.
    default EstanciaMinima aEstanciaMinima(RegistrarTemporadaRequest request) {
        Objects.requireNonNull(request, "El request de temporada es obligatorio");
        return new EstanciaMinima(request.estanciaMinima());
    }
}