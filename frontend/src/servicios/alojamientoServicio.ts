import type { Alojamiento } from '../modelos/alojamiento';
import { api } from './api';

export async function obtenerAlojamiento(): Promise<Alojamiento> {
  const respuesta = await api.get<Alojamiento>('/alojamiento');
  return respuesta.data;
}