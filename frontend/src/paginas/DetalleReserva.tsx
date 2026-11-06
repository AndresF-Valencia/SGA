import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { EstadoReservaBadge } from '../componentes/EstadoReservaBadge';
import type { Folio } from '../modelos/folio';
import type { ReservaResponse } from '../modelos/reserva';
import { cerrarFolio, obtenerFolio, registrarCargo, registrarPago } from '../servicios/folioServicio';
import { cancelarReserva, confirmarReserva, declararNoShow, modificarReserva, obtenerReserva, registrarLlegada, registrarSalida } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import { confirmarAccion, notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';
import { useAuth } from '../hooks/useAuth';

const FECHA_HOY = new Date().toISOString().slice(0, 10);

export default function DetalleReserva() {
  const { codigo = '' } = useParams();
  const { tieneRol, sesion } = useAuth();
  const [reserva, setReserva] = useState<ReservaResponse | null>(null);
  const [folio, setFolio] = useState<Folio | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editando, setEditando] = useState(false);
  const [modificacion, setModificacion] = useState({ apartamentoId: '', fechaEntrada: '', fechaSalida: '', cantidadMascotas: 0 });
  const [salida, setSalida] = useState({ motivo: '', observaciones: '' });
  const [cargo, setCargo] = useState({ concepto: '', monto: 0 });
  const [pago, setPago] = useState({ monto: 0, medioPago: 'EFECTIVO', fechaPago: FECHA_HOY });

  useEffect(() => {
    obtenerReserva(codigo)
      .then((datos) => {
        setReserva(datos);
        setModificacion({ apartamentoId: datos.apartamentoId, fechaEntrada: datos.fechaEntrada, fechaSalida: datos.fechaSalida, cantidadMascotas: datos.cantidadMascotas });
      })
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

  async function ejecutarReserva(accion: () => Promise<ReservaResponse>, mensaje: string) {
    try {
      setReserva(await accion());
      await notificarExito(mensaje);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function confirmar() {
    if (await confirmarAccion('¿Confirmar reserva?', 'Se aplicarán las reglas de anticipo.')) {
      await ejecutarReserva(() => confirmarReserva(codigo), 'Reserva confirmada.');
    }
  }

  async function llegada() {
    if (await confirmarAccion('¿Registrar llegada?', 'La reserva pasará a EN_CURSO.')) {
      await ejecutarReserva(() => registrarLlegada(codigo), 'Llegada registrada.');
    }
  }

  async function noShow() {
    if (await confirmarAccion('¿Declarar no-show?', 'Se aplicará la política de no presentación.')) {
      await ejecutarReserva(() => declararNoShow(codigo), 'No-show registrado.');
    }
  }

  async function modificar(evento: React.FormEvent) {
    evento.preventDefault();
    await ejecutarReserva(() => modificarReserva(codigo, modificacion), 'Reserva modificada.');
    setEditando(false);
  }

  async function salidaReserva(evento: React.FormEvent) {
    evento.preventDefault();
    await ejecutarReserva(() => registrarSalida(codigo, { ...salida, autor: sesion?.correo ?? '' }), 'Salida registrada.');
  }

  async function agregarCargo(evento: React.FormEvent) {
    evento.preventDefault();
    try {
      setFolio(await registrarCargo(codigo, cargo));
      setCargo({ concepto: '', monto: 0 });
      await notificarExito('Cargo registrado.');
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  async function agregarPago(evento: React.FormEvent) {
    evento.preventDefault();
    try {
      setFolio(await registrarPago(codigo, pago));
      await notificarExito('Pago registrado.');
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  async function cerrar() {
    const motivo = await pedirMotivo('Motivo para cerrar el folio');
    if (!motivo) return;
    try {
      setFolio(await cerrarFolio(codigo, { autor: sesion?.correo ?? '', motivo, observaciones: '' }));
      await notificarExito('Folio cerrado.');
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  if (cargando) return <Cargando />;
  if (error || !reserva) return <div className="alert alert-danger">{error ?? 'Reserva no encontrada.'}</div>;

  const puedeCancelar = ['PENDIENTE', 'CONFIRMADA'].includes(reserva.estadoReserva);
  const esRecepcion = tieneRol('RECEPCIONISTA', 'ADMINISTRADOR');
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
        {esRecepcion && reserva.estadoReserva === 'PENDIENTE' && <button type="button" className="btn btn-success" onClick={() => void confirmar()}>Confirmar</button>}
        {esRecepcion && reserva.estadoReserva === 'CONFIRMADA' && <button type="button" className="btn btn-success" onClick={() => void llegada()}>Registrar llegada</button>}
        {esRecepcion && ['CONFIRMADA', 'EN_CURSO'].includes(reserva.estadoReserva) && <button type="button" className="btn btn-outline-dark" onClick={() => void noShow()}>No-show</button>}
        {esRecepcion && ['PENDIENTE', 'CONFIRMADA'].includes(reserva.estadoReserva) && <button type="button" className="btn btn-outline-secondary" onClick={() => setEditando(!editando)}>Modificar</button>}
        <Link to={rutaRegreso} className="btn btn-outline-secondary">Volver</Link>
      </div>

      {esRecepcion && editando && (
        <form className="card card-body mb-4" onSubmit={(evento) => void modificar(evento)}>
          <h2 className="h5">Modificar estancia</h2>
          <div className="row g-2">
            <div className="col-md-3"><label className="form-label">Apartamento</label><input className="form-control" value={modificacion.apartamentoId} onChange={(e) => setModificacion({ ...modificacion, apartamentoId: e.target.value })} required /></div>
            <div className="col-md-3"><label className="form-label">Entrada</label><input type="date" className="form-control" value={modificacion.fechaEntrada} onChange={(e) => setModificacion({ ...modificacion, fechaEntrada: e.target.value })} required /></div>
            <div className="col-md-3"><label className="form-label">Salida</label><input type="date" className="form-control" value={modificacion.fechaSalida} onChange={(e) => setModificacion({ ...modificacion, fechaSalida: e.target.value })} required /></div>
            <div className="col-md-3"><label className="form-label">Mascotas</label><input type="number" min="0" className="form-control" value={modificacion.cantidadMascotas} onChange={(e) => setModificacion({ ...modificacion, cantidadMascotas: Number(e.target.value) })} required /></div>
          </div>
          <button className="btn btn-primary mt-3 align-self-start">Guardar modificación</button>
        </form>
      )}

      {esRecepcion && reserva.estadoReserva === 'EN_CURSO' && (
        <form className="card card-body mb-4" onSubmit={(evento) => void salidaReserva(evento)}>
          <h2 className="h5">Registrar salida</h2>
          <div className="row g-2">
            <div className="col-md-6"><label className="form-label">Motivo</label><input className="form-control" minLength={5} maxLength={300} value={salida.motivo} onChange={(e) => setSalida({ ...salida, motivo: e.target.value })} required /></div>
            <div className="col-md-6"><label className="form-label">Observaciones</label><input className="form-control" maxLength={300} value={salida.observaciones} onChange={(e) => setSalida({ ...salida, observaciones: e.target.value })} /></div>
          </div>
          <button className="btn btn-primary mt-3 align-self-start">Registrar salida</button>
        </form>
      )}

      {folio && (
        <article className="card">
          <div className="card-header d-flex justify-content-between"><span>Folio</span><strong>{folio.estadoFolio}</strong></div>
          <div className="card-body">
            <p className="mb-1">Cargos: {formatearPesos(folio.subtotalCargos)}</p>
            <p className="mb-1">Pagos: {formatearPesos(folio.totalPagos)}</p>
            <p className="mb-0">Saldo pendiente: <strong>{formatearPesos(folio.saldoPendiente)}</strong></p>
            {esRecepcion && folio.estadoFolio === 'ABIERTO' && (
              <div className="row g-3 mt-2">
                <form className="col-12 col-lg-4" onSubmit={(evento) => void agregarCargo(evento)}>
                  <h3 className="h6">Agregar cargo</h3>
                  <input className="form-control mb-2" placeholder="Concepto" value={cargo.concepto} onChange={(e) => setCargo({ ...cargo, concepto: e.target.value })} required />
                  <input type="number" min="1" className="form-control mb-2" placeholder="Monto" value={cargo.monto || ''} onChange={(e) => setCargo({ ...cargo, monto: Number(e.target.value) })} required />
                  <button className="btn btn-sm btn-outline-primary">Registrar cargo</button>
                </form>
                <form className="col-12 col-lg-4" onSubmit={(evento) => void agregarPago(evento)}>
                  <h3 className="h6">Registrar pago</h3>
                  <input type="number" min="1" className="form-control mb-2" placeholder="Monto" value={pago.monto || ''} onChange={(e) => setPago({ ...pago, monto: Number(e.target.value) })} required />
                  <select className="form-select mb-2" value={pago.medioPago} onChange={(e) => setPago({ ...pago, medioPago: e.target.value })}><option>EFECTIVO</option><option>TARJETA_CREDITO</option><option>TARJETA_DEBITO</option></select>
                  <button className="btn btn-sm btn-outline-primary">Registrar pago</button>
                </form>
                <div className="col-12 col-lg-4 d-flex align-items-end"><button type="button" className="btn btn-outline-danger" onClick={() => void cerrar()}>Cerrar folio</button></div>
              </div>
            )}
          </div>
        </article>
      )}
    </section>
  );
}