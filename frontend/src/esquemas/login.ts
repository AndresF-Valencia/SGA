// Reglas del formulario de ingreso (página Login). Los nombres de campo son los de
// LoginRequest.java (correo, contrasena), así el formulario se envía tal cual al backend.
import { z } from 'zod';

// Solo forma del mensaje: la contraseña segura se valida al registrarse, no al ingresar
export const esquemaLogin = z.object({
  correo: z.email('Escribe un correo electrónico válido'),
  contrasena: z.string().min(1, 'Escribe tu contraseña'),
});

// Tipo del formulario deducido del esquema
export type FormularioLogin = z.infer<typeof esquemaLogin>;
