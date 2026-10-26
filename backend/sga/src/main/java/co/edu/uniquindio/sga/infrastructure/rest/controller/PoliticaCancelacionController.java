package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.PublicarPoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.usecase.PublicarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
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

    /** Publica una versión de política y conserva las versiones anteriores. */
    @PostMapping
    @Operation(summary = "Publicar una nueva versión de la política")
    @ApiResponse(responseCode = "201", description = "Política publicada")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "409", description = "La política viola una regla de negocio")
    public ResponseEntity<PoliticaCancelacionResponse> publicarPolitica(
            @Valid @RequestBody PublicarPoliticaCancelacionRequest request) {
        var version = politicaMapper.aVersion(request);
        PoliticaCancelacion politica = new PoliticaCancelacion(
                "Politica de cancelacion v" + version.numero(),
                politicaMapper.aTramos(request));
        var politicaPublicada = publicarPoliticaUseCase.ejecutar(version, politica);
        PoliticaCancelacionResponse response = politicaMapper.aPolitica(politicaPublicada);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{version}")
                .buildAndExpand(version.numero())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Pendiente: consultar el historial porque no existe caso de uso ni método
     * de repository para listar las versiones publicadas.
     */
}