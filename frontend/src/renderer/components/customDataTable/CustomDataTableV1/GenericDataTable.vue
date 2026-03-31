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

        <span
          v-if="selectionEnabled"
          class="generic-data-table__selection-summary"
        >
          {{ selectionSummary }}
        </span>
      </div>

      <div class="generic-data-table__toolbar-actions">
        <PrimeButton
          v-if="showSelectionToolbar"
          type="button"
          icon="pi pi-check-square"
          text
          severity="secondary"
          :label="selectPageLabel"
          :disabled="resolvedRows.length === 0"
          @click="onSelectAllPage"
        />

        <PrimeButton
          v-if="showSelectionToolbar"
          type="button"
          icon="pi pi-plus-circle"
          text
          severity="secondary"
          :label="selectFilteredLabel"
          :disabled="resolvedTotalRecords === 0"
          @click="onSelectAllFiltered"
        />

        <PrimeButton
          v-if="showSelectionToolbar"
          type="button"
          icon="pi pi-times-circle"
          text
          severity="secondary"
          :label="clearSelectionLabel"
          :disabled="!hasSelection"
          @click="onClearSelection"
        />

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
    </div>

    <DataTable
      :value="resolvedRows"
      :lazy="lazy || isProviderMode"
      :loading="resolvedLoading"
      :paginator="showPaginator"
      :rows="normalizedQuery.size"
      :total-records="resolvedTotalRecords"
      :first="first"
      :sort-field="resolvedSortField"
      :sort-order="normalizedQuery.sortOrder"
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
      <Column
        v-if="selectionEnabled"
        :header-style="{ width: '3.5rem', textAlign: 'center' }"
        :body-style="{ width: '3.5rem', textAlign: 'center' }"
      >
        <template #header>
          <div
            class="generic-data-table__selection-cell"
            @click.stop
          >
            <PrimeCheckbox
              :binary="true"
              :model-value="selectionState.allVisibleSelected.value"
              :indeterminate="selectionState.someVisibleSelected.value"
              :disabled="resolvedRows.length === 0"
              @update:model-value="onToggleVisibleRows"
            />
          </div>
        </template>

        <template #body="slotProps">
          <div
            class="generic-data-table__selection-cell"
            @click.stop
          >
            <PrimeCheckbox
              :binary="true"
              :model-value="selectionState.isRowSelected(slotProps.data)"
              @update:model-value="
                (value) => onRowSelectionChange(slotProps.data, value)
              "
            />
          </div>
        </template>
      </Column>

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
          :header-style="
            resolveAlignmentStyle(
              column.align,
              column.width,
              column.minWidth,
              column.maxWidth
            )
          "
          :body-style="
            resolveAlignmentStyle(
              column.align,
              column.width,
              column.minWidth,
              column.maxWidth
            )
          "
        >
          <template #body="slotProps">
            <slot
              :name="`cell-${column.field}`"
              :row="slotProps.data"
              :value="readValue(slotProps.data, column.field)"
              :column="column"
            >
              <div
                v-if="column.type === 'idIcon'"
                class="generic-data-table__id-icon-cell"
                :title="resolveTooltipValue(column, slotProps.data)"
              >
                <i
                  :class="resolveIdIconClass(column, slotProps.data)"
                  aria-hidden="true"
                ></i>
                <span v-if="resolveDisplayValue(column, slotProps.data)">
                  {{ resolveDisplayValue(column, slotProps.data) }}
                </span>
              </div>

              <div
                v-else-if="column.type === 'actions'"
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
                  :class="action.class"
                  :title="action.tooltip"
                  :aria-label="action.tooltip ?? action.label ?? action.key"
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
              <!-- eslint-disable vue/html-indent -->
              <PrimeDropdown
                v-if="
                  resolveFilterType(column) === 'select' ||
                  resolveFilterType(column) === 'list'
                "
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
              <!-- eslint-enable vue/html-indent -->

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
                v-else-if="isNumericFilterType(column)"
                :model-value="resolveNumericFilterValue(column.field)"
                :min-fraction-digits="resolveMinFractionDigits(column)"
                :max-fraction-digits="resolveMaxFractionDigits(column)"
                input-class="generic-data-table__filter-input"
                :placeholder="column.header"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value ?? null)
                "
              />

              <PrimeCalendar
                v-else-if="resolveFilterType(column) === 'date'"
                :model-value="resolveDateFilterValue(column.field)"
                show-icon
                date-format="yy-mm-dd"
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
          {{ resolvedEmptyMessage }}
        </div>
      </template>

      <template #loading>
        <div class="generic-data-table__state-message">
          {{ resolvedLoadingMessage }}
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
import PrimeCalendar from 'primevue/calendar'
import PrimeCheckbox from 'primevue/checkbox'
import PrimeDropdown from 'primevue/dropdown'
import PrimeInputNumber from 'primevue/inputnumber'
import PrimeInputText from 'primevue/inputtext'
import PrimeTag from 'primevue/tag'
import type {
  GenericDataTableAction,
  GenericDataTableActionPayload,
  GenericDataTableExpose,
  GenericDataTableColumn,
  GenericDataTableFilterValue,
  GenericDataTableLoadPayload,
  GenericDataTableProviderErrorPayload,
  GenericDataTableProps,
  GenericDataTableQuery,
  GenericDataTableRow,
  GenericDataTableSelectionPayload
} from './generic-data-table.types'
import { useGenericDataTableProvider } from './useGenericDataTableProvider'
import { useGenericDataTableQuery } from './useGenericDataTableQuery'
import { useGenericDataTableSelection } from './useGenericDataTableSelection'

