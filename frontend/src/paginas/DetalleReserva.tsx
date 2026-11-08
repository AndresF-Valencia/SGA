import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Puerta } from '../componentes/Puerta';
import { EstadoReservaBadge } from '../componentes/EstadoReservaBadge';
import type { Folio } from '../modelos/folio';
import type { ReservaResponse } from '../modelos/reserva';
import { cerrarFolio, obtenerFolio, registrarCargo, registrarPago } from '../servicios/folioServicio';
import { cancelarReserva, confirmarReserva, declararNoShow, modificarReserva, obtenerReserva, registrarLlegada, registrarSalida } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import { etiquetaCanal } from '../utilidades/identidad';
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

  const noches = Math.round((new Date(`${reserva.fechaSalida}T00:00:00`).getTime() - new Date(`${reserva.fechaEntrada}T00:00:00`).getTime()) / 864e5);
  const MEDIOS: Record<string, string> = { EFECTIVO: 'Efectivo', TARJETA_CREDITO: 'Tarjeta de crédito', TARJETA_DEBITO: 'Tarjeta débito' };

  return (
    <section>
      <Link to={rutaRegreso} className="enlace-discreto small">{tieneRol('HUESPED') ? 'Mis reservas' : 'Todas las reservas'}</Link>
      <div className="titulo-pagina mt-2">
        <div className="d-flex align-items-center gap-3">
          <Puerta identificacion={reserva.apartamentoId} tamano="lg" />
          <div>
            <h1 className="h2 mb-1">Reserva {reserva.codigoReserva}</h1>
            <p className="text-body-secondary mb-0">
              Apartamento {reserva.apartamentoId}, {noches === 1 ? '1 noche' : `${noches} noches`}
            </p>
          </div>
        </div>
        <EstadoReservaBadge estado={reserva.estadoReserva} />
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-8 order-lg-1">
          <div className="card mb-4">
            <div className="card-body">
              <dl className="ficha">
                <div><dt>Llegada</dt><dd>{formatearFecha(reserva.fechaEntrada)}{reserva.horaEstimadaLlegada && <span className="text-body-secondary">, hacia las {reserva.horaEstimadaLlegada.slice(0, 5)}</span>}</dd></div>
                <div><dt>Salida</dt><dd>{formatearFecha(reserva.fechaSalida)}</dd></div>
                <div><dt>Titular</dt><dd>{reserva.titular.nombre}{reserva.titular.email && <span className="text-body-secondary d-block small">{reserva.titular.email}</span>}</dd></div>
                <div><dt>Grupo</dt><dd>{reserva.ocupantes.length === 1 ? '1 persona' : `${reserva.ocupantes.length} personas`}{reserva.cantidadMascotas > 0 && `, ${reserva.cantidadMascotas === 1 ? '1 mascota' : `${reserva.cantidadMascotas} mascotas`}`}</dd></div>
                <div><dt>Origen</dt><dd>{etiquetaCanal(reserva.canalOrigen)}{reserva.codigoExterno && <span className="text-body-secondary"> ({reserva.codigoExterno})</span>}</dd></div>
                <div><dt>Valor de la estancia</dt><dd className="fs-5 fw-bold cifra">{formatearPesos(reserva.valorTotal)}</dd></div>
              </dl>
            </div>
          </div>

          {esRecepcion && editando && (
            <form className="card card-body mb-4" onSubmit={(evento) => void modificar(evento)}>
              <h2 className="h5">Modificar estancia</h2>
              <div className="row g-2">
                <div className="col-md-3"><label className="form-label" htmlFor="mod-apto">Apartamento</label><input id="mod-apto" className="form-control" value={modificacion.apartamentoId} onChange={(e) => setModificacion({ ...modificacion, apartamentoId: e.target.value })} required /></div>
                <div className="col-md-3"><label className="form-label" htmlFor="mod-entrada">Llegada</label><input id="mod-entrada" type="date" className="form-control" value={modificacion.fechaEntrada} onChange={(e) => setModificacion({ ...modificacion, fechaEntrada: e.target.value })} required /></div>
                <div className="col-md-3"><label className="form-label" htmlFor="mod-salida">Salida</label><input id="mod-salida" type="date" className="form-control" value={modificacion.fechaSalida} onChange={(e) => setModificacion({ ...modificacion, fechaSalida: e.target.value })} required /></div>
                <div className="col-md-3"><label className="form-label" htmlFor="mod-mascotas">Mascotas</label><input id="mod-mascotas" type="number" min="0" className="form-control" value={modificacion.cantidadMascotas} onChange={(e) => setModificacion({ ...modificacion, cantidadMascotas: Number(e.target.value) })} required /></div>
              </div>
              <button className="btn btn-primary mt-3 align-self-start">Guardar cambios</button>
            </form>
          )}

          {esRecepcion && reserva.estadoReserva === 'EN_CURSO' && (
            <form className="card card-body mb-4" onSubmit={(evento) => void salidaReserva(evento)}>
              <h2 className="h5">Registrar salida</h2>
              <p className="text-body-secondary small">Al registrar la salida, el apartamento pasa a "Por preparar".</p>
              <div className="row g-2">
                <div className="col-md-6"><label className="form-label" htmlFor="sal-motivo">Motivo</label><input id="sal-motivo" className="form-control" minLength={5} maxLength={300} value={salida.motivo} onChange={(e) => setSalida({ ...salida, motivo: e.target.value })} required /></div>
                <div className="col-md-6"><label className="form-label" htmlFor="sal-obs">Observaciones</label><input id="sal-obs" className="form-control" maxLength={300} value={salida.observaciones} onChange={(e) => setSalida({ ...salida, observaciones: e.target.value })} /></div>
              </div>
              <button className="btn btn-primary mt-3 align-self-start">Registrar salida</button>
            </form>
          )}

          {folio && (
            <article className="card">
              <div className="card-header d-flex justify-content-between align-items-center">
                <h2 className="h5 mb-0">Folio</h2>
                <span className={`badge ${folio.estadoFolio === 'ABIERTO' ? 'text-bg-primary' : 'text-bg-secondary'}`}>{folio.estadoFolio === 'ABIERTO' ? 'Abierto' : 'Cerrado'}</span>
              </div>
              <div className="card-body">
                <table className="table table-sm align-middle mb-3">
                  <thead><tr><th>Concepto</th><th className="text-end">Valor</th></tr></thead>
                  <tbody>
                    {folio.cargos.map((c) => <tr key={`c-${c.id}`}><td>{c.descripcion}</td><td className="text-end cifra">{formatearPesos(c.monto)}</td></tr>)}
                    {folio.pagos.map((p) => <tr key={`p-${p.id}`} className="text-success"><td>Pago, {MEDIOS[p.medioPago] ?? p.medioPago}</td><td className="text-end cifra">−{formatearPesos(p.monto)}</td></tr>)}
                  </tbody>
                  <tfoot><tr><th>Saldo pendiente</th><th className="text-end fs-5 cifra">{formatearPesos(folio.saldoPendiente)}</th></tr></tfoot>
                </table>
                {esRecepcion && folio.estadoFolio === 'ABIERTO' && (
                  <div className="row g-3">
                    <form className="col-12 col-md-6" onSubmit={(evento) => void agregarCargo(evento)}>
                      <h3 className="h6">Agregar cargo</h3>
                      <input className="form-control mb-2" placeholder="Concepto" aria-label="Concepto del cargo" value={cargo.concepto} onChange={(e) => setCargo({ ...cargo, concepto: e.target.value })} required />
                      <input type="number" min="1" className="form-control mb-2" placeholder="Valor en pesos" aria-label="Valor del cargo" value={cargo.monto || ''} onChange={(e) => setCargo({ ...cargo, monto: Number(e.target.value) })} required />
                      <button className="btn btn-sm btn-outline-primary">Agregar cargo</button>
                    </form>
                    <form className="col-12 col-md-6" onSubmit={(evento) => void agregarPago(evento)}>
                      <h3 className="h6">Registrar pago</h3>
                      <input type="number" min="1" className="form-control mb-2" placeholder="Valor en pesos" aria-label="Valor del pago" value={pago.monto || ''} onChange={(e) => setPago({ ...pago, monto: Number(e.target.value) })} required />
                      <select className="form-select mb-2" aria-label="Medio de pago" value={pago.medioPago} onChange={(e) => setPago({ ...pago, medioPago: e.target.value })}>
                        {Object.entries(MEDIOS).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
                      </select>
                      <button className="btn btn-sm btn-outline-primary">Registrar pago</button>
                    </form>
                  </div>
                )}
              </div>
            </article>
          )}
        </div>

        <aside className="col-12 col-lg-4 order-first order-lg-2">
          <div className="card">
            <div className="card-body d-grid gap-2">
              <h2 className="h5">Acciones</h2>
              {esRecepcion && reserva.estadoReserva === 'PENDIENTE' && <button type="button" className="btn btn-primary" onClick={() => void confirmar()}>Confirmar reserva</button>}
              {esRecepcion && reserva.estadoReserva === 'CONFIRMADA' && <button type="button" className="btn btn-primary" onClick={() => void llegada()}>Registrar llegada</button>}
              <button type="button" className="btn btn-outline-primary" onClick={() => void verFolio()}>{folio ? 'Actualizar folio' : 'Ver folio'}</button>
              {esRecepcion && ['PENDIENTE', 'CONFIRMADA'].includes(reserva.estadoReserva) && <button type="button" className="btn btn-outline-secondary" onClick={() => setEditando(!editando)}>{editando ? 'Cerrar modificación' : 'Modificar estancia'}</button>}
              {esRecepcion && ['CONFIRMADA', 'EN_CURSO'].includes(reserva.estadoReserva) && <button type="button" className="btn btn-outline-secondary" onClick={() => void noShow()}>Marcar no-show</button>}
              {esRecepcion && folio?.estadoFolio === 'ABIERTO' && <button type="button" className="btn btn-outline-secondary" onClick={() => void cerrar()}>Cerrar folio</button>}
              {puedeCancelar && <button type="button" className="btn btn-outline-danger mt-2" onClick={() => void cancelar()}>Cancelar reserva</button>}
            </div>
          </div>
        </aside>
      </div>
    </section>
  );
}
