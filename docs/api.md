# Contrato de API — SGA (Hotel Sanrocco)

Este documento detalla la estructura, funcionamiento y reglas de negocio del contrato de la API RESTful del **Sistema de Gestión de Alojamiento (SGA)** para el Hotel Sanrocco (Armenia, Quindío).

---

## 1. Funcionamiento de la API

La API RESTful constituye la interfaz de entrada a la capa de aplicación de la **Arquitectura Hexagonal** del proyecto backend, basada en los principios de **Domain-Driven Design (DDD)**. 

Se encarga de exponer los casos de uso para que los clientes del sistema (portal web, recepción, aplicaciones móviles o canales externos) puedan interactuar con el dominio de negocio.

### Métodos HTTP Utilizados:
* **`GET`**: Consultar información del dominio (no altera el estado del sistema).
* **`POST`**: Crear nuevos recursos o movimientos (ej. crear reserva, registrar pago, publicar política).
* **`PUT`**: Actualizar recursos existentes o ejecutar transiciones de estado en el ciclo de vida de una entidad (ej. confirmar reserva, registrar llegada/salida, cambiar estado operativo).

---

## 2. Códigos de Estado HTTP y Manejo de Errores

El sistema responde utilizando los códigos de estado HTTP estándar para representar el resultado de la ejecución de cada caso de uso:

| Código HTTP | Definición | Causa / Significado en el Dominio |
| :--- | :--- | :--- |
| **`200 OK`** | Operación Exitosa | La consulta o modificación se completó correctamente y se retorna la información procesada. |
| **`201 Created`** | Recurso Creado | Se registró exitosamente una nueva entidad en la base de datos (ej. reserva, cargo, pago, bloqueo). Retorna el encabezado `Location`. |
| **`204 No Content`** | Sin Contenido | La operación se completó exitosamente pero no requiere retornar un cuerpo en la respuesta (ej. definir tarifa por temporada). |
| **`400 Bad Request`** | Datos Inválidos | La petición contiene errores de estructura, faltan campos obligatorios o falla la validación sintáctica (`Bean Validation`). |
| **`401 Unauthorized`** | No Autenticado | Falta la cabecera de autenticación (`Bearer Token` para usuarios o `X-Canal-Credencial` para canales externos) o no es válida. |
| **`404 Not Found`** | Recurso No Encontrado | El identificador solicitado no existe en las entidades del dominio (ej. código de reserva o apartamento no encontrado). |
| **`409 Conflict`** | Regla de Negocio Violada | La petición es sintácticamente válida pero viola una regla explicita del dominio (`ReglaDominioException`). |

---

## 3. Módulos y Estructura de Endpoints

Todas las rutas del sistema están prefijadas con el segmento `/api`:

### A. Módulo de Reservas
Maneja el ciclo de vida completo de la reserva, disponibilidad y cotizaciones:
* **`GET /api/disponibilidad`**: Consulta de apartamentos libres según rango de fechas y ocupación.
* **`POST /api/cotizaciones`**: Cálculo detallado del costo noche por noche sin bloquear inventario.
* **`POST /api/reservas`**: Creación de reserva. Congela la tarifa por noche y la versión de la política de cancelación (RN-22).
* **`GET /api/reservas`**: Listado paginado de reservas filtrable por estado.
* **`GET /api/reservas/{codigo}`**: Consulta detallada de una reserva.
* **`PUT /api/reservas/{codigo}`**: Modificación de fechas, apartamento o mascotas en reservas no iniciadas (CU-05).
* **`PUT /api/reservas/{codigo}/confirmar`**: Confirmación de reserva pendiente tras verificar el abono del anticipo (RNP-03).
* **`PUT /api/reservas/{codigo}/cancelar`**: Cancelación de reserva aplicando el porcentaje de retención correspondiente.
* **`PUT /api/reservas/{codigo}/declarar-noshow`**: Declaración de No-Show tras superar el plazo de llegada.
* **`PUT /api/reservas/{codigo}/registrar-llegada`**: Check-in del grupo (pasa la reserva a estado `EN_CURSO`).
* **`PUT /api/reservas/{codigo}/registrar-salida`**: Check-out del grupo y liberación del apartamento.

### B. Folio y Finanzas
Administra los cargos, pagos y saldos contables de las reservas (`/api/reservas/{codigo}/folio`):
* **`GET /.../folio`**: Consulta el balance financiero (subtotal cargos, total pagos y saldo pendiente).
* **`POST /.../folio/cargos`**: Registro de nuevos consumos o servicios.
* **`POST /.../folio/pagos`**: Registro de pagos y abonos realizados con su respectivo medio de pago (RN-15).
* **`POST /.../folio/correcciones`**: Contrabalanceo de movimientos mediante movimientos inversos para garantizar trazabilidad (RN-16).
* **`PUT /.../folio/cerrar`**: Cierre contable del folio.

### C. Apartamentos y Operación
Control del estado operativo e infraestructura física (`/api/apartamentos`):
* **`GET /api/apartamentos/estado`**: Consulta del estado operativo actual de todas las unidades.
* **`GET /api/apartamentos/{id}`**: Detalle de características, capacidad, mascotas e imágenes de un apartamento.
* **`POST /api/apartamentos/{id}/estado-operativo`**: Cambio del estado operativo (`PREPARADO`, `OCUPADO`, `PENDIENTE_PREPARACION`, `EN_PREPARACION`, `FUERA_DE_SERVICIO`).
* **`POST /api/apartamentos/{id}/novedades`**: Registro de fallos o reportes de mantenimiento.
* **`POST /api/bloqueos`**: Bloqueo administrativo de fechas para mantenimiento (RN-07).

### D. Temporadas, Tarifas y Parámetros
Configuración de precios dinámicos y reglas del alojamiento:
* **`GET` / `PUT /api/alojamiento/parametros`**: Configuración de horas de check-in/out, tiempos de preparación y políticas de anticipo.
* **`POST` / `GET /api/temporadas`**: Definición de temporadas y estancias mínimas (sin solapamiento de fechas).
* **`PUT /api/apartamentos/{id}/tarifas/{temporadaId}`**: Asignación de tarifas por noche por apartamento y temporada.
* **`POST` / `GET /api/politicas-cancelacion`**: Publicación y consulta de versiones de políticas de retención.

### E. Integración con Canales Externos (`/api/canales`)
Endpoints para sincronización con plataformas de terceros (OTAs):
* Requieren autenticación mediante el encabezado `X-Canal-Credencial`.
* **`GET /api/canales/disponibilidad`**: Consulta de disponibilidad para canal.
* **`POST /api/canales/reservas-externas`**: Recepción de reservas externas con prevención de sobre-reservas (RN-18) e idempotencia por código externo (RN-19).
* **`PUT /api/conflictos-canal/{id}/resolver`**: Resolución administrativa de colisiones de reserva.

---
