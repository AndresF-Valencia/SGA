package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.CotizacionRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

/**
 * Mapper relacionado con los DTOs CotizacionRequest y CotizacionResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CotizacionMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte una cotización en su respuesta con el desglose por noche.
    @Mapping(target = "identificacionApartamento", source = "identificacionApartamento.valor")
    @Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
    @Mapping(target = "cantidadNoches", expression = "java(cotizacion.getValor().noches())")
    @Mapping(target = "valorTotal", source = "montoTotal")
    @Mapping(target = "valorSubtotal", ignore = true)
    @Mapping(target = "desgloseNoches", source = "valor.detalle")
    CotizacionResponse aCotizacion(Cotizacion cotizacion);

    // Convierte una noche cotizada en el detalle de la respuesta.
    @Mapping(target = "fecha", source = "noche")
    @Mapping(target = "tarifaAplicada", source = "tarifaPorOcupante")
    @Mapping(target = "nombreTemporada", source = "temporada")
    CotizacionResponse.DetalleTarifaNocheDto aDetalle(CargoNoche cargoNoche);

    /** ==================== CONVERSIONES DE RESPONSE DEL CASO DE USO ==================== */

    // Completa la respuesta con el contexto que el caso de uso recibe por separado.
    default CotizacionResponse aCotizacion(ValorCongelado valor,
            String identificacionApartamento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida) {
        Objects.requireNonNull(valor, "El valor congelado es obligatorio");

        return new CotizacionResponse(
                identificacionApartamento,
                fechaEntrada,
                fechaSalida,
                valor.noches(),
                aDinero(valor.total()),
                null,
                valor.detalle().stream().map(this::aDetalle).toList());
    }

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Convierte la identificación del request en su objeto de valor.
    default IdentificacionApartamento aApartamento(CotizacionRequest request) {
        Objects.requireNonNull(request, "El request de cotización es obligatorio");
        return new IdentificacionApartamento(request.identificacionApartamento());
    }

    // Convierte las fechas del request en una estancia del dominio.
    default Estancia aEstancia(CotizacionRequest request) {
        Objects.requireNonNull(request, "El request de cotización es obligatorio");
        return new Estancia(request.fechaEntrada(), request.fechaSalida());
    }

    /** ==================== CONVERSIONES DE OBJETOS DE VALOR ==================== */

    default Long aDinero(Dinero dinero) {
        return dinero == null ? null : dinero.valor().longValueExact();
    }
}