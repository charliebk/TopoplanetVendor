# CustomDataTableV1

## Objetivo

`CustomDataTableV1` es la base limpia y portable de la tabla reutilizable. Su alcance actual es deliberadamente pequeno: renderizado de columnas, paginacion, orden, filtros simples, acciones por fila y un contrato de query estable.

La intencion de esta carpeta es que pueda copiarse a otro proyecto sin arrastrar dependencias del componente heredado.

## Archivos del modulo

- `GenericDataTable.vue`: componente visual principal.
- `GenericDataTableCountBar.vue`: contador desacoplado reutilizable para listados.
- `tableExport.ts`: helpers publicos para exportar CSV y preparar impresion.
- `toBatchRequest.ts`: helper publico para traducir seleccion detallada a request batch.
- `useGenericDataTableQuery.ts`: normalizacion de query y traduccion a filtros de PrimeVue.
- `useGenericDataTableSelection.ts`: seleccion avanzada con `select all filtered`, overrides y payload estable.
- `generic-data-table.types.ts`: tipos publicos del modulo.
- `custom-data-table-v1.public.ts`: punto de entrada recomendado para importar el modulo.

## Dependencias al copiarlo a otro proyecto

Antes de copiar la carpeta, el proyecto destino necesita:

- Vue 3 con `<script setup lang="ts">`.
- PrimeVue con estos componentes disponibles: `DataTable`, `Column`, `Button`, `Checkbox`, `Dropdown`, `InputNumber`, `InputText`, `Tag`, `Calendar`.
- Variables CSS equivalentes o compatibles con:
  - `--app-space-3`
  - `--app-space-4`
  - `--app-text-muted`

Si el proyecto destino no usa esas variables, basta con redefinirlas o ajustar el bloque `<style scoped>`.

## API publica recomendada

Importa siempre desde `custom-data-table-v1.public.ts` para que la carpeta tenga un punto de entrada claro y no se acople a rutas internas.

```ts
import {
  exportDataTableCsv,
  GenericDataTable,
  GenericDataTableCountBar,
  prepareDataTablePrint,
  toBatchRequest,
  useGenericDataTableSelection,
  useGenericDataTableQuery,
  type GenericDataTableColumn,
  type GenericDataTableQuery,
  type GenericDataTableRow
} from './CustomDataTableV1/custom-data-table-v1.public'
```

## Superficie publica congelada en Sprint 1

Durante Sprint 1, la API publica de V1 queda definida asi:

- Componente: `GenericDataTable`.
- Helper publico: `useGenericDataTableQuery`.
- Tipos de modelo: `GenericDataTableRow`, `GenericDataTableColumn`, `GenericDataTableQuery`, `GenericDataTableOption`, `GenericDataTableAction`.
- Tipos de componente: `GenericDataTableProps`, `GenericDataTableEmits`, `GenericDataTableEventName`.
- Tipos de eventos: `GenericDataTableUpdateQueryPayload`, `GenericDataTableRowClickPayload`, `GenericDataTableActionPayload`.
- Helpers tipados: `GenericDataTableQueryController`, `GenericDataTableQueryChangeHandler`, `GenericDataTableRowClickHandler`, `GenericDataTableActionHandler`.

Todo consumo externo debe importar solo desde `custom-data-table-v1.public.ts`.

## Compatibilidad esperada con PrimeVue

Compatibilidad esperada para esta V1:

- PrimeVue DataTable con `paginator`, `lazy`, `filters`, `row-click`, `page` y `sort`.
- PrimeVue Column con `body` y `filter` slots.
- PrimeVue Button para acciones por fila y boton de limpiar filtros.
- PrimeVue InputText para filtro global y filtros de texto.
- PrimeVue InputNumber para filtros numericos.
- PrimeVue Calendar para filtros de fecha.
- PrimeVue Dropdown para filtros booleanos y select.
- PrimeVue Tag para render booleano basico.

Compatibilidad asumida por implementacion actual:

- API de PrimeVue alineada con el uso ya presente en este proyecto.
- Componentes disponibles como imports por modulo, no solo por registro global.
- El consumidor decide si usa client-side o server-side; la tabla no acopla stores ni cliente HTTP.

## Contrato minimo del componente

### Props principales

- `columns`: definicion de columnas.
- `rows`: filas ya cargadas.
- `query`: estado externo de pagina, orden y filtros.
- `loading`: estado de carga.
- `lazy`: indica si la carga depende del consumidor.
- `totalRecords`: total remoto cuando hay paginacion server-side.

