package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.CodigoRecuperacionInvalidoException;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.port.NotificadorUsuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 7.2 Recuperación de contraseña en dos pasos:
 *  1. solicitar: se genera un código de 6 dígitos, se guarda cifrado y se envía al correo.
 *  2. restablecer: con el código (15 minutos, un solo uso) se pone una contraseña nueva.
 *
 * Por seguridad, "solicitar" responde igual exista o no el correo: así nadie puede
 * averiguar qué correos tienen cuenta.
 */
public class RecuperarContrasenaUseCase {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasena cifrador;
    private final NotificadorUsuario notificador;
    private final Supplier<String> generadorCodigo;

    public RecuperarContrasenaUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador,
            NotificadorUsuario notificador) {
        this(usuarioRepository, cifrador, notificador,
                () -> String.format("%06d", ALEATORIO.nextInt(1_000_000)));
    }

    /** Las pruebas pasan un generador fijo para conocer el código. */
    public RecuperarContrasenaUseCase(UsuarioRepository usuarioRepository, CifradorContrasena cifrador,
            NotificadorUsuario notificador, Supplier<String> generadorCodigo) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
        this.notificador = notificador;
        this.generadorCodigo = generadorCodigo;
    }

    @Transactional
    public void solicitar(Correo correo, LocalDateTime ahora) {
        Optional<Usuario> encontrado = usuarioRepository.buscarPorCorreo(correo).filter(Usuario::estaActivo);
        if (encontrado.isEmpty()) {
            return;   // misma respuesta que si existiera
        }
        Usuario usuario = encontrado.get();
        String codigo = generadorCodigo.get();
        usuario.asignarCodigoRecuperacion(codigo, cifrador, ahora);
        usuarioRepository.guardar(usuario);
        notificador.codigoRecuperacion(usuario, codigo, Usuario.MINUTOS_VIGENCIA_CODIGO);
    }

    /** Si el código no sirve, se guarda igual el intento fallido (por eso no se revierte). */
    @Transactional(noRollbackFor = CodigoRecuperacionInvalidoException.class)
    public void restablecer(Correo correo, String codigo, String nuevaContrasena, LocalDateTime ahora) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(CodigoRecuperacionInvalidoException::new);
        boolean cambiada = usuario.restablecerContrasena(codigo, nuevaContrasena, cifrador, ahora);
        usuarioRepository.guardar(usuario);
        if (!cambiada) {
            throw new CodigoRecuperacionInvalidoException();
        }
    }
}
