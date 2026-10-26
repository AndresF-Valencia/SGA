package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ResolverConflictoCanalRequest;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.usecase.ResolverConflictoCanalUseCase;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conflictos-canal")
@Tag(name = "Canal Externo")
@RequiredArgsConstructor
public class CanalController {

    private final ResolverConflictoCanalUseCase resolverConflictoUseCase;
    private final ReservaMapper reservaMapper;

    /** Rechaza la reserva asociada al conflicto y devuelve su estado actualizado. */
    @PutMapping("/{id}/resolver")
    @Operation(summary = "Resolver un conflicto de canal", description = "Rechaza la reserva indicada por el identificador recibido.")
    @ApiResponse(responseCode = "200", description = "Conflicto resuelto")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    @ApiResponse(responseCode = "409", description = "El conflicto no puede resolverse")
    public ResponseEntity<ReservaResponse> resolverConflicto(
            @PathVariable String id,
            @Valid @RequestBody ResolverConflictoCanalRequest request) {
        var reserva = resolverConflictoUseCase.rechazarReservaCanal(
                new CodigoReserva(id), request.resolucion());
        return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /**
     * Pendientes: disponibilidad y recepción/cancelación externa porque faltan
     * casos de uso y datos del titular compatibles con los DTOs actuales.
     */
}