# Análisis del dominio SGA – SANROCCO
## Basado en las tablas definidas por el equipo

**Fuentes de análisis**
- **Tabla 1:** Identificación de términos como Entidad u Objeto de Valor, con su justificación (tres pruebas).
- **Tabla 2:** Relación entre cada término y las reglas de negocio (RN-xx / RNP-xx), con su tipo de regla.
- **Documento base:** `Proyecto-Final-SGA.md` (v2.0), usado solo para contrastar.

**Principio de este documento:** la clasificación y el mapeo del equipo son el punto de partida. Aquí no se reemplazan: se **analizan**, se señalan inconsistencias y vacíos, y se derivan agrupaciones a partir de ellos.

**Convenciones**

| Símbolo | Significado |
|---|---|
| ✔ | Consistente con el documento y con el resto de las tablas |
| ⚠ | Observación: inconsistencia, ambigüedad o mejora recomendada |
| ❗ | Error o contradicción que conviene corregir |
| ➕ | Elemento que no está en las tablas y se propone agregar |

---

## 1. Inventario por tipo (según Tabla 1)

**Resumen:** 17 entidades · 19 objetos de valor del glosario · 5 términos propios (todos objetos de valor) = **41 términos**.

### 1.1 Entidades

| # | Término | Observación del análisis |
|---|---|---|
| E1 | Reserva | ✔ Raíz natural del ciclo de vida (sección 8). |
| E2 | Apartamento | ✔ Identidad estable (AP001…) y eliminación lógica. |
| E3 | Ocupante | ✔ Dos ocupantes con igual fecha de nacimiento no son la misma persona. |
| E4 | Titular | ⚠ El documento (3.2) dice que el titular es siempre un ocupante facturable. Definir si es un `Ocupante` marcado como titular o una entidad distinta que se relaciona con él. |
| E5 | Tarifa | ✔ Se conserva histórico (7.4). |
| E6 | Temporada | ✔ Sus rangos no pueden solaparse. |
| E7 | Temporada base | ⚠ Es un caso especial de Temporada (obligatoria, cubre lo no asignado). Puede modelarse como una marca en `Temporada` en lugar de una entidad separada. |
| E8 | Folio | ✔ Un folio pertenece a una sola reserva. |
| E9 | Cargo | ✔ Inmutable una vez registrado (RN-16). |
| E10 | Pago | ✔ Inmutable una vez registrado (RN-16). |
| E11 | Bloqueo | ✔ Registro con rango y motivo; independiente del estado operativo. |
| E12 | Novedad | ✔ Fecha, autor, descripción, gravedad (7.6). |
| E13 | Alojamiento | ✔ Único en el sistema (F-13). |
| E14 | Canal | ✔ Registra tipo y credencial (7.8). |
| E15 | Conflicto de canal | ✔ Se registra para revisión del administrador (RN-18). |
| E16 | Registro | ⚠ Es el *check-in*. Su par, **Salida**, quedó como objeto de valor: la clasificación es asimétrica (ver 4.1). |
| E17 | Cotización | ⚠ El documento la presenta como un cálculo previo a reservar (7.4), no exige persistirla ni darle estados. Definir si se guarda (entidad) o es solo un resultado (objeto de valor). |

### 1.2 Objetos de valor (glosario)

