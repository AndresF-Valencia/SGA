// Llamada a DisponibilidadController.java (GET /api/disponibilidad, pública).
// Lo usa la página BuscarDisponibilidad.
import type { CriteriosDisponibilidad, DisponibilidadResponse } from '../modelos/disponibilidad';
import { api } from './api';

// GET /api/disponibilidad?fechaEntrada=...&fechaSalida=...&personas=...&mascotas=...
export async function buscarDisponibilidad(criterios: CriteriosDisponibilidad): Promise<DisponibilidadResponse> {
  const respuesta = await api.get<DisponibilidadResponse>('/disponibilidad', { params: criterios });
  return respuesta.data;
}
