package co.edu.uniquindio.sga.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

@DisplayName("Reserva (raíz del agregado)")
class ReservaTest {

    /**
     * Momento fijo en que "se crea" la reserva: las pruebas no dependen del reloj
     * real.
     */
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 12, 10, 0);

    private static final LocalDate ENTRADA = LocalDate.of(2026, 11, 1);
    private static final LocalDate SALIDA = LocalDate.of(2026, 11, 4); // 3 noches

    private static final CodigoReserva CODIGO = new CodigoReserva("RES-2026-00001");
    private static final IdentificacionApartamento APTO = new IdentificacionApartamento("APTO-101");
    private static final VersionPolitica POLITICA = new VersionPolitica(1);
    private static final String RECEPCION = "recepcionista";
    private static final CantidadMascota SIN_MASCOTAS = new CantidadMascota(0);

    private Ocupante titular;
    private Ocupante acompanante;
    private Estancia estancia;
    private Reserva reserva;
    private Alojamiento alojamientoPrueba;

    @BeforeEach
    void setUp() {
        titular = new Ocupante(new DocumentoIdentidad("1094000001"), "Laura Gómez",
                LocalDate.of(1990, 5, 1), AHORA.toLocalDate());
        acompanante = new Ocupante(new DocumentoIdentidad("1094000002"), "Pedro Gómez",
                LocalDate.of(1988, 2, 10), AHORA.toLocalDate());
        estancia = new Estancia(ENTRADA, SALIDA);
        reserva = crearReserva(estancia, List.of(titular, acompanante));

        // Configuración de un Alojamiento de prueba para las validaciones de No-Show
        alojamientoPrueba = new Alojamiento(
                new IdentificacionAlojamiento("HOTEL-01"),
                "Hotel Sanrocco",
                "Armenia",
                "Hotel campestre",
                LocalTime.of(15, 0), // Hora entrada estándar
                LocalTime.of(11, 0), // Hora salida
                LocalTime.of(17, 0), // Hora límite no-show
                new UmbralEdadFacturable(12),
                new TiempoPreparacion(3), // en horas (máximo 24),
                new PlazoConfirmacion(24),
                true,
                50);
    }

    // ---------- Ayudantes ----------

    /** Desglose de 100.000 por ocupante, para cada noche de la estancia. */
    private static ValorCongelado valorPara(Estancia e, int ocupantes) {
        List<CargoNoche> detalle = e.nochesOcupadas().stream()
                .map(noche -> new CargoNoche(noche, "BASE", Dinero.de(100_000), ocupantes))
                .toList();
        return new ValorCongelado(detalle);
    }

    private Reserva crearReserva(Estancia e, List<Ocupante> ocupantes) {
        return Reserva.crear(CODIGO, APTO, e, titular, ocupantes, CanalOrigen.DIRECTO,
                valorPara(e, ocupantes.size()), POLITICA, AHORA, SIN_MASCOTAS);
    }

    private void confirmar() {
        reserva.indicarHoraEstimadaLlegada(LocalTime.of(15, 0), RECEPCION);
        reserva.confirmar(RECEPCION, AHORA);
    }

    private static LocalDateTime elDiaDeEntradaA(int hora, int minuto) {
        return ENTRADA.atTime(hora, minuto);
    }

    private EventoReserva ultimoEvento() {
        List<EventoReserva> historial = reserva.obtenerHistorial();
        return historial.get(historial.size() - 1);
    }

    // ---------- Creación ----------

    @Test
    @DisplayName("Toda reserva nace PENDIENTE y registra su creación")
    void naceEnPendiente() {
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(1, reserva.obtenerHistorial().size());

        EventoReserva creacion = reserva.obtenerHistorial().get(0);
        assertEquals("CREACION", creacion.accion());
        assertNull(creacion.estadoAnterior());
        assertEquals(EstadoReserva.PENDIENTE, creacion.estadoNuevo());
        assertEquals(AHORA, creacion.momento());
    }

    @Test
    @DisplayName("Conserva los datos con los que se creó")
    void conservaDatos() {
        assertEquals(CODIGO, reserva.getCodigo());
        assertEquals(APTO, reserva.getApartamento());
        assertEquals(estancia, reserva.getEstancia());
        assertEquals(titular, reserva.getTitular());
        assertEquals(2, reserva.totalOcupantes());
        assertEquals(CanalOrigen.DIRECTO, reserva.getCanalOrigen());
        assertEquals(POLITICA, reserva.getPolitica());
        assertEquals(AHORA, reserva.getCreadaEn());
        assertEquals(Dinero.de(600_000), reserva.getValor().total());
    }

    @Test
    @DisplayName("Exige los datos obligatorios")
    void exigeDatosObligatorios() {
        List<Ocupante> grupo = List.of(titular);
        ValorCongelado valor = valorPara(estancia, 1);
        CanalOrigen canal = CanalOrigen.PORTAL;

        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                null, APTO, estancia, titular, grupo, canal, valor, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, null, estancia, titular, grupo, canal, valor, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, null, titular, grupo, canal, valor, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, estancia, null, grupo, canal, valor, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, estancia, titular, grupo, null, valor, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, estancia, titular, grupo, canal, null, POLITICA, AHORA, SIN_MASCOTAS));
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, estancia, titular, grupo, canal, valor, null, AHORA, SIN_MASCOTAS));
    }

    @Test
    @DisplayName("Debe tener al menos un ocupante")
    void sinOcupantes() {
        assertThrows(ReglaDominioException.class, () -> crearReserva(estancia, List.of()));
    }

    @Test
    @DisplayName("El titular debe estar entre los ocupantes")
    void titularEntreOcupantes() {
        assertThrows(ReglaDominioException.class, () -> crearReserva(estancia, List.of(acompanante)));
    }

    @Test
    @DisplayName("No se puede repetir un ocupante")
    void ocupanteRepetido() {
        assertThrows(ReglaDominioException.class,
                () -> crearReserva(estancia, List.of(titular, acompanante, acompanante)));
    }

    @Test
    @DisplayName("RN-04: no se crean reservas hacia el pasado")
    void noReservasEnElPasado() {
        Estancia ayer = new Estancia(AHORA.toLocalDate().minusDays(1), AHORA.toLocalDate().plusDays(1));

        assertThrows(ReglaDominioException.class, () -> crearReserva(ayer, List.of(titular)));
    }

    @Test
    @DisplayName("RN-04: sí se puede reservar con entrada hoy")
    void reservaConEntradaHoy() {
        Estancia hoy = new Estancia(AHORA.toLocalDate(), AHORA.toLocalDate().plusDays(2));

        assertEquals(EstadoReserva.PENDIENTE, crearReserva(hoy, List.of(titular)).getEstado());
    }

    @Test
    @DisplayName("El desglose debe tener tantas noches como la estancia")
    void desgloseCorrespondeALaEstancia() {
        Estancia dosNoches = new Estancia(ENTRADA, ENTRADA.plusDays(2));

        assertThrows(ReglaDominioException.class, () -> Reserva.crear(
                CODIGO, APTO, estancia, titular, List.of(titular), CanalOrigen.PORTAL,
                valorPara(dosNoches, 1), POLITICA, AHORA, SIN_MASCOTAS));
    }

    @Test
    @DisplayName("El grupo de ocupantes no se puede alterar desde afuera")
    void ocupantesProtegidos() {
        List<Ocupante> grupo = new ArrayList<>(List.of(titular));
        Reserva r = crearReserva(estancia, grupo);

        grupo.add(acompanante); // modificar la lista original

        assertEquals(1, r.totalOcupantes());
        assertThrows(UnsupportedOperationException.class, () -> r.getOcupantes().add(acompanante));
    }

    @Test
    @DisplayName("El historial no se puede alterar desde afuera")
    void historialProtegido() {
        assertThrows(UnsupportedOperationException.class, () -> reserva.obtenerHistorial().clear());
    }

    // ---------- Confirmación ----------

    @Test
    @DisplayName("RN-09: no se confirma sin hora estimada de llegada")
    void confirmarSinHora() {
        assertThrows(ReglaDominioException.class, () -> reserva.confirmar(RECEPCION, AHORA));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("RN-09: con hora estimada se confirma y queda en el historial")
    void confirmarConHora() {
        confirmar();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(LocalTime.of(15, 0), reserva.getHoraEstimadaLlegada().horaEstimada());
        assertEquals("CONFIRMACION", ultimoEvento().accion());
        assertEquals(EstadoReserva.PENDIENTE, ultimoEvento().estadoAnterior());
    }

    @Test
    @DisplayName("La hora estimada de llegada es obligatoria al indicarla")
    void horaNula() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.indicarHoraEstimadaLlegada(null, RECEPCION));
    }

    @Test
    @DisplayName("RN-08: no se confirma dos veces")
    void noConfirmarDosVeces() {
        confirmar();

        assertThrows(ReglaDominioException.class, () -> reserva.confirmar(RECEPCION, AHORA));
    }

    // ---------- Cancelación y vencimiento ----------

    @Test
    @DisplayName("Toda cancelación debe tener motivo")
    void cancelarSinMotivo() {
        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(" ", RECEPCION, AHORA));
    }

    @Test
    @DisplayName("Una reserva PENDIENTE o CONFIRMADA se puede cancelar")
    void cancelar() {
        confirmar();

        reserva.cancelar("Cambio de planes", RECEPCION, AHORA);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals("CANCELACION", ultimoEvento().accion());
    }

    @Test
    @DisplayName("Una reserva en estado terminal no admite cambios")
    void terminalNoAdmiteCambios() {
        reserva.cancelar("Cambio de planes", RECEPCION, AHORA);

        assertThrows(ReglaDominioException.class,
                () -> reserva.indicarHoraEstimadaLlegada(LocalTime.NOON, RECEPCION));
        assertThrows(ReglaDominioException.class, () -> reserva.confirmar(RECEPCION, AHORA));
        assertThrows(ReglaDominioException.class,
                () -> reserva.cancelar("Otra vez", RECEPCION, AHORA));
        assertThrows(ReglaDominioException.class,
                () -> reserva.modificarEstancia(estancia, valorPara(estancia, 2), RECEPCION, AHORA));
    }

    @Test
    @DisplayName("RN-21: dentro del plazo de confirmación no vence")
    void noVenceDentroDelPlazo() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.vencer(PlazoConfirmacion.unaHora(), AHORA.plusMinutes(30)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("RN-21: vencido el plazo, el sistema la cancela")
    void venceFueraDelPlazo() {
        reserva.vencer(PlazoConfirmacion.unaHora(), AHORA.plusHours(2));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals("VENCIMIENTO", ultimoEvento().accion());
        assertEquals("SISTEMA", ultimoEvento().autor());
    }

    @Test
    @DisplayName("RN-21: una reserva CONFIRMADA no vence")
    void confirmadaNoVence() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.vencer(PlazoConfirmacion.unaHora(), AHORA.plusDays(1)));
    }

    // ---------- Llegada y salida ----------

    @Test
    @DisplayName("RN-10: no se registra la llegada antes de la fecha de entrada")
    void llegadaAntesDeLaFecha() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarLlegada(RECEPCION, ENTRADA.minusDays(1).atTime(20, 0)));
    }

    @Test
    @DisplayName("RN-08: no se registra la llegada de una reserva sin confirmar")
    void llegadaSinConfirmar() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarLlegada(RECEPCION, elDiaDeEntradaA(15, 0)));
    }

    @Test
    @DisplayName("Una reserva CONFIRMADA pasa a EN_CURSO al llegar el grupo")
    void registrarLlegada() {
        confirmar();

        reserva.registrarLlegada(RECEPCION, elDiaDeEntradaA(15, 0));

        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals("REGISTRO", ultimoEvento().accion());
    }

    @Test
    @DisplayName("Una reserva EN_CURSO pasa a FINALIZADA al salir el grupo")
    void registrarSalida() {
        confirmar();
        reserva.registrarLlegada(RECEPCION, elDiaDeEntradaA(15, 0));

        reserva.registrarSalida(RECEPCION, SALIDA.atTime(11, 0));

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    @DisplayName("RN-08: no hay salida sin llegada")
    void salidaSinLlegada() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarSalida(RECEPCION, SALIDA.atTime(11, 0)));
    }

    @Test
    @DisplayName("Ciclo completo: el historial guarda cada paso en orden")
    void cicloCompleto() {
        confirmar();
        reserva.registrarLlegada(RECEPCION, elDiaDeEntradaA(15, 0));
        reserva.registrarSalida(RECEPCION, SALIDA.atTime(11, 0));

        List<String> acciones = reserva.obtenerHistorial().stream().map(EventoReserva::accion).toList();
        assertEquals(List.of("CREACION", "HORA_LLEGADA", "CONFIRMACION", "REGISTRO", "SALIDA"), acciones);
    }

    // ---------- No-show y retraso ----------

    @Test
    @DisplayName("No-show: con llegada a las 15:00 no se declara antes de las 17:00")
    void noShowAntesDelLimite() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.declararNoShow(RECEPCION, elDiaDeEntradaA(16, 59), alojamientoPrueba));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("No-show: con llegada a las 15:00 se puede declarar desde las 17:00")
    void noShowDespuesDelLimite() {
        confirmar();

        reserva.declararNoShow(RECEPCION, elDiaDeEntradaA(17, 0), alojamientoPrueba);

        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        assertEquals("NO_SHOW", ultimoEvento().accion());
    }

    @Test
    @DisplayName("No-show: no se declara sobre una reserva sin confirmar")
    void noShowSinConfirmar() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.declararNoShow(RECEPCION, elDiaDeEntradaA(23, 0), alojamientoPrueba));
    }

    @Test
    @DisplayName("Notificar un retraso actualiza la hora estimada de llegada")
    void notificarRetraso() {
        confirmar();
        NotificacionRetraso aviso = NotificacionRetraso.registrar(
                LocalTime.of(21, 0), "Vuelo retrasado", elDiaDeEntradaA(14, 0));

        reserva.notificarRetraso(aviso, "huesped", elDiaDeEntradaA(14, 0));

        assertNotNull(reserva.getNotificacionRetraso());
        assertEquals(LocalTime.of(21, 0), reserva.getHoraEstimadaLlegada().horaEstimada());
        assertEquals("NOTIFICACION_RETRASO", ultimoEvento().accion());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(), "notificar no cambia el estado");
    }

    @Test
    @DisplayName("Con retraso notificado no se puede declarar no-show")
    void retrasoBloqueaNoShow() {
        confirmar();
        reserva.notificarRetraso(
                NotificacionRetraso.registrar(LocalTime.of(18, 0), "Trancón", elDiaDeEntradaA(14, 0)),
                "huesped", elDiaDeEntradaA(14, 0));

        assertThrows(ReglaDominioException.class,
                () -> reserva.declararNoShow(RECEPCION, elDiaDeEntradaA(23, 0), alojamientoPrueba));
    }

    @Test
    @DisplayName("La notificación de retraso es obligatoria")
    void notificacionNula() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.notificarRetraso(null, "huesped", AHORA));
    }

    // ---------- Modificación ----------

    @Test
    @DisplayName("RN-14: alargar la estancia genera un ajuste a cobrar")
    void alargarEstancia() {
        Estancia cuatroNoches = new Estancia(ENTRADA, SALIDA.plusDays(1));

        Dinero ajuste = reserva.modificarEstancia(cuatroNoches, valorPara(cuatroNoches, 2), RECEPCION, AHORA);

        assertEquals(Dinero.de(200_000), ajuste);
        assertEquals(cuatroNoches, reserva.getEstancia());
        assertEquals(Dinero.de(800_000), reserva.getValor().total());
        assertEquals("MODIFICACION", ultimoEvento().accion());
    }

    @Test
    @DisplayName("RN-14: acortar la estancia genera un ajuste a favor del huésped")
    void acortarEstancia() {
        Estancia unaNoche = new Estancia(ENTRADA, ENTRADA.plusDays(1));

        Dinero ajuste = reserva.modificarEstancia(unaNoche, valorPara(unaNoche, 2), RECEPCION, AHORA);

        assertEquals(Dinero.de(-400_000), ajuste);
    }

    @Test
    @DisplayName("RN-22: la política congelada no cambia al modificar")
    void politicaNoCambiaAlModificar() {
        Estancia otra = new Estancia(ENTRADA.plusDays(5), SALIDA.plusDays(5));

        reserva.modificarEstancia(otra, valorPara(otra, 2), RECEPCION, AHORA);

        assertEquals(POLITICA, reserva.getPolitica());
    }

    @Test
    @DisplayName("La modificación exige un desglose acorde a la nueva estancia")
    void modificacionConDesgloseIncorrecto() {
        Estancia cuatroNoches = new Estancia(ENTRADA, SALIDA.plusDays(1));

        assertThrows(ReglaDominioException.class,
                () -> reserva.modificarEstancia(cuatroNoches, valorPara(estancia, 2), RECEPCION, AHORA));
        assertThrows(ReglaDominioException.class,
                () -> reserva.modificarEstancia(null, valorPara(estancia, 2), RECEPCION, AHORA));
        assertEquals(estancia, reserva.getEstancia(), "una modificación fallida no deja cambios");
    }

    @Test
    @DisplayName("No se modifica una reserva ya iniciada")
    void noModificarEnCurso() {
        confirmar();
        reserva.registrarLlegada(RECEPCION, elDiaDeEntradaA(15, 0));

        assertThrows(ReglaDominioException.class,
                () -> reserva.modificarEstancia(estancia, valorPara(estancia, 2), RECEPCION, AHORA));
    }

    // ---------- Identidad ----------

    @Test
    @DisplayName("Dos reservas con el mismo código son la misma reserva")
    void igualdadPorCodigo() {
        Reserva mismoCodigo = crearReserva(estancia, List.of(titular));

        assertEquals(reserva, mismoCodigo);
        assertEquals(reserva.hashCode(), mismoCodigo.hashCode());
    }
}