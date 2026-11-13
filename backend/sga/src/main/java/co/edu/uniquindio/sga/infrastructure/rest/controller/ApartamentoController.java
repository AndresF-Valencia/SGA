package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarEstadoOperativoRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarNovedadRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoEstadoResponse;
import co.edu.uniquindio.sga.application.dto.response.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.usecase.CambiarEstadoOperativoUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarNovedadUseCase;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apartamentos")
@Tag(name = "Apartamentos y Operación")
@RequiredArgsConstructor
public class ApartamentoController {

    private final CambiarEstadoOperativoUseCase cambiarEstadoOperativoUseCase;
    private final GestionarApartamentoUseCase gestionarApartamentoUseCase;
    private final ApartamentoMapper apartamentoMapper;
    private final RegistrarNovedadUseCase registrarNovedadUseCase;

    @PostMapping
    @Operation(summary = "Crear un apartamento", description = "Se crea inactivo; se activa cuando tenga tarifas completas.")
    @ApiResponse(responseCode = "201", description = "Apartamento creado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "409", description = "El apartamento ya existe")
    public ResponseEntity<ApartamentoDetalleResponse> crear(@Valid @RequestBody CrearApartamentoRequest request) {
        var apartamento = gestionarApartamentoUseCase.crear(
                new IdentificacionApartamento(request.identificacion()),
                request.nombre(),
                new TiempoPreparacion(request.tiempoPreparacionHoras()),
                new Dormitorio(request.dormitorios()),
                new CapacidadOcupantes(request.capacidadMaxima()),
                new LimiteMascota(request.limiteMascotas()),
                request.caracteristicas(),
                request.imagenes());
        URI location = URI.create("/api/apartamentos/" + apartamento.getIdentificacion().valor());
        return ResponseEntity.created(location).body(apartamentoMapper.aApartamento(apartamento));
    }

    @GetMapping
    @Operation(summary = "Listar los apartamentos")
    @ApiResponse(responseCode = "200", description = "Listado de apartamentos")
    public ResponseEntity<List<ApartamentoDetalleResponse>> listar() {
        return ResponseEntity.ok(gestionarApartamentoUseCase.listar().stream()
                .map(apartamentoMapper::aApartamento)
                .toList());
    }

    @GetMapping("/{identificacion}")
    @Operation(summary = "Consultar un apartamento")
    @ApiResponse(responseCode = "200", description = "Detalle del apartamento")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    public ResponseEntity<ApartamentoDetalleResponse> obtener(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.obtener(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}")
    @Operation(summary = "Actualizar un apartamento")
    @ApiResponse(responseCode = "200", description = "Apartamento actualizado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    public ResponseEntity<ApartamentoDetalleResponse> actualizar(
            @PathVariable String identificacion,
            @Valid @RequestBody ActualizarApartamentoRequest request) {
        var apartamento = gestionarApartamentoUseCase.actualizar(
                new IdentificacionApartamento(identificacion),
                request.nombre(),
                new TiempoPreparacion(request.tiempoPreparacionHoras()),
                new Dormitorio(request.dormitorios()),
                new CapacidadOcupantes(request.capacidadMaxima()),
                new LimiteMascota(request.limiteMascotas()),
                request.caracteristicas(),
                request.imagenes());
        return ResponseEntity.ok(apartamentoMapper.aApartamento(apartamento));
    }

    /**
     * Eliminación lógica: deshabilita el apartamento si no tiene reservas activas.
     */
    @DeleteMapping("/{identificacion}")
    @Operation(summary = "Deshabilitar un apartamento", description = "Solo si no tiene reservas activas.")
    @ApiResponse(responseCode = "200", description = "Apartamento deshabilitado")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Tiene reservas activas")
    public ResponseEntity<ApartamentoDetalleResponse> desactivar(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.desactivar(new IdentificacionApartamento(identificacion))));
    }

    @PostMapping("/{identificacion}/activar")
    @Operation(summary = "Activar un apartamento", description = "Solo si tiene tarifa en todas las temporadas.")
    @ApiResponse(responseCode = "200", description = "Apartamento activado")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Tarifas incompletas")
    public ResponseEntity<ApartamentoDetalleResponse> activar(@PathVariable String identificacion) {
        return ResponseEntity.ok(apartamentoMapper.aApartamento(
                gestionarApartamentoUseCase.activar(new IdentificacionApartamento(identificacion))));
    }

