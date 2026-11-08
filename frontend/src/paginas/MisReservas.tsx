// Página "/mis-reservas" (HUESPED): reservas cuyo titular es el usuario del token, con
// reservaServicio.listarMisReservas (GET /api/reservas/mis-reservas) y TarjetaReserva.
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Paginacion } from '../componentes/Paginacion';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { Pagina } from '../modelos/pagina';
import { ESTADOS_RESERVA, ETIQUETA_ESTADO, type EstadoReserva, type ReservaResumen } from '../modelos/reserva';
import { listarMisReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';

// '' significa "todos los estados" (el parámetro no se envía)
type FiltroEstado = EstadoReserva | '';

// Filtro opcional por estado, carga con limpieza y estados de carga, error y lista vacía
export default function MisReservas() {
  const [estado, setEstado] = useState<FiltroEstado>('');
  const [paginaActual, setPaginaActual] = useState(0);
  const [pagina, setPagina] = useState<Pagina<ReservaResumen> | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // El usuario provoca la nueva consulta: aquí se prepara la pantalla
  function cambiarEstado(nuevo: FiltroEstado) {
    setEstado(nuevo);
    setPaginaActual(0);
    setCargando(true);
    setError(null);
  }

  // Se ejecuta al abrir y cada vez que cambia el filtro; descarta respuestas viejas
  useEffect(() => {
    let ignorar = false;
    listarMisReservas(estado || undefined, paginaActual)
      .then((datos) => { if (!ignorar) setPagina(datos); })
      .catch((e: unknown) => { if (!ignorar) setError(mensajeDeError(e)); })
      .finally(() => { if (!ignorar) setCargando(false); });
    return () => { ignorar = true; };
  }, [estado, paginaActual]);

  const reservas = pagina?.contenido ?? [];

  return (
    <section>
      <div className="titulo-pagina">
        <div>
          <h1 className="h2">Mis reservas</h1>
          <p className="text-body-secondary mb-0">Tus estancias en San Rocco: las próximas, la que está en curso y las pasadas.</p>
        </div>
        <div className="d-flex flex-wrap gap-2">
          <Link to="/disponibilidad" className="btn btn-outline-primary">Buscar fechas</Link>
          <Link to="/reservas/nueva" className="btn btn-primary">
            <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva reserva
          </Link>
        </div>
      </div>

      <div className="mb-3" style={{ maxWidth: '16rem' }}>
        <label htmlFor="estado" className="form-label">Estado</label>
        <select id="estado" className="form-select" value={estado}
          onChange={(e) => cambiarEstado(e.target.value as FiltroEstado)}>
          <option value="">Todos</option>
          {ESTADOS_RESERVA.map((valor) => <option key={valor} value={valor}>{ETIQUETA_ESTADO[valor]}</option>)}
        </select>
      </div>

      {cargando && <Cargando />}

      {!cargando && error && <div className="alert alert-danger">{error}</div>}

      {!cargando && !error && reservas.length === 0 && (
        <div className="text-center text-body-secondary py-5">
          <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
          <p className="mb-3">{estado ? `No tienes reservas en estado ${ETIQUETA_ESTADO[estado]}.` : 'Todavía no tienes reservas.'}</p>
          {!estado && <Link to="/disponibilidad" className="btn btn-primary">Buscar fechas</Link>}
        </div>
      )}

      {!cargando && !error && reservas.length > 0 && (
        <>
          <div className="row g-3">
            {reservas.map((reserva) => (
              <div key={reserva.codigoReserva} className="col-12 col-md-6 col-lg-4">
                <TarjetaReserva reserva={reserva} />
              </div>
            ))}
          </div>
          <Paginacion
            paginaActual={paginaActual}
            totalPaginas={pagina?.totalPaginas ?? 0}
            alCambiar={setPaginaActual}
          />
        </>
      )}
    </section>
  );
}
