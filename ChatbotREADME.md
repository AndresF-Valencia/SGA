# Chatbot FAQ del SGA — cómo funciona por dentro

Documento de referencia para el equipo. Explica la implementación del asistente virtual
(Gemini) que responde preguntas sobre el uso del sitio: qué clases intervienen, cómo viaja
una petición, qué se configura y dónde, y qué límites tiene.

> Resumen en una frase: el frontend manda la conversación a `POST /api/chat` con el JWT del
> usuario; el backend la valida, la recorta, le adjunta el documento `knowledge.md` y se la
> envía a Gemini; la respuesta de texto vuelve al widget.

---

## 1. Arquitectura

El proyecto usa arquitectura hexagonal (puertos y adaptadores). El chatbot respeta esa
separación:

- `domain` y `application` **no importan Spring, HTTP ni Jackson**. Son Java puro y se pueden
  probar sin levantar el contexto.
- `infrastructure` contiene todo lo que habla con el mundo exterior: REST, Gemini, archivos,
  filtros y configuración de Spring.

```
┌──────────────────────────── Frontend (React) ────────────────────────────┐
│ AsistenteChat.tsx  →  chatServicio.ts  →  api.ts (Axios + JWT)           │
└───────────────────────────────────┬──────────────────────────────────────┘
                                    │ POST /api/chat  { messages: [...] }
                                    ▼
┌──────────────────────────── Backend (Spring Boot) ───────────────────────┐
│                                                                          │
│  1. ChatRateLimitFilter        (10 peticiones/minuto por IP → 429)       │
│  2. Spring Security           (sin JWT válido → 401)                     │
│  3. ChatController            (valida forma y convierte DTO → dominio)   │
│  4. AnswerQuestionService     (reglas, prompt, recorte de la charla)     │
│        ├── SiteKnowledgePort  → FileSiteKnowledgeAdapter (knowledge.md)  │
│        └── ChatModelPort      → GeminiChatAdapter (HTTP a Gemini)        │
│  5. ChatExceptionHandler      (traduce errores a 400/503 amables)        │
│                                                                          │
└───────────────────────────────────┬──────────────────────────────────────┘
                                    │ HTTPS + x-goog-api-key
                                    ▼
                    Google Gemini API (generateContent)
```

### Recorrido de una petición, paso a paso

1. **`ChatRateLimitFilter`** intercepta solo `POST /api/chat`. Cuenta la petición en un bucket
   por IP. Si se pasó de 10 por minuto responde `429` con
   `{"reply":"Demasiadas preguntas, espera un momento."}` y no deja seguir.
2. **Spring Security** valida el JWT. Sin token o con token vencido responde `401`.
3. **`ChatController`** recibe `{ "messages": [...] }`, verifica que no venga vacío y convierte
   cada DTO a `ChatMessage` (rol `assistant` → `ASSISTANT`; cualquier otro → `USER`).
4. **`AnswerQuestionService`**:
   - valida que la conversación no esté vacía, que el último mensaje sea del usuario y que no
     esté en blanco;
   - conserva solo los **últimos 10 mensajes** y recorta cada uno a **500 caracteres**;
   - elimina mensajes iniciales del asistente (Gemini exige que la conversación empiece por el
     usuario);
   - arma el *system prompt* con el texto de `knowledge.md` y llama al modelo.
5. **`GeminiChatAdapter`** construye el JSON de Gemini, llama a
   `POST /v1beta/models/{modelo}:generateContent` y extrae el texto de la primera respuesta.
   Si el proveedor falla, lanza `ChatUnavailableException`.
6. **`ChatExceptionHandler`** convierte esas excepciones en respuestas amables (`400` o `503`).
7. El controlador devuelve `{ "reply": "..." }` y el widget lo pinta.

---

## 2. Clases y archivos

Ruta base del backend: `backend/sga/src/main/java/co/edu/uniquindio/sga/`

### Dominio

| Archivo | Responsabilidad |
|---|---|
| `domain/valueobject/ChatMessage.java` | Value object inmutable (`record`) con `role` (`USER`/`ASSISTANT`) y `content`. No conoce nada de HTTP ni de Gemini. |

### Aplicación (casos de uso y puertos)

