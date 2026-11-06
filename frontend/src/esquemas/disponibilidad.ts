// Reglas del formulario de búsqueda (página BuscarDisponibilidad). Repiten las del backend:
// Estancia (salida posterior a entrada), personas ≥ 1 (ConsultarDisponibilidadUseCase)
// y CantidadMascota (no negativa).
import { z } from 'zod';

export const esquemaDisponibilidad = z
  .object({
    fechaEntrada: z.iso.date('Ingrese la fecha de entrada'),
    fechaSalida: z.iso.date('Ingrese la fecha de salida'),
    personas: z.number('Ingrese un número').int('Debe ser un número entero').min(1, 'Al menos 1 persona'),
    mascotas: z.number('Ingrese un número').int('Debe ser un número entero').min(0, 'No puede ser negativa'),
  })
  // Ayuda al usuario; la regla vive en Estancia del backend
  .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
    error: 'La fecha de salida debe ser posterior a la de entrada',
    path: ['fechaSalida'],
  });

// Tipo del formulario deducido del esquema; coincide con CriteriosDisponibilidad
export type FormularioDisponibilidad = z.infer<typeof esquemaDisponibilidad>;