| # | Término | Observación del análisis |
|---|---|---|
| V1 | Estancia | ✔ `[entrada, salida)`; incluye la lógica de solapamiento. |
| V2 | Noche | ✔ Es una fecha. |
| V3 | Capacidad | ✔ Tope rígido. |
| V4 | Dormitorio | ✔ Dato descriptivo (2.2). |
| V5 | Estado de la reserva | ✔ Enumeración con transiciones válidas. Corregir "estancia reemplazable" por "instancia reemplazable" en la justificación. |
| V6 | Estado operativo | ✔ Enumeración de cinco valores. |
| V7 | Canal de origen | ✔ Portal, Directo, Externo. No confundir con la entidad **Canal**. |
| V8 | Ocupante facturable | ⚠ Mejor como un cálculo (`esFacturable(fechaEntrada, umbral)`) sobre el ocupante que como una clase independiente. |
| V9 | Disponibilidad | ⚠ Es resultado de un cálculo entre varios agregados; lo calcula un servicio de dominio, y el resultado puede ser el objeto de valor. |
| V10 | Tiempo de preparación | ✔ Duración en horas, configurable. |
| V11 | Plazo de confirmación | ✔ Duración en horas, configurable. |
| V12 | Salida | ⚠ Ver E16. |
| V13 | No-show | ⚠ Coincide con el valor `NO_SHOW` del estado de la reserva. Evitar duplicarlo como clase aparte. |
| V14 | Política de cancelación | ❗ Contradicción con el documento: la política es **versionada** y se conservan las versiones anteriores (F-07, 7.4). Una política que "no se modifica, se reemplaza" solo es válida si cada versión se conserva con su identidad (ver 4.1). |
| V15 | Versión política | ✔ Referencia congelada en la reserva. ⚠ La justificación "no importa su historia" contradice la necesidad de conservar versiones. |
| V16 | Saldo | ✔ Valor calculado: cargos − pagos. |
| V17 | Cargo noche | ✔ Inmutable. Es el subtotal de una noche del desglose. |
| V18 | Valor congelado | ✔ Desglose noche por noche, inmutable (RN-22). |
| V19 | Resultado cancelación | ✔ Agrupa tramo, penalidad y reembolso. |

### 1.3 Términos propios del equipo (todos objetos de valor)

| # | Término | Observación del análisis |
|---|---|---|
| P1 | Mascota | ✔ Basta con cantidad y recargo. |
| P2 | Anticipo | ✔ Monto del 50 %. |
| P3 | Tramo de cancelación | ✔ Pertenece a la política. |
| P4 | Hora de llegada estimada | ✔ Se reemplaza al solicitar cambio. |
| P5 | Notificación de retraso | ✔ Información dentro de la reserva que puede reemplazarse. |

---

## 2. Matriz término → reglas (según Tabla 2)

Se conserva el mapeo del equipo. La columna **Ajuste** recoge correcciones o reglas adicionales detectadas en el análisis.

### 2.1 Entidades

| Término | Reglas (según el equipo) | Tipo de regla | Ajuste |
|---|---|---|---|
| **Reserva** | RN-02, RN-03, RNP-01, RNP-03, RNP-04 | Restricción / Comportamiento | ➕ Como raíz del agregado debería incluir también RN-01, RN-04, RN-08, RN-09, RN-10, RN-12, RN-13, RN-14, RN-21, RN-22 |
| **Apartamento** | RN-01, RN-02, RN-05, RN-07, RN-11, RN-20, RNP-02 | Restricción / Datos | ✔ |
| **Ocupante** | RN-02 | Restricción | ➕ RN-06, RN-14 y la regla 3.2 (el titular es facturable) |
| **Titular** | RN-09, RN-13 | Restricción | ❗ RN-09 trata de la hora estimada, que es dato de la **Reserva**. ⚠ RN-13 aplica a la retención sobre la reserva; mantener la relación con el titular solo como el responsable del pago |
| **Tarifa** | RN-05 | Datos / Comportamiento | ➕ Regla derivada: todo apartamento activo tiene tarifa en todas las temporadas (7.4) |
| **Temporada** | RN-05, RN-14, RNP-01 | Definición / Restricción | ➕ Regla derivada: sin solapamiento entre temporadas (7.4) |
| **Temporada base** | RN-05 | Definición | ➕ Regla derivada: cubre toda fecha no asignada; ninguna fecha reservable queda sin temporada |
| **Folio** | RN-15, RN-16, RN-17, RNP-02, RNP-03 | Datos / Restricción | ✔ |
| **Cargo** | RN-14, RN-15, RN-16, RNP-02 | Definición / Restricción | ✔ |
| **Pago** | RN-15, RN-16, RNP-03 | Definición / Restricción | ✔ |
| **Bloqueo** | RN-07 | Definición / Restricción | ➕ Regla derivada: no puede registrarse sobre noches con reservas activas (7.3) |
| **Novedad** | RN-11 (inferida) | Comportamiento / Datos | ⚠ El documento no vincula Novedad con RN-11. Marcarla como relación inferida |
| **Alojamiento** | RN-13, RN-22 | Comportamiento | ❗ La descripción de RN-13 copia la de RN-17 (cierre del folio). RN-13 trata de la retención con la política congelada |
| **Canal** | — | — | ➕ Sin reglas asociadas. Se relaciona con 7.8 (credencial por canal, bitácora) y con RN-18 y RN-19 |
| **Conflicto de canal** | RN-18 | Definición / Restricción | ✔ |
| **Registro** | RN-08, RN-10, RN-11, RNP-03, RNP-04 | Restricción / Comportamiento | ✔ |
| **Cotización** | RN-05, RN-14 | Definición / Comportamiento | ✔ |

