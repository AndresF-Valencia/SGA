package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;

public final class UsuarioPersistenceMapper {

    private UsuarioPersistenceMapper() {
    }

    public static UsuarioEntity aEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo(usuario.getCorreo().valor());
        entity.setNombre(usuario.getNombre());
        entity.setContrasenaCifrada(usuario.getContrasenaCifrada());
        entity.setTelefono(usuario.getTelefono());
        entity.setDocumento(usuario.getDocumento().numero());
        entity.setFechaNacimiento(usuario.getFechaNacimiento());
        entity.setRol(usuario.getRol());
        entity.setActivo(usuario.estaActivo());
        return entity;
    }

    public static Usuario aDominio(UsuarioEntity entity) {
        return Usuario.reconstruir(
                new Correo(entity.getCorreo()),
                entity.getNombre(),
                entity.getContrasenaCifrada(),
                entity.getTelefono(),
                new DocumentoIdentidad(entity.getDocumento()),
                entity.getFechaNacimiento(),
                entity.getRol(),
                entity.isActivo());
    }
}
