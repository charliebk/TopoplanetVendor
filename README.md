# Vutron Workspace (Electron + Vue + Javalin + SQLite)

Este repositorio ya no usa una estructura plana tipo template original. Ahora esta organizado para separar responsabilidades en tres bloques principales:

- frontend: Electron + Vue 3 + TypeScript
- backend: API local en Java con Javalin
- db: SQLite local y artefactos de datos

## Estructura actual

```text
/
	frontend/    # UI, proceso main/preload de Electron, empaquetado
	backend/     # API Javalin y acceso a datos (JDBC/SQLite)
	db/          # base sqlite local y archivos relacionados
	.github/     # workflows CI/CD
	.vscode/     # tareas y launch configs
```

## Requisitos

- Node.js 20+
- npm
- Java 21 (toolchain del backend)

## Desarrollo local

### 1) Frontend (Electron + Vue)

Desde la carpeta frontend:

```bash
cmd /c npm ci
cmd /c npm run dev
```

Nota Windows: en este entorno es mas estable ejecutar npm con `cmd /c`.

### 2) Backend (Javalin)

Desde la carpeta backend:

```bash
./gradlew run
```

Endpoint de salud:

- `GET http://127.0.0.1:7007/health`

## Calidad

Desde frontend:

```bash
cmd /c npm run lint
```

## Build Windows

Desde frontend:

```bash
cmd /c npm run build:pre
```

El pipeline de Windows usa `electron-builder` para generar paquetes portable e installer (NSIS).

## Estado funcional minimo esperado

1. Login contra backend Javalin.
2. Dashboard que consume mensaje HELLO_WORLD desde SQLite.
3. Gestion minima de proyectos en flujo backend-first.

## Plan de trabajo

Roadmap detallado por sprints:

- [template_electron_vue_javalin_sqlite_sprints_vutron.md](template_electron_vue_javalin_sqlite_sprints_vutron.md)
