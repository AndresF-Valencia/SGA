// Ruta de diseño común (App.tsx): Encabezado arriba, PiePagina abajo y en el <Outlet />
// la página de la ruta activa. El nombre del alojamiento sale de .env.
import { Suspense } from 'react';
import { Outlet } from 'react-router';
import { AsistenteChat } from './AsistenteChat';
import { Cargando } from './Cargando';
import { Encabezado } from './Encabezado';
import { PiePagina } from './PiePagina';

// Estructura que se mantiene al navegar; solo cambia el contenido del Outlet
export function Layout() {
  return (
    <>
      <Encabezado nombreAlojamiento={import.meta.env.VITE_NOMBRE_ALOJAMIENTO} />
      <main className="container pt-4 pb-5">
        <Suspense fallback={<Cargando />}>
          <Outlet />
        </Suspense>
      </main>
      <PiePagina />
      {/* El propio componente se oculta si no hay sesión iniciada */}
      <AsistenteChat />
    </>
  );
}