### Eventos

- `update:query`: emite la query normalizada al cambiar pagina, orden o filtros.
- `row-click`: emite la fila pulsada.
- `action`: emite `{ actionKey, row }` para columnas de acciones.
- `load`: emite `{ query, rows, totalRecords, overallTotal, baselineTotal }` al resolver un `dataProvider`.
- `provider-error`: emite `{ query, message, cause }` cuando falla un `dataProvider`.
- `refresh`: emite `{ query, providerMode }` al usar el boton integrado de refresh o la API expuesta.
- `selection-change`: emite un payload estable para operaciones batch con `query`, `allFiltered`, `selectedKeys`, `unselectedKeys`, `selectedCount` y `selectedRows` visibles/materializados.
- El mismo payload incluye `batch`, un bloque derivado listo para backend con `strategy`, `filterQuery`, `includeKeys`, `excludeKeys`, `disabledKeys`, `ready` y `reason`.
- Sprint 8 anade `exportDataTableCsv(...)` y `prepareDataTablePrint(...)` para reutilizar el mismo contrato de columnas al exportar.

### Props de seleccion

- `selectionMode`: `none` o `multiple`.
- `showSelectionToolbar`: muestra acciones de seleccionar pagina, seleccionar filtrado y limpiar seleccion.
- `selectPageLabel`, `selectFilteredLabel`, `clearSelectionLabel`: textos de los controles de seleccion.
- `disabledRowSelectionScope`: `visible` o `filtered`. En provider mode, `select all filtered` solo es seguro cuando el alcance es `filtered`.
- `disabledFilteredRowKeys`: claves de filas deshabilitadas conocidas para el resultado filtrado completo.
- `disabledFilteredRowsResolved`: confirma que `disabledFilteredRowKeys` ya representa el conjunto filtrado actual.

### Props de toolbar y estados

- `showRefreshButton`, `refreshLabel`: boton integrado de recarga.
- `showCountBar`, `countBarPosition`, `countBarShowShown`: contador desacoplado arriba, abajo o en ambos extremos.
- `emptyMessage`, `loadingMessage`, `errorMessage`: mensajes parametrizables para estados comunes.
- `showClearFiltersButton`, `clearFiltersLabel`: limpieza integrada de filtros.

### Interaccion de filas y acciones

- `rowDisabled`: boolean o resolver por fila para bloquear `row-click`, acciones y seleccion visible en filas deshabilitadas.
- Politica de interaccion: los controles interactivos internos, como checkboxes y botones de accion, siempre frenan la propagacion; `row-click` solo se emite para filas habilitadas.
- `GenericDataTableAction` soporta `tooltip`, `class`, `disabled`, `severity`, `label` e `icon` para modelar acciones por fila.
- `tooltip` y `class` aceptan valor fijo o resolver por fila.

### Exportacion e impresion

- `exportable`: permite excluir columnas concretas del CSV y del payload de impresion.
- `exportHeader`: redefine la cabecera exportada sin tocar el encabezado visible.
- `exportKey`: usa otra propiedad de la fila al exportar; por defecto se toma `displayField || field`.
- `exportFormat`: transforma el valor exportado sin afectar el render visual.
- Las columnas `actions` se excluyen automaticamente aunque no declares `exportable: false`.
- `exportDataTableCsv({ columns, rows })` genera un CSV a partir del mismo contrato de columnas que consume la tabla.
- `prepareDataTablePrint({ columns, rows })` devuelve `{ title, generatedAt, columns, rows }` listo para una vista de impresion.
- La API expuesta via `ref` tambien incorpora `exportCsv()` y `preparePrint()`; por defecto operan sobre las filas visibles/resueltas, pero aceptan un array explicito si la pantalla necesita otro subconjunto.

### Booleanos y accesibilidad

- `booleanLabels`: personaliza etiquetas `true`, `false` y `null`.
- `booleanTag`: permite usar `Tag` de PrimeVue o render texto plano.
- `booleanTagSeverity`: ajusta la severidad visual por estado booleano.
- La V1 aplica `aria-label` a checkboxes de seleccion, botones de accion y celdas booleanas.

### Slots de extension

- `toolbar-main`: contenido adicional junto al filtro global y resumen de seleccion.
- `toolbar-actions`: acciones globales del consumidor en la barra superior.
- `count-bar`: contenido adicional dentro del contador desacoplado.
- `empty`, `loading`, `error`: sobreescritura visual de estados sin reemplazar el componente completo.

### API de seleccion

