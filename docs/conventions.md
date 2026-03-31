# Convenciones Congeladas

## 1. Objetivo

Definir reglas de naming y contratos para mantener consistencia entre backend, frontend, DTO y rutas REST sin cambiar la arquitectura base.

## 2. Convenciones backend (Java + Javalin)

### 2.1 Paquetes y carpetas

- Paquetes Java en minuscula: `com.vutron.backend.manager.project`.
- Dominio en minuscula: `manager/project`, `manager/appmessage`, `manager/health`.
- Capas por dominio con carpeta en PascalCase:
  - `Controller`
  - `DTO`
  - `Repository`
  - `Query`
  - `Services`

### 2.2 Clases

- Controllers: `<Domain>Controller`.
  - Ejemplo: `ProjectController`.
- Services: `<Domain>Service`.
  - Ejemplo: `ProjectService`.
- Repositories: `<Domain>Repository`.
  - Ejemplo: `ProjectRepository`.
- SQL container: `<Domain>Queries`.
  - Ejemplo: `ProjectQueries`.

### 2.3 Metodos

- Verbos explicitos por caso de uso:
  - `listActive`, `getById`, `create`, `updateById`, `softDelete`.
- Parseo de parametros en helpers privados del controller:
  - `parseProjectId`.

### 2.4 Estructura de responsabilidad

- `Controller`: parseo de request + codigos HTTP.
- `Services`: reglas de negocio.
- `Repository`: acceso a datos JDBC.
- `Query`: SQL.

Regla: `Controller` no ejecuta SQL y `Repository` no define reglas funcionales.

## 3. Convenciones DTO

### 3.1 Nombres de archivo/clase

Patron congelado:

- Request: `<Action><Domain>RequestDto`
- Response: `<Action><Domain>ResponseDto`

Ejemplos reales:

- `CreateProjectRequestDto`
- `CreateProjectResponseDto`
- `GetProjectByIdResponseDto`
- `UpdateProjectRequestDto`
- `UpdateProjectResponseDto`
- `ListProjectResponseDto`

### 3.2 Reglas de diseno

- DTO de request solo contiene campos de entrada del endpoint.
- DTO de response expone solo contrato publico (no entidades JDBC internas).
- Un DTO por accion cuando cambian campos o semantica.
- No reutilizar DTO para entrada y salida si los contratos difieren.

## 4. Convenciones REST

## 4.1 Base URL

- API local: `http://127.0.0.1:7007`
- Todas las rutas funcionales bajo prefijo `/api` (salvo `health`).

## 4.2 Patron de rutas (congelado segun codigo actual)

Para cada recurso:

- Listado: `GET /api/<recurso-plural>`
- Obtener por id: `GET /api/<recurso-singular>/{id}`
- Crear: `POST /api/<recurso-singular>`
- Actualizar: `PUT /api/<recurso-singular>/{id}`
- Eliminar: `DELETE /api/<recurso-singular>/{id}`

Ejemplo vigente para project:

- `GET /api/projects`
- `GET /api/project/{id}`
- `POST /api/project`
- `PUT /api/project/{id}`
- `DELETE /api/project/{id}`

Operaciones de I/O de proyecto:

- `POST /api/project/export`
- `POST /api/project/import`

Mensajes de aplicacion:

- `GET /api/app-message/hello-world`

Salud:

- `GET /health`

### 4.3 Reglas de estilo en rutas

- Segmentos en kebab-case cuando aplique (`app-message`).
- IDs en path param: `{id}`.
- Evitar verbos en la ruta CRUD basica.
- Excepciones permitidas para acciones de I/O (`/export`, `/import`).

### 4.4 Codigos HTTP

- `200`: lectura/actualizacion exitosa con payload.
- `201`: creacion exitosa.
- `204`: borrado logico/fisico exitoso sin body.
- `400`: request invalida (parseo/validacion).
- `404`: entidad no encontrada.
- `500`: error no controlado.

## 5. Convenciones frontend (Vue 3 + PrimeVue + Pinia)

### 5.1 Estructura

Ruta base: `frontend/src/renderer/`

