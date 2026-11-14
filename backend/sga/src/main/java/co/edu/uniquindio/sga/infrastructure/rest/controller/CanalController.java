package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.RecibirReservaExternaRequest;
import co.edu.uniquindio.sga.application.dto.request.ResolverConflictoCanalRequest;
import co.edu.uniquindio.sga.application.dto.response.ConflictoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.DisponibilidadResponse;
import co.edu.uniquindio.sga.application.dto.response.EventoCanalResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.usecase.AutenticarCanalUseCase;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase.Criterios;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RecibirReservaExternaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarEventoCanalUseCase;
import co.edu.uniquindio.sga.application.usecase.ResolverConflictoCanalUseCase;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato del canal externo (7.8 y 10.3.1).
 *
 * Las tres operaciones del canal se autentican con la credencial del canal en el
 * encabezado X-Canal-Credencial, no con JWT. Sin credencial válida → 401.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Canal Externo")
@RequiredArgsConstructor
public class CanalController {

    private static final String CREDENCIAL = "X-Canal-Credencial";
    private static final Logger log = LoggerFactory.getLogger(CanalController.class);

    private final AutenticarCanalUseCase autenticarCanalUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final ConsultarReservaUseCase consultarReservaUseCase;
    private final RecibirReservaExternaUseCase recibirReservaExternaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final ResolverConflictoCanalUseCase resolverConflictoUseCase;
    private final RegistrarEventoCanalUseCase registrarEventoCanalUseCase;
    private final ReservaMapper reservaMapper;

