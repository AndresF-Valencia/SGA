package co.edu.uniquindio.sga.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.application.exception.CredencialCanalInvalidaException;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.CanalRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Autenticar canal externo (10.3.1)")
class AutenticarCanalUseCaseTest {

    private CanalRepositoryEnMemoria canales;
    private AutenticarCanalUseCase autenticar;

    @BeforeEach
    void preparar() {
        canales = new CanalRepositoryEnMemoria();
        canales.guardar(new Canal(new IdentificacionCanal("C-1"), CanalOrigen.EXTERNO, "Booking", "clave-booking"));
        autenticar = new AutenticarCanalUseCase(canales);
    }

    @Test
    @DisplayName("con la credencial correcta devuelve el canal dueño")
    void credencialValida() {
        assertEquals("Booking", autenticar.ejecutar("clave-booking").getNombre());
    }

    @Test
    @DisplayName("sin credencial, con una equivocada o de un canal inactivo se rechaza")
    void credencialInvalida() {
        assertThrows(CredencialCanalInvalidaException.class, () -> autenticar.ejecutar(null));
        assertThrows(CredencialCanalInvalidaException.class, () -> autenticar.ejecutar(" "));
        assertThrows(CredencialCanalInvalidaException.class, () -> autenticar.ejecutar("otra"));

        Canal canal = canales.buscarPorCredencial("clave-booking").orElseThrow();
        canal.desactivar();
        canales.guardar(canal);
        assertThrows(CredencialCanalInvalidaException.class, () -> autenticar.ejecutar("clave-booking"));
    }
}