Sprint 5 anade dos superficies complementarias:

- Composable publico `useGenericDataTableSelection` para gestionar seleccion avanzada fuera del componente si el consumidor lo necesita.
- API expuesta por `GenericDataTable` via `ref` con `selectAllPage()`, `selectAllFiltered()`, `clearSelection()`, `refreshVisibleRows()` y `getSelectionPayload()`.

El payload de seleccion queda pensado para backend batch operations:

- `allFiltered=false`: usar `selectedKeys`.
- `allFiltered=true`: usar `query` + `unselectedKeys` como exclusiones.
- Cuando hay `rowDisabled`, el payload tambien expone `rowDisabledSelectionScope`, `disabledRowsResolved`, `disabledKeys`, `disabledCount` y `selectableFilteredCount`.
- En provider mode, si solo se conocen filas deshabilitadas visibles, la tabla bloquea `select all filtered` para no prometer una seleccion masiva incorrecta.

Contrato simplificado para endpoints batch:

- `selection.batch.strategy === 'includeKeys'`: enviar `selection.batch.includeKeys`.
- `selection.batch.strategy === 'filterQuery'`: enviar `selection.batch.filterQuery` + `selection.batch.excludeKeys`.
- `selection.batch.disabledKeys` siempre queda disponible si el backend quiere auditar o informar exclusiones.
- `selection.batch.ready === false` indica que todavia no es seguro ejecutar una operacion batch filtrada.
- `toBatchRequest(selection)` devuelve ese mismo contrato simplificado sin que el consumidor tenga que leer `selection.batch` directamente.

Desde Sprint 6, la API expuesta via `ref` tambien incorpora `refresh()` y `clearFilters()` para que la vista consumidora pueda disparar recarga o limpiar filtros sin envolver la tabla. Desde Sprint 8, ese mismo `ref` tambien puede llamar `exportCsv()` y `preparePrint()`.

## Ejemplo de acciones por fila

```ts
const columns: Array<GenericDataTableColumn<AuditRow>> = [
  {
    field: 'originActive',
    header: 'Origin Status',
    type: 'boolean',
    booleanLabels: {
      trueLabel: 'Active source',
      falseLabel: 'Legacy source'
    },
    booleanTag: true,
    booleanTagSeverity: {
      true: 'success',
      false: 'warning'
    }
  },
  {
    field: 'id',
    header: 'Actions',
    type: 'actions',
    actions: [
      {
        key: 'inspect',
        icon: 'pi pi-search',
        label: 'Inspect',
        tooltip: 'Open the audit detail panel'
      },
      {
        key: 'approve',
        icon: 'pi pi-check',
        label: 'Approve',
        class: (row) => (row.compliance >= 90 ? 'is-ready' : 'is-muted'),
        disabled: (row) => row.compliance < 90
      }
    ]
  }
]

const isRowDisabled = (row: AuditRow) => !row.originActive
const disabledFilteredRowKeys = computed(() =>
  filteredRows.value.filter(isRowDisabled).map((row) => String(row.id))
)

const csvContent = exportDataTableCsv({
  columns,
  rows: filteredRows.value
})

const printPayload = prepareDataTablePrint(
  {
    columns,
    rows: filteredRows.value
  },
  {
    title: 'Audit export'
  }
)
```

```vue
<GenericDataTable
  :columns="columns"
  :rows="rows"
  :query="query"
  :row-disabled="isRowDisabled"
  disabled-row-selection-scope="filtered"
  :disabled-filtered-row-keys="disabledFilteredRowKeys"
  disabled-filtered-rows-resolved
  @row-click="onRowClick"
  @action="onAction"
/>
```

## Ejemplo de toolbar integrada

```vue
<GenericDataTable
  :columns="columns"
  :query="query"
  :data-provider="dataProvider"
  show-refresh-button
  show-count-bar
  count-bar-position="both"
  empty-message="No rows found"
  loading-message="Loading rows..."
  error-message="Rows could not be loaded"
  @refresh="onRefresh"
>
  <template #toolbar-main="slotProps">
    <span>{{ slotProps.filteredTotal }} filtered rows</span>
  </template>

  <template #toolbar-actions>
    <Button text label="Run batch" @click="runBatchAction" />
  </template>

  <template #count-bar="slotProps">
    <span>Visible {{ slotProps.shown }} of {{ slotProps.filteredTotal }}</span>
  </template>
</GenericDataTable>
```

## Patron de uso recomendado

