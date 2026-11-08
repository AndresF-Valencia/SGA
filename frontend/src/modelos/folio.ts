export interface CargoFolio {
  id: string;
  descripcion: string;
  monto: number;
  fechaRegistro: string;
  registradoPor: string;
}

export interface PagoFolio {
  id: string;
  medioPago: string;
  monto: number;
  fechaPago: string;
  referencia: string | null;
}

export interface Folio {
  codigoReserva: string;
  estadoFolio: 'ABIERTO' | 'CERRADO';
  subtotalCargos: number;
  totalPagos: number;
  saldoPendiente: number;
  cargos: CargoFolio[];
  pagos: PagoFolio[];
}