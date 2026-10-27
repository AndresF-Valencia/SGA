package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.time.LocalDate;

/**
 * Caso de uso: el ADMINISTRADOR crea un usuario con el rol que elija (7.2:
 * "los roles internos solo los asigna un Administrador"). Que solo el
 * administrador llegue aquí lo garantiza la seguridad del endpoint.
 */
public class CrearUsuarioInternoUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasena cifrador;

    public CrearUsuarioInternoUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    public Usuario ejecutar(Correo correo, String nombre, String contrasena, String telefono,
            DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol, LocalDate hoy) {
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new ReglaDominioException("Ya existe una cuenta con el correo " + correo.valor());
        }
        Usuario usuario = Usuario.registrar(correo, nombre, contrasena, telefono, documento,
                fechaNacimiento, rol, cifrador, hoy);
        usuarioRepository.guardar(usuario);
        return usuario;
    }
}
