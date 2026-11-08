// Define la forma de la sesión que AuthProvider comparte con toda la app y que se lee con
// useAuth(). Va separado del proveedor para no romper el Fast Refresh de Vite.
import { createContext } from 'react';
import type { LoginRequest } from '../modelos/auth';
import type { Rol } from '../modelos/usuario';
import type { UsuarioResponse } from '../modelos/usuario';

// Datos de la sesión tomados de LoginResponse.usuario (el backend ya los envía: no se decodifica el JWT)
export interface Sesion {
  token: string;
  rol: Rol;
  correo: string;
  nombre: string;
  documento: string; // el huésped reserva a su propio nombre con este documento
  expiraEn: string;
}

// Lo que cualquier componente puede pedir con useAuth()
export interface ValorAuth {
  sesion: Sesion | null;
  autenticado: boolean;
  avisoSesion: string | null; // "Su sesión expiró": lo muestra el Login
  iniciarSesion: (credenciales: LoginRequest) => Promise<Sesion>;
  cerrarSesion: () => void;
  actualizarUsuario: (usuario: UsuarioResponse) => void;
  tieneRol: (...roles: Rol[]) => boolean;
}

// null inicial: así useAuth detecta si se usa fuera del proveedor
export const ContextoAuth = createContext<ValorAuth | null>(null);
