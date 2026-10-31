package co.edu.uniquindio.sga.application.usecase;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.persistence.memoria.ReservaRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Consultar una reserva por código")
class ConsultarReservaUseCaseTest {

    private Reserva reserva;
    private ConsultarReservaUseCase consultar;

    @BeforeEach
    void preparar() {
        ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
        reserva = reserva("RES-1", NOVIEMBRE);   // titular: LAURA
        reservas.guardar(reserva);
        consultar = new ConsultarReservaUseCase(reservas);
    }

    @Test
    @DisplayName("el personal consulta cualquier reserva")
    void personal() {
        assertEquals(reserva, consultar.ejecutar(reserva.getCodigo().valor()));
    }

    @Test
    @DisplayName("el huésped titular consulta su reserva")
    void titular() {
        assertEquals(reserva,
                consultar.ejecutarComoHuesped(reserva.getCodigo().valor(), LAURA.getDocumento()));
    }

    @Test
    @DisplayName("el huésped no consulta la reserva de otro (7.2)")
    void ajena() {
        String codigo = reserva.getCodigo().valor();
        DocumentoIdentidad otro = new DocumentoIdentidad("999");
        assertThrows(AccesoDenegadoException.class, () -> consultar.ejecutarComoHuesped(codigo, otro));
    }

    @Test
    @DisplayName("una reserva inexistente da no encontrada")
    void inexistente() {
        assertThrows(ReservaNoEncontradaException.class, () -> consultar.ejecutar("RES-2026-99999"));
    }
}
