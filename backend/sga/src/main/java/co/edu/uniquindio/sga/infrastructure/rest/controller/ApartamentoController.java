package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CambiarEstadoOperativoRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.usecase.CambiarEstadoOperativoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apartamentos")
@Tag(name = "Apartamentos y Operación")
@RequiredArgsConstructor
public class ApartamentoController {

    private final CambiarEstadoOperativoUseCase cambiarEstadoOperativoUseCase;
    private final ApartamentoMapper apartamentoMapper;

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