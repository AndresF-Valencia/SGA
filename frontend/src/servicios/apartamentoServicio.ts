import type { ApartamentoResumen } from '../modelos/apartamento';
import { api } from './api';

export async function listarApartamentos(): Promise<ApartamentoResumen[]> {
  const respuesta = await api.get<ApartamentoResumen[]>('/apartamentos');
  return respuesta.data;
}

export async function obtenerApartamento(identificacion: string): Promise<ApartamentoResumen> {
  const respuesta = await api.get<ApartamentoResumen>(`/apartamentos/${identificacion}`);
  return respuesta.data;
}