// Identidad visual de San Rocco. Cada apartamento tiene el color de su puerta, como las casas
// de Salento, en tonos de tierra y monte que combinan con la paleta: se usa igual en tarjetas, detalle, reservas y preparación para reconocerlo rápido.

const PUERTAS: Record<string, string> = {
  AP001: '#4d6148', // musgo
  AP002: '#7a5a40', // café
  AP003: '#8a8a5c', // oliva
  AP004: '#b08f62', // arena
  AP005: '#5a4434', // tabaco
};

const RESPALDO = ['#4d6148', '#7a5a40', '#8a8a5c', '#b08f62', '#5a4434', '#6e7a6a'];

// Color de la puerta de un apartamento; los apartamentos nuevos toman uno estable según su código
export function colorPuerta(identificacion: string): string {
  if (PUERTAS[identificacion]) return PUERTAS[identificacion];
  let suma = 0;
  for (const letra of identificacion) suma += letra.charCodeAt(0);
  return RESPALDO[suma % RESPALDO.length];
}

// Textos para la persona, en vez de los valores internos del backend
export const ETIQUETA_ESTADO_OPERATIVO: Record<string, string> = {
  PREPARADO: 'Listo para recibir',
  OCUPADO: 'Ocupado',
  PENDIENTE_PREPARACION: 'Por preparar',
  EN_PREPARACION: 'En preparación',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
};

export const CLASE_ESTADO_OPERATIVO: Record<string, string> = {
  PREPARADO: 'text-bg-success',
  OCUPADO: 'text-bg-primary',
  PENDIENTE_PREPARACION: 'text-bg-warning',
  EN_PREPARACION: 'text-bg-info',
  FUERA_DE_SERVICIO: 'text-bg-danger',
};

export const ETIQUETA_CANAL: Record<string, string> = {
  PORTAL: 'Portal web',
  DIRECTO: 'Recepción',
  EXTERNO: 'Canal externo',
};

export function etiquetaEstadoOperativo(estado: string): string {
  return ETIQUETA_ESTADO_OPERATIVO[estado] ?? estado;
}

export function etiquetaCanal(canal: string): string {
  return ETIQUETA_CANAL[canal] ?? canal;
}

export const ETIQUETA_ROL: Record<string, string> = {
  HUESPED: 'Huésped',
  RECEPCIONISTA: 'Recepción',
  PERSONAL_SERVICIO: 'Personal de servicio',
  ADMINISTRADOR: 'Administración',
};

export const ETIQUETA_MOTIVO_BLOQUEO: Record<string, string> = {
  MANTENIMIENTO: 'Mantenimiento',
  REMODELACION: 'Remodelación',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
};

// Nombres de campo legibles para los formularios de administración
export const ETIQUETA_CAMPO: Record<string, string> = {
  nombre: 'Nombre',
  correo: 'Correo',
  contrasena: 'Contraseña',
  telefono: 'Teléfono',
  documento: 'Documento',
  fechaNacimiento: 'Fecha de nacimiento',
  identificacion: 'Código',
  dormitorios: 'Dormitorios',
  capacidadMaxima: 'Capacidad (personas)',
  limiteMascotas: 'Mascotas permitidas',
  tiempoPreparacionHoras: 'Preparación (horas)',
  horaEntrada: 'Hora de entrada',
  horaSalida: 'Hora de salida',
  horaLimiteNoShow: 'Límite para no-show',
  horasPreparacion: 'Preparación (horas)',
  horasPlazoConfirmacion: 'Plazo para confirmar (horas)',
  edadUmbralFacturable: 'Edad desde la que se cobra',
  porcentajeAnticipo: 'Anticipo (%)',
};

// Fotos del lugar para la portada (Unsplash, licencia libre). Créditos en CREDITOS-FOTOS.md
const unsplash = (id: string, ancho = 2000) => `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=${ancho}&q=80`;

export const FOTO_PORTADA = unsplash('1672851612881-775cfd248971');           // Valle del Cocora, David Restrepo
export const FOTO_SALENTO_CALLE = unsplash('1697818634560-27ddca612f88', 1200); // Parque de Salento, Julian David Loaiza Agudelo
export const FOTO_SALENTO_MONTANA = unsplash('1613498248726-8922766cebdb', 1200); // Salento, Datingjungle

// Versión pequeña de una foto de Unsplash para tarjetas; otras URLs se dejan igual
export function miniatura(url: string, ancho = 800): string {
  return url.includes('images.unsplash.com') ? url.replace(/w=\d+/, `w=${ancho}`) : url;
}
