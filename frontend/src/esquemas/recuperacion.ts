import { z } from 'zod';

const contrasena = z.string()
  .min(8, 'Debe tener mínimo 8 caracteres')
  .regex(/[A-Z]/, 'Debe incluir una mayúscula')
  .regex(/[0-9]/, 'Debe incluir un número');

export const esquemaSolicitarRecuperacion = z.object({
  correo: z.email('Escribe un correo válido'),
});

export const esquemaRestablecerContrasena = z.object({
  correo: z.email('Escribe un correo válido'),
  codigo: z.string().regex(/^\d{6}$/, 'El código debe tener 6 dígitos'),
  nuevaContrasena: contrasena,
});

export type FormularioSolicitarRecuperacion = z.infer<typeof esquemaSolicitarRecuperacion>;
export type FormularioRestablecerContrasena = z.infer<typeof esquemaRestablecerContrasena>;