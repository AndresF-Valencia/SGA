# Modelado de dominio (DDD) del SGA – SANROCCO

**Proyecto:** Sistema de Gestión de Alojamiento (SGA) – Programación Avanzada, Universidad del Quindío
**Fuente:** `Proyecto-Final-SGA.md` (v2.0), incluyendo el lenguaje ubicuo (sección 4), las reglas RN-01 a RN-22, las reglas propias RNP-01 a RNP-04 y la Ficha del Alojamiento (Anexo A).

> Los agregados, entidades y objetos de valor son una **propuesta de diseño derivada del texto**; el documento no los define así de forma explícita. Las reglas de negocio sí provienen directamente del documento.

---

## 1. Agregados

| # | Agregado (raíz) | Contiene | Justificación |
|---|---|---|---|
| A1 | **Alojamiento** | Parámetros de configuración, horarios, normas, servicios adicionales, ubicación | Es el único alojamiento del sistema (F-13) y concentra la configuración (sección 6) |
| A2 | **Apartamento** | Imágenes, dotación/características, estado operativo | Identidad estable, eliminación lógica y estado operativo propio |
| A3 | **Bloqueo** | Rango de fechas, motivo | Decisión administrativa independiente del estado operativo |
| A4 | **Novedad** | Autor, fecha, descripción, gravedad | Crece sin límite y tiene historial propio; referencia al apartamento por ID |
| A5 | **Temporada** | Rangos de fechas | Sus rangos no pueden solaparse y debe existir una temporada base |
| A6 | **Tarifa** | Valor por apartamento × temporada, con histórico | Todo apartamento activo necesita tarifa en todas las temporadas |
| A7 | **PoliticaCancelacion** | Tramos de cancelación, consecuencia del no-show | Única, versionada; se conservan las versiones anteriores |
| A8 | **Reserva** | Ocupantes, mascotas, estancia, valor congelado, notificación de retraso | Concentra el ciclo de vida y las reglas de la sección 8 |
| A9 | **Folio** | Cargos y pagos | Se abre con la reserva, pertenece a una sola reserva y maneja el saldo |
| A10 | **Titular** | Datos de la persona | Existe aunque no tenga usuario (3.6) |
| A11 | **CanalVenta** | Tipo, credencial | Origen de la reserva y acceso de integración |
| A12 | **ConflictoCanal** | Reserva externa rechazada y su colisión | Se registra para revisión del administrador |
| A13 | **EventoCanal** (bitácora) | Mensajes intercambiados con los canales | Registro de auditoría |
| A14 | **Usuario** *(infraestructura/seguridad)* | Credenciales, rol, código de recuperación | El dominio no depende de él (3.6) |

### 1.1 Servicios de dominio

Reglas que cruzan agregados y no pertenecen a uno solo:

- **ServicioDisponibilidad:** reservas activas + bloqueos + estado activo + capacidad + tiempo de preparación (RN-01, RN-07, RN-20).
- **CotizadorEstancia:** tarifa por noche × ocupantes facturables, usando temporadas, tarifas y recargos.
- **ValidadorCreacionReserva:** las seis validaciones en orden (sección 7.5).
- **Registro de bloqueo:** rechaza el bloqueo si hay reservas activas en esas noches.
- **Activación de apartamento:** exige tarifas completas en todas las temporadas.

### 1.2 Eventos de dominio sugeridos

`ReservaCreada`, `ReservaConfirmada`, `ReservaCancelada`, `ReservaVencida`, `NoShowDeclarado`, `LlegadaRegistrada`, `SalidaRegistrada`, `PagoRegistrado`, `ConflictoDeCanalRegistrado`, `ApartamentoPreparado`.

---

## 2. Entidades

Tienen identidad propia y ciclo de vida:

- **Alojamiento**
- **Apartamento** – ID único y estable (AP001…)
- **Imagen** (del apartamento) – mínimo 1, máximo 10, una principal
- **Bloqueo**
- **Novedad**
- **Temporada** – la temporada base es un caso especial
- **Tarifa** y sus versiones históricas
- **PoliticaCancelacion** (versionada) y **TramoCancelacion**
- **Reserva** – el estado es un atributo
- **Ocupante** – fecha de nacimiento, facturable o no, y si es titular
- **Mascota**
- **NotificacionRetraso** (término propio del equipo)
- **Titular** – documento, nombre, contacto; puede o no tener usuario
- **Folio**
- **Cargo** – inmutable
- **Pago** – inmutable
- **AutorizacionCierreFolio** – autor, motivo, fecha
- **CanalVenta**
- **ConflictoCanal**
- **EventoCanal**
- **ServicioAdicional** – por ejemplo, Desayuno ($20.000)
- **Usuario** y **Rol**
- **CodigoRecuperacion** – vigencia de 15 minutos, un solo uso

