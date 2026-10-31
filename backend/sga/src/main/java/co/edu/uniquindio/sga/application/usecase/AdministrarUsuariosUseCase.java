package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 7.2 Gestión de usuarios por el administrador: listar, consultar, cambiar el rol y
 * eliminar (lógico). La eliminación también la usa el propio usuario para cerrar su cuenta.
 *
 * Reglas:
 *  - No se elimina a quien sea titular de reservas PENDIENTE, CONFIRMADA o EN_CURSO.
 *  - Eliminar el usuario no toca al titular ni a sus reservas históricas.
 *  - El sistema nunca se queda sin un administrador activo.
 */
public class AdministrarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public AdministrarUsuariosUseCase(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    /** Filtros opcionales: rol y si está activo. Ordenados por nombre. */
    @Transactional(readOnly = true)
    public List<Usuario> listar(Rol rol, Boolean activo) {
        return usuarioRepository.listar().stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .filter(u -> activo == null || u.estaActivo() == activo)
                .sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario consultar(Correo correo) {
        return buscar(correo);
    }

    @Transactional
    public Usuario cambiarRol(Correo correo, Rol nuevoRol) {
        Usuario usuario = buscar(correo);
        if (usuario.getRol() == Rol.ADMINISTRADOR && nuevoRol != Rol.ADMINISTRADOR) {
            verificarQueNoEsElUltimoAdministrador(usuario);
        }
        usuario.cambiarRol(nuevoRol);
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    @Transactional
    public void eliminar(Correo correo) {
        Usuario usuario = buscar(correo);
        boolean tieneReservasActivas = reservaRepository.obtenerTodas().stream()
                .anyMatch(r -> r.getEstado().esActiva()
                        && r.getTitular().getDocumento().equals(usuario.getDocumento()));
        if (tieneReservasActivas) {
            throw new ReglaDominioException("No se puede eliminar: el usuario es titular de reservas "
                    + "pendientes, confirmadas o en curso");
        }
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            verificarQueNoEsElUltimoAdministrador(usuario);
        }
        usuario.desactivar();
        usuarioRepository.guardar(usuario);
    }

    private void verificarQueNoEsElUltimoAdministrador(Usuario usuario) {
        long otrosAdministradores = usuarioRepository.listar().stream()
                .filter(u -> u.getRol() == Rol.ADMINISTRADOR && u.estaActivo() && !u.equals(usuario))
                .count();
        if (otrosAdministradores == 0) {
            throw new ReglaDominioException("El sistema debe conservar al menos un administrador activo");
        }
    }

    private Usuario buscar(Correo correo) {
        return usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException(correo));
    }
}