### 2.2 Objetos de valor (glosario)

| Término | Reglas (según el equipo) | Tipo de regla | Ajuste |
|---|---|---|---|
| **Estancia** | RN-01, RN-03, RN-04, RN-05, RN-14, RNP-01 | Restricción / Definición | ✔ |
| **Noche** | RN-01, RN-03, RN-05, RN-07 | Definición / Datos | ✔ |
| **Capacidad** | RN-02, RN-06, RN-14 | Restricción | ✔ |
| **Dormitorio** | — | — | ⚠ Sin reglas: es solo descriptivo (2.2). Es correcto que no tenga |
| **Estado de la reserva** | RN-08, RN-09, RN-10, RN-12, RN-21, RNP-03, RNP-04 | Restricción / Comportamiento | ✔ |
| **Estado operativo** | RN-11, RN-20 | Definición / Restricción | ➕ Regla derivada: transiciones permitidas de 7.6 |
| **Canal de origen** | RN-01, RN-18, RN-19 | Restricción / Datos | ✔ |
| **Ocupante facturable** | RN-05, RN-06 | Definición / Datos | ✔ |
| **Disponibilidad** | RN-01, RN-07, RN-20 | Restricción | ➕ RN-02 (capacidad) y RN-12 (liberación de noches) |
| **Tiempo de preparación** | RN-14, RN-20 | Definición / Restricción | ✔ |
| **Plazo de confirmación** | — | — | ➕ RN-21 |
| **Salida** | RN-08, RN-12, RN-17, RN-20 | Restricción / Comportamiento | ✔ |
| **No-show** | RN-08, RN-12, RN-13, RNP-04 | Definición / Comportamiento | ✔ |
| **Política de cancelación** | RN-13, RN-22 | Definición / Comportamiento | ✔ |
| **Versión política** | — | — | ➕ RN-13, RN-22 |
| **Saldo** | RN-15, RN-17 | Definición / Restricción | ✔ |
| **Cargo noche** | — | — | ➕ RN-05 |
| **Valor congelado** | — | — | ➕ RN-22, RN-05, RN-14 |
| **Resultado cancelación** | — | — | ➕ RN-13 y el cálculo de cancelación (7.5) |

### 2.3 Términos propios

| Término | Reglas (según el equipo) | Tipo de regla | Ajuste |
|---|---|---|---|
| **Mascota** | RNP-02 y la inmutabilidad del cargo generado | Definición / Restricción / Comportamiento | ❗ La segunda fila no tiene ID. Debe indicar **RN-16** |
| **Anticipo** | RNP-03, RN-08, RN-15 | Definición / Restricción / Datos | ✔ |
| **Tramo de cancelación** | RN-13, RN-22 | Definición / Comportamiento | ✔ |
| **Hora de llegada estimada** | RN-09, RNP-04 | Definición / Restricción / Datos | ✔ |
| **Notificación de retraso** | RNP-04, RN-08 | Definición / Comportamiento / Restricción | ✔ |

