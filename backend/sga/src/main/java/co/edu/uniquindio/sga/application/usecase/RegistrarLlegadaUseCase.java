package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RegistroLlegadaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import java.time.LocalDateTime;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;

/**
 * Caso de uso (Guía 06): registrar la llegada de un grupo (check-in).
 *
 * <p>
 * Coordina la operación, pero no decide ninguna regla de negocio:
 * <ol>
 * <li>Busca la reserva, su apartamento y su folio en los repositorios.</li>
 * <li>Delega en {@link RegistroLlegadaService}, que valida RN-08, RN-10, RN-11
 * y RNP-03, y hace las transiciones.</li>
 * <li>Guarda los dos agregados que cambiaron: la reserva (CONFIRMADA →
 * EN_CURSO)
 * y el apartamento (PREPARADO → OCUPADO).</li>
 * </ol>
 *
 * <p>
 * El folio no se guarda porque solo se consulta: el check-in exige que esté
 * pagado,
 * pero no le agrega movimientos.
 */
@Service
public class RegistrarLlegadaUseCase {

        private final ReservaRepository reservaRepository;
        private final ApartamentoRepository apartamentoRepository;
        private final FolioRepository folioRepository;
        private final RegistroLlegadaService registroLlegadaService;

        /**
         * Recibe sus dependencias como interfaces (puertos del dominio), no como
         * implementaciones concretas. Así el caso de uso funciona igual con los
         * repositorios en memoria o con base de datos.
         */
        public RegistrarLlegadaUseCase(ReservaRepository reservaRepository,
                        ApartamentoRepository apartamentoRepository,
                        FolioRepository folioRepository,
                        RegistroLlegadaService registroLlegadaService) {
                this.reservaRepository = reservaRepository;
                this.apartamentoRepository = apartamentoRepository;
                this.folioRepository = folioRepository;
                this.registroLlegadaService = registroLlegadaService;
        }

        /**
         * Registra la llegada del grupo de una reserva.
         * si el negocio no lo permite: reserva no confirmada (RN-08), antes de la
         * fecha de entrada (RN-10), apartamento no preparado (RN-11) o saldo
         * pendiente (RNP-03)
         */

        @Transactional
        public Reserva ejecutar(CodigoReserva codigo, String autor, LocalDateTime ahora) {
                // 1. BUSCAR: de lo que llega (el código) a los objetos del dominio
                Reserva reserva = reservaRepository.buscarPorCodigo(codigo)
                                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));
                // La reserva solo guarda la identificación de su apartamento (referencia por
                // id)

                Apartamento apartamento = apartamentoRepository.buscarPorId(reserva.getApartamento())
                                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamento()));

                Folio folio = folioRepository.buscarPorReserva(codigo)
                                .orElseThrow(() -> new FolioNoEncontradoException(codigo));

                // 2. DELEGAR: las reglas las decide el dominio
                registroLlegadaService.registrarLlegada(reserva, apartamento, folio, autor, ahora);

                // 3. GUARDAR: solo lo que cambió
                reservaRepository.guardar(reserva);
                apartamentoRepository.guardar(apartamento);
                return reserva;
        }
}
