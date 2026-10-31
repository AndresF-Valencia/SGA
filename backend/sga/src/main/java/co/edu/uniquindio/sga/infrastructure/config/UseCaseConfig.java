package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.port.EmisorToken;
import co.edu.uniquindio.sga.application.usecase.CambiarEstadoOperativoUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearUsuarioInternoUseCase;
import co.edu.uniquindio.sga.application.usecase.IniciarSesionUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarHuespedUseCase;
import co.edu.uniquindio.sga.application.usecase.VerificarAccesoReservaUseCase;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CerrarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfigurarParametrosAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarDisponibilidadUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CorregirMovimientoUseCase;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarBloqueoUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasTarifasUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.PublicarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.application.usecase.RecibirReservaExternaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarCargoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarNovedadUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarPagoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.application.usecase.ResolverConflictoCanalUseCase;
import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import co.edu.uniquindio.sga.domain.service.CancelacionReservaService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.service.NoShowService;
import co.edu.uniquindio.sga.domain.service.RegistroLlegadaService;
import co.edu.uniquindio.sga.domain.service.RegistroSalidaService;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans de Spring (Guía 09).
 *
 * Los casos de uso no llevan @Service: la capa de aplicación queda sin Spring.
 * Aquí se "arma" cada uno: Spring busca los parámetros de cada método entre sus
 * beans (los repositorios @Repository en memoria y los servicios de
 * DomainServiceConfig) y se los pasa al constructor.
 *
 * Los parámetros van por el tipo de la INTERFAZ del dominio (ReservaRepository),
 * nunca por la clase en memoria. Cuando llegue JPA solo cambia qué clase lleva
 * @Repository; esta configuración no se toca.
 */
@Configuration
public class UseCaseConfig {

    // ---------- Reservas ----------

