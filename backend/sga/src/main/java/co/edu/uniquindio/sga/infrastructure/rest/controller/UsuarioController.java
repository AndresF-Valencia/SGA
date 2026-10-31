package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarPerfilRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarRolRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.AdministrarUsuariosUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearUsuarioInternoUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarPerfilUseCase;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDate;

/**
 * Usuarios (7.2).
 *  - /me: cualquier usuario con sesión, sobre SU propia cuenta (el correo sale del token).
 *  - El resto: solo ADMINISTRADOR (lo controla SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final CrearUsuarioInternoUseCase crearUsuarioInternoUseCase;
    private final GestionarPerfilUseCase gestionarPerfilUseCase;
    private final AdministrarUsuariosUseCase administrarUsuariosUseCase;

    public UsuarioController(CrearUsuarioInternoUseCase crearUsuarioInternoUseCase,
            GestionarPerfilUseCase gestionarPerfilUseCase,
            AdministrarUsuariosUseCase administrarUsuariosUseCase) {
        this.crearUsuarioInternoUseCase = crearUsuarioInternoUseCase;
        this.gestionarPerfilUseCase = gestionarPerfilUseCase;
        this.administrarUsuariosUseCase = administrarUsuariosUseCase;
    }

    // ---------- Mi cuenta ----------

    @Operation(summary = "Consultar mi perfil")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return ResponseEntity.ok(UsuarioRestMapper.aUsuario(gestionarPerfilUseCase.consultar(new Correo(yo.correo()))));
    }

    @Operation(summary = "Actualizar mi nombre, teléfono y foto (el correo no se edita)")
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@AuthenticationPrincipal UsuarioAutenticado yo,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        var usuario = gestionarPerfilUseCase.actualizar(new Correo(yo.correo()),
                request.nombre(), request.telefono(), request.fotoPerfil());
        return ResponseEntity.ok(UsuarioRestMapper.aUsuario(usuario));
    }

    @Operation(summary = "Cambiar mi contraseña (con la actual y la nueva)")
    @PutMapping("/me/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@AuthenticationPrincipal UsuarioAutenticado yo,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        gestionarPerfilUseCase.cambiarContrasena(new Correo(yo.correo()),
                request.contrasenaActual(), request.nuevaContrasena());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar mi cuenta (lógico; mis reservas históricas se conservan)")
    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarMiCuenta(@AuthenticationPrincipal UsuarioAutenticado yo) {
        administrarUsuariosUseCase.eliminar(new Correo(yo.correo()));
        return ResponseEntity.noContent().build();
    }

    // ---------- Administración ----------

    @Operation(summary = "Crear un usuario con su rol (solo administrador)")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        var usuario = crearUsuarioInternoUseCase.ejecutar(
                new Correo(request.correo()),
                request.nombre(),
                request.contrasena(),
                request.telefono(),
                new DocumentoIdentidad(request.documento()),
                request.fechaNacimiento(),
                Rol.valueOf(request.rol()),   // un rol que no existe → IllegalArgumentException → 400
                LocalDate.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioRestMapper.aUsuario(usuario));
    }

    @Operation(summary = "Listar usuarios, con filtros opcionales por rol y estado (solo administrador)")
    @GetMapping
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listar(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int tamano) {
        var usuarios = administrarUsuariosUseCase.listar(rol == null ? null : Rol.valueOf(rol), activo).stream()
                .map(UsuarioRestMapper::aUsuario)
                .toList();
        return ResponseEntity.ok(PaginaResponse.de(usuarios, pagina, tamano));
    }

    @Operation(summary = "Consultar un usuario (solo administrador)")
    @GetMapping("/{correo}")
    public ResponseEntity<UsuarioResponse> consultar(@PathVariable String correo) {
        return ResponseEntity.ok(UsuarioRestMapper.aUsuario(administrarUsuariosUseCase.consultar(new Correo(correo))));
    }

    @Operation(summary = "Cambiar el rol de un usuario (solo administrador)")
    @PutMapping("/{correo}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(@PathVariable String correo,
            @Valid @RequestBody CambiarRolRequest request) {
        var usuario = administrarUsuariosUseCase.cambiarRol(new Correo(correo), Rol.valueOf(request.rol()));
        return ResponseEntity.ok(UsuarioRestMapper.aUsuario(usuario));
    }

    @Operation(summary = "Eliminar un usuario (lógico; solo administrador)")
    @DeleteMapping("/{correo}")
    public ResponseEntity<Void> eliminar(@PathVariable String correo) {
        administrarUsuariosUseCase.eliminar(new Correo(correo));
        return ResponseEntity.noContent().build();
    }
}
