package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CotizacionRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CotizacionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api")
@Tag(name = "Cotizaciones", description = "Operaciones para cotizar estancias")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizarEstanciaUseCase cotizarEstanciaUseCase;
    private final CotizacionMapper cotizacionMapper;

    @PostMapping("/cotizaciones")
    @Operation(summary = "Cotizar una estancia", description = "Calcula el valor de la estancia noche por noche aplicando las tarifas de temporada y el umbral de edad facturable (7.4).")
    public ResponseEntity<CotizacionResponse> cotizarEstancia(@Valid @RequestBody CotizacionRequest request) {
        // Para cotizar solo importa la edad de cada ocupante, pero el dominio exige documento y nombre:
        // se usan unos provisionales (la cotización no crea ni guarda ocupantes).
        LocalDate hoy = LocalDate.now();
        List<Ocupante> ocupantes = IntStream.range(0, request.ocupantes().size())
                .mapToObj(i -> new Ocupante(
                        new DocumentoIdentidad("COTIZACION-" + (i + 1)),
                        "Ocupante " + (i + 1),
                        request.ocupantes().get(i).fechaNacimiento(),
                        hoy))
                .toList();

        var valor = cotizarEstanciaUseCase.ejecutar(
                new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(),
                request.fechaSalida(),
                ocupantes);

        return ResponseEntity.ok(cotizacionMapper.aCotizacion(valor,
                request.identificacionApartamento(), request.fechaEntrada(), request.fechaSalida()));
    }
}
