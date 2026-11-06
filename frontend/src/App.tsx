// Tabla de rutas: todas dentro del Layout común y agrupadas por quién puede verlas, con los
// mismos roles que exige SecurityConfig del backend para los endpoints de cada página.
import { lazy } from 'react';
import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import BuscarDisponibilidad from './paginas/BuscarDisponibilidad';
import Cotizacion from './paginas/Cotizacion';
import Inicio from './paginas/Inicio';
import { FiltrosProvider } from './contexto/FiltrosProvider';
import { useAuth } from './hooks/useAuth';
import { RutaProtegida } from './rutas/RutaProtegida';
import { RutaPublica } from './rutas/RutaPublica';

const Administracion = lazy(() => import('./paginas/Administracion'));
const Alojamiento = lazy(() => import('./paginas/Alojamiento'));
const DetalleApartamento = lazy(() => import('./paginas/DetalleApartamento'));
const DetalleReserva = lazy(() => import('./paginas/DetalleReserva'));
const ListaReservas = lazy(() => import('./paginas/ListaReservas'));
const Login = lazy(() => import('./paginas/Login'));
const MiCuenta = lazy(() => import('./paginas/MiCuenta'));
const MisReservas = lazy(() => import('./paginas/MisReservas'));
const NoAutorizado = lazy(() => import('./paginas/NoAutorizado'));
const NoEncontrada = lazy(() => import('./paginas/NoEncontrada'));
const NuevaReserva = lazy(() => import('./paginas/NuevaReserva'));
const Preparacion = lazy(() => import('./paginas/Preparacion'));
const Registro = lazy(() => import('./paginas/Registro'));
const RecuperarContrasena = lazy(() => import('./paginas/RecuperarContrasena'));

// Rutas públicas, solo sin sesión y protegidas por rol
export default function App() {
  const { sesion } = useAuth();

  return (
    <FiltrosProvider key={sesion?.correo ?? 'anonimo'}>
      <Routes>
      <Route element={<Layout />}>
        {/* Públicas para todos (GET /apartamentos, /disponibilidad y POST /cotizaciones son permitAll) */}
        <Route index element={<Inicio />} />
        <Route path="apartamentos/:identificacion" element={<DetalleApartamento />} />
        <Route path="disponibilidad" element={<BuscarDisponibilidad />} />
        <Route path="cotizacion" element={<Cotizacion />} />
        <Route path="alojamiento" element={<Alojamiento />} />
        <Route path="no-autorizado" element={<NoAutorizado />} />

        {/* Solo sin sesión */}
        <Route element={<RutaPublica />}>
          <Route path="login" element={<Login />} />
          <Route path="registro" element={<Registro />} />
          <Route path="recuperar-contrasena" element={<RecuperarContrasena />} />
        </Route>

        {/* Huésped: sus propias reservas */}
        <Route element={<RutaProtegida roles={['HUESPED']} />}>
          <Route path="mis-reservas" element={<MisReservas />} />
        </Route>

        <Route element={<RutaProtegida />}>
          <Route path="mi-cuenta" element={<MiCuenta />} />
        </Route>

        {/* Listado operativo: recepcionista y administrador */}
        <Route element={<RutaProtegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']} />}>
          <Route path="reservas" element={<ListaReservas />} />
        </Route>

        {/* Crear y consultar una reserva: huésped, recepcionista y administrador */}
        <Route element={<RutaProtegida roles={['HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR']} />}>
          <Route path="reservas/nueva" element={<NuevaReserva />} />
          <Route path="reservas/:codigo" element={<DetalleReserva />} />
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
    </FiltrosProvider>
  );
}
