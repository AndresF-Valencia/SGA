// Espejo de CotizacionRequest.java y CotizacionResponse.java (POST /api/cotizaciones, 7.4).
// Lo usan cotizacionServicio y la página Cotizacion.

// Espejo de: record CotizacionRequest(...). Solo importa la edad de cada ocupante
export interface CotizacionRequest {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  ocupantes: { fechaNacimiento: string }[];
}

// Espejo de: CotizacionResponse.DetalleTarifaNocheDto
export interface DetalleTarifaNoche {
  fecha: string;
  tarifaAplicada: number; // tarifa por ocupante facturable
  nombreTemporada: string;
  ocupantesFacturables: number;
  subtotal: number;
}

// Espejo de: record CotizacionResponse(...)
export interface CotizacionResponse {
  identificacionApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  cantidadNoches: number;
  valorTotal: number;
  valorSubtotal: number;
  desgloseNoches: DetalleTarifaNoche[];
}
