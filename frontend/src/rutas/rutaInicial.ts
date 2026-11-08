// Página principal de cada rol después de ingresar. La usa RutaPublica al redirigir desde el login.
import type { Rol } from '../modelos/usuario';

const RUTA_INICIAL: Record<Rol, string> = {
  ADMINISTRADOR: '/reservas',
  RECEPCIONISTA: '/reservas',
  PERSONAL_SERVICIO: '/preparacion',
  HUESPED: '/',
};

// Devuelve la ruta de inicio del rol
export function rutaInicial(rol: Rol): string {
  return RUTA_INICIAL[rol];
}
