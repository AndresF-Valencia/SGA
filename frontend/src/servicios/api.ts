// Cliente HTTP único de la app: todos los servicios lo usan. La URL sale de .env.development
// o .env.production. Sus interceptores ponen el token de sesion.ts en cada petición y avisan
// al AuthProvider cuando el backend responde 401 (FiltroJwt/ManejadorErroresSeguridad).
import axios from 'axios';
import { obtenerToken } from './sesion';

// Sin Content-Type fijo: Axios pone application/json para objetos
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10_000,
});

// Las rutas /auth/** son públicas: no llevan token y su 401 es "credenciales incorrectas"
function esAutenticacion(url?: string): boolean {
  return url?.startsWith('/auth/') ?? false;
}

// Interceptor de petición: agrega Authorization: Bearer <token> a todas menos /auth/**
api.interceptors.request.use((config) => {
  const token = obtenerToken();
  if (token && !esAutenticacion(config.url)) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Axios vive fuera de React: el AuthProvider le deja aquí la función que cierra la sesión
type ManejadorNoAutorizado = () => void;
let manejadorNoAutorizado: ManejadorNoAutorizado | null = null;

// Registra el manejador del 401 y devuelve la función que lo retira (limpieza del efecto)
export function registrarManejadorNoAutorizado(manejador: ManejadorNoAutorizado) {
  manejadorNoAutorizado = manejador;
  return () => {
    if (manejadorNoAutorizado === manejador) manejadorNoAutorizado = null;
  };
}

// Interceptor de respuesta: un 401 fuera de /auth/** es sesión vencida o token inválido.
// El 403 no cierra la sesión: el rol no alcanza y la página muestra el mensaje
api.interceptors.response.use(
  (respuesta) => respuesta,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401 && !esAutenticacion(error.config?.url)) {
      manejadorNoAutorizado?.();
    }
    return Promise.reject(error);
  },
);
