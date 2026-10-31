# SGA · Sistema de Gestión de Alojamiento

Backend para administrar los apartamentos turísticos **San Rocco** (Armenia, Quindío): reservas, estancias, folios, temporadas, tarifas, bloqueos y canales externos.
Programación Avanzada · Universidad del Quindío.

| Capa | Tecnología |
|---|---|
| Backend | Java 25 · Spring Boot 4.1 · Spring Data JPA (Hibernate) · Spring Security + JWT (jjwt) · Gradle 9.7 (wrapper) · Lombok · MapStruct |
| Base de datos | H2 en memoria (`jdbc:h2:mem:sgadb`) |
| Arquitectura | Hexagonal (puertos y adaptadores) + DDD |
| API | REST · contrato OpenAPI 3.1 |
| Pruebas | JUnit 5 · `@SpringBootTest` con H2 |

---

## 1. Requisitos

- **JDK 25.** Gradle no hace falta instalarlo: el proyecto trae el *wrapper* (`gradlew`).
- Git
- VS Code con *Extension Pack for Java*, o IntelliJ IDEA

## 2. Levantar el proyecto

```bash
git clone https://github.com/AndresF-Valencia/sga.git
cd sga/backend/sga

sh ./gradlew bootRun        # API en http://localhost:8080
```

> En Mac o Linux se usa `sh ./gradlew` porque el archivo puede venir sin permiso de ejecución. En Windows se usa `gradlew.bat bootRun`.

Al arrancar, Hibernate crea las tablas solo (`ddl-auto=update`).

**Consola de la base de datos:** http://localhost:8080/h2-console

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:sgadb` |
| Usuario | `sa` |
| Contraseña | *(vacía)* |

> La base es **en memoria**: todo se borra al apagar la aplicación, y al arrancar se vuelven a cargar los datos de prueba (sección 2.2).

**Documentación interactiva (Swagger):** http://localhost:8080/swagger-ui.html. Para probar rutas protegidas: haz login, copia el `token` y pégalo en el botón **Authorize**.

## 2.1 Usuarios de prueba y login

Se crean solos al arrancar (`sga.datos-prueba.habilitado=true`). Contraseña de todos: **`Sanrocco123`**

| Rol | Correo |
|---|---|
| ADMINISTRADOR | admin@sanrocco.co |
| RECEPCIONISTA | recepcion@sanrocco.co |
| PERSONAL_SERVICIO | servicio@sanrocco.co |
| HUESPED | huesped@sanrocco.co |

```bash
# 1) Login -> copia el "token" de la respuesta
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"admin@sanrocco.co","contrasena":"Sanrocco123"}'

