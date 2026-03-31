# CustomDataTableV1

## Objetivo

`CustomDataTableV1` es la base limpia y portable de la tabla reutilizable. Su alcance actual es deliberadamente pequeno: renderizado de columnas, paginacion, orden, filtros simples, acciones por fila y un contrato de query estable.

La intencion de esta carpeta es que pueda copiarse a otro proyecto sin arrastrar dependencias del componente heredado.

## Archivos del modulo

- `GenericDataTable.vue`: componente visual principal.
- `useGenericDataTableQuery.ts`: normalizacion de query y traduccion a filtros de PrimeVue.
- `generic-data-table.types.ts`: tipos publicos del modulo.
- `custom-data-table-v1.public.ts`: punto de entrada recomendado para importar el modulo.

## Dependencias al copiarlo a otro proyecto

Antes de copiar la carpeta, el proyecto destino necesita:

- Vue 3 con `<script setup lang="ts">`.
- PrimeVue con estos componentes disponibles: `DataTable`, `Column`, `Button`, `Dropdown`, `InputNumber`, `InputText`, `Tag`.
- Variables CSS equivalentes o compatibles con:
  - `--app-space-3`
  - `--app-space-4`
  - `--app-text-muted`

Si el proyecto destino no usa esas variables, basta con redefinirlas o ajustar el bloque `<style scoped>`.

## API publica recomendada

Importa siempre desde `custom-data-table-v1.public.ts` para que la carpeta tenga un punto de entrada claro y no se acople a rutas internas.

```ts
import {
  GenericDataTable,
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

## Patron de uso recomendado

La tabla esta pensada como componente controlado: el padre mantiene la query, escucha `update:query` y recarga datos.

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

## Convenciones recomendadas para columnas

- Usa `type: 'actions'` solo para una columna dedicada a acciones.
- Usa `filterType` cuando el filtro no coincide con `type`.
- Usa `format` para presentacion, no para transformar el dato base.
- Usa `align: 'right'` para importes, factores y porcentajes.

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
