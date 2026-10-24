package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarBloqueoUseCase;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.service.RegistroSalidaService;
import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    RegistroSalidaService registroSalidaService() {
        return new RegistroSalidaService();
    }

    @Bean
    DisponibilidadApartamentoService disponibilidadApartamentoService() {
        return new DisponibilidadApartamentoService();
    }

    @Bean
    EstanciaMinimaTemporadaAltaService estanciaMinimaTemporadaAltaService() {
        return new EstanciaMinimaTemporadaAltaService();
    }

    @Bean
    CotizadorEstanciaService cotizadorEstanciaService() {
        return new CotizadorEstanciaService();
    }

    @Bean
    BloqueoApartamentoService bloqueoApartamentoService() {
        return new BloqueoApartamentoService();
    }

    @Bean
    GestionarBloqueoUseCase gestionarBloqueoUseCase(
            BloqueoApartamentoService bloqueoApartamentoService,
            BloqueoRepository bloqueoRepository,
            ReservaRepository reservaRepository) {
        return new GestionarBloqueoUseCase(
                bloqueoApartamentoService,
                bloqueoRepository,
                reservaRepository);
    }

    @Bean
    CrearReservaUseCase crearReservaUseCase(
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
        return new CrearReservaUseCase(
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
                cotizadorService);
    }

    @Bean
    ModificarReservaUseCase modificarReservaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService) {
        return new ModificarReservaUseCase(
                reservaRepository,
                apartamentoRepository,
                alojamientoRepository,
                temporadaRepository,
                tarifaRepository,
                bloqueoRepository,
                disponibilidadService,
                estanciaMinimaService,
                cotizadorService);
    }

    @Bean
    RegistrarSalidaUseCase registrarSalidaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            FolioRepository folioRepository,
            RegistroSalidaService registroSalidaService) {
        return new RegistrarSalidaUseCase(
                reservaRepository,
                apartamentoRepository,
                folioRepository,
                registroSalidaService);
    }
}