# 2) Llamar una ruta protegida con el token
curl -X POST http://localhost:8080/api/usuarios -H "Authorization: Bearer <token>" ...
```

| Respuesta | Significa |
|---|---|
| **401** | No mandaste token, o venció (duran 120 minutos) |
| **403** | Tu rol no tiene permiso, o el recurso no es tuyo (un huésped con la reserva de otro) |

**Secreto del JWT:** no se versiona. Se configura con la variable de entorno `SGA_JWT_SECRETO` (mínimo 32 caracteres). Si no está, la aplicación arranca con una clave temporal y los tokens dejan de valer al reiniciar.

**Mi cuenta y recuperación de contraseña (7.2):**

| Qué | Ruta |
|---|---|
| Ver / editar mi perfil (nombre, teléfono, foto) | `GET` / `PUT /api/usuarios/me` |
| Cambiar mi contraseña (con la actual) | `PUT /api/usuarios/me/contrasena` |
| Eliminar mi cuenta (lógico) | `DELETE /api/usuarios/me` |
| Pedir código de recuperación (sin token) | `POST /api/auth/recuperar-contrasena` |
| Poner contraseña nueva con el código (sin token) | `POST /api/auth/restablecer-contrasena` |
| Administrador: listar, consultar, cambiar rol, eliminar | `GET /api/usuarios`, `GET` / `DELETE /api/usuarios/{correo}`, `PUT /api/usuarios/{correo}/rol` |

El código de recuperación tiene 6 dígitos, vence en 15 minutos, sirve una sola vez y se anula tras 5 intentos fallidos. En modo consola (por defecto) **el correo con el código aparece en la terminal** donde corre la aplicación.

Reglas al eliminar: no se puede eliminar a quien sea titular de reservas pendientes, confirmadas o en curso; sus reservas históricas se conservan; y el sistema nunca se queda sin un administrador activo.

## 2.1.1 Probar la API sin Swagger: archivos `.http`

En `backend/sga/http/` hay dos archivos listos para enviar peticiones desde VS Code con la extensión **REST Client** (de Huachao Mao): se abre el archivo y se da clic en **Send Request** encima de cada petición, en orden. Los tokens se guardan solos.

| Archivo | Qué muestra |
|---|---|
| `simulador-canal.http` | **Simulador del canal externo (10.3.1):** reserva aceptada, reserva en conflicto rechazada y registrada, mensaje repetido que no duplica, revisión del conflicto por el administrador y cancelación. |
| `demo-corte2.http` | Catálogo público, cotización, login, perfil, reserva del huésped, mis reservas, folio, 401/403, pago y confirmación por recepción, recuperación de contraseña y listado de usuarios. |

Las fechas se calculan a partir de hoy, así que funcionan cualquier día con los datos de prueba recién cargados (reinicia la aplicación para empezar de cero).

## 2.2 Datos de prueba (12.3)

Se cargan solos al arrancar, a partir de la Ficha del Alojamiento. Se apagan con `sga.datos-prueba.habilitado=false`.

- **Alojamiento `SANROCCO`.** Es el único del sistema, así que ninguna ruta pide su identificador.
- **5 apartamentos:** AP001 Familiar 4 (10 personas), AP002 Parejas VIP (2), AP003 Familiar 3 (3), AP004 Familiar 2 (5) y AP005 Parejas (2).
- **Temporadas sin huecos** desde el año pasado hasta el próximo: Baja, Media (fiestas de Armenia, +15 %) y Alta (fin de año, Semana Santa y mitad de año, +30 %, mínimo 3 noches). Cada apartamento tiene tarifa en todas.
- **Tarifa base por persona y noche:** AP002 $120.000 · AP005 $90.000 · AP003 $70.000 · AP004 $65.000 · AP001 $60.000.
- **Política v1:** 1 semana o más antes, se devuelve el 100 %; menos de una semana, se retiene el 100 %. No-show a partir de las 8:00 p. m.
- **Canal externo** "Booking (simulado)", con credencial `clave-booking-demo` (se envía en el encabezado `X-Canal-Credencial`).
- **Bloqueos y estados:** AP003 fuera de servicio con bloqueo vigente, AP004 con mantenimiento programado y AP002 ocupado ahora.

Una reserva en cada estado y de los tres canales (las fechas son relativas a hoy):

| Código | Estado | Canal | Apartamento | Titular |
|---|---|---|---|---|
| RES-AAAA-00001 | FINALIZADA | DIRECTO | AP001 | Carlos Ramírez (familia con una niña que no paga) |
| RES-AAAA-00002 | EN_CURSO | PORTAL | AP002 | **Laura Gómez = `huesped@sanrocco.co`** (con 1 mascota) |
| RES-AAAA-00003 | NO_SHOW | EXTERNO | AP004 | Jennifer Smith |
| RES-AAAA-00004 | CANCELADA | DIRECTO | AP001 | Luis Herrera |
| RES-AAAA-00005 | CONFIRMADA | EXTERNO | AP005 | Valentina Ruiz |
| RES-AAAA-00006 | PENDIENTE | DIRECTO | AP001 | Diego Torres |

## 2.3 Notificaciones por correo (integración de salida)

Al **crear, confirmar o cancelar** una reserva se le avisa al titular, al correo que viene en `titular.email` (7.8).

| Modo | Cómo se activa | Qué hace |
|---|---|---|
| `consola` (por defecto) | No hay que hacer nada | Escribe el correo en el log. Funciona sin internet (I-02). |
| `correo` | Variables de entorno de abajo | Envía correos reales por SMTP (Gmail) |

```bash
export SGA_NOTIFICACIONES_MODO=correo
export SGA_CORREO_USUARIO=tucorreo@gmail.com
export SGA_CORREO_CLAVE="clave de aplicación de 16 letras"   # Google → Seguridad → Contraseñas de aplicaciones
sh ./gradlew bootRun
```

Si el correo falla o tarda más de 5 segundos, **la reserva se crea igual** y el error queda en el log (I-03, I-04, I-05).

## 3. Estado del proyecto

| Parte | Estado |
|---|---|
| Modelo de dominio: 11 agregados y entidades, 41 value objects, `ReglaDominioException` | ✅ |
| Servicios de dominio (disponibilidad, cotizador, estancia mínima, confirmación, llegada, salida, cancelación, no-show, modificación, bloqueos) | ✅ |
| Casos de uso: 21, en `application/usecase` | ✅ |
| DTOs de request y response (records) | ✅ |
| Contrato de la API: 54 operaciones en `docs/api/openapi.yaml`, con seguridad, roles y 401/403 por ruta | ✅ |
| Beans de Spring: `UseCaseConfig` y `DomainServiceConfig` | ✅ |
| **Persistencia JPA en H2 de todos los repositorios** | ✅ |
| Controllers REST, mappers REST y `GlobalExceptionHandler` | ✅ (faltan novedades, correcciones del folio y tablero de estados: Corte 3) |
| Datos de prueba al iniciar (12.3): alojamiento, apartamentos, temporadas, tarifas, política, canal, bloqueos y reservas en todos los estados | ✅ |
| Seguridad: usuarios, registro, login con JWT, roles por endpoint y verificación del propietario | ✅ |
| Perfil, cambio y recuperación de contraseña, eliminación lógica, listar usuarios y cambiar rol | ✅ |
| Canal externo: credencial (`X-Canal-Credencial`), idempotencia, conflictos registrados y simulador `.http` (10.3.1) | ✅ |
| Vencimiento automático de reservas pendientes (cada minuto) | ✅ |
| Notificación por correo al crear, confirmar y cancelar (integración de salida, 10.3.2) | ✅ |
| Asistente con IA (10.2) | ⬜ |
| Frontend | ⬜ |

## 4. Arquitectura

El dominio no conoce Spring ni la base de datos. Las flechas apuntan **hacia adentro**:

```
infrastructure  ──►  application  ──►  domain
(Spring, JPA, REST)  (casos de uso)    (reglas del negocio)
```

```
backend/sga/src/main/java/co/edu/uniquindio/sga/
├── domain/                      ← Java puro, sin Spring
│   ├── entity/                  Reserva, Folio, Apartamento, Alojamiento, Bloqueo, ...
│   ├── valueobject/             Estancia, Dinero, CodigoReserva, EstadoReserva, ...
│   ├── service/                 reglas que necesitan varios agregados (disponibilidad, cancelación, ...)
│   ├── repository/              PUERTOS: interfaces como ReservaRepository
│   └── exception/               ReglaDominioException
├── application/                 ← sin Spring
│   ├── usecase/                 un caso de uso por operación de negocio
│   ├── dto/request, dto/response
│   └── exception/               "no encontrado" (ReservaNoEncontradaException, ...)
└── infrastructure/              ← aquí sí está Spring
    ├── config/                  UseCaseConfig y DomainServiceConfig (@Bean)
    └── persistence/
        ├── jpa/                 ADAPTADORES que usa la aplicación (@Repository)
        │   ├── entity/          tablas (@Entity) y embeddables
        │   └── mapper/          dominio ↔ entity
        └── memoria/             adaptadores en memoria, solo para pruebas unitarias
