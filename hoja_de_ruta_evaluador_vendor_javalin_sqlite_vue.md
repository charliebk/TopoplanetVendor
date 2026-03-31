# Hoja de ruta final por sprints — Evaluador de vendors standalone

**Stack:** Electron + Vue 3 + PrimeVue + TypeScript + Javalin + SQLite  
**Objetivo:** sustituir completamente el evaluador basado en Excel por una aplicación standalone ejecutable, multi-proyecto, relacional, configurable y mantenible.

---

## 1. Punto de partida y decisión correcta

Este proyecto ya no es un Excel mejorado. Ahora es una aplicación relacional con estas reglas duras:

1. El frontend no toca SQLite.
2. Electron main/preload no contiene lógica de negocio.
3. La lógica funcional vive en backend Javalin.
4. SQLite se toca solo desde Repository.
5. Los endpoints se registran explícitamente en `backend/src/main/java/com/vutron/backend/App.java`.
6. El backend sigue estructura por dominio y capas: `Controller`, `DTO`, `Repository`, `Query`, `Services`.
7. En frontend se respetan `views`, `components`, `stores`, `plugins`, y los `.vue` deben seguir orden `template -> script setup lang="ts" -> style`.
8. PrimeVue ya está instalado y debe aprovecharse como librería base de UI.

---

## 2. Traducción del evaluador Excel al nuevo modelo

La idea buena del evaluador anterior sí se conserva: no modelar vendors como bloques de columnas, sino trabajar con estructura larga y escalable.

Eso, en SQLite, se traduce en:

- un proyecto contiene su configuración y sus datos;
- las preguntas son filas;
- los vendors son filas;
- las respuestas son filas;
- los productos son filas;
- las variables por proyecto son filas;
- las opciones de respuesta por pregunta son filas;
- las métricas se calculan por SQL + servicio de agregación.

---

## 3. Objetivo funcional final

La aplicación debe permitir:

- gestionar múltiples proyectos desde una pantalla inicial tipo VS Code;
- crear, abrir, duplicar, archivar e importar proyectos;
- configurar por proyecto variables genéricas (como category, questionType y requirementLevel), preguntas, escalas, pesos, vendors, productos y textos multiidioma;
- capturar respuestas por vendor;
- soportar escenarios con respuestas faltantes y respuestas no aplicables sin romper el cálculo de métricas;
- calcular métricas: frecuencia, media, media ponderada, desviación estándar, cobertura, score total y ranking;
- mostrar dashboards por proyecto;
- versionar cambios de configuración;
- exportar resultados;
- dejar trazabilidad clara de qué cambió, cuándo y en qué proyecto.

---

## 4. Pantallas objetivo

### 4.1 Pantalla 1 — Project Hub

Pantalla inicial tipo launcher, similar al arranque de VS Code.

### 4.2 Pantalla 2 — Workspace del proyecto

Cuando se abre un proyecto, se entra en un workspace con navegación lateral.

### 4.3 Módulos visibles del workspace

- Dashboard
- Resumen Ejecutivo
- Vendors
- Productos
- Preguntas
- Requisitos Funcionales
- Requisitos Técnicos
- Respuestas
- Pesos y Escalas
- Variables
- Versiones
- Importación / Exportación
- Auditoría

### 4.4 Componentes PrimeVue que sí debemos usar

- `DataTable`
- `TreeTable`
- `Dialog`
- `Drawer`
- `Sidebar`
- `Splitter`
- `TabView`
- `Accordion`
- `Panel`
- `Toolbar`
- `Menubar`
- `Breadcrumb`
- `InputText`
- `InputNumber`
- `Dropdown`
- `MultiSelect`
- `Checkbox`
- `RadioButton`
- `Textarea`
- `Calendar`
- `Tag`
- `Badge`
- `Toast`
- `ConfirmDialog`
- `Chart`

---

## 5. Modelo relacional objetivo en SQLite

### 5.1 Tabla `project`

- `id`
- `code`
- `name`
- `description`
- `status`
- `created_at`
- `updated_at`
- `archived_at`

### 5.2 Tabla `project_version`

- `id`
- `project_id`
- `version_number`
- `label`
- `change_summary`
- `created_at`
- `created_by`
- `is_active`

### 5.3 Tabla `vendor`

- `id`
- `project_id`
- `code`
- `name`
- `vendor_group`
- `notes`
- `is_active`

### 5.4 Tabla `product`

- `id`
- `project_id`
- `vendor_id`
- `code`
- `name`
- `product_type`
- `product_module`
- `notes`
- `is_active`

### 5.5 Tabla `project_variable_type`

- `id`
- `project_id`
- `code`
- `name`
- `description`
- `is_active`

### 5.6 Tabla `project_variable_option`

