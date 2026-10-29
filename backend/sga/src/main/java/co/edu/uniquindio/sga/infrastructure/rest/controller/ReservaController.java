package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ModificarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarSalidaRequest;
import co.edu.uniquindio.sga.application.dto.response.ReservaResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.application.usecase.VerificarAccesoReservaUseCase;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Adaptador HTTP para las operaciones de reservas.
 *
 * <p>El autor de cada operación sale del token (usuario que inició sesión).
 * El header {@code X-Alojamiento-Id} se usa porque
 * los casos de uso necesitan esos datos, pero los DTOs y algunas rutas del
 * contrato actual no los exponen. Son una adaptación temporal hasta alinear
 * el contrato OpenAPI con las firmas de aplicación.</p>
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas")
@RequiredArgsConstructor
public class ReservaController {

    /** casos de uso del sistema y mappers*/

    private final RegistrarSalidaUseCase registrarSalidaUseCase;
    private final CrearReservaUseCase crearReservaUseCase;
    private final ModificarReservaUseCase modificarReservaUseCase;
    private final ConfirmarReservaUseCase confirmarReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final DeclararNoShowUseCase declararNoShowUseCase;
    private final RegistrarLlegadaUseCase registrarLlegadaUseCase;
    private final VerificarAccesoReservaUseCase verificarAccesoReservaUseCase;
    private final ReservaMapper reservaMapper;

    /**
     * Pendientes: listar y consultar reservas porque no existen sus casos de uso;
     * cotizar porque el request no permite construir todos los ocupantes; y
     * consultar disponibilidad porque tampoco existe su caso de uso.
     */

    /**
     * Registra la salida del grupo y cierra el folio según RN-17.
     */
    @PutMapping("/{codigo}/registrar-salida")
    @Operation(summary = "Registrar la salida del grupo (check-out)", description = "Aplica RN-17 y las reglas de salida definidas por el dominio.")
    @ApiResponse(responseCode = "200", description = "Salida registrada correctamente")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Reserva, apartamento o folio no encontrado")
    @ApiResponse(responseCode = "409", description = "La salida viola una regla de negocio")
    public ResponseEntity<ReservaResponse> registrarSalida(
	    @PathVariable String codigo,
	    @Valid @RequestBody RegistrarSalidaRequest request) {
	LocalDateTime ahora = LocalDateTime.now();
	AutorizacionCierre autorizacion = new AutorizacionCierre(
		request.autor(), request.motivo(), ahora);
	var reserva = registrarSalidaUseCase.ejecutar(
		new CodigoReserva(codigo), autorizacion, request.autor(), ahora);
	return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Crea una reserva, calcula su valor y abre el folio inicial. */
    @PostMapping
    @Operation(summary = "Crear una reserva", description = "Crea una reserva y abre su folio.")
    @ApiResponse(responseCode = "201", description = "Reserva creada correctamente")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Alojamiento o apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "La reserva viola una regla de negocio")
    public ResponseEntity<ReservaResponse> crearReserva(
        @RequestHeader("X-Alojamiento-Id") String alojamientoId,
        @Valid @RequestBody CrearReservaRequest request,
        @AuthenticationPrincipal UsuarioAutenticado usuario) {
    // 7.1: el huésped reserva por el PORTAL y a su propio nombre
    if (usuario.esHuesped()) {
        if (!CanalOrigen.PORTAL.name().equals(request.canalOrigen())) {
            throw new AccesoDenegadoException("Un huésped solo puede reservar por el canal PORTAL");
        }
        if (!usuario.documento().equals(request.titular().documento())) {
            throw new AccesoDenegadoException("Un huésped solo puede reservar a su propio nombre");
        }
    }
    LocalDate fechaActual = LocalDate.now();
    Ocupante titular = new Ocupante(
        new DocumentoIdentidad(request.titular().documento()),
        request.titular().nombre(),
        request.titular().fechaNacimiento(),
        fechaActual);
    var reserva = crearReservaUseCase.ejecutar(
        new IdentificacionAlojamiento(alojamientoId),
        new IdentificacionApartamento(request.apartamentoId()),
        reservaMapper.aEstancia(request),
        request.horaEstimadaLlegada(),
        titular,
        reservaMapper.aOcupantes(request.ocupantes(), fechaActual),
        CanalOrigen.valueOf(request.canalOrigen()),
        new CantidadMascota(request.cantidadMascotas()),
        new Correo(request.titular().email()),   // a dónde se le avisa al titular (7.8)
        LocalDateTime.now());
    ReservaResponse response = reservaMapper.aReserva(reserva);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{codigo}")
        .buildAndExpand(response.codigoReserva())
        .toUri();
    return ResponseEntity.created(location).body(response);
    }