La tabla esta pensada como componente controlado: el padre mantiene la query, escucha `update:query` y recarga datos.

Ademas, desde Sprint 4 tambien puede operar en modo provider: el padre sigue siendo duenio de la query publica, pero la tabla ejecuta un `dataProvider` asincrono y normaliza `rows`, `totalRecords`, `overallTotal`, `baselineTotal` y errores.

Para Sprint 8, la regla practica es esta:

- Si quieres exportar la pagina visible o el resultado ya resuelto dentro del componente, usa `tableRef.value?.exportCsv()` o `tableRef.value?.preparePrint()`.
- Si quieres exportar un subconjunto especifico o un resultado filtrado que la pantalla ya resolvio, usa `exportDataTableCsv({ columns, rows })` o `prepareDataTablePrint({ columns, rows })` directamente desde la API publica.

### Ejemplo minimo de consumo controlado

Este caso usa la tabla como componente controlado sin `lazy`; el padre conserva la query publica, pero las filas ya viven en memoria.

```vue
<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  GenericDataTable,
  type GenericDataTableColumn,
  type GenericDataTableQuery,
  type GenericDataTableQueryChangeHandler
} from '@/renderer/components/customDataTable/CustomDataTableV1/custom-data-table-v1.public'

type DemoRow = {
  id: number
  code: string
  name: string
  enabled: boolean
}

const query = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'name',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const sourceRows = ref<DemoRow[]>([
  { id: 1, code: 'CAT', name: 'Category', enabled: true },
  { id: 2, code: 'REQ', name: 'Requirement', enabled: false }
])

const columns: Array<GenericDataTableColumn<DemoRow>> = [
  { field: 'code', header: 'Code', sortable: true, filterable: true },
  { field: 'name', header: 'Name', sortable: true, filterable: true },
  {
    field: 'enabled',
    header: 'Enabled',
    type: 'boolean',
    filterable: true,
    align: 'center'
  }
]

const visibleRows = computed(() => sourceRows.value)

const onQueryChange: GenericDataTableQueryChangeHandler = (nextQuery) => {
  query.value = nextQuery
}
</script>

<template>
  <GenericDataTable
    :columns="columns"
    :rows="visibleRows"
    :query="query"
    :loading="false"
    @update:query="onQueryChange"
  />
</template>
```

### Ejemplo minimo de lazy loading

Este caso usa `lazy` para delegar al padre la carga remota. Es el patron recomendado para vistas con backend paginado.

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  GenericDataTable,
  type GenericDataTableActionHandler,
  type GenericDataTableActionPayload,
  type GenericDataTableColumn,
  type GenericDataTableQuery,
  type GenericDataTableQueryChangeHandler
} from '@/renderer/components/customDataTable/CustomDataTableV1/custom-data-table-v1.public'

type CategoryRow = {
  id: number
  code: string
  name: string
  factor: number
  enabled: boolean
}

const loading = ref(false)
const rows = ref<CategoryRow[]>([])
const totalRecords = ref(0)
const query = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'name',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const columns: Array<GenericDataTableColumn<CategoryRow>> = [
  { field: 'code', header: 'Code', sortable: true, filterable: true },
  { field: 'name', header: 'Name', sortable: true, filterable: true },
  {
    field: 'factor',
    header: 'Factor',
    type: 'number',
    sortable: true,
    filterable: true,
    align: 'right'
  },
  {
    field: 'enabled',
    header: 'Enabled',
    type: 'boolean',
    filterable: true,
    align: 'center'
  },
  {
    field: 'id',
    header: 'Actions',
    type: 'actions',
    align: 'center',
    actions: [
      { key: 'edit', icon: 'pi pi-pencil', label: 'Edit' },
      {
        key: 'delete',
        icon: 'pi pi-trash',
        label: 'Delete',
        severity: 'danger'
      }
    ]
  }
]

async function loadData() {
  loading.value = true
  try {
    // Sustituir por el store o el cliente HTTP real del proyecto.
    // La idea es mapear query.value al contrato del backend.
    rows.value = []
    totalRecords.value = 0
  } finally {
    loading.value = false
  }
}

const onAction: GenericDataTableActionHandler<CategoryRow> = (
  payload: GenericDataTableActionPayload<CategoryRow>
) => {
  if (payload.actionKey === 'edit') {
    return
  }

  if (payload.actionKey === 'delete') {
    return
  }
}