    /** 1. Consultar disponibilidad para un rango de fechas y una composición de grupo. */
    @GetMapping("/canales/disponibilidad")
    @Operation(summary = "Canal externo: consultar disponibilidad")
    @ApiResponse(responseCode = "200", description = "Apartamentos disponibles")
    @ApiResponse(responseCode = "401", description = "Credencial de canal inválida")
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidad(
            @RequestHeader(value = CREDENCIAL, required = false) String credencial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam int personas,
            @RequestParam(defaultValue = "0") int mascotas,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {
        Canal canal = autenticar(credencial, EventoCanal.CONSULTA_DISPONIBILIDAD, null);
        String consulta = "Del " + fechaEntrada + " al " + fechaSalida + ", " + personas + " persona(s)";
        try {
            DisponibilidadResponse respuesta = disponibilidad(fechaEntrada, fechaSalida, personas, mascotas, pagina, tamano);
            bitacora(canal.getNombre(), EventoCanal.CONSULTA_DISPONIBILIDAD, null, true,
                    consulta + ": " + respuesta.totalDisponibles() + " apartamento(s) disponible(s)");
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            bitacora(canal.getNombre(), EventoCanal.CONSULTA_DISPONIBILIDAD, null, false, consulta + ". " + e.getMessage());
            throw e;
        }
    }

    private DisponibilidadResponse disponibilidad(LocalDate fechaEntrada, LocalDate fechaSalida, int personas,
            int mascotas, int pagina, int tamano) {
        var dtos = consultarDisponibilidadUseCase.buscar(new Criterios(
                        fechaEntrada, fechaSalida, personas, new CantidadMascota(mascotas),
                        null, null, null, null)).stream()
                .map(r -> new DisponibilidadResponse.ApartamentoDisponibleDto(
                        r.apartamento().getIdentificacion().valor(),
                        r.apartamento().getNombre(),
                        r.apartamento().getCapacidad().cantidad(),
                        r.apartamento().getCaracteristicas(),
                        r.apartamento().getImagenes(),
                        r.tarifaPorNoche()))
                .toList();
        PaginaResponse<DisponibilidadResponse.ApartamentoDisponibleDto> p = PaginaResponse.de(dtos, pagina, tamano);
        return new DisponibilidadResponse(fechaEntrada, fechaSalida,
                (int) p.totalElementos(), p.contenido(), p.pagina(), p.tamano(), p.totalPaginas());
    }

    /**
     * 2. Crear una reserva externa (RN-18, RN-19).
     * Idempotente: si el mismo código externo ya llegó antes, devuelve la reserva existente con 200
     * y NO crea otra. Si es nueva, 201.
     */
    @PostMapping("/canales/reservas-externas")
    @Operation(summary = "Canal externo: crear una reserva externa")
    @ApiResponse(responseCode = "201", description = "Reserva externa creada")
    @ApiResponse(responseCode = "200", description = "Mensaje repetido: se devuelve la reserva que ya existía (RN-19)")
    @ApiResponse(responseCode = "401", description = "Credencial de canal inválida")
    @ApiResponse(responseCode = "409", description = "Choca con una reserva o bloqueo vigente: se rechaza y queda registrada como conflicto (RN-18)")
    public ResponseEntity<ReservaResponse> recibirReservaExterna(
            @RequestHeader(value = CREDENCIAL, required = false) String credencial,
            @Valid @RequestBody RecibirReservaExternaRequest request) {
        Canal canal = autenticar(credencial, EventoCanal.RESERVA_EXTERNA, request.codigoExterno());
        String pedido = request.identificacionApartamento() + ", del " + request.fechaEntrada() + " al " + request.fechaSalida();
        try {
            boolean repetida = existe(request.codigoExterno());
            ReservaResponse respuesta = crearReservaExterna(canal, request);
            bitacora(canal.getNombre(), EventoCanal.RESERVA_EXTERNA, request.codigoExterno(), true,
                    pedido + (repetida ? ". Mensaje repetido: se devolvió la reserva " : ". Reserva creada: ")
                            + respuesta.codigoReserva());
            return ResponseEntity.status(repetida ? HttpStatus.OK : HttpStatus.CREATED).body(respuesta);
        } catch (RuntimeException e) {
            bitacora(canal.getNombre(), EventoCanal.RESERVA_EXTERNA, request.codigoExterno(), false,
                    pedido + ". Rechazada: " + e.getMessage());
            throw e;
        }
    }

    private ReservaResponse crearReservaExterna(Canal canal, RecibirReservaExternaRequest request) {
        LocalDate hoy = LocalDate.now();
        Ocupante titular = new Ocupante(new DocumentoIdentidad(request.titular().documento()),
                request.titular().nombre(), request.titular().fechaNacimiento(), hoy);
        List<Ocupante> ocupantes = request.ocupantes().stream()
                .map(o -> new Ocupante(new DocumentoIdentidad(o.documento()), o.nombre(), o.fechaNacimiento(), hoy))
                .toList();

        var reserva = recibirReservaExternaUseCase.ejecutar(
                request.codigoExterno(),
                canal,                          // el canal sale de la credencial, no de lo que diga la petición
                new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada(),
                request.fechaSalida(),
                titular,
                ocupantes,
                new CantidadMascota(request.cantidadMascotas()),
                LocalDateTime.now());
        return reservaMapper.aReserva(reserva);
    }

    /** 3. Cancelar una reserva externa previamente creada. */
    @PutMapping("/canales/reservas-externas/{codigoExterno}/cancelar")
    @Operation(summary = "Canal externo: cancelar una reserva externa")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada con la política congelada")
    @ApiResponse(responseCode = "401", description = "Credencial de canal inválida")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese código externo")
    @ApiResponse(responseCode = "409", description = "La reserva ya no se puede cancelar")
    public ResponseEntity<ReservaResponse> cancelarReservaExterna(
            @RequestHeader(value = CREDENCIAL, required = false) String credencial,
            @PathVariable String codigoExterno,
            @RequestParam(defaultValue = "Cancelación solicitada por el canal") String motivo) {
        Canal canal = autenticar(credencial, EventoCanal.CANCELACION_EXTERNA, codigoExterno);
        try {
            var reserva = cancelarReservaUseCase.ejecutarPorCodigoExterno(
                    codigoExterno, motivo, LocalDateTime.now(), "CANAL " + canal.getNombre());
            bitacora(canal.getNombre(), EventoCanal.CANCELACION_EXTERNA, codigoExterno, true,
                    "Reserva " + reserva.getCodigo().valor() + " cancelada. Motivo: " + motivo);
            return ResponseEntity.ok(reservaMapper.aReserva(reserva));
        } catch (RuntimeException e) {
            bitacora(canal.getNombre(), EventoCanal.CANCELACION_EXTERNA, codigoExterno, false,
                    "No se canceló: " + e.getMessage());
            throw e;
        }
    }

    /** Bitácora de eventos intercambiados con los canales (administrador, 7.8). Usa JWT. */
    @GetMapping("/eventos-canal")
    @Operation(summary = "Consultar la bitácora de eventos de los canales",
            description = "Cada consulta, reserva y cancelación que envió un canal externo, con su resultado. Roles: Administrador.")
    @ApiResponse(responseCode = "200", description = "Página de eventos, del más reciente al más antiguo")
    public ResponseEntity<PaginaResponse<EventoCanalResponse>> listarEventos(
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {
        List<EventoCanalResponse> eventos = registrarEventoCanalUseCase.listar().stream()
                .map(e -> new EventoCanalResponse(e.getId(), e.getCanal(), e.getOperacion(), e.getCodigoExterno(),
                        e.fueExitoso() ? "EXITOSO" : "RECHAZADO", e.getDetalle(), e.getOcurridoEn()))
                .toList();
        return ResponseEntity.ok(PaginaResponse.de(eventos, pagina, tamano));
    }

    /** Conflictos de canal registrados (administrador, 7.8). Este sí usa JWT. */
    @GetMapping("/conflictos-canal")
    @Operation(summary = "Listar los conflictos de canal")
    public ResponseEntity<PaginaResponse<ConflictoCanalResponse>> listarConflictos(
            @RequestParam(required = false) Boolean resuelto,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {
        var conflictos = resolverConflictoUseCase.listar(resuelto).stream().map(CanalController::aRespuesta).toList();
        return ResponseEntity.ok(PaginaResponse.de(conflictos, pagina, tamano));
    }

    /** Resolver un conflicto de canal (administrador, 7.8 / CU-21): deja por escrito qué se hizo. */
    @PutMapping("/conflictos-canal/{id}/resolver")
    @Operation(summary = "Resolver un conflicto de canal")
    public ResponseEntity<ConflictoCanalResponse> resolverConflicto(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody ResolverConflictoCanalRequest request) {
        var conflicto = resolverConflictoUseCase.resolver(id, request.resolucion(),
                usuario == null ? "ADMINISTRADOR" : usuario.correo(), LocalDateTime.now());
        return ResponseEntity.ok(aRespuesta(conflicto));
    }

    private static ConflictoCanalResponse aRespuesta(ConflictoCanal c) {
        return new ConflictoCanalResponse(c.getId(), c.getCanal(), c.getCodigoExterno(), c.getApartamento(),
                c.getFechaEntrada(), c.getFechaSalida(), c.getTitularNombre(), c.getTitularDocumento(),
                c.getMotivo(), c.getRecibidoEn(), c.estaResuelto() ? "RESUELTO" : "PENDIENTE",
                c.getResolucion(), c.getResueltoPor(), c.getResueltoEn());
    }

    /** Valida la credencial; si no corresponde a ningún canal, el intento también queda en la bitácora. */
    private Canal autenticar(String credencial, String operacion, String codigoExterno) {
        try {
            return autenticarCanalUseCase.ejecutar(credencial);
        } catch (RuntimeException e) {
            bitacora(null, operacion, codigoExterno, false, "Credencial de canal inválida");
            throw e;
        }
    }

    /** Nunca interrumpe la operación del canal: si la bitácora falla, solo queda en el log. */
    private void bitacora(String canal, String operacion, String codigoExterno, boolean exitoso, String detalle) {
        try {
            registrarEventoCanalUseCase.registrar(canal, operacion, codigoExterno, exitoso, detalle, LocalDateTime.now());
        } catch (RuntimeException e) {
            log.error("No se pudo registrar el evento de canal {}: {}", operacion, e.getMessage());
        }
    }

    private boolean existe(String codigoExterno) {
        try {
            consultarReservaUseCase.ejecutar(codigoExterno);
            return true;
        } catch (ReservaNoEncontradaException | IllegalArgumentException | ReglaDominioException e) {
            return false;
        }
    }
}
