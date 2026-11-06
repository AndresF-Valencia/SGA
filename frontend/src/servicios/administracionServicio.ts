import type { ApartamentoAdmin, Bloqueo, ConflictoCanal, PaginaUsuarios, Temporada } from '../modelos/administracion';
import type { Alojamiento } from '../modelos/alojamiento';
import type { UsuarioResponse } from '../modelos/usuario';
import { api } from './api';

export async function listarUsuarios(filtros: { rol?: string; activo?: boolean; pagina?: number }): Promise<PaginaUsuarios> {
  const respuesta = await api.get<PaginaUsuarios>('/usuarios', { params: { ...filtros, page: filtros.pagina ?? 0, size: 10 } });
  return respuesta.data;
}

export async function crearUsuario(datos: Record<string, unknown>): Promise<UsuarioResponse> {
  const respuesta = await api.post<UsuarioResponse>('/usuarios', datos);
  return respuesta.data;
}

export async function cambiarRolUsuario(correo: string, rol: string): Promise<UsuarioResponse> {
  const respuesta = await api.put<UsuarioResponse>(`/usuarios/${encodeURIComponent(correo)}/rol`, { rol });
  return respuesta.data;
}

export async function eliminarUsuario(correo: string): Promise<void> {
  await api.delete(`/usuarios/${encodeURIComponent(correo)}`);
}

export async function listarApartamentosAdmin(): Promise<ApartamentoAdmin[]> {
  const respuesta = await api.get<ApartamentoAdmin[]>('/apartamentos');
  return respuesta.data;
}

export async function crearApartamento(datos: Record<string, unknown>): Promise<ApartamentoAdmin> {
  const respuesta = await api.post<ApartamentoAdmin>('/apartamentos', datos);
  return respuesta.data;
}

export async function actualizarApartamento(identificacion: string, datos: Record<string, unknown>): Promise<ApartamentoAdmin> {
  const respuesta = await api.put<ApartamentoAdmin>(`/apartamentos/${identificacion}`, datos);
  return respuesta.data;
}

export async function activarApartamento(identificacion: string): Promise<ApartamentoAdmin> {
  const respuesta = await api.post<ApartamentoAdmin>(`/apartamentos/${identificacion}/activar`);
  return respuesta.data;
}

export async function desactivarApartamento(identificacion: string): Promise<ApartamentoAdmin> {
  const respuesta = await api.delete<ApartamentoAdmin>(`/apartamentos/${identificacion}`);
  return respuesta.data;
}

export async function listarTemporadas(pagina = 0): Promise<{ contenido: Temporada[]; totalPaginas: number }> {
  const respuesta = await api.get<{ contenido: Temporada[]; totalPaginas: number }>('/temporadas', { params: { page: pagina, size: 10 } });
  return respuesta.data;
}

export async function crearTemporada(datos: Record<string, unknown>): Promise<Temporada> {
  const respuesta = await api.post<Temporada>('/temporadas', datos);
  return respuesta.data;
}

export async function configurarTarifa(identificacion: string, temporadaId: string, valorTarifa: number): Promise<void> {
  await api.put(`/apartamentos/${identificacion}/tarifas/${temporadaId}`, { valorTarifa });
}

export async function crearBloqueo(datos: Record<string, unknown>): Promise<Bloqueo> {
  const respuesta = await api.post<Bloqueo>('/bloqueos', datos);
  return respuesta.data;
}

export async function obtenerParametrosAlojamiento(): Promise<Alojamiento> {
  const respuesta = await api.get<Alojamiento>('/alojamiento');
  return respuesta.data;
}

export async function configurarParametrosAlojamiento(datos: Record<string, unknown>): Promise<Alojamiento> {
  const respuesta = await api.put<Alojamiento>('/alojamiento/parametros', datos);
  return respuesta.data;
}

export async function publicarPolitica(datos: Record<string, unknown>): Promise<void> {
  await api.post('/politicas-cancelacion', datos);
}

export async function listarConflictos(pagina = 0): Promise<{ contenido: ConflictoCanal[]; totalPaginas: number }> {
  const respuesta = await api.get<{ contenido: ConflictoCanal[]; totalPaginas: number }>('/conflictos-canal', { params: { page: pagina, size: 10 } });
  return respuesta.data;
}

export async function resolverConflicto(id: string, resolucion: string): Promise<void> {
  await api.put(`/conflictos-canal/${id}/resolver`, { resolucion });
}