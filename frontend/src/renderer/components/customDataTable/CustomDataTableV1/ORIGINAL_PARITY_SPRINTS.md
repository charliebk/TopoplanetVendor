# Original Parity Sprints

## Alcance

Esta lista convierte la brecha entre `CustomDataTableV1` y el componente original heredado en sprints concretos. La referencia funcional es el conjunto formado por:

- `CustomDataTable.vue`
- `datatable-types.ts`
- `useDataTableBase.ts`
- `useDataTableCommon.ts`
- `useTableSelection.ts`
- `ListingCountBar.vue`

El objetivo no es copiar la implementacion legacy, sino cubrir sus requisitos con una API mas limpia y portable.

## Sprint 1. Cerrar la API publica de V1

Objetivo: congelar el contrato que otro proyecto va a importar.

Estado actual: completado.

Entregables:

- Definir que exports forman parte de la API publica.
- Introducir nombres estables para tipos, eventos y helpers.
- Documentar compatibilidad esperada con PrimeVue.
- Anadir ejemplos minimos de consumo controlado y lazy loading.

Criterio de cierre:

- Cualquier consumidor importa solo desde `custom-data-table-v1.public.ts`.

Resultado aplicado en el modulo:

- Existe un punto de entrada publico unico: `custom-data-table-v1.public.ts`.
- Los tipos publicos de props, eventos, payloads y helpers ya tienen nombres estables.
- El README documenta la compatibilidad esperada con PrimeVue.
- El README incluye ejemplo minimo de consumo controlado y ejemplo minimo de lazy loading.

## Sprint 2. Paridad del modelo de columnas

Objetivo: cubrir la riqueza del `ColumnDef` original sin heredar su complejidad completa.

Estado actual: completado.

Entregables:

- Soporte para tipos adicionales: `integer`, `date`, `percent`, `idIcon`, `list`.
- Soporte para `displayField`, `backendField`, `minWidth`, `maxWidth`.
- Soporte para `tooltipField` e `idIconClass`.
- Estrategia clara de compatibilidad entre `type`, `filterType` y renderizado.

Criterio de cierre:

- La mayoria de columnas del componente original puede declararse sin adaptadores externos.

Resultado aplicado en el modulo:

- V1 soporta `integer`, `date`, `percent`, `idIcon` y `list`.
- V1 soporta `displayField`, `backendField`, `minWidth`, `maxWidth`.
- V1 soporta `tooltipField` e `idIconClass`.
- La compatibilidad entre `type`, `filterType`, renderizado y query esta documentada en el README y aplicada en el componente.

## Sprint 3. Paridad del sistema de filtros

Objetivo: igualar la flexibilidad funcional del filtrado original.

Estado actual: completado.

Entregables:

- Soporte para `matchMode` ampliado: `startsWith`, `endsWith`, `equals`, `contains`.
- Sincronizacion estable entre query externa y filtros internos.
- `paramTransform` por columna para traducir filtros al backend.
- Filtros de fecha y lista con contrato reutilizable.
- Limpieza total de filtros y emision consistente del estado filtrado.

Criterio de cierre:

- Un consumidor puede expresar filtros de backend sin parches en la vista contenedora.

Resultado aplicado en el modulo:

- V1 soporta `startsWith`, `endsWith`, `equals` y `contains`.
- La sincronizacion entre query externa y filtros internos se rehace al cambiar query o columnas.
- V1 soporta `paramTransform` por columna antes de emitir filtros.
- Las columnas `date`, `list` y `select` tienen contrato reutilizable de emision.
- `ProjectMain.vue` incluye una demo temporal lazy con columnas `list`, `date` e `idIcon` para validar la paridad visible.

## Sprint 4. Capa de proveedor de datos

Objetivo: evitar que cada pantalla reinvente la carga remota.

Estado actual: completado.

Entregables:

- Definir interfaz de `dataProvider` asincrono para V1.
- Normalizar respuestas tipo `rows + total + overallTotal`.
- Normalizar estados de fallo sin acoplarse a stores concretos.
- Incorporar baseline total para distinguir total filtrado vs total global.

Criterio de cierre:

- La tabla puede operar en modo controlado actual y en modo provider sin duplicar logica de carga.

Resultado aplicado en el modulo:

- V1 expone `GenericDataTableDataProvider` y tipos publicos para request, success, failure, estado y payloads de eventos.
- `GenericDataTable.vue` detecta provider mode y reutiliza la misma query publica para recargar datos asincronamente.
- `useGenericDataTableProvider.ts` normaliza `rows`, `totalRecords`, `overallTotal`, `baselineTotal` y errores.
- `ProjectMain.vue` valida el flujo con una demo temporal basada en `dataProvider` asincrono.

## Sprint 5. Seleccion avanzada

Objetivo: recuperar la capacidad que hoy aporta `useTableSelection.ts`.

Estado actual: completado.

Entregables:

- Seleccion simple por fila.
- Seleccion masiva de pagina actual.
- `select all filtered` con overrides por fila.
- Payload estable de seleccion para backend batch operations.
- API para refrescar la seleccion al cambiar las filas visibles.

