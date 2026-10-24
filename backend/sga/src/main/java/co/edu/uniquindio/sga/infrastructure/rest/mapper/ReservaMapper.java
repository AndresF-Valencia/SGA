package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import java.time.LocalDate;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ModificarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.RecibirReservaExternaRequest;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

/**
 * Mapper relacionado con los DTOs CrearReservaRequest, ModificarReservaRequest,
 * RecibirReservaExternaRequest, CancelarReservaRequest, ReservaResponse y
 * ReservaResumenResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReservaMapper {

	/** ==================== DOMINIO → RESPONSE ==================== */

	// Convierte una reserva completa en su respuesta detallada.
	@Mapping(target = "codigoReserva", source = "codigo.valor")
	@Mapping(target = "codigoExterno", ignore = true)
	@Mapping(target = "estadoReserva", source = "estado")
	@Mapping(target = "canalOrigen", source = "canalOrigen")
	@Mapping(target = "apartamentoId", source = "apartamento.valor")
	@Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
	@Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
	@Mapping(target = "fechaCreacion", source = "creadaEn")
	@Mapping(target = "valorTotal", source = "valor")
	@Mapping(target = "saldoPendiente", ignore = true)
	@Mapping(target = "titular", source = "titular")
	@Mapping(target = "ocupantes", source = "ocupantes")
	@Mapping(target = "cantidadMascotas", source = "cantidadMascotas.cantidad")
	ReservaResponse aReserva(Reserva reserva);

	// Convierte una reserva en el resumen utilizado por los listados.
	@Mapping(target = "codigoReserva", source = "codigo.valor")
	@Mapping(target = "apartamentoId", source = "apartamento.valor")
	@Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
	@Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
	@Mapping(target = "estadoReserva", source = "estado")
	@Mapping(target = "canalOrigen", source = "canalOrigen")
	@Mapping(target = "nombreTitular", source = "titular.nombre")
	@Mapping(target = "valorTotal", source = "valor")
	ReservaResumenResponse aReservaResumen(Reserva reserva);

	// Convierte el titular del dominio en el resumen incluido en la respuesta.
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "telefono", ignore = true)
	ReservaResponse.TitularResumenDto aTitular(Ocupante titular);

	// Convierte un ocupante del dominio en el DTO de respuesta correspondiente.
	ReservaResponse.OcupanteResumenDto aOcupante(Ocupante ocupante);

	/** ==================== CONVERSIONES DE REQUEST ==================== */

	// Convierte las fechas de creación de una reserva en una estancia del dominio.
	default Estancia aEstancia(CrearReservaRequest request) {
		return new Estancia(request.fechaEntrada(), request.fechaSalida());
	}

	// Convierte las fechas de modificación en una nueva estancia del dominio.
	default Estancia aEstancia(ModificarReservaRequest request) {
		return new Estancia(request.fechaEntrada(), request.fechaSalida());
	}

	// Convierte los datos de un ocupante del request en una entidad del dominio.
	default Ocupante aOcupante(CrearReservaRequest.OcupanteDto ocupante, LocalDate fechaActual) {
		return new Ocupante(
				new DocumentoIdentidad(ocupante.documento()),
				ocupante.nombre(),
				ocupante.fechaNacimiento(),
				fechaActual);
	}

	// Convierte la lista de ocupantes del request en entidades del dominio.
	default List<Ocupante> aOcupantes(List<CrearReservaRequest.OcupanteDto> ocupantes,
			LocalDate fechaActual) {
		return ocupantes.stream()
				.map(ocupante -> aOcupante(ocupante, fechaActual))
				.toList();
	}

	// Convierte las fechas de una reserva externa en una estancia del dominio.
	default Estancia aEstancia(RecibirReservaExternaRequest request) {
		return new Estancia(request.fechaEntrada(), request.fechaSalida());
	}

	// Convierte un ocupante externo en una entidad del dominio.
	default Ocupante aOcupante(RecibirReservaExternaRequest.OcupanteExternoDto ocupante,
			LocalDate fechaActual) {
		return new Ocupante(
				new DocumentoIdentidad(ocupante.documento()),
				ocupante.nombre(),
				ocupante.fechaNacimiento(),
				fechaActual);
	}

	// Convierte la lista de ocupantes externos en entidades del dominio.
	default List<Ocupante> aOcupantesExternos(
			List<RecibirReservaExternaRequest.OcupanteExternoDto> ocupantes,
			LocalDate fechaActual) {
		return ocupantes.stream()
				.map(ocupante -> aOcupante(ocupante, fechaActual))
				.toList();
	}

	// Extrae el motivo que el caso de uso aplica sobre la reserva existente.
	default String aMotivo(CancelarReservaRequest request) {
		return request.motivo();
	}

	/**
	 * ==================== CONVERSIONES DE OBJETOS DE VALOR ====================
	 * MapStruct las usa solo cada vez que necesita pasar de un tipo al otro.
	 */

	default Long aValor(ValorCongelado valor) {
		return valor == null ? null : aDinero(valor.total());
	}

	default Long aDinero(Dinero dinero) {
		return dinero == null ? null : dinero.valor().longValueExact();
	}

	default String aDocumento(DocumentoIdentidad documento) {
		return documento == null ? null : documento.numero();
	}
}