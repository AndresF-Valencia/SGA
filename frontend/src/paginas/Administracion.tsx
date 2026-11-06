import { useCallback, useEffect, useState } from 'react';
import type { ApartamentoAdmin, Temporada } from '../modelos/administracion';
import type { UsuarioResponse } from '../modelos/usuario';
import {
  activarApartamento, cambiarRolUsuario, crearApartamento, crearBloqueo, crearTemporada, crearUsuario,
  actualizarApartamento, configurarParametrosAlojamiento, configurarTarifa, desactivarApartamento, eliminarUsuario, listarApartamentosAdmin,
  listarConflictos, listarTemporadas, listarUsuarios, obtenerParametrosAlojamiento, publicarPolitica, resolverConflicto,
} from '../servicios/administracionServicio';
import { mensajeDeError } from '../utilidades/errores';
import { confirmarAccion, notificarError, notificarExito } from '../utilidades/notificaciones';
import { Paginacion } from '../componentes/Paginacion';

type Seccion = 'usuarios' | 'apartamentos' | 'temporadas' | 'bloqueos' | 'alojamiento' | 'politica' | 'conflictos';

const USUARIO_INICIAL = { nombre: '', correo: '', contrasena: '', telefono: '', documento: '', fechaNacimiento: '', rol: 'HUESPED' };
const APARTAMENTO_INICIAL = { identificacion: '', nombre: '', dormitorios: 1, capacidadMaxima: 1, limiteMascotas: 0, tiempoPreparacionHoras: 0, caracteristicas: '', imagenes: '' };
const TEMPORADA_INICIAL = { nombre: '', fechaInicio: '', fechaFin: '', estanciaMinima: 1, tipo: 'BASE' };
const BLOQUEO_INICIAL = { identificacionApartamento: '', fechaInicio: '', fechaFin: '', motivo: '' };

function ListaUsuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [formulario, setFormulario] = useState(USUARIO_INICIAL);

  const cargar = useCallback(async () => {
    try {
      const datos = await listarUsuarios({ pagina });
      setUsuarios(datos.contenido);
      setTotalPaginas(datos.totalPaginas);
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }, [pagina]);
  useEffect(() => { void cargar(); }, [cargar]);

  async function crear(evento: React.FormEvent) {
    evento.preventDefault();
    try {
      await crearUsuario(formulario);
      setFormulario(USUARIO_INICIAL);
      await notificarExito('Usuario creado.');
      await cargar();
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  async function cambiarRol(usuario: UsuarioResponse, rol: string) {
    try {
      await cambiarRolUsuario(usuario.correo, rol);
      await notificarExito('Rol actualizado.');
      await cargar();
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  async function eliminar(usuario: UsuarioResponse) {
    if (!await confirmarAccion('¿Desactivar usuario?', `Se desactivará ${usuario.correo}.`)) return;
    try {
      await eliminarUsuario(usuario.correo);
      await notificarExito('Usuario desactivado.');
      await cargar();
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  return (
    <div className="row g-4">
      <div className="col-12 col-xl-5">
        <form className="card card-body" onSubmit={(evento) => void crear(evento)}>
          <h2 className="h5">Crear usuario</h2>
          {(['nombre', 'correo', 'contrasena', 'telefono', 'documento', 'fechaNacimiento'] as const).map((campo) => (
            <label key={campo} className="form-label">{campo}
              <input className="form-control" type={campo === 'contrasena' ? 'password' : campo === 'fechaNacimiento' ? 'date' : campo === 'correo' ? 'email' : 'text'} value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: e.target.value })} required />
            </label>
          ))}
          <label className="form-label">Rol
            <select className="form-select" value={formulario.rol} onChange={(e) => setFormulario({ ...formulario, rol: e.target.value })}>
              <option>HUESPED</option><option>RECEPCIONISTA</option><option>PERSONAL_SERVICIO</option><option>ADMINISTRADOR</option>
            </select>
          </label>
          <button className="btn btn-primary mt-2">Crear usuario</button>
        </form>
      </div>
      <div className="col-12 col-xl-7">
        <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Usuario</th><th>Rol</th><th>Estado</th><th></th></tr></thead><tbody>
          {usuarios.map((usuario) => <tr key={usuario.correo}><td>{usuario.nombre}<br /><small>{usuario.correo}</small></td><td><select className="form-select form-select-sm" value={usuario.rol} onChange={(e) => void cambiarRol(usuario, e.target.value)}><option>HUESPED</option><option>RECEPCIONISTA</option><option>PERSONAL_SERVICIO</option><option>ADMINISTRADOR</option></select></td><td>{usuario.activo ? 'Activo' : 'Inactivo'}</td><td><button type="button" className="btn btn-sm btn-outline-danger" onClick={() => void eliminar(usuario)}>Desactivar</button></td></tr>)}
        </tbody></table></div>
        <Paginacion paginaActual={pagina} totalPaginas={totalPaginas} alCambiar={setPagina} />
      </div>
    </div>
  );
}

function ListaApartamentos() {
  const [apartamentos, setApartamentos] = useState<ApartamentoAdmin[]>([]);
  const [formulario, setFormulario] = useState(APARTAMENTO_INICIAL);
  const [identificacionEditada, setIdentificacionEditada] = useState<string | null>(null);

  async function cargar() { try { setApartamentos(await listarApartamentosAdmin()); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  useEffect(() => { void cargar(); }, []);
  const datosApartamento = () => ({ ...formulario, caracteristicas: formulario.caracteristicas.split(',').map((v) => v.trim()).filter(Boolean), imagenes: formulario.imagenes.split('\n').map((v) => v.trim()).filter(Boolean) });

  async function crear(evento: React.FormEvent) { evento.preventDefault(); try { if (identificacionEditada) await actualizarApartamento(identificacionEditada, { ...datosApartamento(), identificacion: undefined }); else await crearApartamento(datosApartamento()); setFormulario(APARTAMENTO_INICIAL); setIdentificacionEditada(null); await notificarExito(identificacionEditada ? 'Apartamento actualizado.' : 'Apartamento creado inactivo.'); await cargar(); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  async function alternar(apartamento: ApartamentoAdmin) { try { if (apartamento.activo) await desactivarApartamento(apartamento.identificacion); else await activarApartamento(apartamento.identificacion); await cargar(); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }

  return <div className="row g-4"><div className="col-12 col-xl-5"><form className="card card-body" onSubmit={(evento) => void crear(evento)}><h2 className="h5">{identificacionEditada ? 'Editar apartamento' : 'Crear apartamento'}</h2>{(['identificacion', 'nombre'] as const).map((campo) => <label key={campo} className="form-label">{campo}<input className="form-control" readOnly={campo === 'identificacion' && Boolean(identificacionEditada)} value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: e.target.value })} required /></label>)}<div className="row">{(['dormitorios', 'capacidadMaxima', 'limiteMascotas', 'tiempoPreparacionHoras'] as const).map((campo) => <label key={campo} className="col-6 form-label">{campo}<input type="number" min="0" className="form-control" value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: Number(e.target.value) })} required /></label>)}</div><label className="form-label">Características separadas por coma<textarea className="form-control" value={formulario.caracteristicas} onChange={(e) => setFormulario({ ...formulario, caracteristicas: e.target.value })} /></label><label className="form-label">URLs de imágenes, una por línea<textarea className="form-control" value={formulario.imagenes} onChange={(e) => setFormulario({ ...formulario, imagenes: e.target.value })} /></label><button className="btn btn-primary">{identificacionEditada ? 'Guardar cambios' : 'Crear apartamento'}</button></form></div><div className="col-12 col-xl-7"><div className="row g-3">{apartamentos.map((apartamento) => <article key={apartamento.identificacion} className="col-12 col-md-6"><div className="card h-100"><div className="card-body"><h3 className="h5">{apartamento.identificacion} · {apartamento.nombre}</h3><p className="small mb-2">{apartamento.dormitorios} dormitorios · {apartamento.capacidadMaxima} personas</p><span className={`badge ${apartamento.activo ? 'text-bg-success' : 'text-bg-secondary'}`}>{apartamento.activo ? 'Activo' : 'Inactivo'}</span></div><div className="card-footer d-flex gap-2"><button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => { setIdentificacionEditada(apartamento.identificacion); setFormulario({ identificacion: apartamento.identificacion, nombre: apartamento.nombre, dormitorios: apartamento.dormitorios, capacidadMaxima: apartamento.capacidadMaxima, limiteMascotas: apartamento.limiteMascotas, tiempoPreparacionHoras: 0, caracteristicas: apartamento.caracteristicas.join(', '), imagenes: apartamento.imagenes.join('\n') }); }}>Editar</button><button type="button" className="btn btn-sm btn-outline-primary" onClick={() => void alternar(apartamento)}>{apartamento.activo ? 'Desactivar' : 'Activar'}</button></div></div></article>)}</div></div></div>;
}

function ListaTemporadas() {
  const [temporadas, setTemporadas] = useState<Temporada[]>([]);
  const [formulario, setFormulario] = useState(TEMPORADA_INICIAL);
  const [tarifa, setTarifa] = useState({ identificacion: '', temporadaId: '', valorTarifa: 0 });
  async function cargar() { try { setTemporadas((await listarTemporadas()).contenido); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  useEffect(() => { void cargar(); }, []);
  async function crear(evento: React.FormEvent) { evento.preventDefault(); try { await crearTemporada(formulario); setFormulario(TEMPORADA_INICIAL); await notificarExito('Temporada creada.'); await cargar(); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  async function guardarTarifa(evento: React.FormEvent) { evento.preventDefault(); try { await configurarTarifa(tarifa.identificacion, tarifa.temporadaId, tarifa.valorTarifa); await notificarExito('Tarifa configurada.'); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <div className="row g-4"><div className="col-12 col-lg-5"><form className="card card-body mb-3" onSubmit={(evento) => void crear(evento)}><h2 className="h5">Crear temporada</h2><input className="form-control mb-2" placeholder="Nombre" value={formulario.nombre} onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })} required /><div className="row"><input type="date" className="form-control col m-1" value={formulario.fechaInicio} onChange={(e) => setFormulario({ ...formulario, fechaInicio: e.target.value })} required /><input type="date" className="form-control col m-1" value={formulario.fechaFin} onChange={(e) => setFormulario({ ...formulario, fechaFin: e.target.value })} required /></div><input type="number" min="1" className="form-control my-2" value={formulario.estanciaMinima} onChange={(e) => setFormulario({ ...formulario, estanciaMinima: Number(e.target.value) })} required /><select className="form-select mb-2" value={formulario.tipo} onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value })}><option>BASE</option><option>MEDIA</option><option>ALTA</option></select><button className="btn btn-primary">Crear temporada</button></form><form className="card card-body" onSubmit={(evento) => void guardarTarifa(evento)}><h2 className="h5">Configurar tarifa</h2><input className="form-control mb-2" placeholder="Apartamento AP001" value={tarifa.identificacion} onChange={(e) => setTarifa({ ...tarifa, identificacion: e.target.value })} required /><input className="form-control mb-2" placeholder="ID de temporada" value={tarifa.temporadaId} onChange={(e) => setTarifa({ ...tarifa, temporadaId: e.target.value })} required /><input type="number" min="1" className="form-control mb-2" placeholder="Valor por persona y noche" value={tarifa.valorTarifa || ''} onChange={(e) => setTarifa({ ...tarifa, valorTarifa: Number(e.target.value) })} required /><button className="btn btn-outline-primary">Guardar tarifa</button></form></div><div className="col-12 col-lg-7"><table className="table"><thead><tr><th>Nombre</th><th>Periodo</th><th>Tipo</th><th>ID</th></tr></thead><tbody>{temporadas.map((temporada) => <tr key={temporada.id}><td>{temporada.nombre}</td><td>{temporada.fechaInicio} a {temporada.fechaFin}</td><td>{temporada.tipo}</td><td><code>{temporada.id}</code></td></tr>)}</tbody></table></div></div>;
}

function CrearBloqueo() {
  const [formulario, setFormulario] = useState(BLOQUEO_INICIAL);
  async function crear(evento: React.FormEvent) { evento.preventDefault(); try { await crearBloqueo(formulario); setFormulario(BLOQUEO_INICIAL); await notificarExito('Bloqueo registrado.'); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <form className="card card-body" onSubmit={(evento) => void crear(evento)}><h2 className="h5">Registrar bloqueo</h2><input className="form-control mb-2" placeholder="Apartamento, por ejemplo AP003" value={formulario.identificacionApartamento} onChange={(e) => setFormulario({ ...formulario, identificacionApartamento: e.target.value })} required /><div className="row"><input type="date" className="form-control col m-1" value={formulario.fechaInicio} onChange={(e) => setFormulario({ ...formulario, fechaInicio: e.target.value })} required /><input type="date" className="form-control col m-1" value={formulario.fechaFin} onChange={(e) => setFormulario({ ...formulario, fechaFin: e.target.value })} required /></div><select className="form-select my-2" value={formulario.motivo} onChange={(e) => setFormulario({ ...formulario, motivo: e.target.value })} required><option value="">Motivo</option><option>MANTENIMIENTO</option><option>REMODELACION</option><option>FUERA_DE_SERVICIO</option></select><textarea className="form-control mb-2" minLength={5} maxLength={300} placeholder="Detalle del bloqueo" value={formulario.motivo} onChange={(e) => setFormulario({ ...formulario, motivo: e.target.value })} required /><button className="btn btn-primary align-self-start">Registrar bloqueo</button></form>;
}

function ConfiguracionAlojamiento() {
  const [formulario, setFormulario] = useState({ direccion: '', latitud: '', longitud: '', normasConvivencia: '', serviciosAdicionales: '[]', horaEntrada: '14:00', horaSalida: '11:00', horaLimiteNoShow: '20:00', horasPreparacion: 3, horasPlazoConfirmacion: 1, edadUmbralFacturable: 5, exigeAnticipo: true, porcentajeAnticipo: 50 });
  useEffect(() => { obtenerParametrosAlojamiento().then((datos) => setFormulario((actual) => ({ ...actual, direccion: datos.direccion, latitud: String(datos.latitud), longitud: String(datos.longitud), normasConvivencia: datos.normasConvivencia, horaEntrada: datos.horaEntrada.slice(0, 5), horaSalida: datos.horaSalida.slice(0, 5), horaLimiteNoShow: datos.horaLimiteNoShow.slice(0, 5), horasPreparacion: datos.minutosPreparacion / 60, horasPlazoConfirmacion: datos.horasPlazoConfirmacion, edadUmbralFacturable: datos.edadUmbralFacturable, exigeAnticipo: datos.exigeAnticipo, porcentajeAnticipo: datos.porcentajeAnticipo, serviciosAdicionales: JSON.stringify(datos.serviciosAdicionales) }))).catch((e: unknown) => void notificarError(mensajeDeError(e))); }, []);
  async function guardar(evento: React.FormEvent) { evento.preventDefault(); try { await configurarParametrosAlojamiento({ ...formulario, latitud: Number(formulario.latitud), longitud: Number(formulario.longitud), serviciosAdicionales: JSON.parse(formulario.serviciosAdicionales) }); await notificarExito('Parámetros actualizados.'); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <form className="card card-body" onSubmit={(evento) => void guardar(evento)}><h2 className="h5">Parámetros del alojamiento</h2><input className="form-control mb-2" placeholder="Dirección" value={formulario.direccion} onChange={(e) => setFormulario({ ...formulario, direccion: e.target.value })} required /><div className="row"><input className="form-control col m-1" placeholder="Latitud" value={formulario.latitud} onChange={(e) => setFormulario({ ...formulario, latitud: e.target.value })} required /><input className="form-control col m-1" placeholder="Longitud" value={formulario.longitud} onChange={(e) => setFormulario({ ...formulario, longitud: e.target.value })} required /></div><textarea className="form-control my-2" placeholder="Normas de convivencia" value={formulario.normasConvivencia} onChange={(e) => setFormulario({ ...formulario, normasConvivencia: e.target.value })} /><div className="row">{(['horaEntrada', 'horaSalida', 'horaLimiteNoShow'] as const).map((campo) => <label key={campo} className="col-md-4">{campo}<input type="time" className="form-control" value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: e.target.value })} /></label>)}</div><div className="row my-2">{(['horasPreparacion', 'horasPlazoConfirmacion', 'edadUmbralFacturable', 'porcentajeAnticipo'] as const).map((campo) => <label key={campo} className="col-md-3">{campo}<input type="number" min="0" className="form-control" value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: Number(e.target.value) })} /></label>)}</div><label className="form-check"><input type="checkbox" className="form-check-input" checked={formulario.exigeAnticipo} onChange={(e) => setFormulario({ ...formulario, exigeAnticipo: e.target.checked })} /> Exige anticipo</label><textarea className="form-control my-2" placeholder="Servicios adicionales como JSON" value={formulario.serviciosAdicionales} onChange={(e) => setFormulario({ ...formulario, serviciosAdicionales: e.target.value })} /><button className="btn btn-primary align-self-start">Guardar parámetros</button></form>;
}

