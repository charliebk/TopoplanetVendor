# Arquitectura Base Congelada

## 1. Objetivo

Este documento congela la arquitectura base del proyecto TopoplanetVendor para que el equipo implemente funcionalidades sin romper responsabilidades.

Alcance:

- Electron + Vue 3 + PrimeVue + Javalin + SQLite.
- Aplicacion desktop monolitica modular.
- Sin microservicios.

## 2. Principios no negociables

1. El frontend no accede a SQLite de forma directa.
2. Electron (main y preload) no contiene logica de negocio funcional.
3. La logica de negocio vive en backend Javalin (Services).
4. El acceso SQL/SQLite vive en Repository + Query del backend.
5. Cada endpoint se registra explicitamente en `backend/src/main/java/com/vutron/backend/App.java`.
6. El backend se organiza por dominio y por capas: `Controller`, `DTO`, `Repository`, `Query`, `Services`.
7. No se permite mover reglas de negocio al renderer de Vue.
8. No se permite ejecutar SQL desde Controller.
9. No se permiten cambios de arquitectura (monolito modular) sin RFC del equipo.

## 3. Vista de arquitectura (C4 simplificado)

### 3.1 Capas

- Capa Presentacion: Vue 3 + PrimeVue (renderer).
- Capa Integracion Desktop: Electron main + preload (bridge tecnico, no funcional).
- Capa Aplicacion/API: Javalin (controllers y servicios).
- Capa Datos: SQLite via JDBC.

### 3.2 Flujo oficial de una operacion

1. Usuario interactua con componente Vue.
2. Vista dispara accion de store (`useXStore`).
3. Store llama endpoint HTTP local del backend.
4. `App.java` ya tiene registrada la ruta y delega al `Controller`.
5. `Controller` valida/parsea request y llama `Service`.
6. `Service` aplica reglas de negocio y orquesta repositorios.
7. `Repository` ejecuta SQL definido en `Query`.
8. Se responde DTO JSON al frontend.
9. Store actualiza estado y la vista renderiza.

## 4. Estructura de carpetas y responsabilidades

### 4.1 Raiz

- `frontend/`: desktop shell (Electron) + UI Vue + stores + router.
- `backend/`: API local en Java con Javalin y capas de dominio.
- `db/`: artefactos de base de datos y scripts SQL.
- `docs/`: documentos de arquitectura, convenciones y modulos.

### 4.2 Frontend

Ruta base: `frontend/src/`

- `main/`: proceso principal Electron (arranque, tray, IPC tecnico).
- `preload/`: puente seguro entre renderer y APIs expuestas.
- `renderer/`: aplicacion Vue 3.

En `renderer/`:

- `views/`: pantallas por modulo.
- `components/`: componentes reutilizables (`common`, `layout`, etc.).
- `stores/`: estado y consumo de API backend.
- `router/`: definicion de rutas de navegacion.
- `plugins/`: inicializacion de i18n, pinia, primevue, vuetify.
- `utils/`: utilidades de UI/integracion.

Convencion actual de stores frontend:

- Cada dominio vive en su propia carpeta bajo `renderer/stores/<dominio>/`.
- El archivo de runtime del store usa el patron `<dominio>.store.ts`.
- El archivo de contratos/tipos usa el patron `<dominio>.types.ts`.
- El barrel raiz de stores usa nombre explicito: `renderer/stores/stores.exports.ts`.
- No se usan `index.ts` dentro de `renderer/stores/` ni dentro de cada carpeta de store.

Responsabilidad del frontend:

- Presentacion, experiencia de usuario, estado local y llamadas HTTP.
- Nunca decisiones de negocio persistente ni SQL.

### 4.3 Backend

Ruta base: `backend/src/main/java/com/vutron/backend/`

- `App.java`: bootstrap, configuracion Javalin y registro explicito de endpoints.
- `config/`: configuraciones de app y datasource.
- `db/`: esquema y bootstrap de base.
- `controller/`: contratos base de registro/controladores CRUD.
- `manager/<dominio>/`: capas funcionales del dominio.
- `io/project/Export` y `io/project/Import`: flujo de export/import de proyectos.

Capas por dominio (`manager/<dominio>/`):

- `Controller/`: mapping HTTP, parseo y codigos de estado.
- `DTO/`: contratos request/response.
- `Services/`: reglas de negocio y orquestacion.
- `Repository/`: acceso a datos via JDBC.
- `Query/`: SQL centralizado.

