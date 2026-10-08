package co.edu.uniquindio.sga.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

/**
 * Raíz del agregado Reserva.
 *
 * Invariantes que garantiza:
 *  - toda reserva nace en estado PENDIENTE;
 *  - solo se transita entre los estados permitidos por la sección 8 (RN-08);
 *  - una reserva en estado terminal no admite ninguna modificación;
 *  - no se confirma una reserva sin hora estimada de llegada (RN-09);
 *  - no se registra la llegada antes de la fecha de entrada (RN-10);
 *  - el valor y la política quedan congelados y solo cambian por modificación explícita (RN-22);
 *  - toda modificación revalida y recalcula, y produce el ajuste correspondiente (RN-14);
 *  - el grupo de ocupantes no puede alterarse desde fuera del agregado;
 *  - toda acción que cambia el estado queda registrada en el historial.
 */
public class Reserva {

    private final CodigoReserva codigo;                     // identidad
    private final CanalOrigen canalOrigen;                  // por dónde entró: no cambia nunca
    private final LocalDateTime creadaEn;
    private final Ocupante titular;
    private final IdentificacionApartamento apartamento;    // referencia a OTRO agregado
    private final VersionPolitica politica;                 // RN-13: congelada de por vida
    private final List<EventoReserva> historial = new ArrayList<>();

    private Estancia estancia;
    private List<Ocupante> ocupantes;
    private EstadoReserva estado;
    private HoraEstimadaLlegada horaEstimadaLlegada;        // Encapsulado en VO
    private NotificacionRetraso notificacionRetraso;        // Encapsulado en VO
    private ValorCongelado valor;

    private Reserva(CodigoReserva codigo, IdentificacionApartamento apartamento,
                    Estancia estancia, Ocupante titular, List<Ocupante> ocupantes,
                    CanalOrigen canalOrigen, ValorCongelado valor,
                    VersionPolitica politica, LocalDateTime creadaEn) {
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.titular = titular;
        this.ocupantes = List.copyOf(ocupantes);
        this.canalOrigen = canalOrigen;
        this.valor = valor;
        this.politica = politica;
        this.creadaEn = creadaEn;
        this.estado = EstadoReserva.PENDIENTE;               // toda reserva nace PENDIENTE
    }

    /**
     * Única puerta de entrada para crear una reserva.
     * El valor ya llega calculado y la disponibilidad ya fue verificada.
     */
    public static Reserva crear(CodigoReserva codigo, IdentificacionApartamento apartamento,
                                Estancia estancia, Ocupante titular, List<Ocupante> ocupantes,
                                CanalOrigen canalOrigen, ValorCongelado valor,
                                VersionPolitica politica, LocalDateTime ahora) {
        if (codigo == null) {
            throw new ReglaDominioException("La reserva debe tener un código");
        }
        if (apartamento == null) {
            throw new ReglaDominioException("La reserva debe indicar el apartamento");
        }
        if (estancia == null) {
            throw new ReglaDominioException("La reserva debe tener una estancia");
        }
        if (titular == null) {
            throw new ReglaDominioException("La reserva debe tener un titular");
        }
        if (canalOrigen == null) {
            throw new ReglaDominioException("La reserva debe indicar su canal de origen");
        }
        if (valor == null || politica == null) {
            throw new ReglaDominioException(
                "La reserva debe nacer con su valor y su política congelados");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("La reserva debe tener al menos un ocupante");
        }
        if (!ocupantes.contains(titular)) {
            throw new ReglaDominioException("El titular debe ser uno de los ocupantes");
        }
        if (ocupantes.stream().distinct().count() != ocupantes.size()) {
            throw new ReglaDominioException("No se puede repetir un ocupante en la reserva");
        }

        // RN-04: no se crean reservas hacia el pasado
        if (estancia.fechaEntrada().isBefore(ahora.toLocalDate())) {
            throw new ReglaDominioException(
                "La fecha de entrada no puede ser anterior a la fecha actual");
        }

        // El desglose congelado debe corresponder a la estancia que se está reservando
        if (valor.noches() != estancia.noches()) {
            throw new ReglaDominioException(
                "El desglose del valor no corresponde al número de noches de la estancia");
        }

        Reserva reserva = new Reserva(codigo, apartamento, estancia, titular, ocupantes,
                                      canalOrigen, valor, politica, ahora);
        reserva.registrarEvento("CREACION", titular.getNombre(), null,
                EstadoReserva.PENDIENTE, "Reserva creada por el canal " + canalOrigen, ahora);
        return reserva;
    }

