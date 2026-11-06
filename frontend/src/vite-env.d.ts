// Tipos de las variables de entorno propias (.env, .env.development, .env.production).
// Las leen servicios/api.ts (URL del backend) y componentes/Layout.tsx (nombre del alojamiento).
interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_NOMBRE_ALOJAMIENTO: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