function PublicarPolitica() {
  const [version, setVersion] = useState('2');
  const [tramos, setTramos] = useState('[{"diasMinimo": 0, "diasMaximo": 6, "porcentajeRetencion": 100}, {"diasMinimo": 7, "diasMaximo": 9999, "porcentajeRetencion": 0}]');
  async function publicar(evento: React.FormEvent) { evento.preventDefault(); try { await publicarPolitica({ version, tramos: JSON.parse(tramos) }); await notificarExito('Política publicada.'); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <form className="card card-body" onSubmit={(evento) => void publicar(evento)}><h2 className="h5">Publicar política de cancelación</h2><input className="form-control mb-2" value={version} onChange={(e) => setVersion(e.target.value)} placeholder="Versión" required /><textarea className="form-control mb-2" rows={8} value={tramos} onChange={(e) => setTramos(e.target.value)} required /><button className="btn btn-primary align-self-start">Publicar política</button></form>;
}

function Conflictos() {
  const [conflictos, setConflictos] = useState<{ id: string; canal: string; codigoExterno: string; motivo: string; estado: string }[]>([]);
  async function cargar() { try { setConflictos((await listarConflictos()).contenido); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  useEffect(() => { void cargar(); }, []);
  async function resolver(id: string) { const resolucion = window.prompt('Escriba la resolución del conflicto'); if (!resolucion) return; try { await resolverConflicto(id, resolucion); await notificarExito('Conflicto resuelto.'); await cargar(); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <div className="table-responsive"><table className="table"><thead><tr><th>Canal</th><th>Código externo</th><th>Motivo</th><th>Estado</th><th></th></tr></thead><tbody>{conflictos.map((conflicto) => <tr key={conflicto.id}><td>{conflicto.canal}</td><td>{conflicto.codigoExterno}</td><td>{conflicto.motivo}</td><td>{conflicto.estado}</td><td>{conflicto.estado !== 'RESUELTO' && <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => void resolver(conflicto.id)}>Resolver</button>}</td></tr>)}</tbody></table></div>;
}

export default function Administracion() {
  const [seccion, setSeccion] = useState<Seccion>('usuarios');
  const secciones: { id: Seccion; titulo: string }[] = [{ id: 'usuarios', titulo: 'Usuarios' }, { id: 'apartamentos', titulo: 'Apartamentos' }, { id: 'temporadas', titulo: 'Temporadas y tarifas' }, { id: 'bloqueos', titulo: 'Bloqueos' }, { id: 'alojamiento', titulo: 'Alojamiento' }, { id: 'politica', titulo: 'Política' }, { id: 'conflictos', titulo: 'Conflictos' }];
  return <section><h1 className="h3 mb-4">Administración del alojamiento</h1><ul className="nav nav-tabs mb-4">{secciones.map((item) => <li className="nav-item" key={item.id}><button type="button" className={`nav-link ${seccion === item.id ? 'active' : ''}`} onClick={() => setSeccion(item.id)}>{item.titulo}</button></li>)}</ul>{seccion === 'usuarios' && <ListaUsuarios />}{seccion === 'apartamentos' && <ListaApartamentos />}{seccion === 'temporadas' && <ListaTemporadas />}{seccion === 'bloqueos' && <CrearBloqueo />}{seccion === 'alojamiento' && <ConfiguracionAlojamiento />}{seccion === 'politica' && <PublicarPolitica />}{seccion === 'conflictos' && <Conflictos />}</section>;
}
