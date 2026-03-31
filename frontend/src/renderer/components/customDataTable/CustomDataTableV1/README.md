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

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  GenericDataTable,
  type GenericDataTableColumn,
  type GenericDataTableQuery
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

function onQueryChange(nextQuery: GenericDataTableQuery) {
  query.value = nextQuery
  void loadData()
}

function onAction(payload: { actionKey: string; row: CategoryRow }) {
  if (payload.actionKey === 'edit') {
    return
  }

  if (payload.actionKey === 'delete') {
    return
  }
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
    @update:query="onQueryChange"
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