---

## 3. Matriz regla → términos (vista inversa)

Muestra qué términos intervienen en cada regla. Sirve para saber qué piezas del dominio hay que implementar para poder probar cada regla.

| Regla | Términos que intervienen | Cobertura |
|---|---|---|
| **RN-01** Sin solapamiento | Apartamento, Estancia, Canal de origen, Noche, Disponibilidad | ✔ |
| **RN-02** Ocupantes ≤ capacidad | Reserva, Apartamento, Capacidad, Ocupante | ✔ |
| **RN-03** Salida > entrada | Reserva, Estancia, Noche | ✔ |
| **RN-04** Entrada no anterior a hoy | Estancia | ⚠ Un solo término |
| **RN-05** Cálculo del valor | Apartamento, Estancia, Tarifa, Temporada, Temporada base, Noche, Ocupante facturable, Cotización | ✔ |
| **RN-06** Definición de facturable | Capacidad, Ocupante facturable | ⚠ Falta Ocupante |
| **RN-07** Bloqueo vigente | Apartamento, Bloqueo, Noche, Disponibilidad | ✔ |
| **RN-08** Transiciones válidas | Estado de la reserva, Registro, Salida, No-show, Anticipo, Notificación de retraso | ✔ |
| **RN-09** Hora estimada antes de confirmar | Estado de la reserva, Hora de llegada estimada, Titular | ⚠ Titular debe pasar a Reserva |
| **RN-10** Registro solo en CONFIRMADA | Estado de la reserva, Registro | ✔ |
| **RN-11** Apartamento PREPARADO | Apartamento, Estado operativo, Registro, Novedad (inferida) | ✔ |
| **RN-12** Liberación inmediata | Estado de la reserva, Salida, No-show | ⚠ Falta mencionar la cancelación como causa |
| **RN-13** Retención con política congelada | Titular, No-show, Política de cancelación, Tramo de cancelación, Alojamiento | ❗ Alojamiento tiene la descripción equivocada |
| **RN-14** Revalidar al modificar | Estancia, Capacidad, Temporada, Tiempo de preparación, Cargo, Cotización | ✔ |
| **RN-15** Pago contra folio y saldo | Folio, Pago, Cargo, Saldo, Anticipo | ✔ |
| **RN-16** Cargos y pagos inmutables | Folio, Pago, Cargo, Mascota (implícita) | ✔ |
| **RN-17** Cierre del folio | Folio, Saldo, Salida | ✔ |
| **RN-18** Conflicto de canal | Canal de origen, Conflicto de canal | ✔ |
| **RN-19** Canal + identificador único | Canal de origen | ⚠ Un solo término; falta `ReferenciaExterna` |
| **RN-20** Tiempo de preparación | Apartamento, Disponibilidad, Estado operativo, Tiempo de preparación, Salida | ✔ |
| **RN-21** Vencimiento automático | Estado de la reserva | ⚠ Falta Plazo de confirmación |
| **RN-22** Valor y política congelados | Alojamiento, Política de cancelación, Tramo de cancelación | ⚠ Faltan Valor congelado y Versión política |
| **RNP-01** Mínimo de noches en Temporada Alta | Reserva, Estancia, Temporada | ✔ |
| **RNP-02** Mascotas y recargo | Apartamento, Folio, Cargo, Mascota | ✔ |
| **RNP-03** Anticipo del 50 % | Reserva, Estado de la reserva, Folio, Pago, Registro, Anticipo | ✔ |
| **RNP-04** Notificación de retraso | Reserva, Estado de la reserva, Registro, No-show, Hora de llegada estimada, Notificación de retraso | ✔ |

---

## 4. Hallazgos del análisis

### 4.1 Inconsistencias de clasificación (Tabla 1)

