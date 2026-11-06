// Llamadas a ReservaController.java (/api/reservas, rutas protegidas con JWT).
// El token lo agrega el interceptor de api.ts. Lo usan NuevaReserva, ListaReservas y MisReservas.
import type { Pagina } from '../modelos/pagina';
import type { CrearReservaRequest, EstadoReserva, ReservaResponse, ReservaResumen } from '../modelos/reserva';
import { api } from './api';

// POST /api/reservas → 201 con la reserva PENDIENTE y su código
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaResponse> {
  const respuesta = await api.post<ReservaResponse>('/reservas', datos);
  return respuesta.data;
}

// GET /api/reservas?estado=... (RECEPCIONISTA, ADMINISTRADOR): primera página, más reciente primero
export async function listarReservas(estado: EstadoReserva): Promise<Pagina<ReservaResumen>> {
  const respuesta = await api.get<Pagina<ReservaResumen>>('/reservas', { params: { estado } });
  return respuesta.data;
}

// GET /api/reservas/mis-reservas (HUESPED): el titular sale del token; estado opcional
export async function listarMisReservas(estado?: EstadoReserva): Promise<Pagina<ReservaResumen>> {
  const respuesta = await api.get<Pagina<ReservaResumen>>('/reservas/mis-reservas', { params: { estado } });
  return respuesta.data;
}
