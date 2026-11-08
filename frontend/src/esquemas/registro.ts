// Reglas del formulario de registro de huésped (página Registro). Repiten las del backend:
// RegistroUsuarioRequest.java (@Email, @Past, mínimo 8) y Usuario.validarContrasenaSegura
// (mayúscula y número). La confirmación de contraseña solo existe en el formulario.
import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

export const esquemaRegistro = z
  .object({
    nombre: z.string().trim().min(1, 'Escribe tu nombre'),
    correo: z.email('Escribe un correo electrónico válido'),
    contrasena: z
      .string()
      .min(8, 'Mínimo 8 caracteres')
      .regex(/[A-Z]/, 'Debe tener al menos una mayúscula')
      .regex(/[0-9]/, 'Debe tener al menos un número'),
    confirmacion: z.string().min(1, 'Confirma la contraseña'),
    telefono: z.string().trim().min(1, 'Escribe tu teléfono'),
    documento: z.string().trim().min(1, 'Escribe tu documento'),
    fechaNacimiento: z
      .iso.date('Escribe tu fecha de nacimiento')
      .refine((fecha) => fecha < hoyISO(), 'La fecha de nacimiento debe ser en el pasado'),
  })
  // Compara dos campos: el error se muestra junto a la confirmación
  .refine((datos) => datos.contrasena === datos.confirmacion, {
    error: 'Las contraseñas no coinciden',
    path: ['confirmacion'],
  });

// Tipo del formulario deducido del esquema
export type FormularioRegistro = z.infer<typeof esquemaRegistro>;