- `id`
- `project_id`
- `variable_type_id`
- `code`
- `name`
- `score_value`
- `sort_order`
- `notes`
- `is_active`

### 5.7 Tabla `question`

- `id`
- `project_id`
- `question_code`
- `title`
- `question_type_option_id`
- `requirement_level_option_id`
- `category_option_id`
- `max_score`
- `is_required`
- `is_active`
- `sort_order`
- `string_response_legacy`

### 5.8 Tabla `question_response_option`

- `id`
- `question_id`
- `code`
- `label`
- `score_value`
- `sort_order`
- `is_not_applicable`
- `is_active`

### 5.9 Tabla `response`

- `id`
- `project_id`
- `vendor_id`
- `product_id`
- `question_id`
- `response_option_id`
- `response_option_code`
- `resolved_score`
- `response_status` (ANSWERED, NO_RESPONSE, NOT_APPLICABLE)
- `comment`
- `responded_at_utc`

### 5.10 Tabla `scale`

- `id`
- `project_id`
- `code`
- `name`
- `description`
- `is_active`

### 5.11 Tabla `scale_option`

- `id`
- `scale_id`
- `code`
- `label`
- `numeric_score`
- `color_token`
- `sort_order`
- `is_default`

### 5.12 Tabla `question_weight`

- `id`
- `project_id`
- `question_id`
- `weight`
- `justification`

### 5.13 Tabla `category_weight`

- `id`
- `project_id`
- `category_option_id`
- `weight`

### 5.14 Tabla `localized_text`

- `id`
- `project_id`
- `text_key`
- `lang_code`
- `text_value`

### 5.15 Tabla `audit_log`

- `id`
- `project_id`
- `entity_name`
- `entity_id`
- `action_type`
- `old_value_json`
- `new_value_json`
- `created_at`
- `created_by`

### 5.16 Validación de validez del modelo genérico

Sí, esta estructura es válida para un evaluador de métricas genérico si se respetan estas reglas:

1. Toda clasificación funcional debe salir de `project_variable_type` + `project_variable_option` (sin enums fijos en código).
2. `question` nunca almacena taxonomías hardcodeadas, solo referencias a opciones del proyecto.
3. `response` debe soportar explícitamente ausencia de respuesta y no aplicabilidad.
4. Las métricas se calculan en backend/SQL a partir de `resolved_score`, pesos y estado de respuesta.
5. El frontend solo consume contratos API y nunca interpreta fórmulas complejas.

Límites y extensibilidad para "todas las posibilidades":

- Si en el futuro se necesitan fórmulas de scoring configurables por proyecto, añadir `metric_definition` y `metric_formula`.
- Si se necesita congelar resultados por versión, añadir `metric_snapshot` versionado por `project_version`.
- Si vuelve el rol evaluador, incorporar dimensión adicional sin romper el núcleo actual.

### 5.17 Tablas o vistas de agregación

No persistiría todo desde el minuto 1.

Mi recomendación:

- tablas base normalizadas
- views SQL para agregados
- servicio Java para snapshots si luego hace falta performance

Vistas:

- `vw_vendor_score_summary`
- `vw_vendor_category_score`
- `vw_vendor_question_score`
- `vw_project_dashboard_summary`

---

## 6. Índices obligatorios

- `idx_question_project`
- `idx_question_type_option`
- `idx_question_category_option`
- `idx_question_requirement_option`
- `idx_vendor_project`
- `idx_product_vendor`
- `idx_response_project`
- `idx_response_vendor`
- `idx_response_product`
- `idx_response_question`
- `idx_response_status`
- `idx_response_vendor_product_question`
- `idx_scale_option_scale`
- `idx_audit_project_entity`
- `idx_var_type_project`
- `idx_var_option_type`

Y además un índice compuesto serio en `response`:

- `(project_id, vendor_id, product_id, question_id)`

Y un índice único recomendado para evitar duplicados funcionales:

- `UNIQUE(project_id, vendor_id, product_id, question_id)`

---

## 7. Arquitectura backend objetivo

Base conservada del template:

```text
backend/src/main/java/com/vutron/backend/
  App.java
  controller/
  db/
  manager/
  io/
```

Nueva expansión por dominios:

```text
manager/
  project/
  projectversion/
  question/
  scale/
  scaleoption/
  variable/
  vendor/
  product/
  response/
  weight/
  dashboard/
  audit/
  settings/
io/
  project/
    Import/
    Export/
```

Cada dominio con:

- `Controller/`
- `DTO/`
- `Repository/`
- `Query/`
- `Services/`

---

## 8. Arquitectura frontend objetivo