const props = withDefaults(
  defineProps<GenericDataTableProps<GenericDataTableRow>>(),
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
    selectionMode: 'none',
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
    selectPageLabel: 'Select page',
    selectFilteredLabel: 'Select filtered',
    clearSelectionLabel: 'Clear selection',
    showGlobalFilter: true,
    showClearFilters: true,
    showSelectionToolbar: true,
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
  (
    event: 'load',
    payload: GenericDataTableLoadPayload<GenericDataTableRow>
  ): void
  (
    event: 'selection-change',
    payload: GenericDataTableSelectionPayload<GenericDataTableRow>
  ): void
  (event: 'provider-error', payload: GenericDataTableProviderErrorPayload): void
}>()

const {
  normalizedQuery,
  primeFilters,
  first,
  currentQuery,
  setPage,
  setSort,
  setFilterValue,
  clearFilters
} = useGenericDataTableQuery(
  props.columns,
  props.query,
  props.query?.size ?? 10
)

const isProviderMode = computed(() => typeof props.dataProvider === 'function')

const providerState = useGenericDataTableProvider({
  columns: computed(() => props.columns),
  query: currentQuery,
  dataProvider: computed(() => props.dataProvider),
  enabled: isProviderMode,
  onLoad: (payload) => emit('load', payload),
  onError: (payload) => emit('provider-error', payload)
})

const booleanFilterOptions = [
  { label: 'Yes', value: true },
  { label: 'No', value: false }
]

const resolvedRows = computed(() =>
  isProviderMode.value ? providerState.rows.value : props.rows
)

const selectionEnabled = computed(() => props.selectionMode === 'multiple')

const resolvedLoading = computed(
  () => props.loading || providerState.loading.value
)

const resolvedTotalRecords = computed(() => {
  if (isProviderMode.value) {
    return providerState.totalRecords.value
  }

  if (typeof props.totalRecords === 'number') {
    return props.totalRecords
  }

  return props.rows.length
})

const resolvedBaselineTotal = computed(() => {
  if (isProviderMode.value) {
    return (
      providerState.baselineTotal.value ??
      providerState.overallTotal.value ??
      providerState.totalRecords.value
    )
  }

  if (typeof props.totalRecords === 'number') {
    return props.totalRecords
  }

  return props.rows.length
})

