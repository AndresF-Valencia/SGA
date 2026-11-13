package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.PublicarPoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.usecase.PublicarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.PoliticaCancelacionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/politicas-cancelacion")
@Tag(name = "Políticas de Cancelación")
@RequiredArgsConstructor
public class PoliticaCancelacionController {

    private final PublicarPoliticaCancelacionUseCase publicarPoliticaUseCase;
    private final PoliticaCancelacionMapper politicaMapper;

    /** Publica una versión de política asignada automáticamente y conserva las versiones anteriores. */
    @PostMapping
    @Operation(summary = "Publicar una nueva versión de la política")
    @ApiResponse(responseCode = "201", description = "Política publicada")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "409", description = "La política viola una regla de negocio")
    public ResponseEntity<PoliticaCancelacionResponse> publicarPolitica(
            @Valid @RequestBody PublicarPoliticaCancelacionRequest request) {
        
        // 1. Construir el objeto de dominio con los tramos del request
        PoliticaCancelacion politica = new PoliticaCancelacion(
                "Política de cancelación",
                politicaMapper.aTramos(request));

        // 2. Ejecutar el caso de uso (el sistema asigna la versión automáticamente)
        VersionPolitica nuevaVersion = publicarPoliticaUseCase.ejecutar(politica);

        // 3. Mapear la respuesta y construir la URI de ubicación con la versión asignada
        PoliticaCancelacionResponse response = politicaMapper.aPolitica(politica); // O el método de mapeo que uses para el DTO de respuesta
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{version}")
                .buildAndExpand(nuevaVersion.numero())
                .toUri();
                
        return ResponseEntity.created(location).body(response);
    }
}