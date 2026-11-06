// Ruta de diseño común (App.tsx): Encabezado arriba, PiePagina abajo y en el <Outlet />
// la página de la ruta activa. El nombre del alojamiento sale de .env.
import { Outlet } from 'react-router';
import { Encabezado } from './Encabezado';
import { PiePagina } from './PiePagina';

// Estructura que se mantiene al navegar; solo cambia el contenido del Outlet
export function Layout() {
  return (
    <>
      <Encabezado nombreAlojamiento={import.meta.env.VITE_NOMBRE_ALOJAMIENTO} />
      <main className="container py-4">
        <Outlet />
      </main>
      <PiePagina />
    </>
  );
}