---

## 3. Objetos de valor

Se definen por sus atributos, son inmutables y no tienen identidad.

### 3.1 Tiempo y estancia

- **Estancia** – `[entrada, salida)`, con número de noches y operación de solapamiento
- **Noche**
- **RangoFechas** – usado en Temporada, Bloqueo y búsquedas
- **FechaNacimiento** – la edad se calcula, nunca se almacena
- **HoraEstimadaLlegada**
- **HoraEntrada / HoraSalida**
- **TiempoPreparacion** – en horas
- **PlazoConfirmacion**
- **HoraLimiteNoShow**

### 3.2 Dinero y cobro

- **Dinero** – COP sin decimales, tipo de precisión exacta (p. ej. `BigDecimal`), redondeo al final de cada cargo
- **TarifaPorNoche**
- **DesgloseNoche** – fecha, temporada, tarifa, facturables, subtotal
- **ValorReservaCongelado**
- **Saldo** – positivo, cero o negativo
- **Anticipo** – 50 %
- **PorcentajeRetencion / PorcentajeDevolucion**
- **RecargoMascotas** – $70.000 de limpieza
- **MedioPago** – Transferencia, Efectivo, Tarjeta
- **TipoCargo** – alojamiento, servicio adicional, ajuste, penalidad
- **AjusteFolio**

### 3.3 Apartamento

- **IdApartamento**
- **Capacidad** – tope rígido
- **NumeroDormitorios**
- **Dotacion / Caracteristica**
- **EstadoOperativo** – `PREPARADO`, `OCUPADO`, `PENDIENTE_PREPARACION`, `EN_PREPARACION`, `FUERA_DE_SERVICIO`
- **MotivoBloqueo**
- **GravedadNovedad**

### 3.4 Reserva y canal

- **EstadoReserva** – `PENDIENTE`, `CONFIRMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA`, `NO_SHOW`
- **CanalOrigen** – `PORTAL`, `DIRECTO`, `EXTERNO`
- **ReferenciaExterna** – canal + identificador externo, único
- **VersionPolitica** – referencia congelada en la reserva
- **UmbralEdadFacturable** – 5 años
- **ComposicionGrupo**

### 3.5 Persona y acceso

- **Documento de identidad**
- **Correo**
- **Telefono**
- **Contraseña (hash)**
- **UbicacionGeografica** – latitud y longitud

---

## 4. Reglas de negocio

### 4.1 Invariantes (RN-01 a RN-22)

**Disponibilidad y reserva**

- **RN-01** Un apartamento no puede tener dos reservas activas solapadas, sin importar el canal.
- **RN-02** Los ocupantes no pueden exceder la capacidad del apartamento.
- **RN-03** La fecha de salida es posterior a la de entrada: toda estancia tiene al menos una noche.
- **RN-04** No se crean reservas con fecha de entrada anterior a hoy.
- **RN-07** Un bloqueo vigente sobre una noche impide venderla.
- **RN-12** Una reserva que deja de estar activa libera sus noches de inmediato.
- **RN-20** Entre una salida y la siguiente entrada se respeta el tiempo de preparación.
- **RN-21** Una reserva `PENDIENTE` que supera el plazo de confirmación se cancela automáticamente.

**Tarifación**

- **RN-05** Valor de la estancia = Σ (tarifa de la temporada de cada noche × ocupantes facturables).
- **RN-06** Es facturable quien, a la fecha de entrada, alcanza el umbral. Los no facturables sí cuentan para la capacidad.
- **RN-22** Valor y política se congelan al crear la reserva y solo cambian por modificación explícita.
- **RN-14** Todo cambio de fechas, ocupantes o apartamento repite las seis validaciones, recalcula el valor y registra la diferencia como ajuste.
- **RN-13** La retención por cancelación o no-show usa la versión de política congelada en la reserva.

**Ciclo de vida y operación**

