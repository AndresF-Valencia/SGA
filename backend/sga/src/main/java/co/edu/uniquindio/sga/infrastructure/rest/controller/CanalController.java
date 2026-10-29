package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.RecibirReservaExternaRequest;
import co.edu.uniquindio.sga.application.dto.request.ResolverConflictoCanalRequest;
import co.edu.uniquindio.sga.application.dto.response.DisponibilidadResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase;
import co.edu.uniquindio.sga.application.usecase.RecibirReservaExternaUseCase;
import co.edu.uniquindio.sga.application.usecase.ResolverConflictoCanalUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Canal Externo")
@RequiredArgsConstructor
public class CanalController {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final RecibirReservaExternaUseCase recibirReservaExternaUseCase;
    private final ResolverConflictoCanalUseCase resolverConflictoUseCase;
    private final ReservaMapper reservaMapper;

    @GetMapping("/canales/disponibilidad")
    @Operation(summary = "Canal externo: consultar disponibilidad", description = "Busca apartamentos disponibles para canales externos.")
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam int personas,
            @RequestParam(defaultValue = "0") int mascotas) {
        
        // 1. Obtener entidades puras del dominio
        List<Apartamento> apartamentosDisponibles = consultarDisponibilidadUseCase.listarDisponibles(
                fechaEntrada, 
                fechaSalida, 
                personas, 
                new CantidadMascota(mascotas)
        );
        
        // 2. Mapear correctamente usando los métodos reales de la entidad Apartamento
        List<DisponibilidadResponse.ApartamentoDisponibleDto> dtos = apartamentosDisponibles.stream()
                .map(ap -> new DisponibilidadResponse.ApartamentoDisponibleDto(
                        ap.getIdentificacion().toString(), 
                        ap.getNombre(),
                        ap.getCapacidad().cantidad(),
                        ap.getCaracteristicas(),            
                        ap.getImagenes(),                   
                        null                                // Tarifa estimada (si aplica)
                ))
                .toList();

        
        DisponibilidadResponse respuesta = new DisponibilidadResponse(
                fechaEntrada,
                fechaSalida,
                dtos.size(),
                dtos
        );
        
        return ResponseEntity.ok(respuesta);
    }

   /** Canal externo: crear una reserva externa (RN-18, RN-19) */
    @PostMapping("/canales/{canal}/reservas-externas")
    @Operation(summary = "Canal externo: crear una reserva externa", description = "Registra una reserva proveniente de un canal externo validando colisiones (RN-19).")
    @ApiResponse(responseCode = "201", description = "Reserva externa creada")
    @ApiResponse(responseCode = "200", description = "Mensaje repetido: ya existía una reserva con este canal y código externo")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "409", description = "Regla de negocio / Conflicto")
    public ResponseEntity<ReservaResponse> recibirReservaExterna(
            @PathVariable String canal,
            @Valid @RequestBody RecibirReservaExternaRequest request) {
        

        // 1. Mapear el titular (creando su DocumentoIdentidad y pasando LocalDate.now() para la validación de fecha de nacimiento)
        Ocupante titular = new Ocupante(
                new DocumentoIdentidad(request.titular().documento()),
                request.titular().nombre(),
                request.titular().fechaNacimiento(),
                LocalDate.now()
        );

        // 2. Mapear la lista de ocupantes externos usando el constructor real de la entidad
        List<Ocupante> listaOcupantes = request.ocupantes().stream()
                .map(o -> new Ocupante(
                        new DocumentoIdentidad(o.documento()),
                        o.nombre(),
                        o.fechaNacimiento(),
                        LocalDate.now()
                ))
                .toList();

        // 3. Ejecutar el caso de uso pasando los tipos de dominio puros
        var reserva = recibirReservaExternaUseCase.ejecutar(
                request.codigoExterno(),
                CanalOrigen.valueOf(canal.toUpperCase()),
                new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(),
                request.fechaSalida(),
                titular,
                listaOcupantes,
                new CantidadMascota(request.cantidadMascotas()), 
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.aReserva(reserva));
    }


    /** Resolver un conflicto de canal (administrador, 7.8 / CU-21) */
    @PutMapping("/conflictos-canal/{id}/resolver")
    @Operation(summary = "Resolver un conflicto de canal", description = "Rechaza o gestiona la reserva indicada por el identificador de conflicto recibido.")
    @ApiResponse(responseCode = "200", description = "Conflicto resuelto")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    @ApiResponse(responseCode = "409", description = "El conflicto no puede resolverse")
    public ResponseEntity<ReservaResponse> resolverConflicto(
            @PathVariable String id,
            @Valid @RequestBody ResolverConflictoCanalRequest request) {
        
        // Invoca el caso de uso pasando el CodigoReserva y el motivo de resolución
        var reserva = resolverConflictoUseCase.rechazarReservaCanal(
                new CodigoReserva(id), request.resolucion());
        
        return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Canal externo: cancelar una reserva previamente creada (10.3.1) */
    @DeleteMapping("/canales/{canal}/reservas-externas/{codigoExterno}")
    @Operation(summary = "Canal externo: cancelar reserva", description = "Permite al canal externo cancelar una reserva por su identificador externo.")
    @ApiResponse(responseCode = "200", description = "Reserva externa cancelada con éxito")
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    public ResponseEntity<ReservaResponse> cancelarReservaExterna(
            @PathVariable String canal,
            @PathVariable String codigoExterno,
            @RequestParam(required = false, defaultValue = "Cancelación solicitada por el canal") String motivo) {
        
        // Delegamos en el caso de uso existente que procesa folios, políticas y notificaciones
        var reserva = cancelarReservaUseCase.ejecutarPorCodigoExterno(
                codigoExterno, 
                motivo, 
                LocalDateTime.now(), 
                "CANAL_" + canal.toUpperCase()
        );
        
        return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }
}