    // ---------- Comportamiento del negocio ----------

    /** RN-09: la hora estimada de llegada es requisito para confirmar. */
    public void indicarHoraEstimadaLlegada(LocalTime hora, String autor) {
        verificarQueNoEsteTerminada();
        if (hora == null) {
            throw new ReglaDominioException("Debe indicarse la hora estimada de llegada");
        }
        this.horaEstimadaLlegada = HoraEstimadaLlegada.de(hora);
        registrarEvento("HORA_LLEGADA", autor, this.estado, this.estado,
                "Hora estimada de llegada: " + hora, LocalDateTime.now());
    }

    /**
     * Registra un aviso/notificación de retraso y actualiza la hora estimada.
     */
    public void notificarRetraso(NotificacionRetraso notificacion, String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        if (notificacion == null) {
            throw new ReglaDominioException("La notificación de retraso es obligatoria");
        }

        this.notificacionRetraso = notificacion;
        this.horaEstimadaLlegada = HoraEstimadaLlegada.de(notificacion.nuevaHoraEstimada());

        registrarEvento("NOTIFICACION_RETRASO", autor, this.estado, this.estado,
                "Retraso notificado: " + notificacion.motivo() + " | Nueva hora: " + notificacion.nuevaHoraEstimada(),
                ahora);
    }

    /**
     * RN-09: solo una reserva PENDIENTE con hora estimada de llegada puede confirmarse.
     */
    public void confirmar(String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        if (this.horaEstimadaLlegada == null) {
            throw new ReglaDominioException(
                "No se puede confirmar una reserva sin hora estimada de llegada");
        }
        EstadoReserva anterior = this.estado;
        verificarTransicion(EstadoReserva.CONFIRMADA);
        this.estado = EstadoReserva.CONFIRMADA;
        registrarEvento("CONFIRMACION", autor, anterior, this.estado, "Reserva confirmada", ahora);
    }

