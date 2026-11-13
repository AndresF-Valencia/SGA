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
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

        @PostMapping
        @Operation(summary = "Registrar un bloqueo administrativo", description = "Bloquea un apartamento durante un periodo sin reservas activas.")
        @ApiResponse(responseCode = "201", description = "Bloqueo registrado")
        @ApiResponse(responseCode = "400", description = "Solicitud invalida")
        @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
        @ApiResponse(responseCode = "409", description = "El bloqueo viola una regla de negocio")
        public ResponseEntity<BloqueoResponse> crearBloqueo(@Valid @RequestBody CrearBloqueoRequest request) {
                IdentificacionBloqueo identificacion = new IdentificacionBloqueo(UUID.randomUUID().toString());
                var bloqueo = gestionarBloqueoUseCase.crearBloqueo(
                                identificacion,
                                bloqueoMapper.aApartamento(request),
                                bloqueoMapper.aPeriodo(request),
                                aMotivo(request.motivo()));

                BloqueoResponse response = bloqueoMapper.aBloqueo(bloqueo);
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(response.id())
                                .toUri();
                return ResponseEntity.created(location).body(response);
        }

        @PutMapping("/{id}/levantar")
        @Operation(summary = "Levantar un bloqueo administrativo", description = "Libera un apartamento previamente bloqueado.")
        @ApiResponse(responseCode = "200", description = "Bloqueo levantado")
        @ApiResponse(responseCode = "404", description = "Bloqueo no encontrado")
        @ApiResponse(responseCode = "409", description = "El bloqueo ya estaba levantado")
        public ResponseEntity<BloqueoResponse> levantarBloqueo(@PathVariable String id) {
                var bloqueo = gestionarBloqueoUseCase.levantarBloqueo(new IdentificacionBloqueo(id));
                return ResponseEntity.ok(bloqueoMapper.aBloqueo(bloqueo));
        }

        /** IllegalArgumentException ya se responde como 400 con ErrorResponse. */
        private MotivoBloqueo aMotivo(String texto) {
                try {
                        return MotivoBloqueo.valueOf(texto.trim().toUpperCase());
                } catch (IllegalArgumentException | NullPointerException e) {
                        throw new IllegalArgumentException("Motivo de bloqueo inválido. Valores permitidos: "
                                        + Arrays.toString(MotivoBloqueo.values()));
                }
        }
}