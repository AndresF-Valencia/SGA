package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RegistroSalidaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import java.time.LocalDateTime;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;

/**
 * Caso de uso (Guía 06): registrar la salida de un grupo (check-out).
 * Delega en RegistroSalidaService, que cierra el folio (RN-17), finaliza la
 * reserva
 * y deja el apartamento PENDIENTE_PREPARACION (7.6). Guarda los tres agregados.
 * La autorización del administrador llega desde afuera: el caso de uso no
 * decide si autorizar.
 */
@Service
public class RegistrarSalidaUseCase {

    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final FolioRepository folioRepository;
    private final RegistroSalidaService registroSalidaService;

    /**
     * Recibe sus dependencias como interfaces (puertos del dominio), no como
     * implementaciones concretas. Así el caso de uso funciona igual con los
     * repositorios en memoria o con base de datos.
     */
    public RegistrarSalidaUseCase(ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            FolioRepository folioRepository,
            RegistroSalidaService registroSalidaService) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.folioRepository = folioRepository;
        this.registroSalidaService = registroSalidaService;
    }

    /**
     * Registra la salida del grupo de una reserva.
     *
     * código de la reserva
     * autorización del administrador para cerrar el folio con saldo;
     * null si no hace falta (RN-17)
     * quién registra la salida
     * momento de la salida
     * la reserva actualizada, en estado FINALIZADA
     * 
     * si la reserva no está EN_CURSO (RN-08) o si queda saldo sin autorización
     * (RN-17)
     */
        @Transactional
        public Reserva ejecutar(CodigoReserva codigo, AutorizacionCierre autorizacion,
            String autor, LocalDateTime ahora) {
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
        registroSalidaService.registrarSalida(reserva, apartamento, folio, autorizacion, autor, ahora);

        // 3. GUARDAR: solo lo que cambió
        reservaRepository.guardar(reserva);
        apartamentoRepository.guardar(apartamento);
        folioRepository.guardar(folio);
        return reserva;
    }
}
