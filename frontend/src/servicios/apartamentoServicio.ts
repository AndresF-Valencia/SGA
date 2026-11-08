import type { ApartamentoResumen } from '../modelos/apartamento';
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