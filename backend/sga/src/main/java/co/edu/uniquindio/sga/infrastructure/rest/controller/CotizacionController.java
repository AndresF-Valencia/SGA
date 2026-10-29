package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CotizacionRequest;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Cotizaciones", description = "Operaciones para cotizar estancias")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizarEstanciaUseCase cotizarEstanciaUseCase;

    @PostMapping("/cotizaciones")
    @Operation(summary = "Cotizar una estancia", description = "Calcula el valor de la estancia noche por noche aplicando las tarifas de temporada y el umbral de edad facturable (Sección 7.4).")
    @ApiResponse(responseCode = "200", description = "Cotización generada con éxito")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    public ResponseEntity<ValorCongelado> cotizarEstancia(@Valid @RequestBody CotizacionRequest request) {
        
        // Mapear los ocupantes del DTO a la entidad de dominio Ocupante
        List<Ocupante> ocupantes = request.ocupantes().stream()
                .map(o -> new Ocupante(
                        null,
                        "Huésped",
                        o.fechaNacimiento(),
                        LocalDate.now()
                ))
                .toList();

        ValorCongelado valorCongelado = cotizarEstanciaUseCase.ejecutar(
                new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(),
                request.fechaSalida(),
                ocupantes
        );

        return ResponseEntity.ok(valorCongelado);
    }
}