```text
frontend/src/renderer/
  views/
    ProjectHubView/
    ProjectWorkspaceView/
    dashboard/
    vendors/
    products/
    questions/
    responses/
    settings/
    versions/
    audit/
  components/
    common/
    layout/
    project-hub/
    dashboard/
    vendors/
    products/
    questions/
    responses/
    settings/
  stores/
    project/
    dashboard/
    vendor/
    product/
    question/
    response/
    settings/
    version/
    audit/
  router/
  plugins/
  utils/
```

---

## 9. Reglas no negociables del proyecto

1. No meter lógica de scoring en Vue.
2. No leer SQLite desde Electron renderer.
3. No hacer consultas SQL desde Controller.
4. No mezclar DTO con entidad de dominio.
5. No calcular métricas complejas en frontend.
6. No crear pantallas sin contrato previo de endpoint.
7. No crear tablas SQLite sin migración versionada.
8. No permitir edición directa de configuraciones históricas ya versionadas.

---

# 10. Hoja de ruta por sprints

---

# Sprint 0 — Blindar la arquitectura base y congelar convenciones

## Objetivo

Partir del template actual y dejar cerradas las reglas del proyecto antes de empezar a programar cosas.

## Tablas implicadas

Ninguna.

## Backend implicado

- `App.java`
- `ControllerRegister.java`
- `DatabaseSchemaManager.java`

## Frontend implicado

- `frontend/src/renderer/router`
- `frontend/src/renderer/plugins`
- `frontend/src/renderer/stores`

## Trabajo exacto

- Revisar template actual.
- Crear `docs/architecture.md`
- Crear `docs/conventions.md`
- Crear `docs/modules.md`
- Documentar naming, flujo Electron↔Javalin↔SQLite y reglas REST.

## Prompt GitHub Copilot

```text
Quiero congelar la arquitectura base de un proyecto Electron + Vue 3 + PrimeVue + Javalin + SQLite ya existente.

Contexto:
- frontend no toca SQLite
- Electron no tiene lógica de negocio
- backend Javalin usa capas Controller, DTO, Repository, Query, Services
- App.java registra endpoints
- quiero un documento de arquitectura ejecutable por el equipo

Tareas:
1. Genera docs/architecture.md con principios, estructura de carpetas y responsabilidades.
2. Genera docs/conventions.md con reglas de naming backend, frontend, DTO y rutas REST.
3. Genera docs/modules.md con lista de módulos funcionales futuros.
4. No inventes microservicios.
5. No cambies la arquitectura base existente.

Devuélveme contenido completo archivo por archivo.
```

## Criterio de aceptación

- arquitectura congelada
- convenciones escritas
- equipo sin margen para improvisar

---

# Sprint 1 — Diseñar el modelo relacional completo en SQLite

## Objetivo

Pasar de idea funcional a modelo de datos real.

## Tablas implicadas

Todas las tablas base del apartado 5.

## Backend implicado

- `db/DatabaseSchemaManager.java`
- carpeta nueva `manager/*/Query`

## Frontend implicado

- `frontend/src/renderer/stores/*`
- `frontend/src/renderer/views/*` (solo cableado minimo)

## Trabajo exacto

- Definir DDL completo.
- Definir claves foráneas.
- Definir índices.
- Definir reglas `ON DELETE` y `ON UPDATE`.
- Definir tablas de variables genéricas por proyecto.
- Definir unicidad funcional de respuestas por vendor + product + question.
- Definir semántica de estados de respuesta (ANSWERED, NO_RESPONSE, NOT_APPLICABLE).
- Validar que el modelo se mantiene genérico y no orientado solo a software.
- Crear `db/schema/V001__initial_schema.sql`
- Crear estructura backend por tabla con capas `Controller/DTO/Repository/Query/Services`.
- Eliminar módulos legacy no usados (`manager/appmessage`, `manager/health`, etc.).
- Mantener únicamente `io/project` como área temporal a revisar después.
- Crear stores Pinia nuevos por dominio de tabla para cableado frontend.
- Retirar stores/view wiring legacy que dependían del modelo antiguo.

## Prompt GitHub Copilot

```text
Quiero diseñar el esquema SQLite completo para una aplicación de evaluación de vendors multi-proyecto.

Contexto funcional:
- el proyecto es genérico (sirve para evaluar cualquier dominio)
- un proyecto tiene muchos vendors
- un vendor tiene muchos products
- un proyecto tiene variables configurables (Category, QuestionType, RequirementLevel, etc.)
- cada question pertenece al proyecto y usa esas variables
- cada response corresponde a vendor + product + question
- evaluator no forma parte de esta fase

Necesito estas entidades:
- project
- project_version
- vendor
- product
- project_variable_type
- project_variable_option
- question
- question_response_option
- response
- scale
- scale_option
- question_weight
- category_weight
- localized_text
- audit_log

Tareas:
1. Diseña el DDL SQLite completo.
2. Añade primary keys, foreign keys e índices.
3. Usa nombres claros y consistentes.
4. Piensa en integridad relacional real.
5. Incluye índice único para (project_id, vendor_id, product_id, question_id) en response.
6. Prepara el SQL para migración inicial.
7. No uses cosas específicas de PostgreSQL.
8. Devuélveme un archivo V001__initial_schema.sql completo.
9. Incluye soporte para estado de respuesta y casos no respondidos/no aplicables.
```

