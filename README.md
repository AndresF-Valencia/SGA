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

> La base es **en memoria**: todo se borra al apagar la aplicación. Al arrancar se crean los usuarios de prueba; el resto de datos iniciales está pendiente.

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

## 2.2 Notificaciones por correo (integración de salida)

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
| Contrato de la API: 34 operaciones en `docs/api/openapi.yaml` | ✅ |
| Beans de Spring: `UseCaseConfig` y `DomainServiceConfig` | ✅ |
| **Persistencia JPA en H2 de todos los repositorios** | ✅ |
| Controllers REST, mappers REST y `GlobalExceptionHandler` | ✅ (faltan los endpoints de consulta GET) |
| Datos iniciales (alojamiento, apartamentos, temporadas, política v1) | ⬜ |
| Seguridad: usuarios, registro, login con JWT, roles por endpoint y verificación del propietario | ✅ |
| Perfil, cambio y recuperación de contraseña, eliminación lógica de usuarios | ⬜ |
| Credencial de canales (`X-Canal-Credencial`) | ⬜ |
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

Hay **278 pruebas**:

- **Unitarias** del dominio, los servicios, los value objects y los mappers. No arrancan Spring.
- **De integración:** `SgaApplicationTests`, `ReservaRepositoryJpaTest`, `RepositoriosJpaTest` y `SeguridadTest`. Arrancan Spring con H2; `SeguridadTest` además levanta el servidor y hace peticiones HTTP de verdad (login, 401, 403, registro).

## 9. Pendientes

- [ ] Endpoints de consulta (GET): reservas, folio, disponibilidad, cotizar, canales externos.
- [ ] Datos iniciales al arrancar: el alojamiento San Rocco, los apartamentos, las temporadas y tarifas, y la política versión 1.
- [ ] **RN-19:** la `Reserva` no guarda el identificador externo del canal, así que la idempotencia de reservas externas todavía no funciona.
- [ ] Leer el plazo de confirmación desde `Alojamiento` en `VencerReservasPendientesUseCase`, en lugar de `application.properties`, para no tenerlo en dos lugares.
- [ ] Ajustes de DTOs: tiempo de preparación en horas, gravedad de la novedad, autor y motivo en las autorizaciones (RN-17), y `horaEstimadaLlegada` en `CrearReservaRequest`.
- [ ] Perfil, cambio y recuperación de contraseña (7.2) y la credencial `X-Canal-Credencial` para los canales.
- [ ] Cambiar el header `X-Alojamiento-Id` por `obtenerUnico()` (hay un solo alojamiento).
- [ ] Opcional: H2 en archivo para que los datos queden guardados entre reinicios.

## 10. Documentación

- `docs/api.md`: explicación del contrato de la API (módulos, códigos HTTP, reglas).
- `backend/sga/docs/api/openapi.yaml`: contrato OpenAPI 3.1.
- Enunciado y guías del curso: repositorio del profesor (`Guias/`).

## Equipo

Juan Pablo Espinosa · Andrés F. Valencia · Mateo Gómez Marulanda