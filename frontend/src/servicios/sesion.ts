// Guarda en localStorage la sesión que devuelve POST /api/auth/login (token + usuario).
// Login la escribe y los servicios protegidos (reservaServicio) leen el token de aquí.
// Riesgo conocido: localStorage es legible por scripts (XSS); el curso lo acepta por simplicidad.
import type { LoginResponse } from '../modelos/auth';

const CLAVE_SESION = 'sga.sesion';

// Guarda la respuesta del login
export function guardarSesion(sesion: LoginResponse): void {
  localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

// Devuelve la sesión guardada, o null si no hay
export function leerSesion(): LoginResponse | null {
  const texto = localStorage.getItem(CLAVE_SESION);
  return texto ? (JSON.parse(texto) as LoginResponse) : null;
}

// Devuelve solo el token JWT, o null si no hay sesión
export function obtenerToken(): string | null {
  return leerSesion()?.token ?? null;
}

// Cierra la sesión en el navegador
export function borrarSesion(): void {
  localStorage.removeItem(CLAVE_SESION);
}
