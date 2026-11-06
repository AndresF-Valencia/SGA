// Espejo de CrearReservaRequest.java, ReservaResponse.java y ReservaResumenResponse.java, con
// los mismos nombres de campo que serializa Jackson. Lo usan reservaServicio, NuevaReserva,
// ListaReservas, MisReservas, TarjetaReserva y EstadoReservaBadge.

// Enums del dominio (EstadoReserva.java, CanalOrigen.java) como uniones de texto
export type EstadoReserva =
  | 'PENDIENTE' | 'CONFIRMADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA' | 'NO_SHOW';

export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';

// Todos los estados en el orden del ciclo de vida, para los filtros de los listados
export const ESTADOS_RESERVA: EstadoReserva[] = [
  'PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW',
];

// Texto legible de cada estado, para el badge y los filtros
export const ETIQUETA_ESTADO: Record<EstadoReserva, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  NO_SHOW: 'No-show',
};

// Espejo de: record ReservaResumenResponse(...) — vista breve de los listados
export interface ReservaResumen {
  codigoReserva: string;
  apartamentoId: string;
  fechaEntrada: string;
  fechaSalida: string;
  estadoReserva: EstadoReserva;
  canalOrigen: CanalOrigen;
  nombreTitular: string;
  valorTotal: number;
}

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
