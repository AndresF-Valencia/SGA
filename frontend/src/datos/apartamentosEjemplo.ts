// Datos de ejemplo mientras Inicio y el detalle no consultan la API (Guía 14).
// Son los apartamentos de la Ficha San Rocco, con los mismos ids que carga
// DatosDePrueba.java en el backend, para que las reservas y cotizaciones coincidan.
import type { ApartamentoResumen } from '../modelos/apartamento';

const DOTACION = ['Cocina', 'Wifi', 'TV', 'Lavadora', 'Amoblado'];

export const apartamentosEjemplo: ApartamentoResumen[] = [
  { identificacion: 'AP001', nombre: 'Familiar 4', dormitorios: 4, capacidadMaxima: 10, limiteMascotas: 2, caracteristicas: [...DOTACION, '2 baños', '4 TV'] },
  { identificacion: 'AP002', nombre: 'Parejas VIP', dormitorios: 1, capacidadMaxima: 2, limiteMascotas: 2, caracteristicas: [...DOTACION, 'Jacuzzi', 'Balcón'] },
  { identificacion: 'AP003', nombre: 'Familiar 3', dormitorios: 1, capacidadMaxima: 3, limiteMascotas: 2, caracteristicas: [...DOTACION, 'Cama sencilla y doble', 'Balcón'] },
  { identificacion: 'AP004', nombre: 'Familiar 2', dormitorios: 2, capacidadMaxima: 5, limiteMascotas: 2, caracteristicas: [...DOTACION, '2 baños'] },
  { identificacion: 'AP005', nombre: 'Parejas', dormitorios: 1, capacidadMaxima: 2, limiteMascotas: 2, caracteristicas: [...DOTACION, 'Balcón'] },
];
