// Configuración de las pruebas del front (npm test). jsdom simula el navegador para
// poder pintar componentes de React sin abrir uno de verdad.
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.{ts,tsx}'],
  },
});
