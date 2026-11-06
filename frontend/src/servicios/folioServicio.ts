import type { Folio } from '../modelos/folio';
import { api } from './api';

export async function obtenerFolio(codigo: string): Promise<Folio> {
  const respuesta = await api.get<Folio>(`/reservas/${codigo}/folio`);
  return respuesta.data;
}

export async function registrarCargo(codigo: string, datos: { concepto: string; monto: number }): Promise<Folio> {
  const respuesta = await api.post<Folio>(`/reservas/${codigo}/folio/cargos`, datos);
  return respuesta.data;
}

export async function registrarPago(codigo: string, datos: { monto: number; medioPago: string; fechaPago: string }): Promise<Folio> {
  const respuesta = await api.post<Folio>(`/reservas/${codigo}/folio/pagos`, datos);
  return respuesta.data;
}

export async function cerrarFolio(codigo: string, datos: { autor: string; motivo: string; observaciones: string }): Promise<Folio> {
  const respuesta = await api.put<Folio>(`/reservas/${codigo}/folio/cerrar`, datos);
  return respuesta.data;
}