    @Bean
    public CrearReservaUseCase crearReservaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            FolioRepository folioRepository,
            GeneradorCodigoReserva generadorCodigo,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService,
            NotificadorReserva notificador) {
        return new CrearReservaUseCase(reservaRepository, apartamentoRepository, alojamientoRepository,
                temporadaRepository, tarifaRepository, bloqueoRepository, folioRepository, generadorCodigo,
                disponibilidadService, estanciaMinimaService, cotizadorService, notificador);
    }

    @Bean
    public CotizarEstanciaUseCase cotizarEstanciaUseCase(
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            AlojamientoRepository alojamientoRepository,
            CotizadorEstanciaService cotizadorEstanciaService) {
        return new CotizarEstanciaUseCase(temporadaRepository, tarifaRepository, alojamientoRepository,
                cotizadorEstanciaService);
    }

    @Bean
    public ConfirmarReservaUseCase confirmarReservaUseCase(
            ReservaRepository reservaRepository,
            FolioRepository folioRepository,
            AlojamientoRepository alojamientoRepository,
            ConfirmacionReservaService confirmacionService,
            NotificadorReserva notificador) {
        return new ConfirmarReservaUseCase(reservaRepository, folioRepository, alojamientoRepository,
                confirmacionService, notificador);
    }

    @Bean
    public ModificarReservaUseCase modificarReservaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService) {
        return new ModificarReservaUseCase(reservaRepository, apartamentoRepository, alojamientoRepository,
                temporadaRepository, tarifaRepository, bloqueoRepository, disponibilidadService,
                estanciaMinimaService, cotizadorService);
    }

    @Bean
    public CancelarReservaUseCase cancelarReservaUseCase(
            ReservaRepository reservaRepository,
            FolioRepository folioRepository,
            PoliticaCancelacionRepository politicaRepository,
            CancelacionReservaService cancelacionReservaService,
            AlojamientoRepository alojamientoRepository,
            NotificadorReserva notificador) {
        return new CancelarReservaUseCase(reservaRepository, folioRepository, politicaRepository,
                cancelacionReservaService, alojamientoRepository, notificador);
    }

    @Bean
    public VencerReservasPendientesUseCase vencerReservasPendientesUseCase(
            ReservaRepository reservaRepository,
            PlazoConfirmacion plazoConfirmacion) {
        return new VencerReservasPendientesUseCase(reservaRepository, plazoConfirmacion);
    }

    @Bean
    public DeclararNoShowUseCase declararNoShowUseCase(
            ReservaRepository reservaRepository,
            FolioRepository folioRepository,
            NoShowService noShowService,
            AlojamientoRepository alojamientoRepository) {
        return new DeclararNoShowUseCase(reservaRepository, folioRepository, noShowService, alojamientoRepository);
    }

    // ---------- Estancia (llegada y salida) ----------

    @Bean
    public RegistrarLlegadaUseCase registrarLlegadaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            FolioRepository folioRepository,
            RegistroLlegadaService registroLlegadaService) {
        return new RegistrarLlegadaUseCase(reservaRepository, apartamentoRepository, folioRepository,
                registroLlegadaService);
    }

    @Bean
    public RegistrarSalidaUseCase registrarSalidaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            FolioRepository folioRepository,
            RegistroSalidaService registroSalidaService) {
        return new RegistrarSalidaUseCase(reservaRepository, apartamentoRepository, folioRepository,
                registroSalidaService);
    }

    // ---------- Folio ----------

    @Bean
    public RegistrarCargoUseCase registrarCargoUseCase(FolioRepository folioRepository) {
        return new RegistrarCargoUseCase(folioRepository);
    }

    @Bean
    public RegistrarPagoUseCase registrarPagoUseCase(FolioRepository folioRepository) {
        return new RegistrarPagoUseCase(folioRepository);
    }

    @Bean
    public CorregirMovimientoUseCase corregirMovimientoUseCase(FolioRepository folioRepository) {
        return new CorregirMovimientoUseCase(folioRepository);
    }

    @Bean
    public CerrarFolioUseCase cerrarFolioUseCase(FolioRepository folioRepository) {
        return new CerrarFolioUseCase(folioRepository);
    }

    // ---------- Apartamentos y bloqueos ----------

    @Bean
    public CambiarEstadoOperativoUseCase cambiarEstadoOperativoUseCase(ApartamentoRepository apartamentoRepository) {
        return new CambiarEstadoOperativoUseCase(apartamentoRepository);
    }

    @Bean
    public GestionarApartamentoUseCase gestionarApartamentoUseCase(
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            ReservaRepository reservaRepository,
            TarifaRepository tarifaRepository,
            TemporadaRepository temporadaRepository) {
        return new GestionarApartamentoUseCase(apartamentoRepository, alojamientoRepository,
                reservaRepository, tarifaRepository, temporadaRepository);
    }

    @Bean
    public RegistrarNovedadUseCase registrarNovedadUseCase(ApartamentoRepository apartamentoRepository) {
        return new RegistrarNovedadUseCase(apartamentoRepository);
    }

    @Bean
    public GestionarBloqueoUseCase gestionarBloqueoUseCase(
            BloqueoApartamentoService bloqueoApartamentoService,
            BloqueoRepository bloqueoRepository,
            ReservaRepository reservaRepository) {
        return new GestionarBloqueoUseCase(bloqueoApartamentoService, bloqueoRepository, reservaRepository);
    }

    // ---------- Configuración del alojamiento ----------

    @Bean
    public ConfigurarParametrosAlojamientoUseCase configurarParametrosAlojamientoUseCase(
            AlojamientoRepository alojamientoRepository) {
        return new ConfigurarParametrosAlojamientoUseCase(alojamientoRepository);
    }

    @Bean
    public ConsultarAlojamientoUseCase consultarAlojamientoUseCase(AlojamientoRepository alojamientoRepository) {
        return new ConsultarAlojamientoUseCase(alojamientoRepository);
    }

    @Bean
    public GestionarTemporadasTarifasUseCase gestionarTemporadasTarifasUseCase(
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository) {
        return new GestionarTemporadasTarifasUseCase(temporadaRepository, tarifaRepository);
    }

    @Bean
    public PublicarPoliticaCancelacionUseCase publicarPoliticaCancelacionUseCase(
            PoliticaCancelacionRepository politicaCancelacionRepository) {
        return new PublicarPoliticaCancelacionUseCase(politicaCancelacionRepository);
    }

    // ---------- Canales externos ----------

    @Bean
    public RecibirReservaExternaUseCase recibirReservaExternaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            FolioRepository folioRepository,
            GeneradorCodigoReserva generadorCodigo,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService) {
        return new RecibirReservaExternaUseCase(
                reservaRepository,
                apartamentoRepository,
                alojamientoRepository,
                temporadaRepository,
                tarifaRepository,
                bloqueoRepository,
                folioRepository,
                generadorCodigo,
                disponibilidadService,
                estanciaMinimaService,
                cotizadorService
        );
    }

    @Bean
    public ResolverConflictoCanalUseCase resolverConflictoCanalUseCase(ReservaRepository reservaRepository) {
        return new ResolverConflictoCanalUseCase(reservaRepository);
    }

    // ---------- Usuarios y sesión (7.2) ----------

    @Bean
    public RegistrarHuespedUseCase registrarHuespedUseCase(UsuarioRepository usuarioRepository,
            CifradorContrasena cifrador) {
        return new RegistrarHuespedUseCase(usuarioRepository, cifrador);
    }

    @Bean
    public CrearUsuarioInternoUseCase crearUsuarioInternoUseCase(UsuarioRepository usuarioRepository,
            CifradorContrasena cifrador) {
        return new CrearUsuarioInternoUseCase(usuarioRepository, cifrador);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(UsuarioRepository usuarioRepository,
            CifradorContrasena cifrador, EmisorToken emisorToken) {
        return new IniciarSesionUseCase(usuarioRepository, cifrador, emisorToken);
    }

    @Bean
    public VerificarAccesoReservaUseCase verificarAccesoReservaUseCase(ReservaRepository reservaRepository) {
        return new VerificarAccesoReservaUseCase(reservaRepository);
    }

    // ---------- Consultas (Reservas y Folios) ----------

    @Bean
    public ConsultarReservaUseCase consultarReservaUseCase(ReservaRepository reservaRepository) {
        return new ConsultarReservaUseCase(reservaRepository);
    }

    @Bean
    public ConsultarReservasUseCase consultarReservasUseCase(ReservaRepository reservaRepository) {
        return new ConsultarReservasUseCase(reservaRepository);
    }

    @Bean
    public ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase(
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            ReservaRepository reservaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            CotizadorEstanciaService cotizadorService) {
        return new ConsultarDisponibilidadUseCase(
                apartamentoRepository,
                alojamientoRepository,
                reservaRepository,
                bloqueoRepository,
                disponibilidadService,
                temporadaRepository,
                tarifaRepository,
                cotizadorService
        );
    }

    @Bean
    public ConsultarFolioUseCase consultarFolioUseCase(
            FolioRepository folioRepository,
            ReservaRepository reservaRepository) {
        return new ConsultarFolioUseCase(folioRepository, reservaRepository);
    }
}