## Criterio de aceptación

- esquema ejecutable
- relaciones coherentes project -> vendor -> product y project -> question
- variables de negocio configurables por proyecto
- respuestas únicas por vendor + product + question
- manejo explícito de no respuesta y no aplicable
- validación explícita de genericidad del modelo
- índices mínimos resueltos
- backend inicial cableado al nuevo modelo por dominios
- frontend con stores nuevos base para evolucionar pantallas
- legacy fuera de uso, excepto `io/project` en transición

---

# Sprint 2 — Crear sistema de migraciones y bootstrap de base de datos

## Objetivo

Que SQLite no sea un archivo mágico, sino una base controlada por migraciones.

## Tablas implicadas

- `schema_version`
- resto del esquema inicial

## Backend implicado

- `DatabaseSchemaManager.java`
- `db/migration/*`

## Frontend implicado

Ninguno.

## Trabajo exacto

- decidir estrategia de migraciones
- crear tabla de control de versión
- aplicar migraciones idempotentes al arranque
- dejar seeds mínimos

## Prompt GitHub Copilot

```text
Quiero implementar un sistema simple y robusto de migraciones SQLite en un backend Javalin.

Tareas:
1. Diseña una tabla schema_version.
2. Crea una clase MigrationRunner.
3. Haz que DatabaseSchemaManager aplique migraciones al arranque.
4. Soporta scripts SQL ordenados por versión.
5. Añade seeds base para project status y idiomas.
6. No uses frameworks pesados si no son necesarios.
7. Quiero código limpio y auditable.
```

## Criterio de aceptación

- al arrancar se crea la base
- se registran migraciones aplicadas
- arranque repetido no rompe nada

---

# Sprint 3 — Módulo Project Hub

## Objetivo

Construir la primera ventana tipo VS Code para seleccionar proyecto.

## Tablas implicadas

- `project`
- `project_version`

## Backend implicado

- `manager/project/*`
- `manager/projectversion/*`

## Frontend implicado

- `views/ProjectHubView`
- `components/project-hub/*`
- `stores/project/*`

## Trabajo exacto

- CRUD de proyectos
- listado de recientes
- crear proyecto
- duplicar proyecto
- archivar proyecto
- abrir proyecto

## PrimeVue

- `DataTable`
- `Dialog`
- `Toolbar`
- `InputText`
- `Textarea`
- `Tag`
- `Button`

## Prompt GitHub Copilot

```text
Quiero construir el módulo Project Hub para una app Electron + Vue + Javalin + SQLite.

Objetivo:
pantalla inicial tipo VS Code donde el usuario ve proyectos recientes y puede crear, abrir, duplicar o archivar proyectos.

Backend:
1. Crea módulo manager/project con Controller, DTO, Repository, Query y Services.
2. Implementa endpoints:
   - GET /api/projects
   - GET /api/project/{id}
   - POST /api/project
   - PUT /api/project/{id}
   - POST /api/project/{id}/duplicate
   - POST /api/project/{id}/archive
3. Usa SQLite y SQL en Query.

Frontend:
1. Crea ProjectHubView.vue
2. Usa PrimeVue DataTable, Dialog y Toolbar
3. Crea store project
4. Navega al workspace al abrir proyecto

Quiero implementación archivo por archivo.
```

## Criterio de aceptación

- abre la app y se ve el launcher
- se puede crear un proyecto
- se puede abrir un proyecto

---

# Sprint 4 — Shell del workspace del proyecto

## Objetivo

Entrar en un proyecto y tener layout real de aplicación.

## Tablas implicadas

- `project`

## Backend implicado

- lectura de proyecto activo

## Frontend implicado

- `ProjectWorkspaceView.vue`
- `components/layout/*`
- router
- breadcrumb
- sidebar

## Trabajo exacto

- layout lateral
- header de proyecto
- navegación por módulos
- tabs o zonas principales

## PrimeVue

- `Menubar`
- `PanelMenu`
- `Breadcrumb`
- `Splitter`
- `TabView`

## Prompt GitHub Copilot