1. **Registro (entidad) vs. Salida (objeto de valor):** son el par *check-in* / *check-out* y deberían tratarse igual. Opciones:
   - Ambos como objetos de valor dentro de la Reserva (fecha y hora de la acción).
   - Ambos como entidades si se quiere guardar historial propio.
2. **Política de cancelación como objeto de valor:** el documento exige que sea única, versionada y que se conserven las versiones anteriores (F-07, 7.4). Opciones:
   - Entidad con historial de versiones, donde cada versión es inmutable y se referencia desde la reserva con `VersionPolitica`.
   - Mantener el objeto de valor, pero guardando todas las versiones y justificando que cada versión se reemplaza y nunca se edita.
3. **No-show como objeto de valor:** se superpone con el estado `NO_SHOW` de la reserva. Conviene dejarlo como valor del estado y como evento, no como una clase aparte.
4. **Cotización como entidad:** el documento no exige persistirla. Si no se guarda, es un objeto de valor (resultado del cálculo).
5. **Temporada base como entidad aparte:** es el mismo concepto que Temporada con una marca.
6. **Titular y Ocupante:** definir cómo se relacionan, porque el titular es siempre un ocupante facturable (3.2).

### 4.2 Errores en el mapeo (Tabla 2)

| Ubicación | Problema | Corrección |
|---|---|---|
| Alojamiento → RN-13 | La descripción corresponde a RN-17 | Reescribir: la retención se calcula con la política congelada, no con la vigente |
| Mascota → segunda fila | Falta el identificador | Indicar RN-16 |
| Titular → RN-09 | La hora estimada pertenece a la reserva | Mover la relación a Reserva |
| Novedad → RN-11 | El vínculo es deducido, no está en el documento | Marcarlo como "inferido" |
| Columna "Tipo regla" | La primera fila de cada grupo mezcla listas de términos relacionados con tipos de regla | Separar en dos columnas distintas |

### 4.3 Vacíos

**Términos sin reglas asignadas:** Canal, Plazo de confirmación, Versión política, Cargo noche, Valor congelado, Resultado cancelación y Dormitorio (este último es correcto que no tenga).

**Reglas apoyadas por un solo término:** RN-04, RN-19, RN-21 y RN-12 (parcial). Indica que falta un concepto en las tablas, por ejemplo `ReferenciaExterna` para RN-19.

### 4.4 Reglas del documento aún no mapeadas a ningún término

| Regla derivada | Fuente | Término que debería recibirla |
|---|---|---|
| Titular siempre facturable | 3.2 | Titular / Ocupante |
| Orden de las seis validaciones de creación | 7.5 | Reserva (validador de creación) |
| Modificación solo en PENDIENTE o CONFIRMADA | 7.5 | Reserva |
| Cancelación solo antes del registro | 7.5 | Reserva / Estado de la reserva |
| Salida anticipada no es cancelación | 7.5 | Salida |
| Bloqueo no cubre noches con reservas activas | 7.3 | Bloqueo |
| Temporadas sin solapamiento y temporada base obligatoria | 7.4 | Temporada / Temporada base |
| Tarifas completas para activar un apartamento | 7.4 | Tarifa / Apartamento |
| Transiciones del estado operativo | 7.6 | Estado operativo |
| Cambio de capacidad no afecta reservas existentes | 7.3 | Capacidad / Apartamento |
| Redondeo al final de cada cargo, en COP sin decimales | 3.4 | Dinero ➕ |
| Folio cerrado como requisito de la salida | 7.6 | Folio / Salida |
| Autorización registrada para cerrar con saldo | 7.7 | AutorizacionCierreFolio ➕ |

### 4.5 Términos que faltan en la Tabla 1 (➕)

