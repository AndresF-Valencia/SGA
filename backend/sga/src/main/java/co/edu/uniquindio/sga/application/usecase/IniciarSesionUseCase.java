package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.port.EmisorToken;
import co.edu.uniquindio.sga.application.port.EmisorToken.TokenAcceso;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;

/**
 * Caso de uso: iniciar sesión. Si el correo y la contraseña son correctos y el
 * usuario está activo, devuelve un token (JWT) para las siguientes peticiones.
 */
public class IniciarSesionUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasena cifrador;
    private final EmisorToken emisorToken;

    public IniciarSesionUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador,
            EmisorToken emisorToken) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
        this.emisorToken = emisorToken;
    }

    public Sesion ejecutar(Correo correo, String contrasena) {
        // 1. BUSCAR: si el correo no existe, el error es el mismo que con mala contraseña
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);
        // 2. DELEGAR: el usuario decide si puede entrar (activo y contraseña correcta)
        if (!usuario.puedeIniciarSesion(contrasena, cifrador)) {
            throw new CredencialesInvalidasException();
        }
        return new Sesion(emisorToken.emitir(usuario), usuario);
    }

    public record Sesion(TokenAcceso token, Usuario usuario) {
    }
}
