package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import co.edu.uniquindio.sga.domain.service.CancelacionReservaService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.service.ModificacionReservaService;
import co.edu.uniquindio.sga.domain.service.NoShowService;
import co.edu.uniquindio.sga.domain.service.RegistroLlegadaService;
import co.edu.uniquindio.sga.domain.service.RegistroSalidaService;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los servicios de dominio como beans de Spring (Guía 09).
 *
 * Los servicios siguen siendo Java puro: no tienen ninguna anotación de Spring.
 * Es esta clase, en infraestructura, la que los crea con {@code new} y se los
 * entrega a Spring, así el dominio no se entera de que existe un framework.
 */
@Configuration
public class DomainServiceConfig {

    /** Parámetro de la Ficha del Alojamiento (RN-21): se lee de application.properties, no se quema en el código. */
    @Bean
    public PlazoConfirmacion plazoConfirmacion(
            @Value("${sga.alojamiento.plazo-confirmacion-horas}") int horas) {
        return new PlazoConfirmacion(horas);
    }

    @Bean
    public DisponibilidadApartamentoService disponibilidadApartamentoService() {
        return new DisponibilidadApartamentoService();
    }

    @Bean
    public EstanciaMinimaTemporadaAltaService estanciaMinimaTemporadaAltaService() {
        return new EstanciaMinimaTemporadaAltaService();
    }

    @Bean
    public CotizadorEstanciaService cotizadorEstanciaService() {
        return new CotizadorEstanciaService();
    }

    @Bean
    public ConfirmacionReservaService confirmacionReservaService() {
        return new ConfirmacionReservaService();
    }

    @Bean
    public BloqueoApartamentoService bloqueoApartamentoService() {
        return new BloqueoApartamentoService();
    }

    @Bean
    public RegistroLlegadaService registroLlegadaService() {
        return new RegistroLlegadaService();
    }

    @Bean
    public RegistroSalidaService registroSalidaService() {
        return new RegistroSalidaService();
    }

    @Bean
    public CancelacionReservaService cancelacionReservaService() {
        return new CancelacionReservaService();
    }

    @Bean
    public NoShowService noShowService() {
        return new NoShowService();
    }

    /** Este sí recibe otros servicios: Spring le pasa los beans de arriba. */
    @Bean
    public ModificacionReservaService modificacionReservaService(
            DisponibilidadApartamentoService disponibilidad,
            EstanciaMinimaTemporadaAltaService estanciaMinima,
            CotizadorEstanciaService cotizador) {
        return new ModificacionReservaService(disponibilidad, estanciaMinima, cotizador);
    }
}
