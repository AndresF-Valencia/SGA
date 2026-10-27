package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.UsuarioPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepositoryJpa implements UsuarioRepository {

    private final UsuarioJpaDataRepository jpa;

    public UsuarioRepositoryJpa(UsuarioJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se puede guardar un usuario nulo");
        }
        jpa.save(UsuarioPersistenceMapper.aEntity(usuario));
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        if (correo == null) {
            return Optional.empty();
        }
        return jpa.findById(correo.valor()).map(UsuarioPersistenceMapper::aDominio);
    }

    @Override
    public boolean existePorCorreo(Correo correo) {
        return correo != null && jpa.existsById(correo.valor());
    }

    @Override
    public boolean existePorDocumento(DocumentoIdentidad documento) {
        return documento != null && jpa.existsByDocumento(documento.numero());
    }
}
