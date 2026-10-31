package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarEstadoOperativoRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.usecase.CambiarEstadoOperativoUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarApartamentoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apartamentos")
@Tag(name = "Apartamentos y Operación")
@RequiredArgsConstructor
public class ApartamentoController {

    private final CambiarEstadoOperativoUseCase cambiarEstadoOperativoUseCase;
    private final GestionarApartamentoUseCase gestionarApartamentoUseCase;
    private final ApartamentoMapper apartamentoMapper;

    @PostMapping
    @Operation(summary = "Crear un apartamento", description = "Se crea inactivo; se activa cuando tenga tarifas completas.")
    @ApiResponse(responseCode = "201", description = "Apartamento creado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "409", description = "El apartamento ya existe")
    public ResponseEntity<ApartamentoDetalleResponse> crear(@Valid @RequestBody CrearApartamentoRequest request) {
        var apartamento = gestionarApartamentoUseCase.crear(
                new IdentificacionApartamento(request.identificacion()),
                request.nombre(),
                new TiempoPreparacion(request.tiempoPreparacionHoras()),
                new Dormitorio(request.dormitorios()),
                new CapacidadOcupantes(request.capacidadMaxima()),
                new LimiteMascota(request.limiteMascotas()),
                request.caracteristicas(),
                request.imagenes());
        URI location = URI.create("/api/apartamentos/" + apartamento.getIdentificacion().valor());
        return ResponseEntity.created(location).body(apartamentoMapper.aApartamento(apartamento));
    }

    @GetMapping
    @Operation(summary = "Listar los apartamentos")
    @ApiResponse(responseCode = "200", description = "Listado de apartamentos")
    public ResponseEntity<List<ApartamentoDetalleResponse>> listar() {
        return ResponseEntity.ok(gestionarApartamentoUseCase.listar().stream()
                .map(apartamentoMapper::aApartamento)
                .toList());
    }

    @GetMapping("/{identificacion}")
    @Operation(summary = "Consultar un apartamento")
    @ApiResponse(responseCode = "200", description = "Detalle del apartamento")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    public ResponseEntity<ApartamentoDetalleResponse> obtener(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.obtener(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}")
    @Operation(summary = "Actualizar un apartamento")
    @ApiResponse(responseCode = "200", description = "Apartamento actualizado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    public ResponseEntity<ApartamentoDetalleResponse> actualizar(
            @PathVariable String identificacion,
            @Valid @RequestBody ActualizarApartamentoRequest request) {
        var apartamento = gestionarApartamentoUseCase.actualizar(
                new IdentificacionApartamento(identificacion),
                request.nombre(),
                new TiempoPreparacion(request.tiempoPreparacionHoras()),
                new Dormitorio(request.dormitorios()),
                new CapacidadOcupantes(request.capacidadMaxima()),
                new LimiteMascota(request.limiteMascotas()),
                request.caracteristicas(),
                request.imagenes());
        return ResponseEntity.ok(apartamentoMapper.aApartamento(apartamento));
    }

    /** Eliminación lógica: deshabilita el apartamento si no tiene reservas activas. */
    @DeleteMapping("/{identificacion}")
    @Operation(summary = "Deshabilitar un apartamento", description = "Solo si no tiene reservas activas.")
    @ApiResponse(responseCode = "200", description = "Apartamento deshabilitado")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Tiene reservas activas")
    public ResponseEntity<ApartamentoDetalleResponse> desactivar(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.desactivar(new IdentificacionApartamento(identificacion))));
    }

    @PostMapping("/{identificacion}/activar")
    @Operation(summary = "Activar un apartamento", description = "Solo si tiene tarifa en todas las temporadas.")
    @ApiResponse(responseCode = "200", description = "Apartamento activado")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Tarifas incompletas")
    public ResponseEntity<ApartamentoDetalleResponse> activar(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.activar(new IdentificacionApartamento(identificacion))));
    }

    /** Cambia el estado operativo aplicando las transiciones del dominio. */
    @PostMapping("/{identificacion}/estado-operativo")
    @Operation(summary = "Cambiar el estado operativo de un apartamento")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Transicion no permitida")
    public ResponseEntity<ApartamentoDetalleResponse> cambiarEstadoOperativo(
            @PathVariable String identificacion,
            @Valid @RequestBody CambiarEstadoOperativoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        // 7.1: el personal de servicio NO puede declarar FUERA_DE_SERVICIO (es decisión administrativa)
        if (usuario.rol() == Rol.PERSONAL_SERVICIO && request.nuevoEstado() == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new AccesoDenegadoException("Solo el administrador puede poner un apartamento fuera de servicio");
        }
        var apartamento = cambiarEstadoOperativoUseCase.ejecutar(
                new IdentificacionApartamento(identificacion), request.nuevoEstado());
        return ResponseEntity.ok(apartamentoMapper.aApartamento(apartamento));
    }

    /**
     * Pendientes: consultas de apartamentos, novedades y bloqueos porque no
     * existen casos de uso; registrar novedades porque falta su mapper de
     * respuesta; y crear bloqueos porque el request no trae su identificador.
     */
}