    /** Cambia el estado operativo aplicando las transiciones del dominio. */
    @PostMapping("/{identificacion}/estado-operativo")
    @Operation(summary = "Cambiar el estado operativo de un apartamento")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Apartamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Transicion no permitida")
    public ResponseEntity<ApartamentoDetalleResponse> cambiarEstadoOperativo(
            @PathVariable String identificacion,
            @Valid @RequestBody CambiarEstadoOperativoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        // 7.1: el personal de servicio NO puede declarar FUERA_DE_SERVICIO (es decisión
        // administrativa)
        if (usuario.rol() == Rol.PERSONAL_SERVICIO && request.nuevoEstado() == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new AccesoDenegadoException("Solo el administrador puede poner un apartamento fuera de servicio");
        }
        var apartamento = cambiarEstadoOperativoUseCase.ejecutar(
                new IdentificacionApartamento(identificacion), request.nuevoEstado());
        return ResponseEntity.ok(apartamentoMapper.aApartamento(apartamento));
    }

    @GetMapping("/estado")
    @Operation(summary = "Consultar el estado operativo de los apartamentos", description = "Vista general de la operación (7.6).")
    @ApiResponse(responseCode = "200", description = "Estado operativo actual")
    public ResponseEntity<ApartamentoEstadoResponse> consultarEstadoOperativo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Puedes delegar a un caso de uso o servicio que agrupe el estado de los
        // apartamentos
        List<ApartamentoEstadoResponse.ApartamentoEstadoDetalleDto> detalles = gestionarApartamentoUseCase.listar()
                .stream()
                .map(ap -> new ApartamentoEstadoResponse.ApartamentoEstadoDetalleDto(
                        ap.getIdentificacion().valor(),
                        ap.getNombre(),
                        ap.getEstadoOperativo().name(),
                        null, // Código de reserva actual si aplica
                        ap.getNovedades().isEmpty() ? "Sin novedades"
                                : ap.getNovedades().get(ap.getNovedades().size() - 1).descripcion()))
                .toList();

        return ResponseEntity.ok(new ApartamentoEstadoResponse(detalles.size(), detalles));
    }

    @PostMapping("/{identificacion}/novedades")
    @Operation(summary = "Registrar una novedad en un apartamento", description = "Reporte de daños o incidencias (7.6).")
    @ApiResponse(responseCode = "201", description = "Novedad registrada")
    public ResponseEntity<NovedadResponse> registrarNovedad(
            @PathVariable String identificacion,
            @Valid @RequestBody RegistrarNovedadRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Novedad novedad = Novedad.de(
                request.descripcion(),
                request.gravedad(),
                usuario.nombre(),
                LocalDateTime.now());

        registrarNovedadUseCase.ejecutar(new IdentificacionApartamento(identificacion), novedad);

        NovedadResponse response = new NovedadResponse(
                "NOV-" + System.currentTimeMillis(),
                identificacion,
                novedad.gravedad(),
                novedad.descripcion(),
                "PENDIENTE",
                novedad.fecha(),
                novedad.autor());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{identificacion}/novedades")
    @Operation(summary = "Consultar el historial de novedades de un apartamento")
    @ApiResponse(responseCode = "200", description = "Página de novedades")
    public ResponseEntity<PaginaResponse<NovedadResponse>> consultarNovedades(
            @PathVariable String identificacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var apto = gestionarApartamentoUseCase.obtener(new IdentificacionApartamento(identificacion));
        List<NovedadResponse> lista = apto.getNovedades().stream()
                .map(n -> new NovedadResponse(
                        "NOV-HIST", identificacion, n.gravedad(), n.descripcion(), "REGISTRADA", n.fecha(),
                        n.autor()))
                .toList();

        PaginaResponse<NovedadResponse> pagina = PaginaResponse.de(lista, page, size);
        return ResponseEntity.ok(pagina);
    }
}