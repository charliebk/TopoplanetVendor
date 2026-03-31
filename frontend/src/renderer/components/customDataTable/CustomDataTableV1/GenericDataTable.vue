<template>
  <div class="generic-data-table">
    <div
      v-if="showToolbar"
      class="generic-data-table__toolbar"
    >
      <div class="generic-data-table__toolbar-main">
        <PrimeInputText
          v-if="showGlobalFilter"
          :model-value="globalFilterValue"
          :placeholder="globalFilterPlaceholder"
          class="generic-data-table__global-filter"
          @update:model-value="onGlobalFilterChange"
        />
      </div>

      <PrimeButton
        v-if="showClearFilters"
        type="button"
        icon="pi pi-filter-slash"
        text
        severity="secondary"
        :label="clearFiltersLabel"
        @click="onClearFilters"
      />
    </div>

    <DataTable
      :value="rows"
      :lazy="lazy"
      :loading="loading"
      :paginator="showPaginator"
      :rows="normalizedQuery.size"
      :total-records="resolvedTotalRecords"
      :first="first"
      :row-hover="rowHover"
      :striped-rows="stripedRows"
      :filter-display="filterDisplay"
      :show-gridlines="showGridlines"
      :data-key="rowKey"
      :filters="primeFilters"
      :rows-per-page-options="rowsPerPageOptions"
      @page="onPage"
      @sort="onSort"
      @row-click="onRowClick"
    >
      <template
        v-for="column in columns"
        :key="column.field"
      >
        <Column
          :field="column.field"
          :header="column.header"
          :sortable="column.sortable === true"
          :filter="column.filterable === true"
          :style="column.width ? { width: column.width } : undefined"
          :header-style="resolveAlignmentStyle(column.align, column.width)"
          :body-style="resolveAlignmentStyle(column.align, column.width)"
        >
          <template #body="slotProps">
            <slot
              :name="`cell-${column.field}`"
              :row="slotProps.data"
              :value="readValue(slotProps.data, column.field)"
              :column="column"
            >
              <div
                v-if="column.type === 'actions'"
                class="generic-data-table__actions"
                @click.stop
              >
                <PrimeButton
                  v-for="action in column.actions ?? []"
                  :key="action.key"
                  type="button"
                  :icon="action.icon"
                  :label="action.label"
                  :severity="action.severity ?? 'secondary'"
                  text
                  size="small"
                  :disabled="resolveActionDisabled(action, slotProps.data)"
                  @click.stop="
                    emit('action', {
                      actionKey: action.key,
                      row: slotProps.data
                    })
                  "
                />
              </div>

              <PrimeTag
                v-else-if="column.type === 'boolean'"
                :value="
                  formatBooleanValue(readValue(slotProps.data, column.field))
                "
                :severity="
                  readValue(slotProps.data, column.field)
                    ? 'success'
                    : 'secondary'
                "
              />

              <span v-else>
                {{ formatCellValue(column, slotProps.data) }}
              </span>
            </slot>
          </template>

          <template
            v-if="column.filterable"
            #filter
          >
            <div class="generic-data-table__filter-cell">
              <PrimeDropdown
                v-if="resolveFilterType(column) === 'select'"
                :model-value="primeFilters[column.field]?.value ?? null"
                :options="column.filterOptions ?? []"
                option-label="label"
                option-value="value"
                show-clear
                :placeholder="column.header"
                class="generic-data-table__filter-input"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value)
                "
              />

              <PrimeDropdown
                v-else-if="resolveFilterType(column) === 'boolean'"
                :model-value="primeFilters[column.field]?.value ?? null"
                :options="booleanFilterOptions"
                option-label="label"
                option-value="value"
                show-clear
                :placeholder="column.header"
                class="generic-data-table__filter-input"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value)
                "
              />

              <PrimeInputNumber
                v-else-if="resolveFilterType(column) === 'number'"
                :model-value="resolveNumericFilterValue(column.field)"
                input-class="generic-data-table__filter-input"
                :placeholder="column.header"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value ?? null)
                "
              />

              <PrimeInputText
                v-else
                :model-value="resolveTextFilterValue(column.field)"
                :placeholder="column.header"
                class="generic-data-table__filter-input"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value || null)
                "
              />
            </div>
          </template>
        </Column>
      </template>

      <template #empty>
        <div class="generic-data-table__state-message">
          {{ emptyMessage }}
        </div>
      </template>

      <template #loading>
        <div class="generic-data-table__state-message">
          {{ loadingMessage }}
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DataTable, {
  type DataTablePageEvent,
  type DataTableRowClickEvent,
  type DataTableSortEvent
} from 'primevue/datatable'
import Column from 'primevue/column'
import PrimeButton from 'primevue/button'
import PrimeDropdown from 'primevue/dropdown'
import PrimeInputNumber from 'primevue/inputnumber'
import PrimeInputText from 'primevue/inputtext'
import PrimeTag from 'primevue/tag'
import type {
  GenericDataTableAction,
  GenericDataTableActionPayload,
  GenericDataTableColumn,
  GenericDataTableFilterValue,
  GenericDataTableQuery,
  GenericDataTableRow
} from './generic-data-table.types'
import { useGenericDataTableQuery } from './useGenericDataTableQuery'

