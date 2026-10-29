package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Optional;

/** Puerto de salida del agregado Usuario. */
public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorCorreo(Correo correo);

    boolean existePorCorreo(Correo correo);

    boolean existePorDocumento(DocumentoIdentidad documento);
}
