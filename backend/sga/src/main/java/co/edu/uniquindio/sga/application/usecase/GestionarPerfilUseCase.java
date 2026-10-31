package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import org.springframework.transaction.annotation.Transactional;

/**
 * 7.2 Perfil y contraseña: cada usuario consulta y edita SU perfil.
 * El correo sale del token, así que nadie puede tocar el perfil de otro.
 */
public class GestionarPerfilUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasena cifrador;

    public GestionarPerfilUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    @Transactional(readOnly = true)
    public Usuario consultar(Correo correo) {
        return buscar(correo);
    }

    @Transactional
    public Usuario actualizar(Correo correo, String nombre, String telefono, String fotoPerfil) {
        Usuario usuario = buscar(correo);
        usuario.actualizarPerfil(nombre, telefono, fotoPerfil);
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    @Transactional
    public void cambiarContrasena(Correo correo, String actual, String nueva) {
        Usuario usuario = buscar(correo);
        usuario.cambiarContrasena(actual, nueva, cifrador);
        usuarioRepository.guardar(usuario);
    }

    private Usuario buscar(Correo correo) {
        return usuarioRepository.buscarPorCorreo(correo)
                .filter(Usuario::estaActivo)
                .orElseThrow(() -> new UsuarioNoEncontradoException(correo));
    }
}