const props = withDefaults(
  defineProps<{
    columns: Array<GenericDataTableColumn<GenericDataTableRow>>
    rows: GenericDataTableRow[]
    query?: GenericDataTableQuery
    loading?: boolean
    lazy?: boolean
    rowKey?: string
    totalRecords?: number
    rowsPerPageOptions?: number[]
    filterDisplay?: 'row' | 'menu'
    showGridlines?: boolean
    stripedRows?: boolean
    rowHover?: boolean
    emptyMessage?: string
    loadingMessage?: string
    globalFilterPlaceholder?: string
    clearFiltersLabel?: string
    showGlobalFilter?: boolean
    showClearFilters?: boolean
    showPaginator?: boolean
  }>(),
  {
    query: () => ({
      page: 0,
      size: 10,
      sortField: null,
      sortOrder: 1,
      globalFilter: null,
      filters: {}
    }),
    loading: false,
    lazy: false,
    rowKey: 'id',
    totalRecords: undefined,
    rowsPerPageOptions: () => [10, 20, 50],
    filterDisplay: 'row',
    showGridlines: true,
    stripedRows: false,
    rowHover: true,
    emptyMessage: 'No data available',
    loadingMessage: 'Loading data...',
    globalFilterPlaceholder: 'Filter all columns',
    clearFiltersLabel: 'Clear filters',
    showGlobalFilter: true,
    showClearFilters: true,
    showPaginator: true
  }
)

const emit = defineEmits<{
  (event: 'update:query', payload: GenericDataTableQuery): void
  (event: 'row-click', payload: GenericDataTableRow): void
  (
    event: 'action',
    payload: GenericDataTableActionPayload<GenericDataTableRow>
  ): void
}>()

const {
  normalizedQuery,
  primeFilters,
  first,
  setPage,
  setSort,
  setFilterValue,
  clearFilters
} = useGenericDataTableQuery(
  props.columns,
  props.query,
  props.query?.size ?? 10
)

const booleanFilterOptions = [
  { label: 'Yes', value: true },
  { label: 'No', value: false }
]

const resolvedTotalRecords = computed(() => {
  if (typeof props.totalRecords === 'number') {
    return props.totalRecords
  }

  return props.rows.length
})

const globalFilterValue = computed(() => {
  const value = primeFilters.value.global?.value
  return typeof value === 'string' ? value : ''
})

const showToolbar = computed(
  () => props.showGlobalFilter || props.showClearFilters
)

const resolveFilterType = (
  column: GenericDataTableColumn<GenericDataTableRow>
): 'text' | 'number' | 'boolean' | 'select' => {
  return (column.filterType ?? column.type ?? 'text') as
    | 'text'
    | 'number'
    | 'boolean'
    | 'select'
}

const resolveAlignmentStyle = (
  align: GenericDataTableColumn<GenericDataTableRow>['align'],
  width?: string
): Record<string, string> => ({
  textAlign: align ?? 'left',
  ...(width ? { width } : {})
})

const readValue = (row: GenericDataTableRow, field: string): unknown =>
  row[field]

const formatBooleanValue = (value: unknown): string => {
  if (value === true) {
    return 'Yes'
  }

  if (value === false) {
    return 'No'
  }

  return '-'
}

const formatCellValue = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  const value = readValue(row, column.field)

  if (column.format) {
    return column.format(value, row)
  }

  if (value === null || value === undefined) {
    return ''
  }

  if (column.type === 'number') {
    return Number(value).toString()
  }

  return String(value)
}

const resolveTextFilterValue = (field: string): string => {
  const value = primeFilters.value[field]?.value
  return typeof value === 'string' ? value : ''
}

const resolveNumericFilterValue = (field: string): number | null => {
  const value = primeFilters.value[field]?.value

  if (typeof value === 'number') {
    return value
  }

  return null
}

const resolveActionDisabled = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): boolean => {
  if (typeof action.disabled === 'function') {
    return action.disabled(row)
  }

  return action.disabled === true
}

const onPage = (event: DataTablePageEvent): void => {
  emit('update:query', setPage(event.page, event.rows))
}

const onSort = (event: DataTableSortEvent): void => {
  const sortField = typeof event.sortField === 'string' ? event.sortField : null
  const sortOrder = event.sortOrder === -1 ? -1 : 1
  emit('update:query', setSort(sortField, sortOrder))
}

const onColumnFilterChange = (
  field: string,
  value: GenericDataTableFilterValue
): void => {
  emit('update:query', setFilterValue(field, value))
}

const onGlobalFilterChange = (value: string | null): void => {
  emit('update:query', setFilterValue('global', value))
}

const onClearFilters = (): void => {
  emit('update:query', clearFilters())
}

const onRowClick = (event: DataTableRowClickEvent): void => {
  emit('row-click', event.data as GenericDataTableRow)
}
</script>

<style scoped>
.generic-data-table {
  display: flex;
  flex-direction: column;
  gap: var(--app-space-3);
}

.generic-data-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--app-space-3);
  flex-wrap: wrap;
}

.generic-data-table__toolbar-main {
  flex: 1 1 280px;
}

.generic-data-table__global-filter,
.generic-data-table__filter-input {
  width: 100%;
}

.generic-data-table__filter-cell {
  min-width: 9rem;
}

.generic-data-table__actions {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.generic-data-table__state-message {
  padding: var(--app-space-4);
  text-align: center;
  color: var(--app-text-muted);
}

:deep(.generic-data-table .p-datatable-tbody > tr) {
  cursor: pointer;
}
</style>