const onLazyQueryChange: GenericDataTableQueryChangeHandler = (nextQuery) => {
  query.value = nextQuery
  void loadData()
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <GenericDataTable
    :columns="columns"
    :rows="rows"
    :query="query"
    :loading="loading"
    :lazy="true"
    :total-records="totalRecords"
    @update:query="onLazyQueryChange"
    @action="onAction"
  />
</template>
```

### Ejemplo minimo de provider mode

### Ejemplo minimo de seleccion avanzada

Este caso usa provider mode y escucha `selection-change` para enviar operaciones batch sin perder el contexto filtrado.

```vue
<script setup lang="ts">
import { ref } from 'vue'
import {
  GenericDataTable,
  toBatchRequest,
  type GenericDataTableExpose,
  type GenericDataTableQuery,
  type GenericDataTableSelectionChangeHandler,
  type GenericDataTableSelectionPayload
} from '@/renderer/components/customDataTable/CustomDataTableV1/custom-data-table-v1.public'

type AuditRow = {
  id: number
  code: string
}

const tableRef = ref<GenericDataTableExpose<AuditRow> | null>(null)
const query = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'code',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const selection = ref<GenericDataTableSelectionPayload<AuditRow> | null>(null)

const onSelectionChange: GenericDataTableSelectionChangeHandler<AuditRow> = (
  payload
) => {
  selection.value = payload

  const batchRequest = toBatchRequest(payload)

  if (batchRequest?.ready) {
    if (batchRequest.strategy === 'includeKeys') {
      console.info('Batch by keys', batchRequest.includeKeys)
    } else {
      console.info(
        'Batch by query',
        batchRequest.filterQuery,
        batchRequest.excludeKeys
      )
    }
  }
}

function refreshSelectionSnapshot() {
  tableRef.value?.refreshVisibleRows()
  selection.value = tableRef.value?.getSelectionPayload() ?? null
}
</script>

<template>
  <GenericDataTable
    ref="tableRef"
    :columns="columns"
    :rows="[]"
    :query="query"
    :data-provider="dataProvider"
    selection-mode="multiple"
    @selection-change="onSelectionChange"
  />
</template>
```

Este caso elimina la logica manual de carga en la vista. La tabla recarga automaticamente cuando cambia `query` y conserva el mismo contrato de filtros y orden.

```vue
<script setup lang="ts">
import { ref } from 'vue'
import {
  GenericDataTable,
  type GenericDataTableColumn,
  type GenericDataTableDataProvider,
  type GenericDataTableLoadHandler,
  type GenericDataTableProviderErrorHandler,
  type GenericDataTableQuery,
  type GenericDataTableQueryChangeHandler
} from '@/renderer/components/customDataTable/CustomDataTableV1/custom-data-table-v1.public'

type AuditRow = {
  id: number
  code: string
  owner: string
  reviewedAt: string
}

const query = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'reviewedAt',
  sortOrder: -1,
  globalFilter: null,
  filters: {}
})

const columns: Array<GenericDataTableColumn<AuditRow>> = [
  { field: 'code', header: 'Code', sortable: true, filterable: true },
  { field: 'owner', header: 'Owner', sortable: true, filterable: true },
  {
    field: 'reviewedAt',
    header: 'Reviewed At',
    type: 'date',
    sortable: true,
    filterable: true
  }
]

const dataProvider: GenericDataTableDataProvider<AuditRow> = async ({
  query
}) => {
  const response = await fetch(
    '/api/audits?' +
      new URLSearchParams({
        page: String(query.page),
        size: String(query.size)
      })
  )

  if (!response.ok) {
    return {
      ok: false,
      message: 'Failed to load audits'
    }
  }

  const data = await response.json()

  return {
    ok: true,
    rows: data.rows,
    totalRecords: data.total,
    overallTotal: data.overallTotal
  }
}

const onQueryChange: GenericDataTableQueryChangeHandler = (nextQuery) => {
  query.value = nextQuery
}

const onLoad: GenericDataTableLoadHandler<AuditRow> = (payload) => {
  console.info('Provider load resolved', payload)
}

const onProviderError: GenericDataTableProviderErrorHandler = (payload) => {
  console.error('Provider load failed', payload)
}
</script>

<template>
  <GenericDataTable
    :columns="columns"
    :rows="[]"
    :query="query"
    :data-provider="dataProvider"
    @update:query="onQueryChange"
    @load="onLoad"
    @provider-error="onProviderError"
  />
