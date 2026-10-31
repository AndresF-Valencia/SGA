package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTarifaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasTarifasUseCase;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.TarifaMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.TemporadaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
@Tag(name = "Temporadas y Tarifas")
@RequiredArgsConstructor
public class TemporadaTarifaController {

    private final GestionarTemporadasTarifasUseCase gestionarUseCase;
    private final TemporadaMapper temporadaMapper;
    private final TarifaMapper tarifaMapper;

    /** Registra una temporada y genera su identificador en el servidor. */
    @PostMapping("/temporadas")
    @Operation(summary = "Registrar una temporada", description = "Registra una temporada sin solaparla con otra existente.")
    @ApiResponse(responseCode = "201", description = "Temporada registrada")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "409", description = "La temporada se solapa con otra")
    public ResponseEntity<TemporadaResponse> registrarTemporada(
            @Valid @RequestBody RegistrarTemporadaRequest request) {
        var temporada = gestionarUseCase.registrarTemporada(
                new IdentificacionTemporada(UUID.randomUUID().toString()),
                temporadaMapper.aPeriodo(request),
                request.nombre(),
                TipoTemporada.BASE,
                temporadaMapper.aEstanciaMinima(request));
        TemporadaResponse response = temporadaMapper.aTemporada(temporada);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Configura o reemplaza la tarifa de un apartamento para una temporada. */
    @PutMapping("/apartamentos/{identificacion}/tarifas/{temporadaId}")
    @Operation(summary = "Configurar una tarifa", description = "Define la tarifa por ocupante y noche.")
    @ApiResponse(responseCode = "204", description = "Tarifa registrada")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento o temporada no encontrado")
    @ApiResponse(responseCode = "409", description = "La tarifa viola una regla de negocio")
    public ResponseEntity<Void> configurarTarifa(
            @PathVariable String identificacion,
            @PathVariable String temporadaId,
            @Valid @RequestBody RegistrarTarifaRequest request) {
        gestionarUseCase.configurarTarifa(tarifaMapper.aTarifa(
                request,
                UUID.randomUUID().toString(),
                identificacion,
                temporadaId));
        return ResponseEntity.noContent().build();
    }

    /** Lista las temporadas ordenadas por fecha de inicio, de a 10 por página (12.4). */
    @GetMapping("/temporadas")
    @Operation(summary = "Listar las temporadas")
    @ApiResponse(responseCode = "200", description = "Página de temporadas")
    public ResponseEntity<PaginaResponse<TemporadaResponse>> listarTemporadas(
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {
        var temporadas = gestionarUseCase.listarTemporadas().stream()
                .map(temporadaMapper::aTemporada)
                .toList();
        return ResponseEntity.ok(PaginaResponse.de(temporadas, pagina, tamano));
    }

    // El tipo BASE se usa porque el request actual no expone tipoTemporada.
}