    /**
     * RN-12: la cancelación libera las noches de inmediato.
     * RN-13: la retención se calcula con la política congelada.
     */
    public void cancelar(String motivo, String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("Toda cancelación debe registrar un motivo");
        }
        EstadoReserva anterior = this.estado;
        verificarTransicion(EstadoReserva.CANCELADA);
        this.estado = EstadoReserva.CANCELADA;
        registrarEvento("CANCELACION", autor, anterior, this.estado,
                motivo + " | política aplicada: versión " + politica.numero(), ahora);
    }

    /**
     * RN-21: una reserva PENDIENTE que supera el plazo de confirmación se cancela sola.
     */
    public void vencer(PlazoConfirmacion plazo, LocalDateTime ahora) {
        if (this.estado != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException("Solo vence una reserva PENDIENTE");
        }
        if (!plazo.venceAntesDe(this.creadaEn, ahora)) {
            throw new ReglaDominioException("La reserva todavía está dentro del plazo de confirmación");
        }
        EstadoReserva anterior = this.estado;
        this.estado = EstadoReserva.CANCELADA;
        registrarEvento("VENCIMIENTO", "SISTEMA", anterior, this.estado,
                "Cancelada por vencimiento del plazo de confirmación", ahora);
    }

    /**
     * RN-10: no se registra la llegada antes de la fecha de entrada, ni sobre una reserva no CONFIRMADA.
     */
    public void registrarLlegada(String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        if (ahora.toLocalDate().isBefore(estancia.fechaEntrada())) {
            throw new ReglaDominioException(
                "No se puede registrar la llegada antes de la fecha de entrada");
        }
        EstadoReserva anterior = this.estado;
        verificarTransicion(EstadoReserva.EN_CURSO);
        this.estado = EstadoReserva.EN_CURSO;
        registrarEvento("REGISTRO", autor, anterior, this.estado, "El grupo tomó el apartamento", ahora);
    }

    public void registrarSalida(String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        EstadoReserva anterior = this.estado;
        verificarTransicion(EstadoReserva.FINALIZADA);
        this.estado = EstadoReserva.FINALIZADA;
        registrarEvento("SALIDA", autor, anterior, this.estado, "El grupo salió del apartamento", ahora);
    }

   /**
     * RN-11: El no-show solo se puede declarar si han transcurrido 2 horas 
     * después de la hora estimada de llegada (o de la hora estándar 14:00 si no la indicó).
     * Tampoco procede si el huésped registró previamente una Notificación de Retraso.
     */
    public void declararNoShow(String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();

        // 1. Si existe una notificación formal de retraso, se bloquea el No-Show
        if (this.notificacionRetraso != null) {
            throw new ReglaDominioException(
                "No se puede declarar no-show: el huésped registró una notificación de retraso (" 
                + this.notificacionRetraso.motivo() + ")");
        }

        // 2. Determinar el VO a usar: el propio de la reserva o la constante ESTANDAR
        HoraEstimadaLlegada horaEfectiva = (this.horaEstimadaLlegada != null) 
                ? this.horaEstimadaLlegada 
                : HoraEstimadaLlegada.ESTANDAR;

        // 3. Calcular momento límite exacto con la fecha de entrada
        LocalDateTime momentoLimite = estancia.fechaEntrada().atTime(horaEfectiva.horaLimiteNoShow());
        
        // 4. Validar tolerancia de 2 horas
        if (ahora.isBefore(momentoLimite)) {
            throw new ReglaDominioException(
                "No se puede declarar no-show antes de la hora límite (" 
                + horaEfectiva.horaLimiteNoShow() + ")");
        }

        // 5. Transición de estado
        EstadoReserva anterior = this.estado;
        verificarTransicion(EstadoReserva.NO_SHOW);
        this.estado = EstadoReserva.NO_SHOW;

        registrarEvento("NO_SHOW", autor, anterior, this.estado,
                "No-show declarado tras superar las 2 horas de tolerancia (hora límite: " 
                + horaEfectiva.horaLimiteNoShow() + ")", ahora);
    }

    /**
     * RN-14: toda modificación recalcula el valor y produce un ajuste.
     * RN-22: la política congelada NO se recalcula.
     */
    public Dinero modificarEstancia(Estancia nuevaEstancia, ValorCongelado nuevoValor,
                                    String autor, LocalDateTime ahora) {
        verificarQueNoEsteTerminada();
        if (this.estado == EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("No se puede modificar una reserva ya iniciada");
        }
        if (nuevaEstancia == null || nuevoValor == null) {
            throw new ReglaDominioException("La modificación requiere la nueva estancia y su valor");
        }
        if (nuevoValor.noches() != nuevaEstancia.noches()) {
            throw new ReglaDominioException(
                "El desglose del valor no corresponde al número de noches de la estancia");
        }

        // Calcula la diferencia usando el método .menos() del VO Dinero
        Dinero ajuste = nuevoValor.total().menos(this.valor.total());
        this.estancia = nuevaEstancia;
        this.valor = nuevoValor;

        registrarEvento("MODIFICACION", autor, this.estado, this.estado,
                "Nueva estancia " + nuevaEstancia.fechaEntrada() + " a "
                        + nuevaEstancia.fechaSalida() + " | ajuste: " + ajuste.valor(), ahora);
        return ajuste;
    }

    // ---------- Verificaciones internas ----------

    private void verificarQueNoEsteTerminada() {
        if (this.estado.esTerminal()) {
            throw new ReglaDominioException(
                "Una reserva en estado " + this.estado + " no admite modificaciones");
        }
    }

    private void verificarTransicion(EstadoReserva siguiente) {
        if (!this.estado.puedeTransicionarA(siguiente)) {
            throw new ReglaDominioException(
                "No se puede pasar de " + this.estado + " a " + siguiente);
        }
    }

    private void registrarEvento(String accion, String autor, EstadoReserva anterior,
                                 EstadoReserva nuevo, String observacion, LocalDateTime ahora) {
        this.historial.add(new EventoReserva(ahora, accion, autor, anterior, nuevo, observacion));
    }

    // ---------- Consultas ----------

    public List<EventoReserva> obtenerHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public List<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public int totalOcupantes() {
        return ocupantes.size();
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public Ocupante getTitular() {
        return titular;
    }

    public ValorCongelado getValor() {
        return valor;
    }

    public VersionPolitica getPolitica() {
        return politica;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public HoraEstimadaLlegada getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public NotificacionRetraso getNotificacionRetraso() {
        return notificacionRetraso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva otra)) {
            return false;
        }
        return this.codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }
}