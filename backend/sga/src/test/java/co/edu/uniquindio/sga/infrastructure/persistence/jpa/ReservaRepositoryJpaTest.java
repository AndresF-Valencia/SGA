package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Prueba de integración: arranca Spring con H2 y guarda reservas DE VERDAD.
 * Cada prueba usa su propio apartamento y sus propios códigos, porque la base
 * en memoria se comparte entre todas las pruebas de la misma ejecución.
 */
@SpringBootTest
@DisplayName("ReservaRepositoryJpa (H2)")
class ReservaRepositoryJpaTest {

    @Autowired
    private ReservaRepository repositorio;

    @Test
    @DisplayName("Spring entrega el adaptador JPA, no el de memoria")
    void usaElAdaptadorJpa() {
        assertInstanceOf(ReservaRepositoryJpa.class, repositorio);
    }

    @Test
    @DisplayName("Guarda una reserva y la lee igual desde la base")
    void guardaYLee() {
        Reserva original = reservaConfirmada("RES-901", NOVIEMBRE);

        repositorio.guardar(original);
        Reserva leida = repositorio.buscarPorCodigo(original.getCodigo()).orElseThrow();

        assertEquals(original.getEstado(), leida.getEstado());
        assertEquals(original.getEstancia(), leida.getEstancia());
        assertEquals(original.getOcupantes(), leida.getOcupantes());
        assertEquals(original.getValor(), leida.getValor());
        assertEquals(original.getHoraEstimadaLlegada(), leida.getHoraEstimadaLlegada());
        assertEquals(original.obtenerHistorial(), leida.obtenerHistorial());
    }

    @Test
    @DisplayName("Guardar de nuevo actualiza la fila: el estado y el historial cambian")
    void actualiza() {
        Reserva reserva = reserva("RES-902", NOVIEMBRE);
        repositorio.guardar(reserva);

        Reserva leida = repositorio.buscarPorCodigo(reserva.getCodigo()).orElseThrow();
        leida.cancelar("Cambio de planes", RECEPCION, AHORA);
        repositorio.guardar(leida);

        Reserva otraVez = repositorio.buscarPorCodigo(reserva.getCodigo()).orElseThrow();
        assertEquals(EstadoReserva.CANCELADA, otraVez.getEstado());
        assertEquals(2, otraVez.obtenerHistorial().size());
    }

    @Test
    @DisplayName("Activas por apartamento: excluye las canceladas y las de otro apartamento")
    void activasPorApartamento() {
        IdentificacionApartamento apto = new IdentificacionApartamento("APTO-JPA-1");
        Reserva pendiente = reserva("RES-903", apto, NOVIEMBRE);
        Reserva cancelada = reserva("RES-904", apto, NOVIEMBRE);
        cancelada.cancelar("Desistió", RECEPCION, AHORA);
        Reserva otroApto = reserva("RES-905", new IdentificacionApartamento("APTO-JPA-2"), NOVIEMBRE);
        List.of(pendiente, cancelada, otroApto).forEach(repositorio::guardar);

        List<Reserva> activas = repositorio.buscarActivasPorApartamento(apto);

        assertEquals(List.of(pendiente.getCodigo()), activas.stream().map(Reserva::getCodigo).toList());
    }

    @Test
    @DisplayName("Un código que no existe da Optional vacío")
    void noExiste() {
        assertTrue(repositorio.buscarPorCodigo(codigo("RES-99999")).isEmpty());
    }
}
