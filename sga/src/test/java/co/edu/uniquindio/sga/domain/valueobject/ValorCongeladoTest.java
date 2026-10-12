package co.edu.uniquindio.sga.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

@DisplayName("ValorCongelado y CargoNoche")
class ValorCongeladoTest {

    private static final LocalDate NOCHE = LocalDate.of(2026, 11, 10);

    private static CargoNoche cargo(int dia, long tarifa, int ocupantes) {
        return new CargoNoche(LocalDate.of(2026, 11, dia), "BASE", Dinero.de(tarifa), ocupantes);
    }

    // ---------- CargoNoche ----------

    @Test
    @DisplayName("RN-05: el subtotal es tarifa por ocupantes facturables")
    void subtotalDeLaNoche() {
        assertEquals(Dinero.de(300_000), cargo(10, 100_000, 3).subtotal());
    }

    @Test
    @DisplayName("Una noche sin ocupantes facturables no cobra nada")
    void nocheSinFacturables() {
        assertTrue(cargo(10, 100_000, 0).subtotal().esCero());
    }

    @Test
    @DisplayName("El cargo de la noche valida sus datos")
    void validaCargoNoche() {
        Dinero tarifa = Dinero.de(100_000);

        assertThrows(ReglaDominioException.class, () -> new CargoNoche(null, "BASE", tarifa, 1));
        assertThrows(ReglaDominioException.class, () -> new CargoNoche(NOCHE, " ", tarifa, 1));
        assertThrows(ReglaDominioException.class, () -> new CargoNoche(NOCHE, "BASE", null, 1));
        assertThrows(ReglaDominioException.class,
                () -> new CargoNoche(NOCHE, "BASE", Dinero.de(-1), 1));
        assertThrows(ReglaDominioException.class, () -> new CargoNoche(NOCHE, "BASE", tarifa, -1));
    }

    // ---------- ValorCongelado ----------

    @Test
    @DisplayName("El total suma el subtotal de cada noche")
    void sumaTotal() {
        ValorCongelado valor = new ValorCongelado(List.of(
                cargo(10, 100_000, 2),
                cargo(11, 150_000, 2)));

        assertEquals(Dinero.de(500_000), valor.total());
        assertEquals(2, valor.noches());
    }

    @Test
    @DisplayName("Exige desglose por noche")
    void exigeDesglose() {
        assertThrows(ReglaDominioException.class, () -> new ValorCongelado(null));
        assertThrows(ReglaDominioException.class, () -> new ValorCongelado(List.of()));
    }

    @Test
    @DisplayName("RN-22: cambiar la lista original no altera el valor congelado")
    void copiaDefensiva() {
        List<CargoNoche> lista = new ArrayList<>(List.of(cargo(10, 100_000, 1)));
        ValorCongelado valor = new ValorCongelado(lista);

        lista.add(cargo(11, 999_999, 5));

        assertEquals(1, valor.noches());
        assertEquals(Dinero.de(100_000), valor.total());
    }

    @Test
    @DisplayName("RN-22: el desglose no se puede modificar desde afuera")
    void desgloseInmodificable() {
        ValorCongelado valor = new ValorCongelado(List.of(cargo(10, 100_000, 1)));

        assertThrows(UnsupportedOperationException.class,
                () -> valor.detalle().add(cargo(11, 1, 1)));
    }
}
