package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.RegistrarNovedadRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoEstadoResponse;
import co.edu.uniquindio.sga.application.dto.response.DisponibilidadResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;

/**
 * Mapper relacionado con los DTOs RegistrarNovedadRequest,
 * ApartamentoDetalleResponse, ApartamentoEstadoResponse y DisponibilidadResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApartamentoMapper {

    /** ==================== DOMINIO -> RESPONSE ==================== */

    // Convierte un apartamento en su respuesta detallada.
    @Mapping(target = "identificacion", source = "identificacion.valor")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "dormitorios", source = "dormitorios.cantidad")
    @Mapping(target = "capacidadMaxima", source = "capacidad.cantidad")
    @Mapping(target = "limiteMascotas", source = "limiteMascotas.limite")
    @Mapping(target = "estadoOperativo", source = "estadoOperativo")
    @Mapping(target = "activo", expression = "java(apartamento.estaActivo())")
    ApartamentoDetalleResponse aApartamento(Apartamento apartamento);

    // Convierte un apartamento en el detalle de su estado operativo.
    @Mapping(target = "identificacionApartamento", source = "identificacion.valor")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "estadoOperativo", source = "estadoOperativo")
    @Mapping(target = "codigoReservaActual", ignore = true)
    @Mapping(target = "observaciones", ignore = true)
    ApartamentoEstadoResponse.ApartamentoEstadoDetalleDto aEstado(Apartamento apartamento);

    // Convierte un apartamento disponible en el detalle de disponibilidad.
    @Mapping(target = "identificacionApartamento", source = "identificacion.valor")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "capacidadMaxima", source = "capacidad.cantidad")
    @Mapping(target = "tarifaEstimadaPorNoche", ignore = true)
    DisponibilidadResponse.ApartamentoDisponibleDto aDisponible(Apartamento apartamento);

    /** ==================== CONVERSIONES DE REQUEST ==================== */

    // Construye una novedad usando los datos del request y el contexto de aplicación.
    default Novedad aNovedad(RegistrarNovedadRequest request, String autor, LocalDateTime fecha) {
        return Novedad.de(request.descripcion(), request.gravedad(), autor, fecha);
    }
}