    /** Modifica las fechas, apartamento y cantidad de mascotas de una reserva. */
    @PutMapping("/{codigo}")
    @Operation(summary = "Modificar una reserva", description = "Modifica la estancia de una reserva existente.")
    @ApiResponse(responseCode = "200", description = "Reserva modificada correctamente")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Reserva, alojamiento o apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "La modificación viola una regla de negocio")
    public ResponseEntity<ReservaResponse> modificarReserva(
        @PathVariable String codigo,
        @RequestHeader("X-Alojamiento-Id") String alojamientoId,
        @AuthenticationPrincipal UsuarioAutenticado usuario,
        @Valid @RequestBody ModificarReservaRequest request) {
    String autor = usuario.correo(); // quién hace la operación, sacado del token
    var reserva = modificarReservaUseCase.ejecutar(
        new CodigoReserva(codigo),
        new IdentificacionAlojamiento(alojamientoId),
        new IdentificacionApartamento(request.apartamentoId()),
        reservaMapper.aEstancia(request),
        null,
        new CantidadMascota(request.cantidadMascotas()),
        autor,
        LocalDateTime.now());
    return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Confirma una reserva después de validar el anticipo requerido. */
    @PutMapping("/{codigo}/confirmar")
    @Operation(summary = "Confirmar una reserva", description = "Confirma una reserva aplicando las reglas de anticipo.")
    @ApiResponse(responseCode = "200", description = "Reserva confirmada correctamente")
    @ApiResponse(responseCode = "404", description = "Reserva, folio o alojamiento no encontrado")
    @ApiResponse(responseCode = "409", description = "La reserva no puede confirmarse")
    public ResponseEntity<ReservaResponse> confirmarReserva(
        @PathVariable String codigo,
        @RequestHeader("X-Alojamiento-Id") String alojamientoId,
        @AuthenticationPrincipal UsuarioAutenticado usuario) {
    String autor = usuario.correo(); // quién hace la operación, sacado del token
    var reserva = confirmarReservaUseCase.ejecutar(
        new CodigoReserva(codigo),
        new IdentificacionAlojamiento(alojamientoId),
        autor,
        LocalDateTime.now());
    return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Cancela una reserva y aplica la política de cancelación congelada. */
    @PutMapping("/{codigo}/cancelar")
    @Operation(summary = "Cancelar una reserva", description = "Cancela una reserva aplicando su política congelada.")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Reserva, folio, política o alojamiento no encontrado")
    @ApiResponse(responseCode = "409", description = "La reserva no puede cancelarse")
    public ResponseEntity<ReservaResponse> cancelarReserva(
        @PathVariable String codigo,
        @RequestHeader("X-Alojamiento-Id") String alojamientoId,
        @AuthenticationPrincipal UsuarioAutenticado usuario,
        @Valid @RequestBody CancelarReservaRequest request) {
    String autor = usuario.correo(); // quién hace la operación, sacado del token
    // 7.2: el huésped solo cancela SU reserva (se verifica el propietario, no solo el rol)
    if (usuario.esHuesped()) {
        verificarAccesoReservaUseCase.verificarTitular(new CodigoReserva(codigo),
                new DocumentoIdentidad(usuario.documento()));
    }
    var reserva = cancelarReservaUseCase.ejecutar(
        new CodigoReserva(codigo),
        new IdentificacionAlojamiento(alojamientoId),
        request.motivo(),
        LocalDateTime.now(),
        autor);
    return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Registra que el titular no se presentó y aplica la retención correspondiente. */
    @PutMapping("/{codigo}/declarar-noshow")
    @Operation(summary = "Declarar no-show", description = "Declara que el titular no se presentó.")
    @ApiResponse(responseCode = "200", description = "No-show registrado correctamente")
    @ApiResponse(responseCode = "404", description = "Reserva, folio o alojamiento no encontrado")
    @ApiResponse(responseCode = "409", description = "La reserva no puede declararse no-show")
    public ResponseEntity<ReservaResponse> declararNoShow(
        @PathVariable String codigo,
        @RequestHeader("X-Alojamiento-Id") String alojamientoId,
        @AuthenticationPrincipal UsuarioAutenticado usuario) {
    String autor = usuario.correo(); // quién hace la operación, sacado del token
    var reserva = declararNoShowUseCase.ejecutar(
        new CodigoReserva(codigo),
        new IdentificacionAlojamiento(alojamientoId),
        autor,
        LocalDateTime.now());
    return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }

    /** Registra el check-in y cambia la reserva a estado en curso. */
    @PutMapping("/{codigo}/registrar-llegada")
    @Operation(summary = "Registrar llegada", description = "Registra el check-in del grupo.")
    @ApiResponse(responseCode = "200", description = "Llegada registrada correctamente")
    @ApiResponse(responseCode = "404", description = "Reserva, apartamento o folio no encontrado")
    @ApiResponse(responseCode = "409", description = "La llegada no puede registrarse")
    public ResponseEntity<ReservaResponse> registrarLlegada(
        @PathVariable String codigo,
        @AuthenticationPrincipal UsuarioAutenticado usuario) {
    String autor = usuario.correo(); // quién hace la operación, sacado del token
    var reserva = registrarLlegadaUseCase.ejecutar(
        new CodigoReserva(codigo), autor, LocalDateTime.now());
    return ResponseEntity.ok(reservaMapper.aReserva(reserva));
    }
}