</template>
```

## Convenciones recomendadas para columnas

- Usa `type: 'actions'` solo para una columna dedicada a acciones.
- Usa `filterType` cuando el filtro no coincide con `type`.
- Usa `format` para presentacion, no para transformar el dato base.
- Usa `align: 'right'` para importes, factores y porcentajes.

## Estrategia de compatibilidad de Sprint 2

La compatibilidad entre `type`, `filterType` y renderizado queda definida asi:

- `type` controla el render por defecto de la celda.
- `filterType` controla el editor de filtro; si no existe, la tabla reutiliza `type`.
- `displayField` permite mostrar un campo distinto de `field` sin cambiar la identidad de la columna.
- `backendField` permite que la query emitida use otra clave distinta de `field` para filtros y orden.

Resolucion actual por defecto:

- `text` renderiza texto y usa `InputText`.
- `number` renderiza numerico con decimales y usa `InputNumber`.
- `integer` renderiza numerico entero y usa `InputNumber` sin decimales.
- `percent` renderiza numerico con sufijo `%` y filtra como numerico.
- `date` renderiza fecha local y usa `Calendar`.
- `boolean` renderiza `Tag` y usa `Dropdown` booleano.
- `select` y `list` usan `Dropdown` con `filterOptions`.
- `idIcon` renderiza icono y texto opcional usando `idIconClass`, `displayField` y `tooltipField`.
- `actions` renderiza botones de accion y no tiene filtro por defecto.

Ejemplo de columna con metadatos nuevos:

```ts
{
  field: 'categoryId',
  header: 'Category',
  type: 'list',
  filterable: true,
  displayField: 'categoryName',
  backendField: 'categoryId',
  filterOptions: [
    { label: 'Safety', value: 1 },
    { label: 'Quality', value: 2 }
  ]
}
```

```ts
{
  field: 'originId',
  header: 'Origin',
  type: 'idIcon',
  displayField: 'originName',
  tooltipField: 'originDescription',
  idIconClass: (row) => row.originActive ? 'pi pi-check-circle' : 'pi pi-ban'
}
```

## Estrategia de filtros de Sprint 3

Sprint 3 fija este contrato para filtros:

- `matchMode` acepta `contains`, `equals`, `startsWith` y `endsWith`.
- Si una columna define `backendField`, la query emitida usa esa clave para filtros y orden.
- Si una columna define `paramTransform`, la tabla aplica esa transformacion antes de emitir la query.
- Si una columna `date` no define `paramTransform`, la tabla normaliza `Date` a `YYYY-MM-DD`.
- Las columnas `list` y `select` emiten directamente el `value` de `filterOptions`.
- `clearFilters` reinicia filtro global y filtros por columna, emitiendo una query limpia y consistente.

Ejemplo de filtros backend-friendly:

```ts
{
  field: 'ownerName',
  header: 'Owner',
  filterable: true,
  matchMode: 'startsWith',
  backendField: 'ownerName'
}
```

```ts
{
  field: 'reviewedAt',
  header: 'Reviewed At',
  type: 'date',
  filterable: true,
  backendField: 'reviewedAt',
  paramTransform: (value) => {
    if (!(value instanceof Date)) return value
    const year = value.getFullYear()
    const month = `${value.getMonth() + 1}`.padStart(2, '0')
    const day = `${value.getDate()}`.padStart(2, '0')
    return `${year}-${month}-${day}`
  }
}
```

Validacion visual temporal del sprint:

- `ProjectMain.vue` contiene una demo mockeada con `lazy=true`.
- La demo usa columnas `list`, `date` e `idIcon`.
- La demo muestra la query emitida para validar que el consumidor ya recibe claves y valores listos para backend.

## Portabilidad

Para copiarlo a otro proyecto sin deuda innecesaria:

1. Copiar la carpeta completa `CustomDataTableV1`.
2. Ajustar el alias de importacion al punto de entrada `custom-data-table-v1.public.ts`.
3. Verificar los nombres de componentes PrimeVue en el proyecto destino.
4. Verificar variables CSS o adaptar el estilo local.
5. Crear un ejemplo minimo de smoke test con una tabla y dos columnas filtrables.

## Limites actuales de V1

Esta version todavia no cubre toda la funcionalidad del componente original. La hoja de trabajo para cerrar esa brecha esta en `ORIGINAL_PARITY_SPRINTS.md`.

## Regla de cierre de Sprint 1

Si un consumidor necesita importar algo desde `GenericDataTable.vue`, `generic-data-table.types.ts` o `useGenericDataTableQuery.ts`, entonces Sprint 1 no esta realmente cerrado. El contrato publico valido es solo `custom-data-table-v1.public.ts`.
