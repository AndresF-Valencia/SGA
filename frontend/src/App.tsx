// Tabla de rutas: todas dentro del Layout común y agrupadas por quién puede verlas, con los
// mismos roles que exige SecurityConfig del backend para los endpoints de cada página.
import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import Administracion from './paginas/Administracion';
import BuscarDisponibilidad from './paginas/BuscarDisponibilidad';
import Cotizacion from './paginas/Cotizacion';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import ListaReservas from './paginas/ListaReservas';
import Login from './paginas/Login';
import MisReservas from './paginas/MisReservas';
import NoAutorizado from './paginas/NoAutorizado';
import NoEncontrada from './paginas/NoEncontrada';
import NuevaReserva from './paginas/NuevaReserva';
import Preparacion from './paginas/Preparacion';
import Registro from './paginas/Registro';
import { RutaProtegida } from './rutas/RutaProtegida';
import { RutaPublica } from './rutas/RutaPublica';

// Rutas públicas, solo sin sesión y protegidas por rol
export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        {/* Públicas para todos (GET /apartamentos, /disponibilidad y POST /cotizaciones son permitAll) */}
        <Route index element={<Inicio />} />
        <Route path="apartamentos/:identificacion" element={<DetalleApartamento />} />
        <Route path="disponibilidad" element={<BuscarDisponibilidad />} />
        <Route path="cotizacion" element={<Cotizacion />} />
        <Route path="no-autorizado" element={<NoAutorizado />} />

        {/* Solo sin sesión */}
        <Route element={<RutaPublica />}>
          <Route path="login" element={<Login />} />
          <Route path="registro" element={<Registro />} />
        </Route>

        {/* Huésped: sus propias reservas */}
        <Route element={<RutaProtegida roles={['HUESPED']} />}>
          <Route path="mis-reservas" element={<MisReservas />} />
        </Route>

        {/* Recepción: recepcionista y administrador */}
        <Route element={<RutaProtegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']} />}>
          <Route path="reservas" element={<ListaReservas />} />
          <Route path="reservas/nueva" element={<NuevaReserva />} />
        </Route>

        {/* Operación del apartamento: personal de servicio y administrador */}
        <Route element={<RutaProtegida roles={['PERSONAL_SERVICIO', 'ADMINISTRADOR']} />}>
          <Route path="preparacion" element={<Preparacion />} />
        </Route>

        {/* Solo administrador */}
        <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
          <Route path="administracion" element={<Administracion />} />
        </Route>

        <Route path="*" element={<NoEncontrada />} />
      </Route>
    </Routes>
  );
}