- **RN-08** Solo se permiten las transiciones de la sección 8.
- **RN-09** La reserva debe tener hora estimada de llegada antes de confirmarse.
- **RN-10** No hay registro antes de la fecha de entrada ni sobre una reserva que no esté `CONFIRMADA`.
- **RN-11** Solo se recibe un grupo si el apartamento está `PREPARADO`.

**Folio**

- **RN-15** Todo pago se registra contra un folio con medio y fecha. Saldo = cargos − pagos.
- **RN-16** Cargos y pagos no se editan ni se borran; se corrigen con un movimiento inverso.
- **RN-17** El folio no se cierra con saldo ≠ 0 sin autorización explícita registrada.

**Canales**

- **RN-18** Una reserva externa en conflicto se rechaza y se registra; nunca sobrescribe la vigente.
- **RN-19** Canal + identificador externo es único: un mensaje repetido no duplica la reserva.

### 4.2 Reglas propias del equipo (RNP-01 a RNP-04)

- **RNP-01** Una estancia en Temporada Alta debe cumplir un mínimo de noches configurable. Si no lo cumple, se rechaza la reserva.
- **RNP-02** Cada apartamento admite un máximo configurable de mascotas (2). Si hay mascotas, se aplica el recargo de limpieza ($70.000).
- **RNP-03** Una reserva con anticipo solo se confirma al registrar el pago inicial del 50 % del valor total. El otro 50 % se paga en el registro de llegada.
- **RNP-04** No se declara No-Show si hubo una notificación de retraso válida antes de aplicarse la condición; se concede tiempo adicional para el registro.

### 4.3 Reglas derivadas del texto (no numeradas)

**Creación y modificación de reservas**

- **Orden de validación al crear:**
  1. Salida posterior a entrada.
  2. Entrada no anterior a hoy.
  3. Apartamento activo y con tarifas completas.
  4. Capacidad suficiente.
  5. Sin solapamiento con reservas ni bloqueos.
  6. Tiempo de preparación respetado.
- **Estado inicial:** la reserva nace en `PENDIENTE`, abre el folio con el cargo de alojamiento y notifica al titular.
- **Titular:** es siempre un ocupante facturable.
- **Intervalo de noches:** `[entrada, salida)`; la noche de la fecha de salida no se cobra ni se ocupa.
- **Solapamiento:** ocurre si `entrada_A < salida_B` y `entrada_B < salida_A`.
- **Zona horaria:** Colombia.
- **Modificación:** solo en `PENDIENTE` o `CONFIRMADA`, y antes de iniciar la estancia.
- **Cancelación:** solo desde `PENDIENTE` o `CONFIRMADA`, siempre antes del registro.
- **Salida anticipada:** es una salida, no una cancelación, y no aplica la política.
- **No-show:** solo puede declararse a partir de la hora límite del día de entrada.

**Disponibilidad y operación**

- **Disponibilidad:** el apartamento debe estar activo, sin reservas ni bloqueos solapados y con capacidad suficiente.
- **Disponibilidad vs. estado operativo vs. bloqueo:** son conceptos separados; un bloqueo no cambia el estado operativo, ni al revés.
- **Ventana de preparación:** si el tiempo de preparación excede la ventana entre salida y entrada, no se admite entrada el mismo día de una salida.
- **Transiciones del estado operativo:**
  - `PREPARADO → OCUPADO` (registro)
  - `OCUPADO → PENDIENTE_PREPARACION` (salida)
  - `PENDIENTE_PREPARACION → EN_PREPARACION → PREPARADO` (servicio)
  - `FUERA_DE_SERVICIO` desde cualquier estado no ocupado, solo por el administrador
- **Personal de servicio:** no puede declarar `FUERA_DE_SERVICIO` ni registrar bloqueos.
- **Registro de llegada:** exige reserva `CONFIRMADA`, fecha de entrada alcanzada y apartamento `PREPARADO`.
- **Salida:** deja la reserva en `FINALIZADA` y el apartamento en `PENDIENTE_PREPARACION`; requiere folio cerrado, salvo autorización del administrador.

**Apartamentos, bloqueos, temporadas y tarifas**

- **Imágenes:** entre 1 y 10, con una principal.
- **Retiro de la venta:** solo sin reservas activas ni futuras; la eliminación es lógica.
- **Cambio de capacidad:** no afecta reservas existentes; se advierte al administrador y se listan.
- **Bloqueo:** no puede registrarse sobre noches con reservas activas.
- **Temporadas:** sin solapamiento, con una temporada base obligatoria que cubre toda fecha.
- **Tarifas:** todo apartamento activo debe tener tarifa en todas las temporadas.
- **Política de cancelación:** única, con mínimo 2 tramos; al actualizarla se conserva la versión anterior.

