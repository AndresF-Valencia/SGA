package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.response.ReporteResponse;
import co.edu.uniquindio.sga.application.usecase.GenerarReporteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reportes de gestión para el administrador (ocupación, ingresos y reservas de un periodo). */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final GenerarReporteUseCase generarReporteUseCase;

    @GetMapping
    @Operation(summary = "Consultar el reporte de gestión de un periodo",
            description = "Ocupación, ingresos y reservas entre dos fechas (ambas incluidas). Roles: Administrador.")
    @ApiResponse(responseCode = "200", description = "Reporte del periodo")
    @ApiResponse(responseCode = "400", description = "Fechas inválidas o periodo mayor a 366 días")
    public ResponseEntity<ReporteResponse> consultar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(generarReporteUseCase.ejecutar(desde, hasta));
    }
}
