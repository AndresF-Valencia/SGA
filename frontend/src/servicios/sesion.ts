// Guarda en localStorage la respuesta de POST /api/auth/login (token + usuario) para que la
// sesión sobreviva a una recarga. La escribe y la lee el AuthProvider; api.ts toma el token de aquí.
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

// La sesión guardada solo si el token no ha vencido (expiraEn); una vencida se descarta
export function sesionVigente(): LoginResponse | null {
  const sesion = leerSesion();
  if (sesion && new Date(sesion.expiraEn).getTime() > Date.now()) {
    return sesion;
  }
  borrarSesion();
  return null;
}

// Devuelve solo el token JWT, o null si no hay sesión
export function obtenerToken(): string | null {
  return leerSesion()?.token ?? null;
}

// Borra la sesión del navegador
export function borrarSesion(): void {
  localStorage.removeItem(CLAVE_SESION);
}
