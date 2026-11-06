# Frontend del SGA

Frontend del **Sistema de Gestión de Alojamiento San Rocco**, construido con React y conectado al backend Spring Boot del mismo repositorio.

Este documento describe cómo funciona el frontend, cómo ejecutarlo y qué debe actualizarse cuando se agreguen cambios relevantes. La referencia académica son las guías 14 a 20 y las buenas prácticas de la guía 21. Si una guía usa nombres distintos a los del backend, prevalece el contrato REST real documentado en `CONSULTA-RAPIDA.md` y `backend/sga/docs/api/openapi.yaml`.

## 1. Requisitos

- Node.js 22.22 o superior; se recomienda Node.js 24 LTS.
- npm.
- Backend ejecutándose en `http://localhost:8080`.
- JDK 25 para ejecutar el backend.

## 2. Ejecutar el frontend

Desde la carpeta `frontend/`:

```bash
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

Comandos disponibles:

| Comando | Uso |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática. |
| `npm run build` | Verifica TypeScript y genera `dist/`. |
| `npm run lint` | Analiza el código con Oxlint. |
| `npm run preview` | Sirve localmente la compilación de producción. |

Para levantar el proyecto completo, use dos terminales:

```bash
# Terminal 1
cd backend/sga
sh ./gradlew bootRun

# Terminal 2
cd frontend
npm install
npm run dev
```

## 3. Variables de entorno

Vite solo expone al navegador las variables que empiezan por `VITE_`. No deben contener contraseñas, tokens ni claves privadas.

| Archivo | Uso | Valor actual |
|---|---|---|
| `.env` | Valores comunes | `VITE_NOMBRE_ALOJAMIENTO=Apartamentos Turísticos San Rocco` |
| `.env.development` | `npm run dev` | `VITE_API_URL=http://localhost:8080/api` |
| `.env.production` | `npm run build` | URL del backend desplegado |
| `.env.*.local` | Sobrescritura local, no se versiona | Configuración personal |

Después de cambiar un archivo `.env`, reinicie Vite.

## 4. Estructura del código

```text
src/
├── componentes/   piezas visuales reutilizables y módulos administrativos
├── contexto/      estado compartido de autenticación y filtros
├── datos/         datos locales de apoyo
├── esquemas/      validaciones Zod de los formularios
├── hooks/         hooks personalizados
├── modelos/       tipos TypeScript espejo de los DTOs REST
├── paginas/       pantallas asociadas a rutas
├── rutas/         guardas y redirecciones de React Router
├── servicios/     cliente Axios y llamadas a la API
└── utilidades/    formato, errores, fechas y notificaciones
```

La consola administrativa se divide por responsabilidad en `src/componentes/administracion/`: usuarios, apartamentos, temporadas/tarifas, bloqueos y configuración general. `paginas/Administracion.tsx` solo coordina las pestañas.

Reglas del proyecto:

- Los nombres de archivos, funciones y componentes están en español.
- Las páginas usan `export default`; los componentes reutilizables usan exportaciones nombradas.
- Los tipos se importan con `import type`.
- Los enums del backend se modelan como uniones de texto de TypeScript.
- Las páginas no construyen URLs ni headers HTTP manualmente: usan los servicios.
- Los componentes de presentación reciben datos por `props` y no consultan la API.
- No se modifica `backend/` para resolver una diferencia de implementación del frontend.

## 5. Arquitectura y flujo de datos

```text
Página React
    ↓
Servicio del frontend
    ↓
api.ts (Axios + interceptores)
    ↓
Backend Spring Boot: http://localhost:8080/api
```

### Cliente HTTP

`src/servicios/api.ts` crea una única instancia de Axios. Lee `VITE_API_URL`, aplica un timeout de 10 segundos y agrega automáticamente el token JWT en el header `Authorization: Bearer ...`.

Las respuestas `401` que no vienen de `/auth/**` cierran la sesión mediante el callback registrado por `AuthProvider`. Los errores de negocio se traducen con `src/utilidades/errores.ts`.

### Autenticación

1. `Login.tsx` valida correo y contraseña con React Hook Form y Zod.
2. `authServicio.ts` llama `POST /api/auth/login`.
3. `AuthProvider` guarda la sesión en memoria y en `localStorage`.
4. `RutaProtegida` valida autenticación y rol antes de mostrar una pantalla.
5. El backend sigue siendo la autoridad de seguridad; las guardas del frontend solo mejoran la navegación.

Usuarios de prueba del backend, todos con contraseña `Sanrocco123`:

| Rol | Correo |
|---|---|
| Administrador | `admin@sanrocco.co` |
| Recepcionista | `recepcion@sanrocco.co` |
| Personal de servicio | `servicio@sanrocco.co` |
| Huésped | `huesped@sanrocco.co` |

### Formularios

Los formularios usan `useForm`, `zodResolver` y esquemas ubicados en `src/esquemas/`. Los formularios de reservas usan `useFieldArray` para administrar ocupantes. La validación del frontend es una ayuda visual; el backend valida nuevamente todas las reglas.

### Diseño visual

Bootstrap 5.3 y Bootstrap Icons se cargan en `main.tsx`. `Layout` mantiene el encabezado y el pie de página, mientras `Outlet` muestra la página activa. Los estilos específicos de un componente usan CSS Modules.

Las fechas se conservan como texto ISO y se formatean con `Intl` sin convertirlas a UTC accidentalmente. Los valores monetarios se muestran como pesos colombianos sin decimales.

## 6. Rutas y permisos actuales

