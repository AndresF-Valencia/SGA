package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import static co.edu.uniquindio.sga.domain.service.Escenario.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.AlojamientoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ApartamentoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.BloqueoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CanalPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CotizacionPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.FolioPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.PoliticaCancelacionPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TarifaPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.TemporadaPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

/**
 * Ida y vuelta de cada mapper: dominio → entity → dominio debe conservar todos los datos.
 * Son pruebas unitarias: no arrancan Spring ni la base de datos.
 */
@DisplayName("Mappers de persistencia")
class MappersPersistenciaTest {

    @Test
    @DisplayName("Alojamiento: conserva sus parámetros y la versión vigente de la política")
    void alojamientoIdaYVuelta() {
        Alojamiento original = alojamiento();
        original.publicarNuevaVersionPolitica();
        original.publicarNuevaVersionPolitica();

        Alojamiento copia = AlojamientoPersistenceMapper.aDominio(AlojamientoPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacion(), copia.getIdentificacion());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getCiudad(), copia.getCiudad());
        assertEquals(original.getDescripcion(), copia.getDescripcion());
        assertEquals(original.getHoraEntrada(), copia.getHoraEntrada());
        assertEquals(original.getHoraSalida(), copia.getHoraSalida());
        assertEquals(original.getHoraLimiteNoShow(), copia.getHoraLimiteNoShow());
        assertEquals(original.getUmbralEdadFacturable(), copia.getUmbralEdadFacturable());
        assertEquals(original.getTiempoPreparacion(), copia.getTiempoPreparacion());
        assertEquals(original.getPlazoConfirmacion(), copia.getPlazoConfirmacion());
        assertEquals(original.isExigeAnticipo(), copia.isExigeAnticipo());
        assertEquals(original.getPorcentajeAnticipo(), copia.getPorcentajeAnticipo());
        assertEquals(new VersionPolitica(3), copia.getVersionPoliticaVigente());
    }

    @Test
    @DisplayName("Apartamento: conserva estado operativo, activo, listas y novedades")
    void apartamentoIdaYVuelta() {
        Apartamento original = apartamento(APTO_101);
        original.agregarCaracteristica("Wifi");
        original.agregarImagen("https://sanrocco.co/101.jpg");
        original.registrarNovedad(Novedad.de("Grifo goteando", "BAJA", RECEPCION, AHORA));
        original.marcarOcupado();
        original.desactivar();

        Apartamento copia = ApartamentoPersistenceMapper.aDominio(ApartamentoPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacion(), copia.getIdentificacion());
        assertEquals(original.getIdentificacionAlojamiento(), copia.getIdentificacionAlojamiento());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getDormitorios(), copia.getDormitorios());
        assertEquals(original.getCapacidad(), copia.getCapacidad());
        assertEquals(original.getLimiteMascotas(), copia.getLimiteMascotas());
        assertEquals(original.getTiempoPreparacion(), copia.getTiempoPreparacion());
        assertEquals(EstadoOperativo.OCUPADO, copia.getEstadoOperativo());
        assertFalse(copia.estaActivo());
        assertEquals(original.getCaracteristicas(), copia.getCaracteristicas());
        assertEquals(original.getImagenes(), copia.getImagenes());
        assertEquals(original.getNovedades(), copia.getNovedades());
    }

    @Test
    @DisplayName("Bloqueo: un bloqueo levantado vuelve levantado")
    void bloqueoIdaYVuelta() {
        Bloqueo original = new Bloqueo(new IdentificacionBloqueo("B-1"), APTO_101,
                new Periodo(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5)), MotivoBloqueo.MANTENIMIENTO);
        original.levantar();

        Bloqueo copia = BloqueoPersistenceMapper.aDominio(BloqueoPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacionBloqueo(), copia.getIdentificacionBloqueo());
        assertEquals(original.getApartamento(), copia.getApartamento());
        assertEquals(original.getPeriodo(), copia.getPeriodo());
        assertEquals(original.getMotivo(), copia.getMotivo());
        assertEquals(EstadoBloqueo.LEVANTADO, copia.getEstado());
    }

    @Test
    @DisplayName("Canal: un canal desactivado vuelve desactivado")
    void canalIdaYVuelta() {
        Canal original = new Canal(new IdentificacionCanal("C-1"), CanalOrigen.EXTERNO, "Booking", "clave-booking");
        original.desactivar();

        Canal copia = CanalPersistenceMapper.aDominio(CanalPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacion(), copia.getIdentificacion());
        assertEquals(original.getTipoCanal(), copia.getTipoCanal());
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getCredencialAcceso(), copia.getCredencialAcceso());
        assertFalse(copia.isActivo());
    }

    @Test
    @DisplayName("Cotización: conserva estancia, ocupantes, mascotas y desglose")
    void cotizacionIdaYVuelta() {
        Cotizacion original = new Cotizacion(new IdentificacionCotizacion("COT-1"), APTO_101, NOVIEMBRE, 2,
                new CantidadMascota(1), valorBase(NOVIEMBRE, 2), AHORA);

        Cotizacion copia = CotizacionPersistenceMapper.aDominio(CotizacionPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacion(), copia.getIdentificacion());
        assertEquals(original.getIdentificacionApartamento(), copia.getIdentificacionApartamento());
        assertEquals(original.getEstancia(), copia.getEstancia());
        assertEquals(original.getOcupantesFacturables(), copia.getOcupantesFacturables());
        assertEquals(original.getCantidadMascota(), copia.getCantidadMascota());
        assertEquals(original.getValor(), copia.getValor());
        assertEquals(original.getCreadaEn(), copia.getCreadaEn());
    }

    @Test
    @DisplayName("Folio: conserva cargos (incluido un ajuste negativo), pagos y cierre autorizado")
    void folioCerradoConAutorizacion() {
        Folio original = folio(reserva("RES-1", NOVIEMBRE));
        original.registrarPago(pago(150_000));
        original.registrarCargo(new Cargo(Dinero.de(-10_000), "Descuento", TipoCargo.AJUSTE, AHORA));
        original.cerrarConAutorizacion(new AutorizacionCierre("administrador", "Cortesía", AHORA));

        Folio copia = FolioPersistenceMapper.aDominio(FolioPersistenceMapper.aEntity(original));

        assertEquals(original.getReserva(), copia.getReserva());
        assertEquals(original.getCargos(), copia.getCargos());
        assertEquals(1, copia.getPagos().size());
        assertEquals(original.getPagos().get(0).getMonto(), copia.getPagos().get(0).getMonto());
        assertEquals(original.getPagos().get(0).getMedioPago(), copia.getPagos().get(0).getMedioPago());
        assertEquals(original.getPagos().get(0).getHoraPago(), copia.getPagos().get(0).getHoraPago());
        assertEquals(original.saldo(), copia.saldo());
        assertTrue(copia.isCerrado());
        assertEquals(original.getAutorizacionCierre(), copia.getAutorizacionCierre());
    }

    @Test
    @DisplayName("Folio: uno abierto vuelve abierto, y uno cerrado en cero vuelve cerrado sin autorización")
    void folioAbiertoYCerradoEnCero() {
        Folio abierto = folio(reserva("RES-2", NOVIEMBRE));
        Folio copiaAbierto = FolioPersistenceMapper.aDominio(FolioPersistenceMapper.aEntity(abierto));
        assertFalse(copiaAbierto.isCerrado());

        Folio enCero = folio(reserva("RES-3", NOVIEMBRE));
        enCero.registrarPago(pago(600_000));
        enCero.cerrar();
        Folio copiaEnCero = FolioPersistenceMapper.aDominio(FolioPersistenceMapper.aEntity(enCero));
        assertTrue(copiaEnCero.isCerrado());
        assertNull(copiaEnCero.getAutorizacionCierre());
    }

    @Test
    @DisplayName("Tarifa: conserva apartamento, temporada y valor")
    void tarifaIdaYVuelta() {
        Tarifa original = TARIFAS.get(1);

        Tarifa copia = TarifaPersistenceMapper.aDominio(TarifaPersistenceMapper.aEntity(original));

        assertEquals(original.getIdentificacion(), copia.getIdentificacion());
        assertEquals(original.getApartamento(), copia.getApartamento());
        assertEquals(original.getTemporada(), copia.getTemporada());
        assertEquals(original.getValorPorOcupanteNoche(), copia.getValorPorOcupanteNoche());
    }

    @Test
    @DisplayName("Temporada: conserva la estancia mínima, y null cuando no tiene")
    void temporadaIdaYVuelta() {
        for (Temporada original : List.of(ALTA, BASE)) {
            Temporada copia = TemporadaPersistenceMapper.aDominio(TemporadaPersistenceMapper.aEntity(original));

            assertEquals(original.getIdentificacion(), copia.getIdentificacion());
            assertEquals(original.getPeriodo(), copia.getPeriodo());
            assertEquals(original.getNombre(), copia.getNombre());
            assertEquals(original.getTipo(), copia.getTipo());
            assertEquals(original.getEstanciaMinima(), copia.getEstanciaMinima());
        }
    }

    @Test
    @DisplayName("Política de cancelación: conserva el nombre y los tramos en orden")
    void politicaIdaYVuelta() {
        PoliticaCancelacion copia = PoliticaCancelacionPersistenceMapper.aDominio(
                PoliticaCancelacionPersistenceMapper.aEntity(new VersionPolitica(1), POLITICA));

        assertEquals(POLITICA, copia);
    }
}
