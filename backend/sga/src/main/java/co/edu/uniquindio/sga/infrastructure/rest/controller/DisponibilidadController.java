package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.response.DisponibilidadResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase.Criterios;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Búsqueda pública de disponibilidad para el huésped (7.5). */
@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "Reservas")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;

    @GetMapping
    @Operation(summary = "Buscar apartamentos disponibles", description = "Filtra por capacidad, precio por noche y características; 10 resultados por página.")
    @ApiResponse(responseCode = "200", description = "Apartamentos disponibles en todas las noches del rango")
    @ApiResponse(responseCode = "400", description = "Fechas o parámetros inválidos")
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam int personas,
            @RequestParam(defaultValue = "0") int mascotas,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Long precioMin,
            @RequestParam(required = false) Long precioMax,
            @RequestParam(required = false) List<String> caracteristicas,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {

        var dtos = consultarDisponibilidadUseCase.buscar(new Criterios(
                        fechaEntrada, fechaSalida, personas, new CantidadMascota(mascotas),
                        capacidadMinima, precioMin, precioMax, caracteristicas)).stream()
                .map(r -> new DisponibilidadResponse.ApartamentoDisponibleDto(
                        r.apartamento().getIdentificacion().valor(),
                        r.apartamento().getNombre(),
                        r.apartamento().getCapacidad().cantidad(),
                        r.apartamento().getCaracteristicas(),
                        r.apartamento().getImagenes(),
                        r.tarifaPorNoche()))
                .toList();

        PaginaResponse<DisponibilidadResponse.ApartamentoDisponibleDto> p =
                PaginaResponse.de(dtos, pagina, tamano);
        return ResponseEntity.ok(new DisponibilidadResponse(fechaEntrada, fechaSalida,
                (int) p.totalElementos(), p.contenido(), p.pagina(), p.tamano(), p.totalPaginas()));
    }
}