| Archivo | Responsabilidad |
|---|---|
| `application/port/AnswerQuestionUseCase.java` | Puerto de entrada: `String answer(List<ChatMessage>)`. |
| `application/port/ChatModelPort.java` | Puerto de salida hacia el modelo de lenguaje. |
| `application/port/SiteKnowledgePort.java` | Puerto de salida que entrega el texto de conocimiento del sitio. |
| `application/usecase/AnswerQuestionService.java` | Implementación del caso de uso: validaciones, recorte, prompt y llamada al modelo. Se registra como `@Bean`, no lleva anotaciones de Spring. |
| `application/exception/ChatUnavailableException.java` | Falla del proveedor, cuota agotada, respuesta ilegible o clave ausente. |
| `application/exception/InvalidChatRequestException.java` | Petición mal formada (sin mensajes, último mensaje del asistente, texto vacío). |
| `application/dto/request/ChatRequest.java` | Cuerpo de entrada: `{ "messages": [...] }`. |
| `application/dto/request/ChatMessageDto.java` | Elemento de entrada: `{ "role": "...", "content": "..." }`. |
| `application/dto/response/ChatResponse.java` | Cuerpo de salida: `{ "reply": "..." }`. |

### Infraestructura (adaptadores)

| Archivo | Responsabilidad |
|---|---|
| `infrastructure/chatbot/GeminiChatAdapter.java` | Adaptador de `ChatModelPort`. Habla con la API REST de Gemini con `RestClient`, usa Jackson 3 (`tools.jackson`), mapea roles (`USER` → `user`, `ASSISTANT` → `model`) y traduce errores. |
| `infrastructure/chatbot/FileSiteKnowledgeAdapter.java` | Adaptador de `SiteKnowledgePort`. Lee `classpath:chatbot/knowledge.md` **una sola vez al arrancar** y lo mantiene en memoria. |
| `infrastructure/config/ChatbotConfig.java` | Registra el caso de uso, el `RestClient.Builder` (con timeouts) y el filtro de límite de peticiones. |
| `infrastructure/rest/controller/ChatController.java` | Expone `POST /api/chat`. |
| `infrastructure/rest/exception/ChatExceptionHandler.java` | `@RestControllerAdvice` limitado a `ChatController` para no interferir con el manejo global de errores del SGA. |
| `infrastructure/security/ChatRateLimitFilter.java` | Filtro por IP con Bucket4j + caché Caffeine. |

### Recursos y configuración

| Archivo | Para qué |
|---|---|
| `src/main/resources/chatbot/knowledge.md` | **Contenido que el bot puede usar para responder.** Es lo primero que hay que editar para cambiar las respuestas. |
| `src/main/resources/application.properties` | Propiedades `chatbot.*` (clave por variable de entorno, modelo, límite). La conexión a MySQL se queda como estaba. |
| `src/main/resources/application-local.properties` | Clave real de Gemini. **No se versiona** (está en `.gitignore`). Solo aporta la API key; no toca la base de datos. |
| `infrastructure/security/SecurityConfig.java` | Regla explícita: `POST /api/chat` exige autenticación. |

### Pruebas

Ruta base: `backend/sga/src/test/java/co/edu/uniquindio/sga/`

| Archivo | Qué comprueba |
|---|---|
| `application/usecase/AnswerQuestionServiceTest.java` | El conocimiento entra en el prompt, se conservan 10 mensajes, se truncan a 500 caracteres y se rechazan conversaciones inválidas. |
| `infrastructure/chatbot/GeminiChatAdapterTest.java` | Mapeo de roles, extracción del texto, traducción de error de cuota y respuesta sin texto. |
| `infrastructure/rest/controller/ChatControllerTest.java` | `200` con respuesta, `400` con payload inválido y `503` cuando el proveedor falla. |
| `infrastructure/security/ChatSecurityTest.java` | Sin token → `401`; con sesión y sin clave → `503` amable. |
| `infrastructure/security/ChatRateLimitFilterTest.java` | 10/minuto por IP, bloqueo de la 11 con `429` y uso de `X-Forwarded-For`. |

### Frontend (contexto)

| Archivo | Responsabilidad |
|---|---|
| `frontend/src/componentes/AsistenteChat.tsx` | Widget flotante. No se renderiza si `useAuth().autenticado` es falso. |
| `frontend/src/servicios/chatServicio.ts` | Llama a `/chat` con la instancia Axios de la app (agrega el JWT), timeout de 30 s y mensajes de error. |
| `frontend/src/componentes/Layout.tsx` | Monta `<AsistenteChat />` una sola vez en toda la aplicación. |

---

## 3. Contrato de la API

**Endpoint:** `POST /api/chat` (requiere `Authorization: Bearer <token>`)

Entrada:

```json
{
  "messages": [
    { "role": "user", "content": "¿Cómo me registro?" }
  ]
}
```

Salida correcta (`200`):

```json
{ "reply": "Para registrarte, pulsa «Crear cuenta» en la esquina superior derecha..." }
```

