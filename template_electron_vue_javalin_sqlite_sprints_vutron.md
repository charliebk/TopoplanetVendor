# Template Vutron - Estado Actual (Electron + Vue 3 + TypeScript + Javalin + SQLite)

## 1. Proposito

Este archivo documenta el estado actual del template despues de limpieza y refactorizacion.
El objetivo es que cualquier persona pueda mantener el proyecto respetando la misma arquitectura,
convenciones y jerarquia de carpetas en backend y frontend.

## 2. Principios de arquitectura

1. `frontend` no accede a SQLite directamente.
2. Electron (main/preload) no contiene reglas de negocio.
3. Las reglas funcionales viven en backend Javalin.
4. SQLite solo se toca desde repositorios del backend.
5. Los endpoints se registran de forma explicita en `backend/src/main/java/com/vutron/backend/App.java`.
6. Cada modulo se separa por responsabilidades y por dominio.

## 3. Jerarquia de carpetas (raiz)

```text
/
  backend/
  db/
  frontend/
  README.md
  template_electron_vue_javalin_sqlite_sprints_vutron.md
```

Nota: se eliminaron carpetas y archivos no esenciales del template original para dejar una base minima y clara.

## 4. Backend (Java + Javalin)

### 4.1 Estructura por capas y dominios

```text
backend/src/main/java/com/vutron/backend/
  App.java
  controller/
    ControllerRegister.java
    CrudCapabilitiesController.java
  db/
    DatabaseSchemaManager.java
  manager/
    health/
      Controller/
      DTO/
      Repository/
      Query/
      Services/
    appmessage/
      Controller/
      DTO/
      Repository/
      Query/
      Services/
    project/
      Controller/
      DTO/
      Repository/
      Query/
      Services/
  io/
    project/
      Export/
        Controller/
        DTO/
        Repository/
        Query/
        Services/
      Import/
        Controller/
        DTO/
        Repository/
        Query/
        Services/
```

### 4.2 Convenios backend

1. Nombres de carpetas por capa: `Controller`, `DTO`, `Repository`, `Query`, `Services`.
2. Los controladores implementan registro por capacidad usando `CrudCapabilitiesController` cuando aplica.
3. DTOs con naming por accion (ejemplo: `CreateProjectRequestDto`, `UpdateProjectResponseDto`).
4. El bootstrap de endpoints es declarativo y visible en `App.java`.

### 4.3 Convenio REST singular/plural

Regla activa:

1. Coleccion/listado: plural.
2. Recurso individual y acciones sobre recurso: singular.

Ejemplos vigentes:

1. `GET /api/projects` (lista)
2. `GET /api/project/{id}`
3. `POST /api/project`
4. `PUT /api/project/{id}`
5. `DELETE /api/project/{id}`
6. `POST /api/project/export`
7. `POST /api/project/import`
8. `GET /api/app-message/hello-world`

### 4.4 Base de datos

1. Esquema SQLite idempotente en arranque.
2. Seeds idempotentes para datos base.
3. Operaciones SQL centralizadas en `Query`.

## 5. Frontend (Vue 3 + Electron Renderer)

### 5.1 Estructura objetivo

```text
frontend/src/renderer/
  components/
    common/
    layout/
  locales/
  plugins/
    i18n.ts
    pinia.ts
    primevue.ts
    vuetify.ts
  router/
  stores/
    appMessage/
    exportProject/
    health/
    importProject/
    project/
    client.ts
    index.ts
  utils/
  views/
    MainView/
    ErrorView/
```

### 5.2 Convenios frontend

1. `views/`: paginas conectadas al router.
2. `components/`: piezas reutilizables (layout + comunes).
3. `stores/`: estado y endpoints backend por dominio de controller.
4. `plugins/`: inicializacion de librerias globales.

### 5.3 Convenio obligatorio para archivos `.vue`

Orden requerido en todo archivo `.vue` de `views` y `components`:

1. `<template>`
2. `<script setup lang="ts">`
3. `<style>` (solo si aplica)

No usar `lang="tsx"` salvo necesidad real de JSX.

### 5.4 PrimeVue

1. PrimeVue esta configurado en `frontend/src/renderer/plugins/primevue.ts`.
2. Registro de componentes en forma explicita (enfoque seguro).
3. Se evito auto-registro masivo para prevenir errores de runtime por dependencias opcionales.

## 6. Integracion Electron + Backend

### 6.1 Flujo de arranque

1. Electron main inicia backend en entorno dev (`BackendRunner.ts`).
2. Se hace health-check a `GET /health`.
3. La UI consume endpoints cuando backend esta disponible.

### 6.2 Convencion operativa importante

Si aparece `404` en endpoints aparentemente correctos, verificar que no exista un backend viejo ocupando el puerto `7007`.
Debe existir un solo proceso backend activo con el contrato actual.

## 7. Limpieza y refactorizacion ya realizadas

1. Eliminacion de artefactos no esenciales y estructura redundante.
2. Homologacion de capas backend por dominio.
3. Introduccion de base reutilizable `CrudCapabilitiesController`.
4. Estandarizacion de DTOs por accion.
5. Estandarizacion de rutas API con regla singular/plural.
6. Reorganizacion frontend por `views`, `components`, `stores`, `plugins`.
7. Eliminacion de vistas y pruebas temporales que no eran foco.

## 8. Checklist de mantenimiento

Antes de dar un cambio por valido:

1. `frontend`: `npm run lint`
2. `frontend`: `npx vue-tsc --noEmit`
3. `backend`: `gradlew.bat build`
4. Validar endpoints desde frontend real (sin procesos backend legacy en paralelo).

## 9. Regla de evolucion

Toda nueva funcionalidad debe respetar la jerarquia y convenios de este documento.
Si se cambia arquitectura, primero se actualiza este archivo y luego se implementa codigo.
