package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CerrarFolioRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarCargoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.usecase.CerrarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarCargoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarPagoUseCase;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.FolioMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/reservas/{codigo}/folio")
@Tag(name = "Folio y Finanzas")
@RequiredArgsConstructor
public class FolioController {

    private final RegistrarCargoUseCase registrarCargoUseCase;
    private final ConsultarFolioUseCase consultarFolioUseCase;
    private final ConsultarReservaUseCase consultarReservaUseCase;
    private final RegistrarPagoUseCase registrarPagoUseCase;
    private final CerrarFolioUseCase cerrarFolioUseCase;
    private final FolioMapper folioMapper;

    /** Registra un cargo adicional y devuelve el estado actualizado del folio. */
    @PostMapping("/cargos")
    @Operation(summary = "Registrar un cargo en el folio", description = "Agrega un servicio adicional al folio.")
    @ApiResponse(responseCode = "201", description = "Cargo registrado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    @ApiResponse(responseCode = "409", description = "El folio no permite registrar el cargo")
    public ResponseEntity<FolioResponse> registrarCargo(
            @PathVariable String codigo,
            @Valid @RequestBody RegistrarCargoRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        Cargo cargo = new Cargo(
                Dinero.de(request.monto()),
                request.concepto(),
                TipoCargo.SERVICIO_ADICIONAL,
                ahora);
        
        CodigoReserva codigoReserva = resolverCodigoReserva(codigo); 

        var folio = registrarCargoUseCase.ejecutar(codigoReserva, cargo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(folioMapper.aFolio(folio));
    }

    /** Registra un pago con la fecha proporcionada por el cliente. */
    @PostMapping("/pagos")
    @Operation(summary = "Registrar un pago", description = "Registra un pago y actualiza el saldo del folio.")
    @ApiResponse(responseCode = "201", description = "Pago registrado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    @ApiResponse(responseCode = "409", description = "El folio no permite registrar el pago")
    public ResponseEntity<FolioResponse> registrarPago(
            @PathVariable String codigo,
            @Valid @RequestBody RegistrarPagoRequest request) {
        Pago pago = new Pago(
                Dinero.de(request.monto()),
                MedioPago.valueOf(request.medioPago()),
                request.fechaPago().atStartOfDay());
        
        CodigoReserva codigoReserva = resolverCodigoReserva(codigo);
        var folio = registrarPagoUseCase.ejecutar(codigoReserva, pago);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(folioMapper.aFolio(folio));
    }

    /** Cierra el folio, usando la autorización enviada si el saldo lo exige. */
    @PutMapping("/cerrar")
    @Operation(summary = "Cerrar el folio", description = "Cierra el folio aplicando RN-17.")
    @ApiResponse(responseCode = "200", description = "Folio cerrado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    @ApiResponse(responseCode = "409", description = "El folio no puede cerrarse")
    public ResponseEntity<FolioResponse> cerrarFolio(
            @PathVariable String codigo,
            @Valid @RequestBody CerrarFolioRequest request) {
        AutorizacionCierre autorizacion = new AutorizacionCierre(
                request.autor(), request.motivo(), LocalDateTime.now());
        
        CodigoReserva codigoReserva = resolverCodigoReserva(codigo);
        var folio = cerrarFolioUseCase.ejecutar(codigoReserva, autorizacion);
        return ResponseEntity.ok(folioMapper.aFolio(folio));
    }

    /** Consultar el folio de una reserva (cargos, pagos y saldo pendiente) */
    @GetMapping
    @Operation(summary = "Consultar folio", description = "Obtiene el estado financiero, cargos y pagos del folio.")
    @ApiResponse(responseCode = "200", description = "Folio encontrado")
    @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    public ResponseEntity<FolioResponse> consultarFolio(@PathVariable String codigo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        // 7.2: un huésped solo ve el folio de SUS reservas (se verifica el titular, no solo el rol)
        if (usuario.esHuesped()) {
            consultarReservaUseCase.ejecutarComoHuesped(codigo, new DocumentoIdentidad(usuario.documento()));
        }
        // Soporta código interno (RES-...) o externo del canal
        Folio folio = consultarFolioUseCase.ejecutar(codigo);
        return ResponseEntity.ok(folioMapper.aFolio(folio));
    }

    /**
     * Helper privado para soportar tanto códigos internos (RES-...) como externos en los POST/PUT del folio.
     * Si deseas simplificarlo, puedes inyectar el ReservaRepository o crear un método auxiliar.
     */
    private CodigoReserva resolverCodigoReserva(String codigo) {
        if (codigo.startsWith("RES-")) {
            return new CodigoReserva(codigo);
        }
        // Si es externo, consultamos el folio usando el caso de uso de consulta que ya resuelve ambos mundos
        Folio folio = consultarFolioUseCase.ejecutar(codigo);
        return folio.getReserva();
    }
}