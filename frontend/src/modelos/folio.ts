// Espejo de FolioResponse.java. Los movimientos no tienen identificador: una corrección
// se pide con la descripción del cargo (CorregirMovimientoRequest.movimientoIdOriginal).

export interface CargoFolio {
  descripcion: string;
  tipo: 'ALOJAMIENTO' | 'SERVICIO_ADICIONAL' | 'AJUSTE' | string;
  monto: number; // negativo en los ajustes que corrigen un cargo
  fechaRegistro: string;
}

export interface PagoFolio {
  medioPago: string;
  monto: number;
  fechaPago: string;
}

// RN-17: quién autorizó cerrar el folio con saldo, por qué y cuándo
export interface CierreFolio {
  autor: string;
  motivo: string;
  fecha: string;
}

export interface Folio {
  codigoReserva: string;
  estadoFolio: 'ABIERTO' | 'CERRADO';
  subtotalCargos: number;
  totalPagos: number;
  saldoPendiente: number;
  cargos: CargoFolio[];
  pagos: PagoFolio[];
  autorizacionCierre?: CierreFolio | null;
}
