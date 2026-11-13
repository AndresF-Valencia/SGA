package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.time.LocalDateTime;

public class CorregirMovimientoUseCase {

    private final FolioRepository folioRepository;

    public CorregirMovimientoUseCase(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    @Transactional
    public Folio ejecutar(CodigoReserva codigoReserva, String movimientoIdOriginal, String motivo, String autor, LocalDateTime ahora) {
        if (codigoReserva == null || movimientoIdOriginal == null || movimientoIdOriginal.isBlank()) {
            throw new ReglaDominioException("El código de reserva y el ID del movimiento original son obligatorios");
        }

        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new FolioNoEncontradoException(codigoReserva));

        // Buscar el cargo original en el folio por coincidencia en su descripción o identificador
        Cargo cargoOriginal = folio.getCargos().stream()
                .filter(c -> c.descripcion().equals(movimientoIdOriginal) || c.toString().contains(movimientoIdOriginal))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("No se encontró el movimiento original con ID o concepto: " + movimientoIdOriginal));

        // Crear movimiento inverso (compensatorio) asegurando compatibilidad aritmética con Dinero
        Dinero valorInverso = Dinero.CERO.menos(cargoOriginal.valor());
        Cargo cargoInverso = new Cargo(
                valorInverso, 
                "Corrección (" + cargoOriginal.descripcion() + "): " + motivo, 
                TipoCargo.AJUSTE, 
                ahora
        );

        folio.registrarCargo(cargoInverso);
        folioRepository.guardar(folio);
        return folio;
    }
}