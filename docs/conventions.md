# Convenciones Congeladas

## 1. Objetivo

Definir reglas de naming y contratos para mantener consistencia entre backend, frontend, DTO y rutas REST sin cambiar la arquitectura base.

## 2. Convenciones backend (Java + Javalin)

### 2.1 Paquetes y carpetas

- Paquetes Java en minuscula: `com.vutron.backend.manager.coreproject`.
- Dominio en minuscula: `manager/coreproject`, `manager/appmessage`, `manager/health`.
- Capas por dominio con carpeta en PascalCase:
  - `Controller`
  - `DTO`
  - `Repository`
  - `Query`
  - `Services`

### 2.2 Clases

- Controllers: `<Domain>Controller`.
  - Ejemplo: `CoreProjectController`.
- Services: `<Domain>Service`.
  - Ejemplo: `CoreProjectService`.
- Repositories: `<Domain>Repository`.
  - Ejemplo: `CoreProjectRepository`.
- SQL container: `<Domain>Queries`.
  - Ejemplo: `CoreProjectQueries`.

### 2.3 Metodos

- Verbos explicitos por caso de uso:
  - `listActive`, `getById`, `create`, `updateById`, `softDelete`.
- Parseo de parametros en helpers privados del controller:
  - `parseCoreProjectId`.

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

- `CreateCoreProjectRequestDto`
- `CreateCoreProjectResponseDto`
- `GetCoreProjectByIdResponseDto`
- `UpdateCoreProjectRequestDto`
- `UpdateCoreProjectResponseDto`
- `ListCoreProjectResponseDto`

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

Ejemplo vigente para core-project:

- `GET /api/core-projects`
- `GET /api/core-project/{id}`
- `POST /api/core-project`
- `PUT /api/core-project/{id}`
- `DELETE /api/core-project/{id}`

Operaciones de I/O de proyecto:

- `POST /api/core-project/export`
- `POST /api/core-project/import`

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
- `locales/`: traducciones separadas por area funcional o vista.

### 5.2 Naming frontend

- Componentes Vue en PascalCase: `MainView.vue`, `ActionIconButton.vue`.
- Store runtime file: `<domain>.store.ts`.
- Store contract file: `<domain>.types.ts`.
- Barrel explicito de stores: `stores.exports.ts`.
- Evitar `index.ts` dentro de `stores/` y dentro de cada carpeta de store.
- Store id con prefijo `backend-` cuando consume API backend:
  - `backend-core-project`
  - `backend-app-message`
  - `backend-health`

Ejemplo recomendado:

- `renderer/stores/coreProject/coreProject.store.ts`
- `renderer/stores/coreProject/coreProject.types.ts`
- `renderer/stores/stores.exports.ts`

Regla adicional:

- Los imports deben apuntar al archivo explicito (`coreProject.store`, `coreProject.types`, `stores.exports`) y no a barrels genericos implicitos.

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

### 5.4.1 Estructura de locales

Regla:

- Las traducciones del renderer se organizan por carpeta funcional en `frontend/src/renderer/locales/`.
- Estructura actual recomendada:
  - `locales/Common/`
  - `locales/MainView/`
  - `locales/ErrorView/`
- Cada carpeta contiene archivos por idioma (`en.json`, `es.json`, etc.).
- `Common/` mantiene claves compartidas o legacy comunes; cada vista nueva debe tener su propia carpeta si concentra textos propios.

Objetivo:

- Evitar archivos monoliticos de textos.
- Permitir que cada vista o componente tenga mensajes dedicados y faciles de mantener.

### 5.5 Sistema visual global

Regla:

- La densidad visual base de la aplicacion se define en `frontend/src/renderer/styles/global.css`.
- Las vistas deben consumir primero clases y tokens globales antes de crear CSS local nuevo.
- El CSS local de cada vista se reserva para layout o acabado visual especifico del modulo.

Tokens base congelados:

- Espaciado:
  - `--app-space-1 = 0.25rem`
  - `--app-space-2 = 0.5rem`
  - `--app-space-3 = 0.75rem`
  - `--app-space-4 = 1rem`
  - `--app-space-5 = 1.25rem`
  - `--app-space-6 = 1.5rem`
  - `--app-space-8 = 2rem`
  - `--app-space-10 = 2.5rem`
- Tipografia:
  - `--app-text-xs = 0.72rem`
  - `--app-text-sm = 0.84rem`
  - `--app-text-md = 0.94rem`
  - `--app-text-lg = 1.15rem`
  - `--app-text-xl = clamp(2rem, 4vw, 3.25rem)`
- Radios:
  - `--app-radius-block = 16px`
  - `--app-radius-panel = 22px`

Clases globales recomendadas:

- Pagina y shell: `app-page`, `app-page__shell`, `app-page--centered`, `app-page--narrow`
- Layout: `app-grid-two-columns`, `app-stack`, `app-stack--compact`
- Paneles: `app-panel`
- Listados: `app-list`, `app-list--compact`, `app-list-item`, `app-list-item__main`, `app-list-item__title`, `app-list-item__meta`
- Cabeceras de seccion: `app-section-heading`
- Texto y acciones: `app-copy`, `app-copy--muted`, `app-action-row`

