# Backend (Javalin)

Backend local de la app desktop, organizado por responsabilidad para facilitar crecimiento tipo Spring-style.

## Estructura de paquetes

- `com.vutron.backend.db`
  - Responsable del esquema de base de datos.
  - Crea y mantiene tablas, relaciones, indices y seeds.
  - En cada arranque ejecuta validacion/creacion idempotente del esquema.

- `com.vutron.backend.manager`
  - Capa funcional por dominio.
  - Subestructura por feature: `endpoint`, `service`, `repository`, `query`.
  - Aqui vive el CRUD y las queries de negocio por tabla.

- `com.vutron.backend.io`
  - Responsable de import/export de proyectos.
  - Maneja serializacion y restauracion de proyecto + usuarios asociados.

## Run

1. Instalar Java 21.
2. Desde `backend` ejecutar:

```bash
./gradlew run
```

Servicio por defecto:

- `http://127.0.0.1:7007`

## Endpoints actuales

- `GET /health`
- `GET /api/app-messages/hello-world`
- `GET /api/projects`
- `POST /api/projects`
- `DELETE /api/projects/{id}`
- `POST /api/projects/export`
- `POST /api/projects/import`

## Base de datos

- Ruta SQLite por defecto: `../db/app.sqlite`
- Override por entorno: `APP_DB_PATH`
- Validacion/actualizacion de esquema: `DatabaseSchemaManager.ensureSchema(...)`

### Esquema que se garantiza al arrancar

- Tabla `projects`
- Tabla `users` con relacion `users.project_id -> projects.id`
- Tabla `app_messages`
- Indices para `projects` y `users`
- Seed `HELLO_WORLD`
- Seed de super admin global

## Regla de usuarios por proyecto

- Cada proyecto tiene su propia coleccion de usuarios (`1 -> N`).
- Los usuarios del proyecto no se comparten entre proyectos.
- Existe un unico super admin global con acceso transversal.
- Se persiste siempre hash, nunca password en claro.

La estrategia de hash actual usa SHA-256 sobre:

- `lowercase(email) + ':' + password`
