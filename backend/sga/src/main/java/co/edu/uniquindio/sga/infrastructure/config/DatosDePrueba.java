package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CancelacionReservaService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.NoShowService;
import co.edu.uniquindio.sga.domain.service.RegistroLlegadaService;
import co.edu.uniquindio.sga.domain.service.RegistroSalidaService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.NormasConvivencia;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 12.3: al iniciar, el sistema queda con datos suficientes para operar y demostrar.
 * Todo sale de la Ficha del Alojamiento (Anexo A) de SAN ROCCO.
 */
@Component
public class DatosDePrueba implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDePrueba.class);

    public static final IdentificacionAlojamiento SANROCCO = new IdentificacionAlojamiento("SANROCCO");
    private static final String RECEPCION = "recepcion@sanrocco.co";
    private static final LocalTime ENTRADA = LocalTime.of(14, 0);
    private static final LocalTime SALIDA = LocalTime.of(11, 0);

    private final AlojamientoRepository alojamientos;
    private final ApartamentoRepository apartamentos;
    private final TemporadaRepository temporadas;
    private final TarifaRepository tarifas;
    private final PoliticaCancelacionRepository politicas;
    private final CanalRepository canales;
    private final BloqueoRepository bloqueos;
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final GeneradorCodigoReserva generadorCodigo;
    private final CotizadorEstanciaService cotizador;
    private final ConfirmacionReservaService confirmacion;
    private final RegistroLlegadaService llegada;
    private final RegistroSalidaService salida;
    private final NoShowService noShow;
    private final CancelacionReservaService cancelacion;
    private final boolean habilitado;

    private Alojamiento alojamiento;
    private PoliticaCancelacion politica;

    public DatosDePrueba(AlojamientoRepository alojamientos, ApartamentoRepository apartamentos,
                         TemporadaRepository temporadas, TarifaRepository tarifas,
                         PoliticaCancelacionRepository politicas, CanalRepository canales,
                         BloqueoRepository bloqueos, ReservaRepository reservas, FolioRepository folios,
                         GeneradorCodigoReserva generadorCodigo, CotizadorEstanciaService cotizador,
                         ConfirmacionReservaService confirmacion, RegistroLlegadaService llegada,
                         RegistroSalidaService salida, NoShowService noShow,
                         CancelacionReservaService cancelacion,
                         @Value("${sga.datos-prueba.habilitado:true}") boolean habilitado) {
        this.alojamientos = alojamientos;
        this.apartamentos = apartamentos;
        this.temporadas = temporadas;
        this.tarifas = tarifas;
        this.politicas = politicas;
        this.canales = canales;
        this.bloqueos = bloqueos;
        this.reservas = reservas;
        this.folios = folios;
        this.generadorCodigo = generadorCodigo;
        this.cotizador = cotizador;
        this.confirmacion = confirmacion;
        this.llegada = llegada;
        this.salida = salida;
        this.noShow = noShow;
        this.cancelacion = cancelacion;
        this.habilitado = habilitado;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado || alojamientos.buscarPorId(SANROCCO).isPresent()) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        cargarAlojamientoYPolitica();
        cargarApartamentos();
        cargarTemporadasYTarifas(hoy.getYear());
        cargarCanales();
        cargarReservas(hoy);
        cargarBloqueosYFueraDeServicio(hoy);
        log.info("Datos de prueba cargados: alojamiento {}, 5 apartamentos, temporadas, tarifas, "
                + "política v1, canal Booking, bloqueos y {} reservas", SANROCCO.identificacion(),
                reservas.obtenerTodas().size());
    }

    // =====================================================================
    // Alojamiento y política (Ficha A.1 y A.4)
    // =====================================================================

    private void cargarAlojamientoYPolitica() {
        alojamiento = new Alojamiento(SANROCCO, "San Rocco", "Armenia",
                "Cinco apartamentos turísticos pet friendly en Armenia, Quindío. "
                        + "Todos con cocina, wifi, TV, lavadora y amoblados.",
                ENTRADA, SALIDA,
                LocalTime.of(20, 0),                 // hora límite para declarar no-show
                new UmbralEdadFacturable(5),          // los menores de 5 años no pagan
                new TiempoPreparacion(3),             // 3 h entre salida (11:00) y entrada (14:00)
                new PlazoConfirmacion(1),             // 1 h para pagar el anticipo
                true, 50,                             // anticipo del 50 % (RNP-03)
                Dinero.de(70000));                    // RNP-02: Recargo fijo por mascota
        
        alojamiento.actualizarFichaDescriptiva("Armenia, Quindío", new BigDecimal("4.5339000"),
                new BigDecimal("-75.6811000"),
                new NormasConvivencia("Mascotas permitidas según el límite de cada apartamento. Silencio de 10 p. m. a 7 a. m. "
                        + "No fumar dentro de los apartamentos."),
                List.of(new ServicioAdicional("Parqueadero", Dinero.de(15000)),
                        new ServicioAdicional("Lavandería", Dinero.de(20000))));
        alojamientos.guardar(alojamiento);

        politica = new PoliticaCancelacion("Política San Rocco v1", List.of(
                new TramoCancelacion(7, 3650, 100),
                new TramoCancelacion(0, 6, 0)));
        politicas.guardar(new VersionPolitica(1), politica);
    }

    // =====================================================================
    // Apartamentos (Ficha A.2)
    // =====================================================================

    private static final List<String> DOTACION = List.of("Cocina", "Wifi", "TV", "Lavadora", "Amoblado");

    private static String foto(String id) {
        return "https://images.unsplash.com/photo-" + id + "?auto=format&fit=crop&w=1600&q=80";
    }

    private void cargarApartamentos() {
        apartamento("AP001", "Familiar 4", 4, 10,
                List.of(foto("1522708323590-d24dbb6b0267"), foto("1484154218962-a197022b5858"), foto("1512918728675-ed5a9ecdebfd")),
                "2 baños", "4 TV");
        apartamento("AP002", "Parejas VIP", 1, 2,
                List.of(foto("1586023492125-27b2c045efd7"), foto("1560448204-e02f11c3d0e2")),
                "Jacuzzi", "Balcón");
        apartamento("AP003", "Familiar 3", 1, 3,
                List.of(foto("1502672260266-1c1ef2d93688"), foto("1583847268964-b28dc8f51f92")),
                "Cama sencilla y doble", "Balcón");
        apartamento("AP004", "Familiar 2", 2, 5,
                List.of(foto("1542298005-68757d20ee7e"), foto("1765464184843-105e144bd54b"), foto("1680965585463-386646047473")),
                "2 baños");
        apartamento("AP005", "Parejas", 1, 2,
                List.of(foto("1763827513396-5917fc6c84f3"), foto("1774311237295-a65a4c1ff38a")),
                "Balcón");
    }

    private void apartamento(String id, String nombre, int dormitorios, int capacidad, List<String> imagenes,
            String... extras) {
        List<String> caracteristicas = new ArrayList<>(DOTACION);
        caracteristicas.addAll(List.of(extras));
        apartamentos.guardar(new Apartamento(new IdentificacionApartamento(id), nombre,
                new TiempoPreparacion(3), SANROCCO, new Dormitorio(dormitorios),
                new CapacidadOcupantes(capacidad), new LimiteMascota(2),
                caracteristicas, imagenes));
    }

    private static long tarifaBase(String apartamento) {
        return switch (apartamento) {
            case "AP002" -> 120_000;
            case "AP005" -> 90_000;
            case "AP003" -> 70_000;
            case "AP004" -> 65_000;
            default -> 60_000;
        };
    }

    // =====================================================================
    // Temporadas y tarifas
    // =====================================================================

    private void cargarTemporadasYTarifas(int anio) {
        LocalDate inicio = LocalDate.of(anio - 1, 1, 1);
        LocalDate fin = LocalDate.of(anio + 1, 12, 31);

        List<Temporada> especiales = new ArrayList<>();
        for (int y = anio - 2; y <= anio + 1; y++) {
            especiales.add(temporada(LocalDate.of(y, 12, 15), LocalDate.of(y + 1, 1, 15),
                    "Alta fin de año " + y, TipoTemporada.ALTA));
        }
        for (int y = anio - 1; y <= anio + 1; y++) {
            LocalDate pascua = domingoDePascua(y);
            especiales.add(temporada(pascua.minusDays(7), pascua, "Alta Semana Santa " + y, TipoTemporada.ALTA));
            especiales.add(temporada(LocalDate.of(y, 6, 15), LocalDate.of(y, 7, 15),
                    "Alta mitad de año " + y, TipoTemporada.ALTA));
            especiales.add(temporada(LocalDate.of(y, 10, 10), LocalDate.of(y, 10, 20),
                    "Media fiestas Armenia " + y, TipoTemporada.MEDIA));
        }

        List<Temporada> todas = new ArrayList<>();
        LocalDate cursor = inicio;
        List<Temporada> ordenadas = especiales.stream()
                .filter(t -> !t.getPeriodo().hasta().isBefore(inicio) && !t.getPeriodo().desde().isAfter(fin))
                .sorted(Comparator.comparing(t -> t.getPeriodo().desde()))
                .toList();
        for (Temporada especial : ordenadas) {
            LocalDate desde = especial.getPeriodo().desde().isBefore(inicio) ? inicio : especial.getPeriodo().desde();
            LocalDate hasta = especial.getPeriodo().hasta().isAfter(fin) ? fin : especial.getPeriodo().hasta();
            if (desde.isAfter(cursor)) {
                todas.add(temporada(cursor, desde.minusDays(1), "Baja", TipoTemporada.BASE));
            }
            todas.add(temporada(desde, hasta, especial.getNombre(), especial.getTipo()));
            cursor = hasta.plusDays(1);
        }
        if (!cursor.isAfter(fin)) {
            todas.add(temporada(cursor, fin, "Baja", TipoTemporada.BASE));
        }

        for (Temporada temporada : todas) {
            temporadas.guardar(temporada);
            for (String apto : List.of("AP001", "AP002", "AP003", "AP004", "AP005")) {
                tarifas.guardar(new Tarifa(
                        new IdentificacionTarifa("TF-" + apto + "-" + temporada.getIdentificacion().id()),
                        new IdentificacionApartamento(apto),
                        temporada.getIdentificacion(),
                        Dinero.de(tarifa(tarifaBase(apto), temporada.getTipo()))));
            }
        }
    }

    private static Temporada temporada(LocalDate desde, LocalDate hasta, String nombre, TipoTemporada tipo) {
        return new Temporada(new IdentificacionTemporada("T-" + tipo + "-" + desde), new Periodo(desde, hasta),
                nombre, tipo, tipo == TipoTemporada.ALTA ? new EstanciaMinima(3) : null);
    }

    private static long tarifa(long base, TipoTemporada tipo) {
        double factor = switch (tipo) {
            case ALTA -> 1.30;
            case MEDIA -> 1.15;
            default -> 1.0;
        };
        return Math.round(base * factor / 1000.0) * 1000;
    }

    private static LocalDate domingoDePascua(int y) {
        int a = y % 19, b = y / 100, c = y % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3, h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31, dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(y, mes, dia);
    }

    // =====================================================================
    // Canales
    // =====================================================================

    private void cargarCanales() {
        canales.guardar(new Canal(new IdentificacionCanal("CANAL-BOOKING"), CanalOrigen.EXTERNO,
                "Booking (simulado)", "clave-booking-demo"));
    }

    // =====================================================================
    // Reservas
    // =====================================================================

    private void cargarReservas(LocalDate hoy) {
        // FINALIZADA · DIRECTO · AP001
        Reservada finalizada = crear("AP001", hoy.minusDays(10), CanalOrigen.DIRECTO, 0,
                "carlos.ramirez@example.com", hoy.minusDays(30).atTime(9, 0), LocalTime.of(15, 0),
                persona("1094000101", "Carlos Ramírez", 1985), persona("1094000102", "Marta López", 1987),
                persona("1094000103", "Tomás Ramírez", hoy.getYear() - 12), persona("1094000104", "Sofía Ramírez", hoy.getYear() - 3));
        confirmarConAnticipo(finalizada, hoy.minusDays(29).atTime(10, 0));
        registrarLlegada(finalizada, hoy.minusDays(10).atTime(15, 0));
        Apartamento ap001 = apto("AP001");
        salida.registrarSalida(finalizada.reserva, ap001, finalizada.folio, null, RECEPCION, hoy.minusDays(7).atTime(10, 30));
        ap001.marcarEnPreparacion();
        ap001.marcarPreparado();
        guardar(finalizada, ap001);

        // EN_CURSO · PORTAL · AP002: con 1 mascota (incluye cargo por mascota en la lista inicial del folio)
        Reservada enCurso = crear("AP002", hoy.minusDays(1), CanalOrigen.PORTAL, 1,
                "huesped@sanrocco.co", hoy.minusDays(20).atTime(20, 0), LocalTime.of(15, 30),
                persona("1094000001", "Laura Gómez", 1995), persona("1094000002", "Andrés Gómez", 1994));
        confirmarConAnticipo(enCurso, hoy.minusDays(19).atTime(9, 0));
        registrarLlegada(enCurso, hoy.minusDays(1).atTime(15, 30));

        // NO_SHOW · EXTERNO · AP004
        Reservada noPresentada = crear("AP004", hoy.minusDays(3), CanalOrigen.EXTERNO, 0,
                null, hoy.minusDays(15).atTime(11, 0), LocalTime.of(16, 0),
                persona("1094000301", "Jennifer Smith", 1990), persona("1094000302", "Mark Smith", 1989));
        confirmarConAnticipo(noPresentada, hoy.minusDays(14).atTime(11, 0));
        noShow.declararNoShow(noPresentada.reserva, noPresentada.folio, alojamiento, RECEPCION,
                hoy.minusDays(3).atTime(20, 30));
        guardar(noPresentada, null);

        // CANCELADA · DIRECTO · AP001
        Reservada cancelada = crear("AP001", hoy.plusDays(20), CanalOrigen.DIRECTO, 0,
                "luis.herrera@example.com", hoy.minusDays(5).atTime(10, 0), LocalTime.of(17, 0),
                persona("1094000401", "Luis Herrera", 1980), persona("1094000402", "Ana Herrera", 1982),
                persona("1094000403", "Pablo Herrera", 2010));
        pagarAnticipo(cancelada, hoy.minusDays(5).atTime(10, 15));
        cancelacion.cancelar(cancelada.reserva, cancelada.folio, politica, alojamiento,
                "Cambio de planes del huésped", RECEPCION, hoy.minusDays(2).atTime(9, 0));
        guardar(cancelada, null);

        // CONFIRMADA · EXTERNO · AP005
        Reservada confirmada = crear("AP005", hoy.plusDays(10), CanalOrigen.EXTERNO, 0,
                "valentina.ruiz@example.com", hoy.minusDays(3).atTime(14, 0), LocalTime.of(14, 30),
                persona("1094000501", "Valentina Ruiz", 1998), persona("1094000502", "Camilo Díaz", 1997));
        confirmarConAnticipo(confirmada, hoy.minusDays(3).atTime(14, 20));
        guardar(confirmada, null);

        // PENDIENTE · DIRECTO · AP001
        Reservada pendiente = crear("AP001", hoy.plusDays(30), CanalOrigen.DIRECTO, 0,
                "diego.torres@example.com", LocalDateTime.now(), LocalTime.of(16, 0),
                persona("1094000601", "Diego Torres", 1992), persona("1094000602", "Laura Ríos", 1993),
                persona("1094000603", "Mateo Torres", 2019));
        guardar(pendiente, null);
    }

    private record Reservada(Reserva reserva, Folio folio) {
    }

    private record Persona(String documento, String nombre, LocalDate nacimiento) {
    }

    private static Persona persona(String documento, String nombre, int anioNacimiento) {
        return new Persona(documento, nombre, LocalDate.of(anioNacimiento, 3, 1));
    }

    private Reservada crear(String apto, LocalDate entrada, CanalOrigen canal, int mascotas, String correo,
                            LocalDateTime creadaEn, LocalTime horaLlegada, Persona... personas) {
        IdentificacionApartamento id = new IdentificacionApartamento(apto);
        Estancia estancia = new Estancia(entrada, entrada.plusDays(3));
        List<Ocupante> ocupantes = new ArrayList<>();
        for (Persona p : personas) {
            ocupantes.add(new Ocupante(new DocumentoIdentidad(p.documento()), p.nombre(), p.nacimiento(),
                    creadaEn.toLocalDate()));
        }
        ValorCongelado valor = cotizador.cotizar(id, estancia, ocupantes, alojamiento.getUmbralEdadFacturable(),
                temporadas.buscarEntre(entrada, estancia.fechaSalida().minusDays(1)),
                tarifas.buscarPorApartamento(id));
        
        Reserva reserva = Reserva.crear(generadorCodigo.siguiente(creadaEn.getYear()), id, estancia,
                ocupantes.get(0), ocupantes, canal, valor, alojamiento.getVersionPoliticaVigente(), creadaEn,
                new CantidadMascota(mascotas));
        if (correo != null) {
            reserva.registrarCorreoContacto(new Correo(correo));
        }
        reserva.indicarHoraEstimadaLlegada(horaLlegada, RECEPCION);

        // Construir la lista inicial de cargos (F-08 y RNP-02)
        List<Cargo> cargosIniciales = new ArrayList<>();
        cargosIniciales.add(new Cargo(valor.total(), "Alojamiento", TipoCargo.ALOJAMIENTO, creadaEn));
        if (mascotas > 0) {
            cargosIniciales.add(new Cargo(alojamiento.getRecargoMascota(), "Recargo por mascota", TipoCargo.SERVICIO_ADICIONAL, creadaEn));
        }

        Folio folio = new Folio(reserva.getCodigo(), cargosIniciales);
        return new Reservada(reserva, folio);
    }

    private void pagarAnticipo(Reservada r, LocalDateTime cuando) {
        Dinero anticipo = confirmacion.calcularAnticipoRequerido(r.reserva.getValor().total(),
                alojamiento.getPorcentajeAnticipo());
        r.folio.registrarPago(new Pago(anticipo, MedioPago.TARJETA_CREDITO, cuando));
    }

    private void confirmarConAnticipo(Reservada r, LocalDateTime cuando) {
        pagarAnticipo(r, cuando);
        confirmacion.autorizarConfirmacion(r.reserva, r.folio, alojamiento, cuando.plusMinutes(5), RECEPCION);
    }

    private void registrarLlegada(Reservada r, LocalDateTime cuando) {
        if (r.folio.saldo().esPositivo()) {
            r.folio.registrarPago(new Pago(r.folio.saldo(), MedioPago.EFECTIVO, cuando));
        }
        Apartamento apartamento = apto(r.reserva.getApartamento().valor());
        llegada.registrarLlegada(r.reserva, apartamento, r.folio, RECEPCION, cuando);
        guardar(r, apartamento);
    }

    private Apartamento apto(String id) {
        return apartamentos.buscarPorId(new IdentificacionApartamento(id)).orElseThrow();
    }

    private void guardar(Reservada r, Apartamento apartamento) {
        reservas.guardar(r.reserva);
        folios.guardar(r.folio);
        if (apartamento != null) {
            apartamentos.guardar(apartamento);
        }
    }

    private void cargarBloqueosYFueraDeServicio(LocalDate hoy) {
        Apartamento ap003 = apto("AP003");
        ap003.marcarFueraDeServicio();
        apartamentos.guardar(ap003);
        bloqueos.guardar(new Bloqueo(new IdentificacionBloqueo("B-AP003-DANO"), ap003.getIdentificacion(),
                new Periodo(hoy, hoy.plusDays(15)), MotivoBloqueo.FUERA_DE_SERVICIO));

        bloqueos.guardar(new Bloqueo(new IdentificacionBloqueo("B-AP004-MANT"), new IdentificacionApartamento("AP004"),
                new Periodo(hoy.plusDays(40), hoy.plusDays(45)), MotivoBloqueo.MANTENIMIENTO));

        Bloqueo remodelacion = new Bloqueo(new IdentificacionBloqueo("B-AP002-REMO"),
                new IdentificacionApartamento("AP002"), new Periodo(hoy.minusDays(60), hoy.minusDays(50)),
                MotivoBloqueo.REMODELACION);
        remodelacion.levantar();
        bloqueos.guardar(remodelacion);
    }
}