Regla de densidad:

- La referencia visual es una interfaz compacta, sobria y operativa, cercana a VS Code.
- Evitar bloques sobredimensionados, paddings excesivos y tipografia ornamental fuera de contexto.
- Si una vista necesita mas aire visual, debe justificarse por contenido, no por inercia del componente.

### 5.6 Compactacion global de PrimeVue

Regla:

- PrimeVue se configura globalmente en `frontend/src/renderer/plugins/primevue.ts`.
- Inputs, dropdowns, tags, chips, dialogos y botones deben respetar la escala compacta definida en `global.css`.
- La personalizacion global se aplica via selectores base y no debe duplicarse vista por vista salvo excepcion real.

### 5.7 Assets compartidos de frontend

Regla:

- Los assets visuales compartidos del frontend viven en `frontend/src/public/assets/`.
- Estructura base:
  - `frontend/src/public/assets/icons/`
  - `frontend/src/public/assets/images/`
- Los nombres canonicos de aplicacion deben ser estables para permitir reemplazo directo del archivo sin tocar codigo.

Nombres reservados actuales:

- `assets/icons/app-icon.png`
- `assets/icons/app-tray-icon.png`
- `assets/images/app-logo.webp`

Prefijos canonicos para nuevos assets compartidos:

- `empty-state-*` para estados vacios y placeholders funcionales.
- `tutorial-*` para walkthroughs, ayuda y onboarding.
- `module-*` para ilustraciones o banners ligados a modulos funcionales.
- `brand-*` para variantes de identidad visual comun.

Uso:

- Si se quiere cambiar el icono principal o el logo visual, se sustituye el contenido del archivo manteniendo nombre y dimensiones esperadas.
- Nuevos iconos o graficos compartidos deben agregarse aqui y no repartirse por vistas o modulos sin necesidad.
- El renderer debe consumir rutas canonicas desde una utilidad central (`frontend/src/renderer/utils/assets.ts`) en lugar de repetir strings literales de rutas.
- Estados vacios, errores, splash o onboarding deben priorizar `assets/icons/app-icon.png` o assets canonicos del prefijo correspondiente.

Alineacion con packaging:

- Los iconos de empaquetado viven en `frontend/buildAssets/icons/` y deben seguir la misma logica de nombres estables.
- Nombres canonicos de packaging:
  - `buildAssets/icons/app-icon.png`
  - `buildAssets/icons/app-icon.ico`
  - `buildAssets/icons/app-icon.icns`
- Si cambia el branding, se reemplazan estos archivos y los equivalentes de runtime sin introducir nuevos nombres arbitrarios.

Configuracion compartida:

- El nombre visible de la aplicacion, el titulo de ventana y las rutas canonicas de branding compartido se definen en `frontend/src/shared/branding.ts`.
- Electron main y renderer deben consumir ese modulo compartido en lugar de repetir strings de titulo, tooltip o nombres de assets.

## 6. Convenciones de tipos y fechas

- Frontend y backend deben usar nombres semanticos (`createdAt`, `updatedAt`).
- Fechas en formato ISO-8601 en respuestas JSON.
- IDs numericos como `number` en frontend y `Long` en backend.

## 7. Convenciones de base de datos (SQLite)

### 7.1 Alcance y genericidad

- Todas las tablas funcionales cuelgan de `core_project_id` cuando dependen del nucleo reutilizable `coreProject`.
- El modelo debe servir para evaluar cualquier dominio (software, bienes, servicios, etc.).
- No se crean tablas o columnas especificas para un sector concreto.
- Los nombres fisicos de tablas SQL se escriben siempre en singular.
- Las tablas base reutilizables del template usan prefijo logico `core` y el ejemplo vigente es `coreProject`.

Ejemplos:

- `coreProject`
- `vendor`
- `product`
- `response`

### 7.2 Relaciones base

- `coreProject` 1:N `vendor`.
- `vendor` 1:N `product`.
- `coreProject` 1:N `question`.
- `coreProject` 1:N tablas de variables configurables.
- `response` referencia proyecto, vendor, product, question y opcion respondida.

### 7.3 Variables configurables por proyecto

Modelo recomendado:

- `project_variable_type`:
  - tipos de variable (CATEGORY, QUESTION_TYPE, REQUIREMENT_LEVEL, etc.).
- `project_variable_option`:
  - opciones por tipo y por proyecto.

Regla:

- `question.category`, `question.question_type` y `question.requirement_level` deben venir de opciones del core project, no de enums fijos de codigo.

### 7.4 Preguntas y opciones de respuesta

- `question` mantiene campos de negocio: `question_code`, `title`, `question_type`, `requirement_level`, `category`, `max_score`, `is_required`, `is_active`, `sort_order`.
- Cuando se requiera lista cerrada puntuable (antes `StringResponse`), se recomienda normalizar en tabla de opciones de respuesta por pregunta.
- Si existe columna legacy tipo `StringResponse` (ejemplo `No:0|Parcial:2.5|Si:5`), debe planearse migracion a estructura relacional.

### 7.5 Reglas para response

- Una respuesta corresponde a una combinacion de `coreProject + vendor + product + question`.
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
