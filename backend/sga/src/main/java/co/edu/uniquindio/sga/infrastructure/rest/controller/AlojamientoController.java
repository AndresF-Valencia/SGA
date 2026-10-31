package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ConfigurarParametrosAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.response.ParametrosAlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ConfigurarParametrosAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.AlojamientoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ConsultarAlojamientoUseCase consultarAlojamientoUseCase;
    private final AlojamientoMapper alojamientoMapper;

    /**
     * /parametros es la ruta de ESCRITURA (solo administrador): reemplaza la configuración del
     * alojamiento (horarios, anticipo, no-show, plazos, ubicación, normas y servicios adicionales).
     * Para LEER esa misma configuración se usa GET /api/alojamiento. Lleva el header
     * X-Alojamiento-Id porque la ruta no identifica el alojamiento.
     */
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
                request.porcentajeAnticipo(),
                request.direccion(),
                request.latitud(),
                request.longitud(),
                alojamientoMapper.aNormasConvivencia(request),
                alojamientoMapper.aServiciosAdicionales(request));
        return ResponseEntity.ok(alojamientoMapper.aParametrosAlojamiento(alojamiento));
    }

    /**
     * Consulta el alojamiento único con su ubicación, normas, servicios adicionales y parámetros.
     * Es pública (ver SecurityConfig): el huésped la usa para ver la ficha antes de reservar.
     */
    @GetMapping
    @Operation(summary = "Consultar el alojamiento")
    @ApiResponse(responseCode = "200", description = "Alojamiento con su configuración vigente")
    @ApiResponse(responseCode = "404", description = "No hay alojamiento configurado")
    public ResponseEntity<ParametrosAlojamientoResponse> consultar() {
        return ResponseEntity.ok(alojamientoMapper.aParametrosAlojamiento(consultarAlojamientoUseCase.ejecutar()));
    }
}
