package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Optional;

// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class UsuarioRepositoryEnMemoria extends AlmacenEnMemoria<Correo, Usuario> implements UsuarioRepository {

    public UsuarioRepositoryEnMemoria() {
        super(Usuario::getCorreo);
    }

    @Override
    public void guardar(Usuario usuario) {
        almacenar(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        return porId(correo);
    }

    @Override
    public boolean existePorCorreo(Correo correo) {
        return porId(correo).isPresent();
    }

    @Override
    public boolean existePorDocumento(DocumentoIdentidad documento) {
        return !filtrar(u -> u.getDocumento().equals(documento)).isEmpty();
    }
}