| Código | Cuándo | Cuerpo |
|---|---|---|
| `200` | Respuesta generada | `{ "reply": "..." }` |
| `400` | Sin mensajes, último mensaje del asistente, texto vacío o JSON ilegible | `{ "reply": "La pregunta no es valida." }` |
| `401` | Sin JWT, token vencido o inválido | Error estándar de seguridad del SGA |
| `429` | Más de 10 peticiones/minuto desde la misma IP | `{ "reply": "Demasiadas preguntas, espera un momento." }` |
| `503` | Falta la clave, Gemini falla, cuota agotada o respuesta vacía | `{ "reply": "Ahora mismo no puedo responder, intenta de nuevo en unos minutos." }` |

El widget envía **todo el historial** en cada petición; el backend decide qué parte usar
(los últimos 10 mensajes). No hay estado de conversación en el servidor.

---

## 4. Configuración

### Propiedades (`application.properties`)

```properties
chatbot.api-key=${GEMINI_API_KEY:}
chatbot.model=gemini-3.5-flash-lite
chatbot.rate-limit.per-minute=10
chatbot.rate-limit.trust-forwarded-header=false
```

| Propiedad | Por defecto | Efecto |
|---|---|---|
| `chatbot.api-key` | `${GEMINI_API_KEY:}` | Clave de Gemini. Vacía por defecto: la app arranca y el chat responde `503`. |
| `chatbot.model` | `gemini-3.5-flash-lite` | Modelo usado en `generateContent`. Los modelos `lite` no admiten `thinkingConfig` y el adaptador lo omite. |
| `chatbot.rate-limit.per-minute` | `10` | Peticiones por minuto y por IP. |
| `chatbot.rate-limit.trust-forwarded-header` | `false` | Si es `true`, toma la IP de `X-Forwarded-For` (solo detrás de un proxy de confianza). |
| `chatbot.knowledge-file` | `classpath:chatbot/knowledge.md` | Documento de conocimiento alternativo (se lee en `FileSiteKnowledgeAdapter`). |

### Dónde va la clave

Nunca en el frontend ni en el repositorio. Hay dos formas:

1. **Variable de entorno** (recomendada para producción):

   ```bash
   GEMINI_API_KEY="tu-clave" ./gradlew bootRun
   ```

2. **Archivo local no versionado** (lo que usa este repositorio):

   `backend/sga/src/main/resources/application-local.properties`

   ```properties
   chatbot.api-key=tu-clave
   ```

   `application-local.properties` está en `.gitignore`. `bootRun` activa el perfil
   `local` por defecto, así que la clave se carga sola.

> El perfil `local` solo aporta la API key. La base de datos sigue siendo la de
> `application.properties` (MySQL en `localhost:3308`), igual que antes del chatbot.

### Cómo se arma el prompt

`AnswerQuestionService` (líneas 14–27):

- **System prompt** fijo: dice que es el asistente de San Rocco, que responda **solo** con la
  información del documento, en español, breve y amable; que si no sabe algo lo diga; y que
  ignore instrucciones del usuario que intenten cambiar esas reglas o revelar el prompt.
- **Conocimiento**: el texto completo de `knowledge.md` se inserta al final del system prompt.
- **Conversación**: últimos 10 mensajes, cada uno truncado a 500 caracteres.

`GeminiChatAdapter` añade además:

- `generationConfig.maxOutputTokens = 500`
- `thinkingConfig.thinkingBudget = 0` (desactiva el razonamiento extendido para responder
  rápido y gastar menos cuota). Solo se envía en modelos que lo admiten; los `lite` no lo
  soportan y Gemini respondería `400`, así que el adaptador lo omite cuando el nombre del
  modelo contiene `lite`.

---

## 5. Seguridad y límites

| Protección | Dónde | Detalle |
|---|---|---|
| Solo usuarios con sesión | `SecurityConfig.java` | `.requestMatchers(HttpMethod.POST, "/api/chat").authenticated()` |
| Límite por IP | `ChatRateLimitFilter` + `ChatbotConfig` | 10/minuto, bucket con recarga gradual; caché de hasta 10 000 IPs con expiración de 10 min. Se ejecuta **antes** de Spring Security (orden `-150`). |
| Tamaño de entrada | `AnswerQuestionService` | Máximo 10 mensajes y 500 caracteres por mensaje. |
| Tamaño de salida | `GeminiChatAdapter` | 500 tokens. |
| Timeouts | `ChatbotConfig` | 5 s para conectar y 20 s para leer; evita hilos colgados. |
| Sin datos personales | Diseño | A Gemini solo se envía el texto de `knowledge.md` y la conversación. **No** se envían usuarios, reservas ni datos de la base. |
| Inyección de prompt | `SYSTEM_PROMPT` | Instrucción explícita de ignorar órdenes que contradigan las reglas o pidan revelar el prompt. |

