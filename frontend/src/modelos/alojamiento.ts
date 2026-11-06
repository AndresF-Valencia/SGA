export interface ServicioAdicional {
  nombre: string;
  valor: number;
}

export interface Alojamiento {
  identificacion: string;
  nombre: string;
  ciudad: string;
  descripcion: string;
  direccion: string;
  latitud: number;
  longitud: number;
  normasConvivencia: string;
  serviciosAdicionales: ServicioAdicional[];
  horaEntrada: string;
  horaSalida: string;
  horaLimiteNoShow: string | null;
  minutosPreparacion: number;
  horasPlazoConfirmacion: number;
  edadUmbralFacturable: number;
  exigeAnticipo: boolean;
  porcentajeAnticipo: number;
  versionPoliticaVigente: number;
}