const selectionState = useGenericDataTableSelection({
  visibleRows: resolvedRows,
  query: currentQuery,
  filteredTotal: computed(() => resolvedTotalRecords.value),
  baselineTotal: computed(() => resolvedBaselineTotal.value),
  rowKeyField: computed(() => props.rowKey),
  enabled: selectionEnabled,
  onChange: (payload) => emit('selection-change', payload)
})

const resolvedEmptyMessage = computed(() => {
  if (providerState.error.value) {
    return providerState.error.value.message
  }

  return props.emptyMessage
})

const resolvedLoadingMessage = computed(() => props.loadingMessage)

const globalFilterValue = computed(() => {
  const value = primeFilters.value.global?.value
  return typeof value === 'string' ? value : ''
})

const showToolbar = computed(
  () =>
    props.showGlobalFilter ||
    props.showClearFilters ||
    showSelectionToolbar.value
)

const showSelectionToolbar = computed(
  () => selectionEnabled.value && props.showSelectionToolbar
)

const hasSelection = computed(() => {
  if (selectionState.allFiltered.value) {
    return true
  }

  return (selectionState.selectedCount.value ?? 0) > 0
})

const selectionSummary = computed(() => {
  if (!selectionEnabled.value) {
    return ''
  }

  if (selectionState.allFiltered.value) {
    const selectedCount = selectionState.selectedCount.value
    const excludedCount = selectionState.unselectedKeys.value.length

    if (selectedCount === null) {
      return excludedCount > 0
        ? `All filtered rows selected (${excludedCount} excluded)`
        : 'All filtered rows selected'
    }

    return excludedCount > 0
      ? `${selectedCount} filtered rows selected (${excludedCount} excluded)`
      : `${selectedCount} filtered rows selected`
  }

  const selectedCount = selectionState.selectedCount.value ?? 0

  if (selectedCount === 0) {
    return 'No rows selected'
  }

  return `${selectedCount} rows selected`
})

const resolvedSortField = computed(() => {
  const querySortField = normalizedQuery.value.sortField

  if (!querySortField) {
    return null
  }

  const column = props.columns.find(
    (candidate) =>
      candidate.backendField === querySortField ||
      candidate.field === querySortField
  )

  return column?.field ?? querySortField
})

const resolveFilterType = (
  column: GenericDataTableColumn<GenericDataTableRow>
): 'text' | 'number' | 'date' | 'boolean' | 'list' | 'select' => {
  const resolvedType = column.filterType ?? column.type ?? 'text'

  if (resolvedType === 'integer' || resolvedType === 'percent') {
    return 'number'
  }

  if (resolvedType === 'actions' || resolvedType === 'idIcon') {
    return 'text'
  }

  return resolvedType as
    | 'text'
    | 'number'
    | 'date'
    | 'boolean'
    | 'list'
    | 'select'
}

const isNumericFilterType = (
  column: GenericDataTableColumn<GenericDataTableRow>
): boolean => resolveFilterType(column) === 'number'

const resolveAlignmentStyle = (
  align: GenericDataTableColumn<GenericDataTableRow>['align'],
  width?: string,
  minWidth?: string,
  maxWidth?: string
): Record<string, string> => ({
  textAlign: align ?? 'left',
  ...(width ? { width } : {}),
  ...(minWidth ? { minWidth } : {}),
  ...(maxWidth ? { maxWidth } : {})
})

const readValue = (row: GenericDataTableRow, field: string): unknown =>
  row[field]

const readColumnValue = (
  row: GenericDataTableRow,
  column: GenericDataTableColumn<GenericDataTableRow>
): unknown => {
  const sourceField = column.displayField ?? column.field
  return row[sourceField]
}

const resolveDisplayValue = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  const value = readColumnValue(row, column)

  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

