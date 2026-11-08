// Reglas del formulario de nueva reserva (página NuevaReserva). Cada regla existe también
// en el backend: CrearReservaRequest.java (@NotBlank, @Future, @NotNull), Ocupante
// (nacimiento no futuro), CantidadMascota (no negativa) y Estancia (salida posterior a entrada).
import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

// Un ocupante; el de la posición 0 es el titular
const esquemaOcupante = z.object({
  documento: z.string().trim().min(1, 'Ingrese el documento'),
  nombre: z.string().trim().min(1, 'Ingrese el nombre'),
  fechaNacimiento: z
    .iso.date('Ingrese la fecha de nacimiento')
    .refine((fecha) => fecha <= hoyISO(), 'La fecha de nacimiento no puede ser futura'),
});

export const esquemaNuevaReserva = z
  .object({
    apartamentoId: z.string().trim().min(1, 'Indique el apartamento'),
    fechaEntrada: z
      .iso.date('Ingrese la fecha de entrada')
      .refine((fecha) => fecha > hoyISO(), 'La fecha de entrada debe ser en el futuro'),
    fechaSalida: z.iso.date('Ingrese la fecha de salida'),
    horaEstimadaLlegada: z.iso.time('Ingrese la hora estimada de llegada'),
    emailTitular: z.email('Ingrese un correo válido para el titular'),
    telefonoTitular: z.string().trim().min(1, 'Ingrese el teléfono del titular'),
    cantidadMascotas: z
      .number('Ingrese un número')
      .int('Debe ser un número entero')
      .min(0, 'No puede ser negativa'),
    ocupantes: z.array(esquemaOcupante).min(1, 'La reserva debe tener al menos un ocupante'),
  })
  // Ayuda al usuario; la regla vive en Estancia y el backend responde 409 si se viola
  .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
    error: 'La fecha de salida debe ser posterior a la de entrada',
    path: ['fechaSalida'],
  });

// Tipo del formulario deducido del esquema
export type FormularioNuevaReserva = z.infer<typeof esquemaNuevaReserva>;
