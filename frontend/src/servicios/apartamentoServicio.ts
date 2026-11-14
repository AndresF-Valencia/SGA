import type { Novedad } from '../modelos/administracion';
import type { ApartamentoResumen } from '../modelos/apartamento';
import type { Pagina } from '../modelos/pagina';
import { api } from './api';

const EVENTO_APARTAMENTOS_CAMBIARON = 'sga:apartamentos-cambiaron';

export function notificarCambioApartamentos(): void {
  window.dispatchEvent(new Event(EVENTO_APARTAMENTOS_CAMBIARON));
}

export function escucharCambiosApartamentos(manejador: () => void): () => void {
  window.addEventListener(EVENTO_APARTAMENTOS_CAMBIARON, manejador);
  return () => window.removeEventListener(EVENTO_APARTAMENTOS_CAMBIARON, manejador);
}

export async function listarApartamentos(): Promise<ApartamentoResumen[]> {
  const respuesta = await api.get<ApartamentoResumen[]>('/apartamentos');
  return respuesta.data;
}

export async function obtenerApartamento(identificacion: string): Promise<ApartamentoResumen> {
  const respuesta = await api.get<ApartamentoResumen>(`/apartamentos/${identificacion}`);
  return respuesta.data;
}

export async function cambiarEstadoOperativo(identificacion: string, nuevoEstado: string): Promise<ApartamentoResumen> {
  const respuesta = await api.post<ApartamentoResumen>(`/apartamentos/${identificacion}/estado-operativo`, { nuevoEstado });
  return respuesta.data;
}

// Daños o incidencias que reporta el personal de servicio (7.6)
export async function registrarNovedad(identificacion: string, datos: { descripcion: string; gravedad: string }): Promise<Novedad> {
  const respuesta = await api.post<Novedad>(`/apartamentos/${identificacion}/novedades`, datos);
  return respuesta.data;
}

export async function listarNovedades(identificacion: string, pagina = 0): Promise<Pagina<Novedad>> {
  const respuesta = await api.get<Pagina<Novedad>>(`/apartamentos/${identificacion}/novedades`, { params: { page: pagina, size: 10 } });
  return respuesta.data;
}