| Tipo | Elemento | Por qué es necesario |
|---|---|---|
| Objeto de valor | **Dinero** | COP, sin decimales, tipo de precisión exacta, redondeo al final (3.4) |
| Objeto de valor | **MedioPago** | Todo pago tiene medio y fecha (RN-15) |
| Objeto de valor | **TipoCargo** | Alojamiento, servicio, ajuste, penalidad (7.7) |
| Objeto de valor | **ReferenciaExterna** | Canal + identificador, único (RN-19) |
| Objeto de valor | **UmbralEdadFacturable** | Configuración (RN-06) |
| Objeto de valor | **FechaNacimiento** | La edad se calcula, no se almacena (3.2) |
| Objeto de valor | **HoraLimiteNoShow, HoraEntrada, HoraSalida** | Configuración del alojamiento |
| Entidad | **ServicioAdicional** | Desayuno u otros con cargo (A.5) |
| Entidad | **Imagen** | De 1 a 10 por apartamento (7.3) |
| Entidad | **AutorizacionCierreFolio** | Autor y motivo registrados (RN-17) |
| Seguridad | **Usuario, Rol, CodigoRecuperacion** | Fuera del dominio núcleo (3.6) |
| Auditoría | **EventoCanal** | Bitácora de eventos con canales (7.8) |

---

## 5. Agrupación en agregados a partir de los términos definidos

Propuesta construida únicamente con los términos de las tablas del equipo.

| Agregado (raíz) | Entidades internas | Objetos de valor | Reglas que protege |
|---|---|---|---|
| **Reserva** | Ocupante (y titular marcado) | Estancia, Estado de la reserva, Canal de origen, Anticipo, Mascota, Hora de llegada estimada, Notificación de retraso, Valor congelado (con Cargo noche), Versión política, Resultado cancelación, marcas de Registro y Salida | RN-02*, 03, 04, 08, 09, 10, 12, 14, 21, 22, RNP-01, 03, 04 |
| **Apartamento** | — | Capacidad, Dormitorio, Estado operativo, límite de mascotas | RN-02*, 11, RNP-02 |
| **Folio** | Cargo, Pago | Saldo (calculado) | RN-15, 16, 17, RNP-02, 03 |
| **Bloqueo** | — | Rango de fechas | RN-07 |
| **Novedad** | — | Gravedad | RN-11 (inferida) |
| **Temporada** | — | Rango de fechas; marca de temporada base | RN-05, RNP-01 |
| **Tarifa** | — | Dinero | RN-05 |
| **Alojamiento** | — | Tiempo de preparación, Plazo de confirmación, horarios | Configuración (sección 6) |
| **Política de cancelación** *(según decisión 4.1.2)* | Tramo de cancelación | Versión | RN-13, 22 |
| **Canal** | — | Credencial | RN-18, 19 |
| **Conflicto de canal** | — | Referencia externa | RN-18 |
| **Titular** | — | Documento, contacto | RN-09 (a través de la reserva) |

*RN-02 se valida en la reserva con la capacidad del apartamento, obtenida por consulta.

**Servicios de dominio** (reglas que cruzan agregados):

| Servicio | Términos que coordina | Reglas |
|---|---|---|
| **Disponibilidad** | Apartamento, Reserva, Bloqueo, Estado operativo, Tiempo de preparación | RN-01, 07, 20 |
| **Cotización** (si no se persiste) | Tarifa, Temporada, Estancia, Ocupantes | RN-05, 14 |
| **Validación de creación** | Reserva, Apartamento, Disponibilidad | RN-01 a 04, 20, RNP-01 |

---

## 6. Decisiones pendientes para el equipo

| # | Decisión | Opciones | Impacto |
|---|---|---|---|
| D1 | ¿Cómo se versiona la política de cancelación? | Entidad con historial / Objeto de valor con versiones conservadas | RN-13, RN-22 |
| D2 | Registro y Salida | Ambos objetos de valor o ambos entidades | RN-08, 10, 11, 17 |
| D3 | ¿Se persiste la Cotización? | Entidad / Objeto de valor resultado de cálculo | RN-05, 14 |
| D4 | Titular y Ocupante | Un solo concepto con marca / Dos entidades relacionadas | RN-02, 06, RN-09 |
| D5 | Temporada base | Entidad aparte / Marca en Temporada | RN-05 |
| D6 | No-show | Solo valor del estado / Clase propia | RN-08, 12, 13, RNP-04 |
| D7 | Alcance de la retención del anticipo | Sobre el anticipo / Sobre el valor total | RN-13 |
| D8 | Regla de niños menores de 5 años | Aclarar el "cobro adicional" mencionado en la Ficha, que no coincide con RN-05 y RN-06 | RN-05, 06 |

