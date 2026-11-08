// Llamadas a AuthController.java (/api/auth/**, rutas públicas sin token).
// Lo usan las páginas Login y Registro.
import type { LoginRequest, LoginResponse } from '../modelos/auth';
import type { RegistroUsuarioRequest, UsuarioResponse } from '../modelos/usuario';
import { api } from './api';

// POST /api/auth/login → 200 con token y usuario; 401 si las credenciales no sirven
export async function login(credenciales: LoginRequest): Promise<LoginResponse> {
  const respuesta = await api.post<LoginResponse>('/auth/login', credenciales);
  return respuesta.data;
}

// POST /api/auth/registro → 201 con el usuario HUESPED creado (no devuelve token)
export async function registrar(datos: RegistroUsuarioRequest): Promise<UsuarioResponse> {
  const respuesta = await api.post<UsuarioResponse>('/auth/registro', datos);
  return respuesta.data;
}

export async function solicitarRecuperacion(correo: string): Promise<void> {
  await api.post('/auth/recuperar-contrasena', { correo });
}

export async function restablecerContrasena(datos: {
  correo: string;
  codigo: string;
  nuevaContrasena: string;
}): Promise<void> {
  await api.post('/auth/restablecer-contrasena', datos);
}
