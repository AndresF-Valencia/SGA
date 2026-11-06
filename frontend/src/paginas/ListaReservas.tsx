// Página "/reservas" (RECEPCIONISTA, ADMINISTRADOR): lista las reservas de un estado con
// reservaServicio.listarReservas (GET /api/reservas) y las pinta con TarjetaReserva.
import axios from 'axios';
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Paginacion } from '../componentes/Paginacion';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import { ESTADOS_RESERVA, ETIQUETA_ESTADO, type ReservaResumen } from '../modelos/reserva';
import { useDebounce } from '../hooks/useDebounce';
import { useFiltros } from '../hooks/useFiltros';
import type { Pagina } from '../modelos/pagina';
import { cancelarReserva, confirmarReserva, listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { confirmarAccion, notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';

// Filtro por estado, carga con limpieza y estados de carga, error y lista vacía
export default function ListaReservas() {
  const { filtros, despachar } = useFiltros();
  const documentoTitularEstable = useDebounce(filtros.documentoTitular);
  const [version, setVersion] = useState(0);
  const [pagina, setPagina] = useState<Pagina<ReservaResumen> | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Cada consulta cancela la anterior para que una respuesta vieja no sobrescriba la actual.
  useEffect(() => {
    const controlador = new AbortController();
    setCargando(true);
    setError(null);
    listarReservas(filtros.estado, filtros.pagina, documentoTitularEstable, controlador.signal)
      .then((datos) => setPagina(datos))
      .catch((e: unknown) => {
        if (!axios.isCancel(e)) setError(mensajeDeError(e));
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCargando(false);
      });
    return () => controlador.abort();
  }, [filtros.estado, filtros.pagina, documentoTitularEstable, version]);

  async function confirmar(codigo: string) {
    if (!await confirmarAccion('¿Confirmar reserva?', 'La reserva pasará al estado confirmada.')) return;
    try {
      await confirmarReserva(codigo);
      await notificarExito('Reserva confirmada.');
      setVersion((actual) => actual + 1);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function cancelar(codigo: string) {
    const motivo = await pedirMotivo('Cancelar reserva');
    if (!motivo) return;
    try {
      await cancelarReserva(codigo, motivo);
      await notificarExito('Reserva cancelada.');
      setVersion((actual) => actual + 1);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  const reservas = pagina?.contenido ?? [];

  return (
    <section>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 className="h3 mb-0"><i className="bi bi-journal-text me-2" aria-hidden="true"></i>Reservas</h1>
        <Link to="/reservas/nueva" className="btn btn-primary">
          <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva reserva
        </Link>
      </div>

      <div className="mb-3" style={{ maxWidth: '16rem' }}>
        <label htmlFor="estado" className="form-label">Estado</label>
          <select id="estado" className="form-select" value={filtros.estado}
            onChange={(e) => despachar({ tipo: 'cambiarEstado', estado: e.target.value as typeof filtros.estado })}>
            <option value="">Todos los estados</option>
          {ESTADOS_RESERVA.map((valor) => <option key={valor} value={valor}>{ETIQUETA_ESTADO[valor]}</option>)}
        </select>
      </div>

      <div className="mb-3" style={{ maxWidth: '24rem' }}>
        <label htmlFor="documentoTitular" className="form-label">Documento del titular</label>
        <input
          id="documentoTitular"
          className="form-control"
          value={filtros.documentoTitular}
          onChange={(e) => despachar({ tipo: 'cambiarDocumentoTitular', documentoTitular: e.target.value })}
          placeholder="Buscar por documento"
        />
      </div>

      {cargando && <Cargando />}

      {!cargando && error && <div className="alert alert-danger">{error}</div>}

      {!cargando && !error && reservas.length === 0 && (
        <div className="text-center text-body-secondary py-5">
          <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
          No hay reservas {filtros.estado ? `en estado ${ETIQUETA_ESTADO[filtros.estado]}` : 'para estos filtros'}.
        </div>
      )}

      {!cargando && !error && reservas.length > 0 && (
        <>
          {/* El backend entrega de a 10; la paginación con botones llega en la Guía 19 */}
          <p className="text-body-secondary small">
            Mostrando {reservas.length} de {pagina?.totalElementos} reserva(s).
          </p>
          <div className="row g-3">
            {reservas.map((reserva) => (
              <div key={reserva.codigoReserva} className="col-12 col-md-6 col-lg-4">
                <TarjetaReserva
                  reserva={reserva}
                  acciones={(
                    <>
                      {reserva.estadoReserva === 'PENDIENTE' && (
                        <button type="button" className="btn btn-sm btn-success" onClick={() => void confirmar(reserva.codigoReserva)}>
                          Confirmar
                        </button>
                      )}
                      {['PENDIENTE', 'CONFIRMADA'].includes(reserva.estadoReserva) && (
                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => void cancelar(reserva.codigoReserva)}>
                          Cancelar
                        </button>
                      )}
                    </>
                  )}
                />
              </div>
            ))}
          </div>
          <Paginacion
            paginaActual={filtros.pagina}
            totalPaginas={pagina?.totalPaginas ?? 0}
            alCambiar={(pagina) => despachar({ tipo: 'cambiarPagina', pagina })}
          />
        </>
      )}
    </section>
  );
}
