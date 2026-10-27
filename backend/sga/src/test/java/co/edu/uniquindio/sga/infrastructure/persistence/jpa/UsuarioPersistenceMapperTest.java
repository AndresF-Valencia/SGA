package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.UsuarioPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("UsuarioPersistenceMapper")
class UsuarioPersistenceMapperTest {

    @Test
    @DisplayName("Conserva todos los datos, incluida la contraseña ya cifrada y el estado inactivo")
    void idaYVuelta() {
        Usuario original = Usuario.reconstruir(new Correo("recepcion@sanrocco.co"), "Recepción",
                "$2a$10$hashDePrueba", "3000000000", new DocumentoIdentidad("1000000002"),
                LocalDate.of(1990, 1, 1), Rol.RECEPCIONISTA, true);
        original.desactivar();

        Usuario copia = UsuarioPersistenceMapper.aDominio(UsuarioPersistenceMapper.aEntity(original));

        assertEquals(original.getCorreo(), copia.getCorreo());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getContrasenaCifrada(), copia.getContrasenaCifrada());
        assertEquals(original.getTelefono(), copia.getTelefono());
        assertEquals(original.getDocumento(), copia.getDocumento());
        assertEquals(original.getFechaNacimiento(), copia.getFechaNacimiento());
        assertEquals(Rol.RECEPCIONISTA, copia.getRol());
        assertFalse(copia.estaActivo());
    }
}
