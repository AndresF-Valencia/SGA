import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { EstadoReservaBadge } from '../componentes/EstadoReservaBadge';
import type { Folio } from '../modelos/folio';
import type { ReservaResponse } from '../modelos/reserva';
import { obtenerFolio } from '../servicios/folioServicio';
import { cancelarReserva, obtenerReserva } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import { confirmarAccion, notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';
import { useAuth } from '../hooks/useAuth';

export default function DetalleReserva() {
  const { codigo = '' } = useParams();
  const { tieneRol } = useAuth();
  const [reserva, setReserva] = useState<ReservaResponse | null>(null);
  const [folio, setFolio] = useState<Folio | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    obtenerReserva(codigo)
      .then(setReserva)
      .catch((e: unknown) => setError(mensajeDeError(e)))
      .finally(() => setCargando(false));
  }, [codigo]);

  async function verFolio() {
    try {
      setFolio(await obtenerFolio(codigo));
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function cancelar() {
    if (!await confirmarAccion('¿Cancelar esta reserva?', 'La política de cancelación se aplicará según las fechas.')) return;
    const motivo = await pedirMotivo('Motivo de cancelación');
    if (!motivo) return;
    try {
      setReserva(await cancelarReserva(codigo, motivo));
      await notificarExito('Reserva cancelada.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  if (cargando) return <Cargando />;
  if (error || !reserva) return <div className="alert alert-danger">{error ?? 'Reserva no encontrada.'}</div>;

  const puedeCancelar = ['PENDIENTE', 'CONFIRMADA'].includes(reserva.estadoReserva);
  const rutaRegreso = tieneRol('HUESPED') ? '/mis-reservas' : '/reservas';

  return (
    <section>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">Reserva {reserva.codigoReserva}</h1>
        <EstadoReservaBadge estado={reserva.estadoReserva} />
      </div>
      <div className="card mb-4">
        <div className="card-body row g-3">
          <div className="col-12 col-md-6"><strong>Apartamento:</strong> {reserva.apartamentoId}</div>
          <div className="col-12 col-md-6"><strong>Canal:</strong> {reserva.canalOrigen}</div>
          <div className="col-12 col-md-6"><strong>Entrada:</strong> {formatearFecha(reserva.fechaEntrada)}</div>
          <div className="col-12 col-md-6"><strong>Salida:</strong> {formatearFecha(reserva.fechaSalida)}</div>
          <div className="col-12 col-md-6"><strong>Total:</strong> {formatearPesos(reserva.valorTotal)}</div>
          <div className="col-12 col-md-6"><strong>Mascotas:</strong> {reserva.cantidadMascotas}</div>
          <div className="col-12"><strong>Titular:</strong> {reserva.titular.nombre} · {reserva.titular.email}</div>
        </div>
      </div>

      <div className="d-flex gap-2 mb-4">
        <button type="button" className="btn btn-outline-primary" onClick={() => void verFolio()}>Ver folio</button>
        {puedeCancelar && <button type="button" className="btn btn-outline-danger" onClick={() => void cancelar()}>Cancelar reserva</button>}
        <Link to={rutaRegreso} className="btn btn-outline-secondary">Volver</Link>
      </div>

      {folio && (
        <article className="card">
          <div className="card-header d-flex justify-content-between"><span>Folio</span><strong>{folio.estadoFolio}</strong></div>
          <div className="card-body">
            <p className="mb-1">Cargos: {formatearPesos(folio.subtotalCargos)}</p>
            <p className="mb-1">Pagos: {formatearPesos(folio.totalPagos)}</p>
            <p className="mb-0">Saldo pendiente: <strong>{formatearPesos(folio.saldoPendiente)}</strong></p>
          </div>
        </article>
      )}
    </section>
  );
}