- `views/`: pantallas por modulo.
- `components/`: piezas reutilizables.
- `stores/`: acceso a API y estado.
- `router/`: rutas de UI.
- `plugins/`: wiring (i18n, pinia, primevue, etc.).

### 5.2 Naming frontend

- Componentes Vue en PascalCase: `MainView.vue`, `ActionIconButton.vue`.
- Store runtime file: `<domain>.store.ts`.
- Store contract file: `<domain>.types.ts`.
- Barrel explicito de stores: `stores.exports.ts`.
- Evitar `index.ts` dentro de `stores/` y dentro de cada carpeta de store.
- Store id con prefijo `backend-` cuando consume API backend:
  - `backend-project`
  - `backend-app-message`
  - `backend-health`

Ejemplo recomendado:

- `renderer/stores/project/project.store.ts`
- `renderer/stores/project/project.types.ts`
- `renderer/stores/stores.exports.ts`

Regla adicional:

- Los imports deben apuntar al archivo explicito (`project.store`, `project.types`, `stores.exports`) y no a barrels genericos implicitos.

### 5.3 Orden de archivo `.vue`

Orden obligatorio:

1. `template`
2. `script setup lang="ts"`
3. `style` (si aplica)

### 5.4 Regla de responsabilidad

- Vista/componente: render y eventos.
- Store: llamada HTTP, manejo de errores API, tipado de payload/response.
- Nunca acceso SQLite desde frontend.
- Nunca logica de negocio de scoring en Vue.

## 6. Convenciones de tipos y fechas

- Frontend y backend deben usar nombres semanticos (`createdAt`, `updatedAt`).
- Fechas en formato ISO-8601 en respuestas JSON.
- IDs numericos como `number` en frontend y `Long` en backend.

## 7. Convenciones de base de datos (SQLite)

### 7.1 Alcance y genericidad

- Todas las tablas funcionales cuelgan de `project_id`.
- El modelo debe servir para evaluar cualquier dominio (software, bienes, servicios, etc.).
- No se crean tablas o columnas especificas para un sector concreto.
- Los nombres fisicos de tablas SQL se escriben siempre en singular.

Ejemplos:

- `project`
- `vendor`
- `product`
- `response`

### 7.2 Relaciones base

- `project` 1:N `vendor`.
- `vendor` 1:N `product`.
- `project` 1:N `question`.
- `project` 1:N tablas de variables configurables.
- `response` referencia proyecto, vendor, product, question y opcion respondida.

### 7.3 Variables configurables por proyecto

Modelo recomendado:

- `project_variable_type`:
  - tipos de variable (CATEGORY, QUESTION_TYPE, REQUIREMENT_LEVEL, etc.).
- `project_variable_option`:
  - opciones por tipo y por proyecto.

Regla:

- `question.category`, `question.question_type` y `question.requirement_level` deben venir de opciones del proyecto, no de enums fijos de codigo.

### 7.4 Preguntas y opciones de respuesta

- `question` mantiene campos de negocio: `question_code`, `title`, `question_type`, `requirement_level`, `category`, `max_score`, `is_required`, `is_active`, `sort_order`.
- Cuando se requiera lista cerrada puntuable (antes `StringResponse`), se recomienda normalizar en tabla de opciones de respuesta por pregunta.
- Si existe columna legacy tipo `StringResponse` (ejemplo `No:0|Parcial:2.5|Si:5`), debe planearse migracion a estructura relacional.

### 7.5 Reglas para response

- Una respuesta corresponde a una combinacion de `project + vendor + product + question`.
- Se permite ausencia de respuesta (no todos los productos responden todas las preguntas).
- Campos minimos esperados: `response_option_code`, `resolved_score`, `comment`, `responded_at_utc`.
- Definir indice unico para evitar duplicados por combinacion funcional.

### 7.6 Fuera de alcance actual

- `evaluator` no se considera en esta etapa y no bloquea el modelo principal.

## 8. Checklist para nueva feature

1. Crear dominio backend con capas `Controller/DTO/Repository/Query/Services`.
2. Registrar endpoints nuevos en `App.java`.
3. Crear o extender store `useXStore.ts` con `endpoints` centralizados.
4. Consumir store desde vistas/componentes (sin SQL directo).
5. Mantener naming y codigos HTTP de este documento.