Criterio de cierre:

- Las operaciones masivas pueden ejecutarse sin perder el contexto de seleccion filtrada.

Resultado aplicado en el modulo:

- V1 incorpora `useGenericDataTableSelection.ts` como capa desacoplada de seleccion avanzada.
- `GenericDataTable.vue` soporta seleccion por fila, seleccion masiva de pagina y `select all filtered` con overrides por fila.
- El evento `selection-change` emite un payload estable con `query`, `selectedKeys`, `unselectedKeys`, `selectedCount` y totales filtrados/globales.
- La tabla expone `refreshVisibleRows()` y `getSelectionPayload()` para resincronizar la seleccion cuando cambian las filas visibles.
- `ProjectMain.vue` valida el flujo en provider mode mostrando el payload de seleccion emitido.

## Sprint 6. Toolbar y componentes auxiliares

Objetivo: recuperar piezas de UX del original sin contaminar el core.

Estado actual: completado.

Entregables:

- Barra superior extensible con slots para acciones del consumidor.
- Contador de listado tipo `ListingCountBar` desacoplado.
- Mensajes parametrizables de empty/loading/error.
- Botones opcionales para clear filters, refresh y acciones globales.

Criterio de cierre:

- El consumidor no necesita envolver la tabla con otra toolbar para casos comunes.

Resultado aplicado en el modulo:

- `GenericDataTable.vue` incorpora slots `toolbar-main`, `toolbar-actions`, `count-bar`, `empty`, `loading` y `error`.
- V1 incorpora `GenericDataTableCountBar.vue` como componente desacoplado para total global, total filtrado, visibles y excluidos.
- La tabla expone boton opcional de refresh, evento `refresh` y API expuesta `refresh()` y `clearFilters()`.
- `ProjectMain.vue` valida la integracion con una demo que consume toolbar slots, count bar y refresh.

## Sprint 7. Acciones, filas y accesibilidad

Objetivo: completar el comportamiento interactivo que el original esperaba.

Estado actual: completado.

Entregables:

- Tooltips por accion.
- Clases visuales por accion.
- Politica clara de click en fila vs click en accion.
- Estados disabled por accion y por fila.
- Ajustes minimos de accesibilidad para controles y etiquetas booleanas.

Criterio de cierre:

- Las columnas de acciones cubren el mismo rango funcional que `ColumnAction` del original.

Resultado aplicado en el modulo:

- `GenericDataTable.vue` resuelve `tooltip`, `class`, `disabled` y `severity` por accion, y bloquea propagacion desde botones y checkboxes internos.
- V1 soporta `rowDisabled` para bloquear `row-click`, acciones y seleccion visible en filas deshabilitadas, con affordance visual diferenciada.
- Las columnas booleanas soportan `booleanLabels`, `booleanTag` y `booleanTagSeverity`, con `aria-label` minimo en controles y estado booleano.
- `ProjectMain.vue` valida el flujo con una demo temporal que incluye acciones por fila, filas legacy deshabilitadas y registro de la ultima interaccion.

## Sprint 8. Exportacion e impresion

Objetivo: recuperar una de las capacidades declaradas en los tipos legacy.

Entregables:

- `exportable`, `exportHeader`, `exportKey`, `exportFormat` por columna.
- API publica para exportar CSV y preparar impresion.
- Exclusion automatica de columnas de acciones.

Criterio de cierre:

- Una pantalla puede exportar sin reimplementar mapeo de columnas.

## Sprint 9. Opciones dinamicas para columnas tipo lista

Objetivo: cubrir el caso de filtros de listas remotas o cargadas por store.

Entregables:

- `optionItemsProvider` equivalente en V1.
- Mapeo configurable con `optionLabelField`, `optionValueField`, `optionTransform`.
- `includeAllOption` e `includeAllLabel`.
- Estrategia de recarga cuando las opciones dependen de otro store o query.

Criterio de cierre:

- Las columnas tipo lista funcionan sin que la pantalla tenga que preprocesar todas las opciones manualmente.

## Sprint 10. Endurecimiento para reutilizacion real

Objetivo: dejar el modulo listo para copiarse entre proyectos con bajo riesgo.

Entregables:

- Tests unitarios de tipos, query y acciones.
- Uno o dos ejemplos reales en vistas del proyecto.
- Checklist de portabilidad entre proyectos.
- Politica de versionado interno para futuras evoluciones de V1.

Criterio de cierre:

- El modulo puede copiarse a otro proyecto con una receta corta y verificable.

## Orden recomendado

Orden de implementacion sugerido:

1. Sprint 1
2. Sprint 2
3. Sprint 3
4. Sprint 4
5. Sprint 5
6. Sprint 6
7. Sprint 7
8. Sprint 9
9. Sprint 8
10. Sprint 10

La razon de ese orden es que exportacion, seleccion y toolbar dependen de cerrar antes el contrato de columnas, filtros y carga de datos.
