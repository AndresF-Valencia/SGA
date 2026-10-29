package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTarifaRequest;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;

/**
 * Mapper relacionado con el DTO RegistrarTarifaRequest.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TarifaMapper {

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Construye una tarifa con el valor del request y los identificadores de la ruta.
    default Tarifa aTarifa(RegistrarTarifaRequest request,
            String identificacionTarifa,
            String identificacionApartamento,
            String identificacionTemporada) {
        Objects.requireNonNull(request, "El request de tarifa es obligatorio");

        return new Tarifa(
                new IdentificacionTarifa(identificacionTarifa),
                new IdentificacionApartamento(identificacionApartamento),
                new IdentificacionTemporada(identificacionTemporada),
                aDinero(request));
    }

    /** ==================== CONVERSIONES DE OBJETOS DE VALOR ==================== */

    // Convierte el valor entero del request en dinero del dominio.
    default Dinero aDinero(RegistrarTarifaRequest request) {
        Objects.requireNonNull(request, "El request de tarifa es obligatorio");
        return Dinero.de(request.valorTarifa());
    }
}