```text
Quiero construir el shell del workspace al abrir un proyecto.

Objetivo:
tener una pantalla principal con sidebar, header, breadcrumb y contenido central, preparada para módulos de configuración, respuestas y dashboard.

Tareas:
1. Crea ProjectWorkspaceView.vue
2. Crea layout reutilizable
3. Muestra nombre del proyecto activo
4. Añade navegación lateral por módulos
5. Usa PrimeVue Menubar, PanelMenu, Breadcrumb y Splitter
6. Mantén todo en script setup lang="ts"

No metas todavía lógica de módulos internos.
```

## Criterio de aceptación

- al abrir proyecto se entra en workspace
- layout estable
- navegación lista para crecer

---

# Sprint 5 — Catalogos base: variables por proyecto, escalas e idiomas

## Objetivo

Crear la base configurable del sistema.

## Tablas implicadas

- `project_variable_type`
- `project_variable_option`
- `scale`
- `scale_option`
- `localized_text`

## Backend implicado

- `manager/variable/*`
- `manager/scale/*`
- `manager/scaleoption/*`
- `manager/settings/*`

## Frontend implicado

- `views/settings/*`
- `stores/settings/*`

## Trabajo exacto

- CRUD de tipos de variable
- CRUD de opciones por tipo
- CRUD de escalas
- CRUD de opciones de escala
- gestión básica de idiomas

## PrimeVue

- `DataTable`
- `Dialog`
- `Accordion`
- `Dropdown`
- `InputNumber`

## Prompt GitHub Copilot

```text
Quiero construir el módulo de configuración base del evaluador.

Contexto:
cada proyecto necesita variables configurables (Category, QuestionType, RequirementLevel), escalas y opciones de escala.

Tareas backend:
1. Crear módulos variable, scale y scaleoption.
2. Implementar CRUD REST.
3. Validar relaciones project -> variable_type -> variable_option.
4. Evitar borrar registros si tienen dependencias activas.

Tareas frontend:
1. Crear vistas de configuración en settings.
2. Usar PrimeVue DataTable y Dialog.
3. Permitir editar orden, código, nombre y estado activo.
4. Mantener UX limpia y modular.

Dame implementación archivo por archivo.
```

## Criterio de aceptación

- se pueden configurar variables por proyecto
- se puede definir una escala de respuesta real

---

# Sprint 6 — Módulo de preguntas

## Objetivo

Gestionar preguntas funcionales y técnicas como entidad seria.

## Tablas implicadas

- `question`
- `project_variable_option`

## Backend implicado

- `manager/question/*`

## Frontend implicado

- `views/questions/*`
- `components/questions/*`
- `stores/question/*`

## Trabajo exacto

- alta/edición/baja lógica
- filtro por tipo
- filtro por categoría
- orden
- activar/desactivar
- importación futura preparada

## PrimeVue

- `DataTable`
- `Column`
- `MultiSelect`
- `Drawer`
- `Checkbox`

## Prompt GitHub Copilot

```text
Quiero construir el módulo de preguntas del evaluador.

Contexto:
hay preguntas funcionales y técnicas por proyecto, agrupadas por categorías configurables.

Tareas:
1. Crear backend manager/question completo.
2. Implementar endpoints de listado filtrado, detalle, create, update y deactivate.
3. Soportar filtros por question_type, category_id e is_active.
4. Crear frontend con DataTable y formulario lateral tipo Drawer.
5. Mostrar columnas: code, title, type, category, active, sort_order.
6. Preparar diseño pensando en cientos de preguntas.

Quiero código completo por archivo.
```

## Criterio de aceptación

- preguntas funcionales y técnicas viven en la misma arquitectura
- no hay que tocar código para añadir preguntas nuevas

---

# Sprint 7 — Módulo de vendors y productos

## Objetivo

Separar bien vendor de producto, que es donde Excel solía convertirse en barro.

## Tablas implicadas

- `vendor`
- `product`

## Backend implicado

- `manager/vendor/*`
- `manager/product/*`

## Frontend implicado

- `views/vendors/*`
- `views/products/*`
- `stores/vendor/*`
- `stores/product/*`

## Trabajo exacto

- CRUD vendor
- CRUD productos por vendor
- vista maestro-detalle

## PrimeVue

- `DataTable`
- `TreeTable` opcional
- `Dialog`
- `Tag`
- `Badge`

## Prompt GitHub Copilot

```text
Quiero construir los módulos de vendors y productos en una app de evaluación de software.

Reglas:
1. Vendor y producto no son la misma entidad.
2. Un vendor puede tener múltiples productos.
3. Todo cuelga de project_id.

Tareas backend:
- crear manager/vendor
- crear manager/product
- implementar endpoints CRUD y listados por project y por vendor

Tareas frontend:
- vista Vendors con listado principal
- panel secundario con productos del vendor seleccionado
- formularios modales para crear y editar

Usa PrimeVue DataTable, Dialog, Tag y Badge.
Quiero implementación archivo por archivo.
```

