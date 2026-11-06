// Espejo de UsuarioResponse.java y de los requests de cuenta del backend.
// Lo usan auth.ts (respuesta del login), authServicio y la página de Registro.

// Roles del SGA (Rol.java). Unión de textos: erasableSyntaxOnly no admite enum
export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'ADMINISTRADOR' | 'PERSONAL_SERVICIO';

// Espejo de: record UsuarioResponse(...)
export interface UsuarioResponse {
  correo: string;
  nombre: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string; // LocalDate: "AAAA-MM-DD"
  rol: Rol;
  activo: boolean;
  fotoPerfil: string | null;
}

// Espejo de: record RegistroUsuarioRequest(...) → POST /api/auth/registro
export interface RegistroUsuarioRequest {
  nombre: string;
  correo: string;
  contrasena: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;
}
