// Componente raíz: pinta Encabezado y PiePagina en todas las páginas y asocia cada URL
// con su página. main.tsx lo monta dentro del BrowserRouter.
import { Route, Routes } from 'react-router';
import { Encabezado } from './componentes/Encabezado';
import { PiePagina } from './componentes/PiePagina';
import Administracion from './paginas/Administracion';
import BuscarDisponibilidad from './paginas/BuscarDisponibilidad';
import Cotizacion from './paginas/Cotizacion';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import Login from './paginas/Login';
import MisReservas from './paginas/MisReservas';
import NoEncontrada from './paginas/NoEncontrada';
import NuevaReserva from './paginas/NuevaReserva';
import Preparacion from './paginas/Preparacion';
import Recepcion from './paginas/Recepcion';
import Registro from './paginas/Registro';

// Tabla de rutas de la aplicación
export default function App() {
  return (
    <>
      <Encabezado nombreAlojamiento="Apartamentos Turísticos San Rocco" />

      <main>
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/apartamentos/:identificacion" element={<DetalleApartamento />} />
          <Route path="/disponibilidad" element={<BuscarDisponibilidad />} />
          <Route path="/cotizacion" element={<Cotizacion />} />
          <Route path="/mis-reservas" element={<MisReservas />} />
          <Route path="/recepcion" element={<Recepcion />} />
          <Route path="/reservas/nueva" element={<NuevaReserva />} />
          <Route path="/preparacion" element={<Preparacion />} />
          <Route path="/administracion" element={<Administracion />} />
          <Route path="*" element={<NoEncontrada />} />
        </Routes>
      </main>

      <PiePagina />
    </>
  );
}