## Criterio de aceptación

- se puede registrar Bentley mañana y 20 productos si hace falta
- no cambia la estructura del sistema

---

# Sprint 8 — Módulo de pesos (sin evaluadores)

## Objetivo

Modelar correctamente pesos por categoría y por pregunta.

## Tablas implicadas

- `question_weight`
- `category_weight`

## Backend implicado

- `manager/weight/*`

## Frontend implicado

- `views/settings/weights/*`
- `stores/settings/*`

## Trabajo exacto

- peso por categoría
- peso por pregunta
- validaciones de suma de pesos

## Prompt GitHub Copilot

```text
Quiero construir el sistema de pesos del evaluador.

Necesito:
- pesos por categoría
- pesos por pregunta

Tareas:
1. Diseña backend para weight.
2. Implementa validaciones de negocio para evitar pesos negativos.
3. Añade endpoint de validación global de consistencia.
4. Crea frontend con tablas editables y mensajes claros.
5. Usa PrimeVue InputNumber y Toast para validaciones.

Quiero una solución robusta y auditable.
```

## Criterio de aceptación

- pesos configurables
- errores visibles si la configuración es incoherente

---

# Sprint 9 — Módulo de captura de respuestas

## Objetivo

Construir el corazón de la app.

## Tablas implicadas

- `response`
- `question`
- `vendor`
- `product`
- `question_response_option`
- `scale_option`

## Backend implicado

- `manager/response/*`

## Frontend implicado

- `views/responses/*`
- `components/responses/*`
- `stores/response/*`

## Trabajo exacto

- grid de captura
- filtros por vendor, product, tipo y categoría
- guardado masivo
- edición de comentario y evidencia

## PrimeVue

- `DataTable`
- `Dropdown`
- `Textarea`
- `Paginator`
- `Toast`
- `ConfirmDialog`

## Prompt GitHub Copilot

```text
Quiero construir el módulo de captura de respuestas del evaluador.

Contexto:
cada respuesta pertenece a project, vendor, product, question y question_response_option.

Objetivo:
crear una pantalla usable para evaluar muchas preguntas sin sufrir.

Tareas backend:
1. Implementa manager/response con create, update, bulk-upsert y list filtrado.
2. Usa bulk save para mejorar rendimiento.
3. Valida que project, vendor, product, question y question_response_option existan.

Tareas frontend:
1. Crea una vista tipo grid con filtros por vendor, tipo y categoría.
2. Cada fila debe mostrar pregunta y selector de respuesta.
3. Permite comentario libre y evidencia.
4. Usa PrimeVue DataTable, Dropdown, Textarea y Toast.
5. Optimiza UX para trabajar con muchas filas.

Dame implementación archivo por archivo.
```

## Criterio de aceptación

- ya se pueden evaluar vendors de verdad
- el sistema deja de ser maqueta

---

# Sprint 10 — Cálculo de métricas y vistas SQL de agregación

## Objetivo

Mover el cerebro numérico al modelo relacional.

## Tablas implicadas

- `response`
- `question_weight`
- `category_weight`
- vistas `vw_*`

## Backend implicado

- `manager/dashboard/*`
- `manager/response/Query/*`
- `manager/dashboard/Services/*`

## Frontend implicado

Ninguno todavía.

## Trabajo exacto

- calcular score base
- media
- media ponderada
- desviación
- cobertura
- ranking
- vistas SQL
- servicios Java para lectura

## Prompt GitHub Copilot

```text
Quiero construir el motor de métricas de un evaluador relacional en SQLite.

Métricas:
- frecuencia
- media
- media ponderada
- desviación estándar
- cobertura
- score total
- ranking

Tareas:
1. Propón vistas SQL para agregados por vendor, categoría y pregunta.
2. Usa la tabla response y los pesos configurados.
3. Diseña consultas legibles y mantenibles.
4. Implementa un servicio Java que exponga estas métricas al dashboard.
5. No calcules todo en frontend.
6. Devuélveme SQL + clases Query + Service.
```

## Criterio de aceptación

- métricas coherentes
- ranking ya disponible por backend

---

# Sprint 11 — Dashboard ejecutivo por proyecto

## Objetivo

Construir la capa visible de decisión.

## Tablas implicadas

- `vw_vendor_score_summary`
- `vw_vendor_category_score`
- `vw_vendor_question_score`
- `vw_project_dashboard_summary`

## Backend implicado

- `manager/dashboard/*`

## Frontend implicado

- `views/dashboard/*`
- `components/dashboard/*`
- `stores/dashboard/*`

## Trabajo exacto

