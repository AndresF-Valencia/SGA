package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CrearBloqueoRequest;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarBloqueoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.BloqueoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/bloqueos")
@Tag(name = "Apartamentos y Operación")
@RequiredArgsConstructor
public class BloqueoController {

    private final GestionarBloqueoUseCase gestionarBloqueoUseCase;
    private final BloqueoMapper bloqueoMapper;

    /** Crea un bloqueo administrativo y genera su identificador en el servidor. */
    @PostMapping
    @Operation(summary = "Registrar un bloqueo administrativo", description = "Bloquea un apartamento durante un periodo sin reservas activas.")
    @ApiResponse(responseCode = "201", description = "Bloqueo registrado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "El bloqueo viola una regla de negocio")
    public ResponseEntity<BloqueoResponse> crearBloqueo(
            @Valid @RequestBody CrearBloqueoRequest request) {
        IdentificacionBloqueo identificacion = new IdentificacionBloqueo(UUID.randomUUID().toString());
        var bloqueo = gestionarBloqueoUseCase.crearBloqueo(
                identificacion,
                bloqueoMapper.aApartamento(request),
                bloqueoMapper.aPeriodo(request),
                MotivoBloqueo.valueOf(request.motivo()));
        BloqueoResponse response = bloqueoMapper.aBloqueo(bloqueo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}