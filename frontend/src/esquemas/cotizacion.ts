// Reglas del formulario de cotización (página Cotizacion), las mismas de
// CotizacionRequest.java: apartamento obligatorio, entrada en el futuro y al menos un ocupante.
import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

export const esquemaCotizacion = z
  .object({
    identificacionApartamento: z.string().trim().min(1, 'Elige el apartamento'),
    fechaEntrada: z
      .iso.date('Escribe la fecha de entrada')
      .refine((fecha) => fecha > hoyISO(), 'La fecha de entrada debe ser en el futuro'),
    fechaSalida: z.iso.date('Escribe la fecha de salida'),
    ocupantes: z
      .array(
        z.object({
          fechaNacimiento: z
            .iso.date('Escribe la fecha de nacimiento')
            .refine((fecha) => fecha <= hoyISO(), 'La fecha de nacimiento no puede ser futura'),
        }),
      )
      .min(1, 'Agrega al menos una persona'),
  })
  // Ayuda al usuario; la regla vive en Estancia del backend
  .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
    error: 'La fecha de salida debe ser posterior a la de entrada',
    path: ['fechaSalida'],
  });

// Tipo del formulario deducido del esquema
export type FormularioCotizacion = z.infer<typeof esquemaCotizacion>;