- KPI cards
- ranking
- gráfico por vendor
- gráfico por categoría
- cobertura
- distribución de respuestas

## PrimeVue

- `Chart`
- `Panel`
- `Card`
- `Tag`
- `DataTable`

## Prompt GitHub Copilot

```text
Quiero construir el dashboard ejecutivo del proyecto.

Objetivo:
mostrar rápidamente qué vendor va mejor y por qué.

Tareas backend:
1. Exponer endpoints de summary, ranking, by-category y by-question.
2. Mantener payloads simples para frontend.

Tareas frontend:
1. Crear DashboardView.vue
2. Mostrar KPI cards de score medio, cobertura y desviación
3. Mostrar ranking de vendors
4. Mostrar gráficos por categoría y distribución de respuestas
5. Usar PrimeVue Chart, Card, Panel y DataTable
6. Diseñar una UI limpia para dirección y equipo técnico

Quiero implementación archivo por archivo.
```

## Criterio de aceptación

- dashboard útil de verdad
- no solo bonito, también interpretable

---

# Sprint 12 — Resumen ejecutivo y ficha comparativa de vendor

## Objetivo

Crear una lectura más humana que el dashboard bruto.

## Tablas implicadas

- vistas agregadas
- `vendor`
- `product`

## Backend implicado

- `manager/dashboard/*`
- `manager/vendor/*`

## Frontend implicado

- `views/vendors/VendorDetailView.vue`
- `components/dashboard/*`

## Trabajo exacto

- ficha por vendor
- resumen por fortalezas
- top categorías
- bottom categorías
- preguntas críticas sin cobertura

## Prompt GitHub Copilot

```text
Quiero una vista de detalle por vendor.

Objetivo:
que al seleccionar un vendor pueda ver:
- score total
- ranking
- top categorías
- categorías débiles
- productos asociados
- preguntas críticas peor valoradas
- cobertura

Tareas:
1. Crea endpoints backend de detalle agregado por vendor.
2. Crea una vista frontend clara y ejecutiva.
3. Usa PrimeVue Card, Tag, Panel y DataTable.
4. Mantén diseño modular y reutilizable.
```

## Criterio de aceptación

- cada vendor tiene ficha comparativa seria

---

# Sprint 13 — Versionado de configuración por proyecto

## Objetivo

Evitar el clásico desastre de alguien cambió preguntas o pesos y ya no sabemos por qué cambió el dashboard.

## Tablas implicadas

- `project_version`
- `audit_log`

## Backend implicado

- `manager/projectversion/*`
- `manager/audit/*`

## Frontend implicado

- `views/versions/*`
- `views/audit/*`

## Trabajo exacto

- crear snapshot de configuración
- activar versión
- ver diff lógico
- trazar cambios

## Prompt GitHub Copilot

```text
Quiero implementar versionado de configuración por proyecto.

Contexto:
necesito saber cuándo cambian preguntas, pesos, escalas o categorías y poder versionar ese estado.

Tareas:
1. Diseña el módulo projectversion y audit.
2. Implementa creación de versión desde estado actual.
3. Guarda change_summary.
4. Registra cambios en audit_log.
5. Crea frontend para listar versiones y auditoría.
6. No permitas editar directamente versiones cerradas.
```

## Criterio de aceptación

- cambios importantes trazables
- menos suicidio operativo

---

# Sprint 14 — Importación desde legados

## Objetivo

Migrar datos históricos o plantillas antiguas al nuevo sistema.

## Tablas implicadas

Prácticamente todas las de configuración y captura.

## Backend implicado

- `io/project/Import/*`

## Frontend implicado

- `views/settings/import/*`

## Trabajo exacto

- importar CSV/XLSX exportado
- mapear preguntas
- mapear vendors
- mapear respuestas
- validar inconsistencias

## Prompt GitHub Copilot

```text
Quiero construir un módulo de importación para poblar un proyecto desde archivos legados.

Objetivo:
importar configuración y respuestas desde formatos de intercambio, sin depender de que el frontend lea archivos complejos por sí solo.

Tareas:
1. Crea módulo io/project/Import con Controller, DTO, Services.
2. Diseña proceso de validación previa.
3. Genera reporte de errores de importación.
4. Separa import de preguntas, vendors, productos y respuestas.
5. Devuelve resumen de inserciones, actualizaciones y errores.
```

## Criterio de aceptación

- se puede poblar un proyecto sin carga manual infinita

---

# Sprint 15 — Exportación de resultados y snapshot del proyecto

## Objetivo

Poder sacar evidencia, informes y congelar estados.

## Tablas implicadas

- vistas agregadas
- tablas del proyecto

## Backend implicado

- `io/project/Export/*`

## Frontend implicado

- `views/settings/export/*`

