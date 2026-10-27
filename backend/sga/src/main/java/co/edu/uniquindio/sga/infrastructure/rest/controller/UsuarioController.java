package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioRequest;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.CrearUsuarioInternoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Gestión de usuarios y roles. Solo ADMINISTRADOR (lo controla SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final CrearUsuarioInternoUseCase crearUsuarioInternoUseCase;

    public UsuarioController(CrearUsuarioInternoUseCase crearUsuarioInternoUseCase) {
        this.crearUsuarioInternoUseCase = crearUsuarioInternoUseCase;
    }

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
}
