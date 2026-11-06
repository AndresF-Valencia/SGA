// Llamadas a ReservaController.java (/api/reservas, rutas protegidas con JWT).
// Toma el token de sesion.ts; lo usa la página NuevaReserva.
import type { CrearReservaRequest, ReservaResponse } from '../modelos/reserva';
import { api } from './api';
import { obtenerToken } from './sesion';

// Provisional: arma el encabezado Authorization; en la Guía 18 lo hará un interceptor
function encabezadoAutorizacion() {
  const token = obtenerToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// POST /api/reservas → 201 con la reserva PENDIENTE y su código
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaResponse> {
  const respuesta = await api.post<ReservaResponse>('/reservas', datos, {
    headers: encabezadoAutorizacion(),
  });
  return respuesta.data;
}