## Trabajo exacto

- export resumen
- export respuestas
- export métricas
- export snapshot JSON del proyecto

## Prompt GitHub Copilot

```text
Quiero construir el módulo de exportación del proyecto.

Objetivo:
poder exportar:
- respuestas
- vendors y productos
- preguntas
- métricas agregadas
- snapshot JSON completo del proyecto

Tareas:
1. Crea módulo io/project/Export.
2. Implementa exportes separados y export completo.
3. Devuelve archivos listos para descarga.
4. Mantén el código desacoplado por caso de uso.
```

## Criterio de aceptación

- el proyecto puede salir del sistema con estructura clara

---

# Sprint 16 — Multiidioma real de UI y textos de proyecto

## Objetivo

Que la app soporte PT / ES / EN con seriedad.

## Tablas implicadas

- `localized_text`

## Backend implicado

- `manager/settings/*`

## Frontend implicado

- `plugins/i18n.ts`
- `locales/*`
- pantallas de configuración

## Trabajo exacto

- textos fijos UI por locale
- textos configurables del proyecto
- selector de idioma

## Prompt GitHub Copilot

```text
Quiero añadir soporte multiidioma PT, ES y EN al proyecto.

Objetivo:
separar:
1. textos fijos de interfaz
2. textos configurables por proyecto almacenados en SQLite

Tareas:
1. Diseña estructura de localized_text.
2. Integra i18n del frontend.
3. Añade selector de idioma.
4. Permite traducir labels configurables del proyecto.
5. No hardcodees textos por todas partes.
```

## Criterio de aceptación

- idioma intercambiable
- sistema preparado para crecer

---

# Sprint 17 — Validaciones, permisos funcionales y endurecimiento UX

## Objetivo

Que el usuario no pueda romper la app ni meter basura alegremente.

## Tablas implicadas

- base completa
- audit log

## Backend implicado

- services de validación
- errores consistentes

## Frontend implicado

- formularios
- guards de navegación
- confirmaciones

## Trabajo exacto

- validaciones de negocio
- mensajes claros
- guardado seguro
- confirmación de cambios
- bloqueo de acciones destructivas

## Prompt GitHub Copilot

```text
Quiero endurecer la UX y las validaciones funcionales del evaluador.

Tareas:
1. Añade validaciones backend consistentes.
2. Devuelve errores con formato uniforme.
3. Refuerza formularios frontend con mensajes claros.
4. Usa ConfirmDialog y Toast donde haga falta.
5. Evita operaciones destructivas sin confirmación.
6. Piensa en experiencia real de usuario de negocio.
```

## Criterio de aceptación

- app más robusta
- menos errores silenciosos

---

# Sprint 18 — Tests, QA técnico y release 1.0

## Objetivo

Cerrar una versión que no sea humo.

## Tablas implicadas

Todas.

## Backend implicado

- tests repository
- tests services
- tests endpoints

## Frontend implicado

- tests stores
- validación de tipos
- lint

## Trabajo exacto

- tests backend
- test de migraciones
- test de scoring
- lint frontend
- `vue-tsc --noEmit`
- build electron
- build backend

## Prompt GitHub Copilot

```text
Quiero preparar la release 1.0 de esta app Electron + Vue + Javalin + SQLite.

Tareas:
1. Genera estrategia de tests para backend y frontend.
2. Crea tests para migraciones, scoring y módulos críticos.
3. Añade checklist final de calidad.
4. Verifica que el build completo funcione.
5. Propón una checklist de release 1.0 concreta y segura.
```

## Criterio de aceptación

- build pasa
- tests críticos pasan
- release empaquetable

---

## 11. Orden recomendado real

1. Sprint 0
2. Sprint 1
3. Sprint 2
4. Sprint 3
5. Sprint 4
6. Sprint 5
7. Sprint 6
8. Sprint 7
9. Sprint 8
10. Sprint 9
11. Sprint 10
12. Sprint 11
13. Sprint 12
14. Sprint 13
15. Sprint 14
16. Sprint 15
17. Sprint 16
18. Sprint 17
19. Sprint 18

---

## 12. Recomendación técnica fuerte

La decisión de salir de Excel fue correcta. No era un problema de formato. Era un problema de modelo relacional y mantenibilidad.

La apuesta buena aquí es:

- SQLite como almacenamiento local serio
- Javalin como cerebro funcional
- Vue + PrimeVue como interfaz operativa
- Electron como contenedor standalone

Eso os da algo muchísimo más sano que Excel: estructura, trazabilidad, versionado, métricas reales y crecimiento sin cirugía por columnas.

Y además, una ventaja brutal: mañana podéis pasar de comparador de vendors a gestor de evaluación de soluciones sin rehacer el edificio.
