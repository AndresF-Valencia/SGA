// Datos de ejemplo mientras Inicio y el detalle no consultan la API (Guía 14).
// Son los apartamentos de la Ficha San Rocco, con los mismos ids que carga
// DatosDePrueba.java en el backend, para que las reservas y cotizaciones coincidan.
import type { ApartamentoResumen } from '../modelos/apartamento';

const DOTACION = ['Cocina', 'Wifi', 'TV', 'Lavadora', 'Amoblado'];

export const apartamentosEjemplo: ApartamentoResumen[] = [
  { identificacion: 'AP001', nombre: 'Familiar 4', descripcion: '', dormitorios: 4, capacidadMaxima: 10, limiteMascotas: 2, estadoOperativo: 'DISPONIBLE', activo: true, caracteristicas: [...DOTACION, '2 baños', '4 TV'], imagenes: [] },
  { identificacion: 'AP002', nombre: 'Parejas VIP', descripcion: '', dormitorios: 1, capacidadMaxima: 2, limiteMascotas: 2, estadoOperativo: 'DISPONIBLE', activo: true, caracteristicas: [...DOTACION, 'Jacuzzi', 'Balcón'], imagenes: [] },
  { identificacion: 'AP003', nombre: 'Familiar 3', descripcion: '', dormitorios: 1, capacidadMaxima: 3, limiteMascotas: 2, estadoOperativo: 'DISPONIBLE', activo: true, caracteristicas: [...DOTACION, 'Cama sencilla y doble', 'Balcón'], imagenes: [] },
  { identificacion: 'AP004', nombre: 'Familiar 2', descripcion: '', dormitorios: 2, capacidadMaxima: 5, limiteMascotas: 2, estadoOperativo: 'DISPONIBLE', activo: true, caracteristicas: [...DOTACION, '2 baños'], imagenes: [] },
  { identificacion: 'AP005', nombre: 'Parejas', descripcion: '', dormitorios: 1, capacidadMaxima: 2, limiteMascotas: 2, estadoOperativo: 'DISPONIBLE', activo: true, caracteristicas: [...DOTACION, 'Balcón'], imagenes: [] },
];
