import { z } from 'zod';

const contrasenaSegura = z.string()
  .min(8, 'Mínimo 8 caracteres')
  .regex(/[A-Z]/, 'Debe incluir una mayúscula')
  .regex(/[0-9]/, 'Debe incluir un número');

export const esquemaPerfil = z.object({
  nombre: z.string().trim().min(1, 'Escribe tu nombre'),
  telefono: z.string().trim().min(1, 'Escribe tu teléfono'),
  fotoPerfil: z.string().url('Escribe una URL válida').max(500, 'Máximo 500 caracteres').or(z.literal('')),
});

export const esquemaCambioContrasena = z.object({
  contrasenaActual: z.string().min(1, 'Escribe tu contraseña actual'),
  nuevaContrasena: contrasenaSegura,
});

export type FormularioPerfil = z.infer<typeof esquemaPerfil>;
export type FormularioCambioContrasena = z.infer<typeof esquemaCambioContrasena>;