Responsabilidad del backend:

- Contratos API, validaciones, reglas de negocio, integridad transaccional y acceso SQLite.

### 4.4 Base de datos

- Motor: SQLite local.
- Acceso exclusivo desde backend.
- Inicializacion de esquema al arranque mediante `DatabaseSchemaManager`.
- El frontend consume solo contratos HTTP, nunca tablas.

Convencion SQL congelada para nombres fisicos:

- Los nombres de tablas se definen siempre en singular.
- Ejemplo actual minimo: `project`.
- Esta regla aplica a nuevas tablas, indices y consultas SQL asociadas.

Arquitectura de datos congelada para esta fase:

- `project` es la raiz funcional de todo el modelo.
- `vendor` depende de `project` (1:N).
- `product` depende de `vendor` y `project` (project 1:N vendor, vendor 1:N product).
- Las variables de negocio son genericas y dependen de `project`.
- `question` depende de `project` y referencia variables configurables del proyecto.
- `response` representa la respuesta de una `question` para una combinacion `vendor + product`.
- `evaluator` queda fuera de alcance por ahora.

Variables genericas por proyecto (en lugar de catalogos hardcodeados):

- Tipo `CATEGORY` (ejemplo: Functional, Security, Operations).
- Tipo `QUESTION_TYPE` (ejemplo: Functional, Security, Operations, Compliance).
- Tipo `REQUIREMENT_LEVEL` (ejemplo: Mandatory, Important, Optional).

Regla de modelado:

- Si una opcion cambia segun proyecto, debe vivir en tablas de variables por proyecto.
- No se hardcodean listas de negocio en frontend ni backend.

## 5. Responsabilidades por capa

### 5.1 Vue + PrimeVue

- Renderizar UI y capturar interacciones.
- Mantener estado de pantalla y delegar operaciones al store.
- Traducir datos API a componentes visuales.

### 5.2 Stores (Pinia)

- Encapsular endpoints y llamadas fetch.
- Normalizar errores de API para la UI.
- Exponer operaciones de caso de uso a las vistas.

### 5.3 Electron main/preload

- Arranque de aplicacion, ventana, integraciones desktop e IPC tecnico.
- Sin validaciones de negocio, sin reglas de scoring, sin SQL.

### 5.4 Controller (backend)

- Entradas/salidas HTTP.
- Parseo de path/body.
- Respuestas HTTP (200/201/204/400/404/500) segun resultado del Service.

### 5.5 Service (backend)

- Reglas de negocio del dominio.
- Validaciones funcionales.
- Orquestacion de operaciones de repositorio.

### 5.6 Repository + Query (backend)

- `Query`: SQL y sentencias preparadas.
- `Repository`: ejecucion JDBC, mapeo record/DTO interno.
- Sin logica de presentacion.

## 6. Registro de endpoints

Regla congelada:

- Toda nueva ruta debe agregarse explicitamente en `App.java`.
- No se permite autodiscovery de controladores ni wiring implicito.

Ejemplos actuales:

- `GET /health`
- `GET /api/app-message/hello-world`
- `GET /api/projects`
- `GET /api/project/{id}`
- `POST /api/project`
- `PUT /api/project/{id}`
- `DELETE /api/project/{id}`
- `POST /api/project/export`
- `POST /api/project/import`

## 7. Politica de evolucion (sin cambiar arquitectura base)

Se permite:

- Agregar nuevos dominios bajo `manager/<dominio>/` con las 5 capas.
- Agregar nuevas vistas/componentes/stores en frontend.
- Agregar tablas/indices/migraciones SQL en backend/db.

No se permite:

- Separar backend en microservicios.
- Mover logica funcional al renderer o a Electron main.
- Saltarse Service para hablar directo de Controller a Query.
- Saltarse Repository para ejecutar SQL desde otras capas.

## 8. Definition of Done arquitectonica (por feature)

1. Existe modulo backend por dominio con carpetas `Controller`, `DTO`, `Repository`, `Query`, `Services`.
2. Endpoints nuevos registrados en `App.java`.
3. Store frontend usa solo endpoints backend (sin acceso DB).
4. UI no contiene reglas de negocio persistente.
5. SQL encapsulado en Query/Repository.
6. Documentacion de contratos y convenciones actualizada en `docs/`.
