// Espejo de los DTOs de login del backend (LoginRequest.java y LoginResponse.java).
// Lo usan authServicio (petición), sesion.ts (lo que se guarda) y la página Login.
import type { UsuarioResponse } from './usuario';

// Espejo de: record LoginRequest(String correo, String contrasena)
export interface LoginRequest {
  correo: string;
  contrasena: string;
}

// Espejo de: record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario)
export interface LoginResponse {
  token: string;
  tipo: string; // "Bearer"
  expiraEn: string; // Instant como texto ISO-8601
  usuario: UsuarioResponse; // trae el rol del usuario
}