**Dinero y folio**

- **Moneda:** COP sin decimales, con tipo de precisión exacta (prohibido `float` y `double`).
- **Redondeo:** al final del cálculo de cada cargo, no noche por noche.
- **El sistema registra dinero, no lo ejecuta:** una devolución se calcula y se registra; el desembolso ocurre fuera del sistema.
- **Folio:** pertenece a una sola reserva y admite pagos parciales con medios distintos.
- **Tipos de cargo:** alojamiento, servicio adicional, ajuste por modificación, penalidad por cancelación o no-show.
- **Cancelación:** la retención se registra como cargo y la devolución como saldo a favor.
- **Cierre con saldo ≠ 0:** la autorización del administrador queda registrada con autor y motivo.

**Usuarios y seguridad**

- **Registro:** correo único, contraseña de mínimo 8 caracteres con mayúsculas y números, rol Huésped por defecto.
- **Roles internos:** solo los asigna el administrador.
- **Acceso a recursos:** el huésped solo accede a sus propias reservas y folios, verificado por propietario del recurso.
- **Correo:** no es editable.
- **Recuperación de contraseña:** código con vigencia de 15 minutos y un solo uso.
- **Eliminación lógica de usuarios:** no se permite si es titular de reservas `PENDIENTE`, `CONFIRMADA` o `EN_CURSO`; no elimina al titular ni sus reservas históricas.

**Integraciones y transversales**

- **Puertos:** el dominio no conoce proveedores; toda integración va detrás de un puerto, con implementación local alternativa.
- **Fallo de integración:** no puede impedir reservar, registrar, cobrar ni cerrar folios.
- **IA:** es no bloqueante; el sistema funciona igual con ella deshabilitada.
- **Reportes:**
  - Ocupación = noches vendidas ÷ (apartamentos activos × noches del periodo).
  - Ingresos = pagos registrados en el periodo, por medio de pago.
- **Paginación:** 10 resultados por página en todos los listados.
- **Configuración:** los valores de la sección 6 son configuración, no constantes en el código.

### 4.4 Reglas de la Ficha SANROCCO (valores concretos)

| Parámetro | Valor |
|---|---|
| Umbral de edad facturable | 5 años |
| Hora de entrada / salida | 2:00 PM / 11:00 AM |
| Tiempo de preparación | 3 h |
| Plazo de confirmación | 1 h |
| Hora límite no-show | 2 h |
| Anticipo | 50 % |
| Mascotas | Máx. 2, $70.000 de limpieza |
| Temporadas | Baja (base), Media (+15 %), Alta (+30 %) |
| Cancelación | ≥ 1 semana: devolución 100 %; < 1 semana: retención 100 % |
| Servicio adicional | Desayuno, $20.000 |
| Medios de pago | Transferencia, Efectivo, Tarjeta crédito/débito |
| Normas de convivencia | Sin fiestas ni eventos |

---

## 5. Puntos por aclarar en la Ficha

1. **Tarifas:** la tabla de tarifas por apartamento × temporada está vacía; es requisito del Corte 1.
2. **Consecuencia del no-show** y **comportamiento ante fallo de la integración:** están sin completar.
3. **Anticipo y plazo de confirmación:** la reserva solo se confirma con el 50 % pagado (RNP-03), pero el plazo de confirmación es de 1 hora. Conviene definir si ese pago debe llegar dentro de esa hora.
4. **Niños menores de 5 años:** el umbral indica que no generan cargo, pero la justificación habla de un cobro adicional si hay "muchos niños menores". Esa regla no está definida y no coincide con RN-05 y RN-06.
5. **Ventana de preparación:** entre las 11:00 y las 14:00 hay 3 h, igual al tiempo de preparación. Como no lo "excede", se permite entrada el mismo día de una salida. Conviene dejar explícito si el límite es inclusivo.
6. **Mínimo de noches en Temporada Alta (RNP-01):** el valor no está definido en la Ficha.
7. **Retención del anticipo:** la política define retención de 0 % o 100 %; habría que aclarar si el porcentaje se aplica sobre el anticipo o sobre el valor total.
