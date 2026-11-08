import { ETIQUETA_ROL, ETIQUETA_CAMPO } from '../../utilidades/identidad';
import { useCallback, useEffect, useState } from 'react';
import type { UsuarioResponse } from '../../modelos/usuario';
import { cambiarRolUsuario, crearUsuario, eliminarUsuario, listarUsuarios } from '../../servicios/administracionServicio';
import { Paginacion } from '../Paginacion';
import { confirmarAccion, notificarError, notificarExito } from '../../utilidades/notificaciones';
import { mensajeDeError } from '../../utilidades/errores';

const INICIAL = { nombre: '', correo: '', contrasena: '', telefono: '', documento: '', fechaNacimiento: '', rol: 'HUESPED' };

export function AdminUsuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [formulario, setFormulario] = useState(INICIAL);
  const [rolFiltro, setRolFiltro] = useState('');
  const [activoFiltro, setActivoFiltro] = useState('');

  const cargar = useCallback(async () => {
    try {
      const datos = await listarUsuarios({ pagina, rol: rolFiltro || undefined, activo: activoFiltro === '' ? undefined : activoFiltro === 'true' });
      setUsuarios(datos.contenido);
      setTotalPaginas(datos.totalPaginas);
    } catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }, [pagina, rolFiltro, activoFiltro]);
  useEffect(() => { void cargar(); }, [cargar]);

  async function crear(evento: React.FormEvent) {
    evento.preventDefault();
    try { await crearUsuario(formulario); setFormulario(INICIAL); await notificarExito('Usuario creado.'); await cargar(); }
    catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }
  async function cambiarRol(usuario: UsuarioResponse, rol: string) {
    try { await cambiarRolUsuario(usuario.correo, rol); await notificarExito('Rol actualizado.'); await cargar(); }
    catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }
  async function eliminar(usuario: UsuarioResponse) {
    if (!await confirmarAccion('¿Desactivar usuario?', `Se desactivará ${usuario.correo}.`)) return;
    try { await eliminarUsuario(usuario.correo); await notificarExito('Usuario desactivado.'); await cargar(); }
    catch (e: unknown) { await notificarError(mensajeDeError(e)); }
  }

  return <div className="row g-4">
    <div className="col-12 col-xl-5"><form className="card card-body" onSubmit={(evento) => void crear(evento)}>
      <h2 className="h5">Crear usuario</h2>
      {(['nombre', 'correo', 'contrasena', 'telefono', 'documento', 'fechaNacimiento'] as const).map((campo) => <label key={campo} className="form-label">{ETIQUETA_CAMPO[campo] ?? campo}<input className="form-control" type={campo === 'contrasena' ? 'password' : campo === 'fechaNacimiento' ? 'date' : campo === 'correo' ? 'email' : 'text'} value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: e.target.value })} required /></label>)}
      <label className="form-label">Rol<select className="form-select" value={formulario.rol} onChange={(e) => setFormulario({ ...formulario, rol: e.target.value })}>{Object.entries(ETIQUETA_ROL).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}</select></label>
      <button className="btn btn-primary mt-2">Crear usuario</button>
    </form></div>
    <div className="col-12 col-xl-7">
      <div className="row g-2 mb-3"><div className="col-sm-6"><label className="form-label">Rol<select className="form-select" value={rolFiltro} onChange={(e) => { setPagina(0); setRolFiltro(e.target.value); }}><option value="">Todos</option>{Object.entries(ETIQUETA_ROL).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}</select></label></div><div className="col-sm-6"><label className="form-label">Estado<select className="form-select" value={activoFiltro} onChange={(e) => { setPagina(0); setActivoFiltro(e.target.value); }}><option value="">Todos</option><option value="true">Activos</option><option value="false">Inactivos</option></select></label></div></div>
      <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Usuario</th><th>Rol</th><th>Estado</th><th /></tr></thead><tbody>{usuarios.map((usuario) => <tr key={usuario.correo}><td>{usuario.nombre}<br /><small>{usuario.correo}</small></td><td><select className="form-select form-select-sm" value={usuario.rol} onChange={(e) => void cambiarRol(usuario, e.target.value)}>{Object.entries(ETIQUETA_ROL).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}</select></td><td>{usuario.activo ? 'Activo' : 'Inactivo'}</td><td><button type="button" className="btn btn-sm btn-outline-danger" onClick={() => void eliminar(usuario)}>Desactivar</button></td></tr>)}</tbody></table></div>
      <Paginacion paginaActual={pagina} totalPaginas={totalPaginas} alCambiar={setPagina} />
    </div>
  </div>;
}
