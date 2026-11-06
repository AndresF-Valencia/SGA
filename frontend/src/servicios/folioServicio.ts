import type { Folio } from '../modelos/folio';
import { api } from './api';

export async function obtenerFolio(codigo: string): Promise<Folio> {
  const respuesta = await api.get<Folio>(`/reservas/${codigo}/folio`);
  return respuesta.data;
}