---

## 7. Lista de verificación para la auditoría del código (`SGA/src/main`)

Lista de los 41 términos definidos por el equipo, lista para completar al revisar el código.

**Estados posibles:** ✅ Implementado · 🟡 Parcial · ❌ Faltante · ⚪ No verificable

| Término | Tipo definido | Reglas asociadas | Estado en código | Ruta / evidencia |
|---|---|---|---|---|
| Reserva | Entidad | RN-02, 03, RNP-01, 03, 04 (+ las del ajuste) | | |
| Apartamento | Entidad | RN-01, 02, 05, 07, 11, 20, RNP-02 | | |
| Ocupante | Entidad | RN-02 (+ RN-06, 14) | | |
| Titular | Entidad | RN-09, 13 | | |
| Tarifa | Entidad | RN-05 | | |
| Temporada | Entidad | RN-05, 14, RNP-01 | | |
| Temporada base | Entidad | RN-05 | | |
| Folio | Entidad | RN-15, 16, 17, RNP-02, 03 | | |
| Cargo | Entidad | RN-14, 15, 16, RNP-02 | | |
| Pago | Entidad | RN-15, 16, RNP-03 | | |
| Bloqueo | Entidad | RN-07 | | |
| Novedad | Entidad | RN-11 (inferida) | | |
| Alojamiento | Entidad | RN-13, 22 | | |
| Canal | Entidad | — | | |
| Conflicto de canal | Entidad | RN-18 | | |
| Registro | Entidad | RN-08, 10, 11, RNP-03, 04 | | |
| Cotización | Entidad | RN-05, 14 | | |
| Estancia | Objeto de valor | RN-01, 03, 04, 05, 14, RNP-01 | | |
| Noche | Objeto de valor | RN-01, 03, 05, 07 | | |
| Capacidad | Objeto de valor | RN-02, 06, 14 | | |
| Dormitorio | Objeto de valor | — | | |
| Estado de la reserva | Objeto de valor | RN-08, 09, 10, 12, 21, RNP-03, 04 | | |
| Estado operativo | Objeto de valor | RN-11, 20 | | |
| Canal de origen | Objeto de valor | RN-01, 18, 19 | | |
| Ocupante facturable | Objeto de valor | RN-05, 06 | | |
| Disponibilidad | Objeto de valor | RN-01, 07, 20 | | |
| Tiempo de preparación | Objeto de valor | RN-14, 20 | | |
| Plazo de confirmación | Objeto de valor | (RN-21) | | |
| Salida | Objeto de valor | RN-08, 12, 17, 20 | | |
| No-show | Objeto de valor | RN-08, 12, 13, RNP-04 | | |
| Política de cancelación | Objeto de valor | RN-13, 22 | | |
| Versión política | Objeto de valor | (RN-13, 22) | | |
| Saldo | Objeto de valor | RN-15, 17 | | |
| Cargo noche | Objeto de valor | (RN-05) | | |
| Valor congelado | Objeto de valor | (RN-22, 05, 14) | | |
| Resultado cancelación | Objeto de valor | (RN-13) | | |
| Mascota | Objeto de valor | RNP-02, RN-16 | | |
| Anticipo | Objeto de valor | RNP-03, RN-08, RN-15 | | |
| Tramo de cancelación | Objeto de valor | RN-13, 22 | | |
| Hora de llegada estimada | Objeto de valor | RN-09, RNP-04 | | |
| Notificación de retraso | Objeto de valor | RNP-04, RN-08 | | |

Las reglas entre paréntesis son las que se proponen agregar en el ajuste (secciones 2 y 3).
