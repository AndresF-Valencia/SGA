// Página "/reservas" (RECEPCIONISTA, ADMINISTRADOR): lista las reservas de un estado con
// reservaServicio.listarReservas (GET /api/reservas) y las pinta con TarjetaReserva.
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { Pagina } from '../modelos/pagina';
import { ESTADOS_RESERVA, ETIQUETA_ESTADO, type EstadoReserva, type ReservaResumen } from '../modelos/reserva';
import { listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';

// Filtro por estado, carga con limpieza y estados de carga, error y lista vacía
export default function ListaReservas() {
  const [estado, setEstado] = useState<EstadoReserva>('PENDIENTE');
  const [pagina, setPagina] = useState<Pagina<ReservaResumen> | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // El usuario provoca la nueva consulta: aquí se prepara la pantalla
  function cambiarEstado(nuevo: EstadoReserva) {
    setEstado(nuevo);
    setCargando(true);
    setError(null);
  }

  // Se ejecuta al abrir y cada vez que cambia el estado; descarta respuestas viejas
  useEffect(() => {
    let ignorar = false;
    listarReservas(estado)
      .then((datos) => { if (!ignorar) setPagina(datos); })
      .catch((e: unknown) => { if (!ignorar) setError(mensajeDeError(e)); })
      .finally(() => { if (!ignorar) setCargando(false); });
    return () => { ignorar = true; };
  }, [estado]);

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
        <select id="estado" className="form-select" value={estado}
          onChange={(e) => cambiarEstado(e.target.value as EstadoReserva)}>
          {ESTADOS_RESERVA.map((valor) => <option key={valor} value={valor}>{ETIQUETA_ESTADO[valor]}</option>)}
        </select>
      </div>

      {cargando && <Cargando />}

      {!cargando && error && <div className="alert alert-danger">{error}</div>}

      {!cargando && !error && reservas.length === 0 && (
        <div className="text-center text-body-secondary py-5">
          <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
          No hay reservas en estado {ETIQUETA_ESTADO[estado]}.
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
                <TarjetaReserva reserva={reserva} />
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
