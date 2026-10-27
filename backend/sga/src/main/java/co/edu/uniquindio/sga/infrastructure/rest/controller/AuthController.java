package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistroUsuarioRequest;
import co.edu.uniquindio.sga.application.dto.response.LoginResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.IniciarSesionUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarHuespedUseCase;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
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

/** Registro público e inicio de sesión. Son las únicas rutas de /api que no piden token. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final RegistrarHuespedUseCase registrarHuespedUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;

    public AuthController(RegistrarHuespedUseCase registrarHuespedUseCase,
                          IniciarSesionUseCase iniciarSesionUseCase) {
        this.registrarHuespedUseCase = registrarHuespedUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @Operation(summary = "Registrarse como huésped")
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        var usuario = registrarHuespedUseCase.ejecutar(
                new Correo(request.correo()),
                request.nombre(),
                request.contrasena(),
                request.telefono(),
                new DocumentoIdentidad(request.documento()),
                request.fechaNacimiento(),
                LocalDate.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioRestMapper.aUsuario(usuario));
    }

    @Operation(summary = "Iniciar sesión y obtener el token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        var sesion = iniciarSesionUseCase.ejecutar(new Correo(request.correo()), request.contrasena());
        return ResponseEntity.ok(UsuarioRestMapper.aLogin(sesion));
    }
}
