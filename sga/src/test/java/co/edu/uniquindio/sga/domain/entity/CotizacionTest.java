package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

@DisplayName("Cotizacion")
class CotizacionTest {

    private static final IdentificacionCotizacion ID = new IdentificacionCotizacion("COT-1");
    private static final IdentificacionApartamento APTO = new IdentificacionApartamento("APTO-101");
    private static final Estancia DOS_NOCHES = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 12, 10, 0);

    private static ValorCongelado valor(int noches) {
        return new ValorCongelado(DOS_NOCHES.fechaEntrada().datesUntil(DOS_NOCHES.fechaEntrada().plusDays(noches))
                .map(noche -> new CargoNoche(noche, "Base", Dinero.de(100_000), 2))
                .toList());
    }

    @Test
    @DisplayName("El total es la suma del desglose noche por noche")
    void totalDelDesglose() {
        Cotizacion cotizacion = new Cotizacion(ID, APTO, DOS_NOCHES, 2, null, valor(2), AHORA);

        assertEquals(Dinero.de(400_000), cotizacion.getMontoTotal());
        assertEquals(0, cotizacion.getCantidadMascota().cantidad());
    }

    @Test
    @DisplayName("Exige identificación y un desglose que corresponda a la estancia")
    void validaDatos() {
        assertThrows(ReglaDominioException.class,
                () -> new Cotizacion(null, APTO, DOS_NOCHES, 2, null, valor(2), AHORA));
        assertThrows(ReglaDominioException.class,
                () -> new Cotizacion(ID, APTO, DOS_NOCHES, 2, null, valor(3), AHORA));
        assertThrows(ReglaDominioException.class,
                () -> new Cotizacion(ID, APTO, DOS_NOCHES, 0, null, valor(2), AHORA));
    }
}