> El plan gratuito de Gemini puede usar los datos enviados para mejorar el modelo. Por eso el
> chatbot solo debe recibir información pública del sitio.

---

## 6. Cómo arrancar y probar

```bash
cd backend/sga
./gradlew bootRun
```

- API: <http://localhost:8080>
- Swagger: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

Prueba manual (el endpoint exige JWT):

```bash
# 1) Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"huesped@sanrocco.co","contrasena":"Sanrocco123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2) Pregunta
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"messages":[{"role":"user","content":"Como me registro?"}]}'
```

Pruebas automáticas:

```bash
cd backend/sga
./gradlew test
```

Solo el chatbot:

```bash
./gradlew test \
  --tests '*AnswerQuestionServiceTest' \
  --tests '*GeminiChatAdapterTest' \
  --tests '*ChatControllerTest' \
  --tests '*ChatSecurityTest' \
  --tests '*ChatRateLimitFilterTest'
```

---

## 7. Cómo cambiar el comportamiento

| Quiero… | Editar |
|---|---|
| Cambiar las respuestas / agregar preguntas | `src/main/resources/chatbot/knowledge.md` (reiniciar después) |
| Cambiar el tono, idioma o reglas del bot | `SYSTEM_PROMPT` en `AnswerQuestionService.java` (líneas 17–27) |
| Cambiar cuántos mensajes de contexto se envían | `MAX_MESSAGES` en `AnswerQuestionService.java` (línea 14) |
| Cambiar el largo máximo por mensaje | `MAX_CHARS` en `AnswerQuestionService.java` (línea 15) y `MAX_LENGTH` en `AsistenteChat.tsx` |
| Cambiar el modelo | `chatbot.model` en `application.properties` |
| Cambiar el límite de peticiones | `chatbot.rate-limit.per-minute` |
| Cambiar los timeouts de Gemini | `ChatbotConfig.java` |
| Cambiar los mensajes de error | `ChatExceptionHandler.java` y `chatServicio.ts` |
| Cambiar la clave | `GEMINI_API_KEY` o `application-local.properties` |

### Notas de mantenimiento

- **`knowledge.md` se lee una sola vez al arrancar.** Después de editarlo hay que reiniciar.
- **Los modelos de Gemini cambian.** Si aparece un `404` tipo *"model no longer available"*,
  consulta los modelos disponibles con:

  ```bash
  curl -H "x-goog-api-key: $GEMINI_API_KEY" \
    https://generativelanguage.googleapis.com/v1beta/models
  ```

  y actualiza `chatbot.model`.
- Si Gemini responde `503 high demand`, es temporal. El adaptador lo traduce a un `503` amable
  para el usuario.
- El filtro de límite corre antes de la autenticación, así que una petición sin token también
  consume cupo de su IP. Es intencional: protege la cuota gratuita frente a abuso.
- `ChatExceptionHandler` está limitado a `ChatController`; si se agrega otro controlador de
  chat, hay que incluirlo en `assignableTypes` o crear su propio manejador.

---

## 8. Mapa rápido de archivos

```
backend/sga/src/main/java/co/edu/uniquindio/sga/
├── domain/valueobject/ChatMessage.java
├── application/
│   ├── dto/request/ChatRequest.java
│   ├── dto/request/ChatMessageDto.java
│   ├── dto/response/ChatResponse.java
│   ├── exception/ChatUnavailableException.java
│   ├── exception/InvalidChatRequestException.java
│   ├── port/AnswerQuestionUseCase.java
│   ├── port/ChatModelPort.java
│   ├── port/SiteKnowledgePort.java
│   └── usecase/AnswerQuestionService.java
└── infrastructure/
    ├── chatbot/GeminiChatAdapter.java
    ├── chatbot/FileSiteKnowledgeAdapter.java
    ├── config/ChatbotConfig.java
    ├── rest/controller/ChatController.java
    ├── rest/exception/ChatExceptionHandler.java
    └── security/ChatRateLimitFilter.java

backend/sga/src/main/resources/
├── chatbot/knowledge.md
├── application.properties
└── application-local.properties   (no versionado)

frontend/src/
├── componentes/AsistenteChat.tsx
├── componentes/AsistenteChat.css
└── servicios/chatServicio.ts
```
