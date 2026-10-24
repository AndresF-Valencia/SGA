package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ConfigurarParametrosAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ConfigurarParametrosAlojamientoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.AlojamientoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alojamiento")
@Tag(name = "Alojamiento")
@RequiredArgsConstructor
public class AlojamientoController {

    private final ConfigurarParametrosAlojamientoUseCase configurarParametrosUseCase;
    private final AlojamientoMapper alojamientoMapper;

    /** Actualiza los parámetros operativos y comerciales del alojamiento. */
    @PutMapping("/parametros")
    @Operation(summary = "Configurar los parámetros del alojamiento")
    @ApiResponse(responseCode = "200", description = "Parámetros actualizados")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    public ResponseEntity<ParametrosAlojamientoResponse> configurarParametros(
            @RequestHeader("X-Alojamiento-Id") String alojamientoId,
            @Valid @RequestBody ConfigurarParametrosAlojamientoRequest request) {
        var alojamiento = configurarParametrosUseCase.ejecutar(
                new IdentificacionAlojamiento(alojamientoId),
                request.horaEntrada(),
                request.horaSalida(),
                request.horaLimiteNoShow(),
                alojamientoMapper.aTiempoPreparacion(request),
                alojamientoMapper.aPlazoConfirmacion(request),
                alojamientoMapper.aUmbralEdadFacturable(request),
                request.exigeAnticipo(),
                request.porcentajeAnticipo());
        return ResponseEntity.ok(alojamientoMapper.aParametrosAlojamiento(alojamiento));
    }

    /**
     * Pendiente: consultar parámetros porque no existe un caso de uso de lectura.
     * El header X-Alojamiento-Id se usa porque la ruta OpenAPI no identifica el alojamiento.
     */
}