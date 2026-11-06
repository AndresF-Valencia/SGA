// Llamada a CotizacionController.java (POST /api/cotizaciones, pública).
// Lo usa la página Cotizacion.
import type { CotizacionRequest, CotizacionResponse } from '../modelos/cotizacion';
import { api } from './api';

// POST /api/cotizaciones → 200 con el valor total y el desglose noche a noche
export async function cotizar(datos: CotizacionRequest): Promise<CotizacionResponse> {
  const respuesta = await api.post<CotizacionResponse>('/cotizaciones', datos);
  return respuesta.data;
}
