package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.RegistrarCargoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;

/**
 * Mapper relacionado con los DTOs RegistrarCargoRequest, RegistrarPagoRequest
 * y FolioResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FolioMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte el estado financiero del folio en su respuesta plana.
    @Mapping(target = "codigoReserva", source = "reserva.valor")
    @Mapping(target = "estadoFolio", source = "cerrado")
    @Mapping(target = "subtotalCargos", expression = "java(aDinero(folio.totalCargos()))")
    @Mapping(target = "totalPagos", expression = "java(aDinero(folio.totalPagado()))")
    @Mapping(target = "saldoPendiente", expression = "java(aDinero(folio.saldo()))")
    @Mapping(target = "cargos", source = "cargos")
    @Mapping(target = "pagos", source = "pagos")
    FolioResponse aFolio(Folio folio);

    // Convierte un cargo del folio en el resumen de cargo de la respuesta.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "monto", source = "valor")
    @Mapping(target = "fechaRegistro", source = "fecha")
    @Mapping(target = "registradoPor", ignore = true)
    FolioResponse.CargoResumenDto aCargo(Cargo cargo);

    // Convierte un pago del folio en el resumen de pago de la respuesta.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medioPago", source = "medioPago")
    @Mapping(target = "monto", source = "monto")
    @Mapping(target = "fechaPago", source = "horaPago")
    @Mapping(target = "referencia", ignore = true)
    FolioResponse.PagoResumenDto aPago(Pago pago);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte el monto de un cargo en el objeto monetario del dominio.
    default Dinero aDinero(RegistrarCargoRequest request) {
        Objects.requireNonNull(request, "El request de cargo es obligatorio");
        return Dinero.de(request.monto());
    }

    // Convierte el monto de un pago en el objeto monetario del dominio.
    default Dinero aDinero(RegistrarPagoRequest request) {
        Objects.requireNonNull(request, "El request de pago es obligatorio");
        return Dinero.de(request.monto());
    }

    /** ==================== CONVERSIONES DE OBJETOS DE VALOR ==================== */

    default Long aDinero(Dinero dinero) {
        return dinero == null ? null : dinero.valor().longValueExact();
    }

    default String aEstadoFolio(boolean cerrado) {
        return cerrado ? "CERRADO" : "ABIERTO";
    }
}