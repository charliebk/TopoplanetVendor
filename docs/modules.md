# Modulos Funcionales Futuros

## 1. Objetivo

Definir el backlog funcional de alto nivel para evolucionar el producto sin alterar la arquitectura base (monolito modular Electron + Vue + Javalin + SQLite).

## 2. Modulos

## 2.1 Project Hub

- Proposito: crear, abrir, editar, duplicar y archivar proyectos.
- Backend esperado: `manager/project` + `manager/projectversion`.
- Frontend esperado: `views/ProjectHubView`, `renderer/stores/project/project.store.ts`, `renderer/stores/project/project.types.ts`.
- Estado: base parcial ya existente (`project`).

## 2.2 Workspace Shell

- Proposito: layout principal al abrir un proyecto (sidebar, header, breadcrumb, contenido).
- Backend esperado: lectura de proyecto activo y metadata minima.
- Frontend esperado: `views/ProjectWorkspaceView`, `components/layout`.
- Estado: futuro.

## 2.3 Dashboard

- Proposito: metricas agregadas por proyecto y ranking de vendors.
- Backend esperado: `manager/dashboard`.
- Frontend esperado: `views/dashboard`, `components/dashboard`, `stores/dashboard`.
- Estado: futuro.

## 2.4 Resumen Ejecutivo

- Proposito: vista ejecutiva de estado del proyecto y principales hallazgos.
- Backend esperado: `manager/dashboard` y/o `manager/report`.
- Frontend esperado: `views/executive-summary`.
- Estado: futuro.

## 2.5 Vendors

- Proposito: CRUD de vendors por proyecto.
- Backend esperado: `manager/vendor`.
- Frontend esperado: `views/vendors`, `stores/vendor`.
- Estado: futuro.

## 2.6 Productos por Vendor

- Proposito: gestionar productos/soluciones asociadas a cada vendor.
- Backend esperado: `manager/product`.
- Frontend esperado: `views/products`, `stores/product`.
- Estado: futuro.

## 2.7 Preguntas

- Proposito: gestionar banco de preguntas y estructura por categorias.
- Backend esperado: `manager/question`, `manager/variable`.
- Frontend esperado: `views/questions`, `stores/question`.
- Estado: futuro.

## 2.8 Requisitos Funcionales

- Proposito: gestionar subset funcional de preguntas/requisitos.
- Backend esperado: extension de `manager/question`.
- Frontend esperado: `views/functional-requirements`.
- Estado: futuro.

## 2.9 Requisitos Tecnicos

- Proposito: gestionar subset tecnico de preguntas/requisitos.
- Backend esperado: extension de `manager/question`.
- Frontend esperado: `views/technical-requirements`.
- Estado: futuro.

## 2.10 Respuestas

- Proposito: capturar respuestas por vendor, producto y pregunta, con opcion de respuesta y score resuelto.
- Backend esperado: `manager/response`.
- Frontend esperado: `views/responses`, `stores/response`.
- Estado: futuro.

## 2.11 Pesos y Escalas

- Proposito: definir escalas de puntuacion y pesos por categoria/pregunta.
- Backend esperado: `manager/scale`, `manager/scaleoption`, `manager/weight`.
- Frontend esperado: `views/settings/weights-scales`, `stores/settings`.
- Estado: futuro.

## 2.12 Variables del Proyecto

- Proposito: administrar catalogos genericos por proyecto (Category, QuestionType, RequirementLevel y otros).
- Backend esperado: `manager/settings`.
- Frontend esperado: `views/settings/variables`, `stores/settings`.
- Estado: futuro.

## 2.13 Versiones de Configuracion

- Proposito: versionar cambios de configuracion y activar version vigente.
- Backend esperado: `manager/projectversion`.
- Frontend esperado: `views/versions`, `stores/version`.
- Estado: futuro.

## 2.14 Importacion/Exportacion

- Proposito: respaldo y restauracion de proyecto completo.
- Backend esperado: `io/project/Export`, `io/project/Import`.
- Frontend esperado: `views/settings/import-export`, `stores/importProject`, `stores/exportProject`.
- Estado: base parcial ya existente.

## 2.15 Auditoria

- Proposito: trazabilidad de cambios por entidad y usuario.
- Backend esperado: `manager/audit`.
- Frontend esperado: `views/audit`, `stores/audit`.
- Estado: futuro.

## 3. Orden sugerido de implementacion

1. Project Hub (cerrar CRUD completo y UX).
2. Workspace Shell.
3. Catalogos base: variable, scale, scaleoption.
4. Vendors y productos.
5. Preguntas y requisitos funcionales/tecnicos.
6. Respuestas.
7. Pesos, calculo por categoria y dashboard.
8. Versiones + auditoria.
9. Importacion/exportacion avanzada.

Nota de alcance:

- El modulo de evaluadores queda fuera de la fase actual y se reconsidera en una fase posterior.

## 4. Regla de arquitectura para todos los modulos

Cada modulo nuevo debe cumplir:

1. Backend por dominio con `Controller`, `DTO`, `Repository`, `Query`, `Services`.
2. Endpoints registrados en `App.java`.
3. Frontend consume solo API backend por stores.
4. Sin acceso SQLite desde frontend.
5. Sin microservicios.
6. En frontend, cada store usa archivos explicitos `<domain>.store.ts` y `<domain>.types.ts`.
7. En SQL, los nombres fisicos de tablas se definen siempre en singular.