| Ruta | Acceso | Pantalla |
|---|---|---|
| `/` | Público | Catálogo de apartamentos |
| `/apartamentos/:identificacion` | Público | Detalle del apartamento |
| `/disponibilidad` | Público | Búsqueda de disponibilidad |
| `/cotizacion` | Público | Cotización de estancia |
| `/alojamiento` | Público | Ficha y normas del alojamiento |
| `/login` | Sin sesión | Inicio de sesión |
| `/registro` | Sin sesión | Registro de huésped |
| `/recuperar-contrasena` | Sin sesión | Solicitar código y restablecer contraseña |
| `/mis-reservas` | `HUESPED` | Reservas propias |
| `/mi-cuenta` | Usuario autenticado | Perfil, contraseña y eliminación de cuenta |
| `/reservas` | `RECEPCIONISTA`, `ADMINISTRADOR` | Listado operativo de reservas |
| `/reservas/nueva` | `HUESPED`, `RECEPCIONISTA`, `ADMINISTRADOR` | Crear reserva en `PORTAL` o `DIRECTO` |
| `/reservas/:codigo` | `HUESPED`, `RECEPCIONISTA`, `ADMINISTRADOR` | Detalle, folio y acciones de reserva |
| `/preparacion` | `PERSONAL_SERVICIO`, `ADMINISTRADOR` | Preparación de apartamentos |
| `/administracion` | `ADMINISTRADOR` | Administración del alojamiento |

Las páginas secundarias se cargan bajo demanda con `React.lazy`; `Layout` muestra `Cargando` mientras llega el módulo.

## 7. Funcionalidades implementadas de las guías 14-20

| Guía | Estado | Implementación actual |
|---|---|---|
| 14 | Completa | Proyecto Vite, React Router, componentes, páginas, modelos y rutas base. |
| 15 | Completa con adaptación | React Hook Form, Zod, Axios, login, registro, disponibilidad, cotización y reserva. Los nombres de campos siguen el backend actual. |
| 16 | Completa | Bootstrap, layout con `Outlet`, navegación responsiva, CSS Modules, formato de fechas y pesos. |
| 18 | Completa | `AuthProvider`, `useAuth`, `RutaProtegida`, `RutaPublica`, JWT en Axios, manejo centralizado de `401` y menú por rol. |
| 19 | Completa para reservas | `React.lazy`, `Suspense`, paginación del servidor, `Paginacion`, SweetAlert2 y acciones de confirmar/cancelar. |
| 20 | Adaptada al contrato actual | Contexto con reducer, filtros persistentes, debounce de 400 ms y `AbortController`. La búsqueda usa `documentoTitular`, que es el parámetro aceptado por el backend actual; la guía propone búsqueda por nombre con `titular`. |

## 8. Paginación, filtros y notificaciones

El backend devuelve páginas con `contenido`, `pagina`, `tamano`, `totalElementos` y `totalPaginas`. El frontend pide diez registros por página, usa índices que empiezan en cero y envía los parámetros REST `page` y `size`.

En `/reservas`:

- Cambiar estado o documento reinicia la página a cero.
- Escribir el documento espera 400 ms antes de consultar.
- Una consulta nueva cancela la anterior.
- Los filtros permanecen al navegar dentro de la aplicación.
- El contexto se reinicia cuando cambia el usuario autenticado.
- Las acciones de confirmar y cancelar muestran confirmación, errores y resultado mediante SweetAlert2.

## 9. Estado del desarrollo de vistas

Las rutas ya existen, pero algunas son pantallas iniciales y todavía requieren desarrollo funcional y visual:

- **Público:** catálogo, detalle, alojamiento, disponibilidad, cotización, registro, login y recuperación ya consumen los flujos principales del backend.
- **Huésped:** puede crear reservas por `PORTAL`, consultar sus reservas, ver detalle y folio, cancelar reservas permitidas, editar perfil, cambiar contraseña y eliminar su cuenta.
- **Recepción:** puede listar, filtrar, crear, consultar y modificar reservas; confirmar, cancelar, registrar llegada, salida y no-show; y consultar/operar el folio con cargos, pagos y cierre.
- **Personal de servicio:** puede consultar los apartamentos activos y cambiar estados permitidos de preparación; `FUERA_DE_SERVICIO` queda reservado al administrador.
- **Administrador:** dispone de una consola por pestañas para usuarios, apartamentos, temporadas/tarifas, bloqueos, parámetros del alojamiento, políticas de cancelación y conflictos de canal.

La consola administrativa permite filtrar usuarios por rol y estado, paginar usuarios, temporadas y conflictos, seleccionar una temporada existente para configurar la tarifa de un apartamento y resolver conflictos mediante formularios controlados. En temporadas/tarifas existen dos flujos: asignación rápida por tipo (`BASE` o `ALTA`) para aplicar un mismo valor a todas las temporadas de ese tipo, y asignación específica para modificar una sola temporada. Los bloqueos envían el motivo enum que exige el backend y los parámetros del alojamiento aceptan correctamente campos opcionales.

## 10. Verificación antes de entregar cambios

Desde `frontend/`:

```bash
npm run build
npm run lint
```

El build debe terminar sin errores de TypeScript ni Vite. El lint puede mostrar advertencias no bloqueantes, que deben revisarse y documentarse.

## 11. Regla de mantenimiento de esta documentación

Cada cambio relevante del frontend debe actualizar este README en el mismo cambio o commit. Se considera relevante:

- Nueva ruta, pantalla, rol o permiso.
- Nuevo servicio, endpoint consumido o modelo REST.
- Cambio en autenticación, interceptores, almacenamiento de sesión o variables de entorno.
- Cambio en formularios, validaciones, paginación, filtros o notificaciones.
- Nueva dependencia, comando de ejecución o requisito de entorno.
- Cambio que altere el flujo visible de un usuario.

Al actualizarlo, revise especialmente las secciones **Rutas y permisos actuales**, **Funcionalidades implementadas**, **Estado del desarrollo de vistas** y **Verificación**.
