package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.EnviarRecordatoriosLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Tareas automáticas del sistema.
 *
 * RN-06 / 7.4: una reserva PENDIENTE que no se confirma dentro del plazo se vence sola
 * y libera el apartamento. Antes solo pasaba si alguien llamaba el endpoint; ahora el
 * sistema lo revisa cada minuto.
 *
 * El primer chequeo espera 1 hora (configurable) para que las pruebas y la demo
 * arranquen con los datos de prueba intactos.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "sga.vencimiento.habilitado", havingValue = "true", matchIfMissing = true)
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final VencerReservasPendientesUseCase vencerReservasPendientesUseCase;
    private final EnviarRecordatoriosLlegadaUseCase enviarRecordatoriosLlegadaUseCase;

    public TareasProgramadas(VencerReservasPendientesUseCase vencerReservasPendientesUseCase,
                             EnviarRecordatoriosLlegadaUseCase enviarRecordatoriosLlegadaUseCase) {
        this.vencerReservasPendientesUseCase = vencerReservasPendientesUseCase;
        this.enviarRecordatoriosLlegadaUseCase = enviarRecordatoriosLlegadaUseCase;
    }

    /**
     * Recordatorio de llegada: todos los días a las 9:00 a. m. (hora de Colombia) se le avisa
     * a quien tiene una reserva confirmada con entrada al día siguiente.
     * El horario se cambia con sga.recordatorios.cron; con el valor "-" queda apagado.
     */
    @Scheduled(cron = "${sga.recordatorios.cron:0 0 9 * * *}", zone = "America/Bogota")
    public void recordarLlegadas() {
        try {
            List<Reserva> avisadas = enviarRecordatoriosLlegadaUseCase.ejecutar(
                    LocalDate.now(ZoneId.of("America/Bogota")));
            if (!avisadas.isEmpty()) {
                log.info("Se enviaron {} recordatorio(s) de llegada", avisadas.size());
            }
        } catch (RuntimeException e) {
            log.error("Falló el envío de recordatorios de llegada", e);
        }
    }

    @Scheduled(initialDelayString = "${sga.vencimiento.retraso-inicial-ms:3600000}",
            fixedDelayString = "${sga.vencimiento.intervalo-ms:60000}")
    public void vencerReservasPendientes() {
        try {
            List<Reserva> vencidas = vencerReservasPendientesUseCase.ejecutar(LocalDateTime.now());
            if (!vencidas.isEmpty()) {
                log.info("Se vencieron {} reserva(s) pendiente(s) sin confirmar", vencidas.size());
            }
        } catch (RuntimeException e) {
            // Un error en una vuelta no debe detener las siguientes
            log.error("Falló el vencimiento automático de reservas pendientes", e);
        }
    }
}
