package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.CrearBloqueoRequest;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

/**
 * Mapper relacionado con los DTOs CrearBloqueoRequest y BloqueoResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BloqueoMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte un bloqueo del dominio en su respuesta plana.
    @Mapping(target = "id", source = "identificacionBloqueo.valor")
    @Mapping(target = "identificacionApartamento", source = "apartamento.valor")
    @Mapping(target = "fechaInicio", source = "periodo.desde")
    @Mapping(target = "fechaFin", source = "periodo.hasta")
    @Mapping(target = "motivo", source = "motivo")
    @Mapping(target = "registradoPor", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    BloqueoResponse aBloqueo(Bloqueo bloqueo);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte el identificador del apartamento en su objeto de valor.
    default IdentificacionApartamento aApartamento(CrearBloqueoRequest request) {
        Objects.requireNonNull(request, "El request de bloqueo es obligatorio");
        return new IdentificacionApartamento(request.identificacionApartamento());
    }

    // Convierte las fechas del request en el periodo cerrado del dominio.
    default Periodo aPeriodo(CrearBloqueoRequest request) {
        Objects.requireNonNull(request, "El request de bloqueo es obligatorio");
        return new Periodo(request.fechaInicio(), request.fechaFin());
    }
}