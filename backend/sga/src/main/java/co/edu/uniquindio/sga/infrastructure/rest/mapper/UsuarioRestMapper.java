package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.LoginResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.IniciarSesionUseCase;
import co.edu.uniquindio.sga.domain.entity.Usuario;

/** Usuario del dominio → DTOs de respuesta. Nunca expone la contraseña. */
public final class UsuarioRestMapper {

    private UsuarioRestMapper() {
    }

    public static UsuarioResponse aUsuario(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getCorreo().valor(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getDocumento().numero(),
                usuario.getFechaNacimiento(),
                usuario.getRol().name(),
                usuario.estaActivo());
    }

    public static LoginResponse aLogin(IniciarSesionUseCase.Sesion sesion) {
        return new LoginResponse(
                sesion.token().valor(),
                "Bearer",
                sesion.token().expiraEn(),
                aUsuario(sesion.usuario()));
    }
}
