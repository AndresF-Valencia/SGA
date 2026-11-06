import type { UsuarioResponse } from '../modelos/usuario';
import { api } from './api';

export async function obtenerMiPerfil(): Promise<UsuarioResponse> {
  const respuesta = await api.get<UsuarioResponse>('/usuarios/me');
  return respuesta.data;
}

export async function actualizarMiPerfil(datos: {
  nombre: string;
  telefono: string;
  fotoPerfil: string;
}): Promise<UsuarioResponse> {
  const respuesta = await api.put<UsuarioResponse>('/usuarios/me', datos);
  return respuesta.data;
}

export async function cambiarMiContrasena(datos: {
  contrasenaActual: string;
  nuevaContrasena: string;
}): Promise<void> {
  await api.put('/usuarios/me/contrasena', datos);
}

export async function eliminarMiCuenta(): Promise<void> {
  await api.delete('/usuarios/me');
}