const resolveTooltipValue = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  if (column.tooltipField) {
    const tooltipValue = row[column.tooltipField]
    return tooltipValue === null || tooltipValue === undefined
      ? ''
      : String(tooltipValue)
  }

  return resolveDisplayValue(column, row)
}

const resolveIdIconClass = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  if (typeof column.idIconClass === 'function') {
    return column.idIconClass(row)
  }

  return column.idIconClass ?? 'pi pi-circle'
}

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
  const value = readColumnValue(row, column)

  if (column.format) {
    return column.format(value, row)
  }

  if (value === null || value === undefined) {
    return ''
  }

  if (column.type === 'number' || column.type === 'integer') {
    const fractionDigits =
      column.type === 'integer' ? 0 : (column.decimals ?? 2)

    return Number(value).toLocaleString(undefined, {
      minimumFractionDigits: 0,
      maximumFractionDigits: fractionDigits
    })
  }

  if (column.type === 'percent') {
    const decimals = column.decimals ?? 2

    return `${Number(value).toLocaleString(undefined, {
      minimumFractionDigits: 0,
      maximumFractionDigits: decimals
    })}%`
  }

  if (column.type === 'date') {
    const dateValue = value instanceof Date ? value : new Date(String(value))

    if (Number.isNaN(dateValue.getTime())) {
      return String(value)
    }

    return dateValue.toLocaleDateString()
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

const resolveDateFilterValue = (field: string): Date | null => {
  const value = primeFilters.value[field]?.value

  if (value instanceof Date) {
    return value
  }

  if (typeof value === 'string' && value) {
    const parsedDate = new Date(value)
    return Number.isNaN(parsedDate.getTime()) ? null : parsedDate
  }

  return null
}

const resolveMinFractionDigits = (
  column: GenericDataTableColumn<GenericDataTableRow>
): number => (column.type === 'integer' ? 0 : 0)

const resolveMaxFractionDigits = (
  column: GenericDataTableColumn<GenericDataTableRow>
): number => {
  if (column.type === 'integer') {
    return 0
  }

  return column.decimals ?? 2
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

const onRowSelectionChange = (
  row: GenericDataTableRow,
  value: boolean | null | undefined
): void => {
  selectionState.setRowSelected(row, value === true)
}

const onToggleVisibleRows = (value: boolean | null | undefined): void => {
  if (value === true) {
    selectionState.selectAllPage(resolvedRows.value)
    return
  }

  selectionState.clearVisibleRows(resolvedRows.value)
}

const onSelectAllPage = (): void => {
  selectionState.selectAllPage(resolvedRows.value)
}

const onSelectAllFiltered = (): void => {
  selectionState.selectAllFiltered(resolvedRows.value)
}

const onClearSelection = (): void => {
  selectionState.clearSelection()
}

const onRowClick = (event: DataTableRowClickEvent): void => {
  emit('row-click', event.data as GenericDataTableRow)
}

defineExpose<GenericDataTableExpose<GenericDataTableRow>>({
  selectAllPage: (rows) => selectionState.selectAllPage(rows),
  selectAllFiltered: (rows) => selectionState.selectAllFiltered(rows),
  clearSelection: () => selectionState.clearSelection(),
  refreshVisibleRows: (rows) => selectionState.refreshVisibleRows(rows),
  getSelectionPayload: () => selectionState.getSelectionPayload()
})
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
  display: grid;
  gap: 0.5rem;
}

.generic-data-table__toolbar-actions {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.generic-data-table__global-filter,
.generic-data-table__filter-input {
  width: 100%;
}

.generic-data-table__selection-summary {
  color: var(--app-text-muted);
  font-size: 0.9rem;
}

.generic-data-table__filter-cell {
  min-width: 9rem;
}

.generic-data-table__selection-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.generic-data-table__actions {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.generic-data-table__id-icon-cell {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.generic-data-table__id-icon-cell i {
  font-size: 0.95rem;
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