```

### Cómo funciona la persistencia

Cada agregado tiene 5 piezas, todas en `infrastructure/persistence/jpa`:

| Pieza | Ejemplo | Qué hace |
|---|---|---|
| Entity | `ReservaEntity` | La tabla. Tipos simples (`String`, `LocalDate`, `BigDecimal`), no value objects. |
| Embeddables | `EstanciaEmbeddable`, `OcupanteEmbeddable` | Value objects convertidos en columnas, o en filas de una tabla aparte si son listas. |
| Spring Data | `ReservaJpaDataRepository` | Interfaz `extends JpaRepository`. Spring arma el SQL a partir del nombre del método (`findByEstado`). |
| Mapper | `ReservaPersistenceMapper` | Traduce dominio ↔ entity. Para volver al dominio usa `Reserva.reconstruir(...)` o el constructor y los métodos del agregado. |
| Adaptador | `ReservaRepositoryJpa` | `@Repository` que `implements ReservaRepository`. Es el que Spring les entrega a los casos de uso. |

Los casos de uso piden la **interfaz** (`ReservaRepository`), nunca la clase. Por eso cambiar de memoria a JPA no tocó ningún caso de uso.

## 5. Configuración del alojamiento (Ficha San Rocco)

Si algo cambia de un alojamiento a otro, **no puede estar quemado en el código** (sección 6 del enunciado). Estos valores se guardan en la entidad `Alojamiento`, que vive en la base, o en `application.properties`:

| Parámetro | Valor | Dónde vive |
|---|---|---|
| Umbral de edad facturable | 5 años | `Alojamiento` |
| Hora de entrada / salida | 2:00 p. m. / 11:00 a. m. | `Alojamiento` |
| Tiempo de preparación | 3 horas | `Alojamiento` y `Apartamento` |
| Plazo de confirmación (RN-21) | 1 hora | `Alojamiento` y `sga.alojamiento.plazo-confirmacion-horas` |
| Anticipo (RNP-03) | 50 %, el resto al llegar | `Alojamiento` |
| Mascotas (RNP-02) | Máximo 2, recargo de $70.000 | `Apartamento` (`LimiteMascota`) |
| Política de cancelación | 1 semana o más: 0 % de retención · menos de 1 semana: 100 % | `PoliticaCancelacion`, versionada |
| No-show | Se retiene el anticipo | `NoShowService` |

## 6. Forma de trabajo con Git

- `main` siempre estable. **Nadie hace push directo a `main`.**
- Cada uno trabaja en su rama: `Desarrollo-Juan`, `Desarrollo-Andres`, `Desarrollo-Mateo`.

```bash
git checkout Desarrollo-Juan
git pull origin main                      # traer lo último antes de empezar
# ... trabajar ...
cd backend/sga && sh ./gradlew test && cd ../..
git add .
git status                                # revisar que no se cuelen PDFs, bin/ ni build/
git commit -m "feat: registrar pago en el folio"
git push
# en GitHub: Pull request de tu rama hacia main -> revisar -> Merge
```

**Commits convencionales:** el tipo, dos puntos y la descripción en infinitivo y minúscula.

| Prefijo | Para qué | Ejemplo |
|---|---|---|
| `feat:` | Funcionalidad nueva | `feat: persistir reservas en H2 con JPA` |
| `fix:` | Corregir un error | `fix: corregir la hora límite del no-show` |
| `test:` | Pruebas | `test: cubrir la cancelación con política congelada` |
| `refactor:` | Reorganizar sin cambiar el comportamiento | `refactor: mover estructura a backend/` |
| `docs:` | Documentación | `docs: actualizar el README` |
| `chore:` | Mantenimiento | `chore: dar permiso de ejecución a gradlew` |

## 7. Reglas del equipo

1. **Casos de uso: BUSCAR → DELEGAR → GUARDAR.** Se busca con el repositorio, se delega la regla al agregado o al servicio de dominio, y se guarda. Ningún `if` de reglas de negocio en el caso de uso.
2. **Siempre llamar a `guardar` después de modificar un agregado.** Con JPA, lo que lees es una copia: si no la guardas, el cambio se pierde.
3. **Errores:**
   - Regla de negocio violada → `ReglaDominioException`, que se responde como **409**.
   - Algo que no existe → la excepción de `application/exception`, que se responde como **404**.
   - Datos mal formados → Bean Validation en el DTO, que se responde como **400**.
4. **El dominio no lleva anotaciones de Spring, y la aplicación solo `@Transactional`.** Los casos de uso se registran en `UseCaseConfig` y los servicios en `DomainServiceConfig`. No se les pone `@Service`: si una clase tiene `@Service` y además un `@Bean`, Spring no arranca.
5. **El autor de cada operación sale del token,** no de un header: en el controller se recibe `@AuthenticationPrincipal UsuarioAutenticado usuario`. Si un huésped toca una reserva, se verifica que sea el titular con `VerificarAccesoReservaUseCase`.
6. **Nunca devolver entidades por la API.** Se usan los DTOs (records) de `application/dto`.
7. **Dinero siempre con `Dinero` (`BigDecimal`)**, nunca `double`.
8. **Agregado nuevo que se guarda:** se crean sus 5 piezas JPA (sección 4) y su prueba en `RepositoriosJpaTest`. A la versión en memoria no se le pone `@Repository`.
9. **Pruebas:** mínimo una prueba del camino feliz y una por cada regla que puede fallar.

## 8. Pruebas

```bash
cd backend/sga
sh ./gradlew test
# reporte: backend/sga/build/reports/tests/test/index.html
```

Hay **294 pruebas unitarias** y además las de integración:

- **Unitarias** del dominio, los servicios, los value objects y los mappers. No arrancan Spring.
- **De integración:** `SgaApplicationTests`, `ReservaRepositoryJpaTest`, `RepositoriosJpaTest` y `SeguridadTest`. Arrancan Spring con H2; `SeguridadTest` además levanta el servidor y hace peticiones HTTP de verdad (login, 401, 403, registro).

## 9. Pendientes

- [ ] Endpoints de Corte 3 que ya están en el contrato: novedades, bloqueos por apartamento, correcciones del folio y tablero de estados.
- [ ] Bitácora de eventos intercambiados con los canales (7.8).
- [ ] Leer el plazo de confirmación desde `Alojamiento` en `VencerReservasPendientesUseCase`, en lugar de `application.properties`, para no tenerlo en dos lugares.
- [ ] Ajustes de DTOs: gravedad de la novedad, autor y motivo en las autorizaciones (RN-17), y `horaEstimadaLlegada` en `CrearReservaRequest`.
- [ ] Que un token deje de valer en cuanto el usuario se elimina o cambia de rol (hoy vale hasta que vence, máximo 120 minutos).
- [ ] Opcional: H2 en archivo para que los datos queden guardados entre reinicios.

## 10. Documentación

- `docs/api.md`: explicación del contrato de la API (módulos, códigos HTTP, reglas).
- `backend/sga/docs/api/openapi.yaml`: contrato OpenAPI 3.1.
- Enunciado y guías del curso: repositorio del profesor (`Guias/`).

## Equipo

Juan Pablo Espinosa · Andrés F. Valencia · Mateo Gómez Marulanda