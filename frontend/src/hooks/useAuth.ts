// Hook para leer la sesión del AuthProvider desde cualquier componente
// (Encabezado, Login, RutaProtegida, RutaPublica, páginas).
import { useContext } from 'react';
import { ContextoAuth } from '../contexto/contextoAuth';

// Devuelve el valor del contexto, con un error claro si falta el proveedor
export function useAuth() {
  const valor = useContext(ContextoAuth);
  if (!valor) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  }
  return valor;
}
