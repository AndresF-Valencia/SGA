package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.time.LocalDate;

/** Caso de uso: registro público. Siempre crea un HUÉSPED (7.2). */
public class RegistrarHuespedUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasena cifrador;

    public RegistrarHuespedUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    public Usuario ejecutar(Correo correo, String nombre, String contrasena, String telefono,
            DocumentoIdentidad documento, LocalDate fechaNacimiento, LocalDate hoy) {
        // 1. BUSCAR: el correo es único (7.2)
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new ReglaDominioException("Ya existe una cuenta con el correo " + correo.valor());
        }
        // 2. DELEGAR: el dominio valida los datos y la contraseña segura, y la cifra
        Usuario usuario = Usuario.registrarHuesped(correo, nombre, contrasena, telefono, documento,
                fechaNacimiento, cifrador, hoy);
        // 3. GUARDAR
        usuarioRepository.guardar(usuario);
        return usuario;
    }
}
