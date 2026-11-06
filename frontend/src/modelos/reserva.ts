// Espejo de CrearReservaRequest.java y ReservaResponse.java, con los mismos nombres de
// campo que serializa Jackson. Lo usan reservaServicio y la página NuevaReserva.

// Enums del dominio (EstadoReserva.java, CanalOrigen.java) como uniones de texto
export type EstadoReserva =
  | 'PENDIENTE' | 'CONFIRMADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA' | 'NO_SHOW';

export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';

// Espejo de: CrearReservaRequest.OcupanteDto
export interface OcupanteRequest {
  nombre: string;
  documento: string;
  fechaNacimiento: string; // "AAAA-MM-DD"
}

// Espejo de: CrearReservaRequest.TitularDto (el titular lleva además correo y teléfono)
export interface TitularRequest extends OcupanteRequest {
  email: string;
  telefono: string;
}

// Espejo de: record CrearReservaRequest(...) → POST /api/reservas
export interface CrearReservaRequest {
  apartamentoId: string;
  fechaEntrada: string;
  fechaSalida: string;
  horaEstimadaLlegada: string; // LocalTime "16:30"; el backend la exige (@NotNull)
  canalOrigen: CanalOrigen;
  titular: TitularRequest;
  ocupantes: OcupanteRequest[]; // incluye al titular
  cantidadMascotas: number;
}

// Espejo de: ReservaResponse.TitularResumenDto (email y teléfono llegan null hoy)
export interface TitularResumen {
  nombre: string;
  documento: string;
  email: string | null;
  telefono: string | null;
}

// Espejo de: ReservaResponse.OcupanteResumenDto
export interface OcupanteResumen {
  nombre: string;
  documento: string;
  fechaNacimiento: string;
}

// Espejo de: record ReservaResponse(...)
export interface ReservaResponse {
  codigoReserva: string; // "RES-2026-00007"
  codigoExterno: string | null;
  estadoReserva: EstadoReserva;
  canalOrigen: CanalOrigen;
  apartamentoId: string;
  fechaEntrada: string;
  fechaSalida: string;
  fechaCreacion: string;
  horaEstimadaLlegada: string | null;
  valorTotal: number; // pesos, sin decimales (Long en Java)
  saldoPendiente: number | null; // hoy siempre null: el saldo está en el folio
  titular: TitularResumen;
  ocupantes: OcupanteResumen[];
  cantidadMascotas: number;
}
