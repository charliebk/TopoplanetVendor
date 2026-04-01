<template>
  <div class="generic-data-table">
    <div
      v-if="showToolbar"
      class="generic-data-table__toolbar"
    >
      <div class="generic-data-table__toolbar-main">
        <slot
          name="toolbar-main"
          :query="currentQuery"
          :rows="resolvedRows"
          :filtered-total="resolvedTotalRecords"
          :baseline-total="resolvedBaselineTotal"
          :has-filters="hasActiveFilters"
          :selection="selectionPayload"
          :option-errors="optionErrorSummary"
          :has-option-errors="hasOptionErrors"
          :clear-filters="onClearFilters"
          :clear-selection="onClearSelection"
          :refresh="onRefresh"
          :reload-filter-options="onReloadAllFilterOptions"
        ></slot>

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
          v-if="showRefreshButton"
          type="button"
          icon="pi pi-refresh"
          text
          severity="secondary"
          :label="refreshLabel"
          :disabled="resolvedLoading"
          @click="void onRefresh()"
        />

        <PrimeButton
          v-if="showSelectionToolbar"
          type="button"
          icon="pi pi-check-square"
          text
          severity="secondary"
          :label="selectPageLabel"
          :disabled="getSelectableRows(resolvedRows).length === 0"
          @click="onSelectAllPage"
        />

        <PrimeButton
          v-if="showSelectionToolbar"
          type="button"
          icon="pi pi-plus-circle"
          text
          severity="secondary"
          :label="selectFilteredLabel"
          :disabled="resolvedTotalRecords === 0 || !canSelectAllFiltered"
          :title="selectAllFilteredDisabledReason || undefined"
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

        <slot
          name="toolbar-actions"
          :query="currentQuery"
          :rows="resolvedRows"
          :filtered-total="resolvedTotalRecords"
          :baseline-total="resolvedBaselineTotal"
          :has-filters="hasActiveFilters"
          :selection="selectionPayload"
          :option-errors="optionErrorSummary"
          :has-option-errors="hasOptionErrors"
          :clear-filters="onClearFilters"
          :clear-selection="onClearSelection"
          :refresh="onRefresh"
          :reload-filter-options="onReloadAllFilterOptions"
        ></slot>
      </div>
    </div>

    <div
      v-if="shouldRenderOptionErrorsState"
      class="generic-data-table__option-errors-state"
    >
      <slot
        name="option-errors"
        :query="currentQuery"
        :rows="resolvedRows"
        :filtered-total="resolvedTotalRecords"
        :baseline-total="resolvedBaselineTotal"
        :has-filters="hasActiveFilters"
        :selection="selectionPayload"
        :option-errors="optionErrorSummary"
        :has-option-errors="hasOptionErrors"
        :clear-filters="onClearFilters"
        :clear-selection="onClearSelection"
        :refresh="onRefresh"
        :reload-filter-options="onReloadAllFilterOptions"
      >
        <div
          v-if="hasOptionErrors"
          class="generic-data-table__option-errors-summary"
          role="status"
          aria-live="polite"
        >
          <div class="generic-data-table__option-errors-copy">
            <strong>
              {{ formatOptionSourceFailureLabel(optionErrorSummary.length) }}
            </strong>
            <span>
              {{ optionErrorSummaryText }}
            </span>
          </div>
          <PrimeButton
            type="button"
            icon="pi pi-refresh"
            text
            size="small"
            severity="danger"
            :label="GENERIC_DATA_TABLE_LOCALE.retryAllLabel"
            @click="void onReloadAllFilterOptions()"
          />
        </div>
      </slot>
    </div>

    <GenericDataTableCountBar
      v-if="showCountBarTop"
      class="generic-data-table__count-bar"
      :baseline-total="resolvedBaselineTotal"
      :filtered-total="resolvedTotalRecords"
      :shown="resolvedRows.length"
      :has-filters="hasActiveFilters"
      :show-shown="countBarShowShown"
      :show-clear-filters-button="false"
      :clear-filters-label="clearFiltersLabel"
    >
      <slot
        name="count-bar"
        :query="currentQuery"
        :rows="resolvedRows"
        :filtered-total="resolvedTotalRecords"
        :baseline-total="resolvedBaselineTotal"
        :shown="resolvedRows.length"
        :has-filters="hasActiveFilters"
        :selection="selectionPayload"
        :option-errors="optionErrorSummary"
        :has-option-errors="hasOptionErrors"
        :clear-filters="onClearFilters"
        :clear-selection="onClearSelection"
        :refresh="onRefresh"
        :reload-filter-options="onReloadAllFilterOptions"
      ></slot>
    </GenericDataTableCountBar>

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
      :row-class="resolveRowClass"
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
              :disabled="getSelectableRows(resolvedRows).length === 0"
              :aria-label="GENERIC_DATA_TABLE_LOCALE.selectVisibleRowsAriaLabel"
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
              :disabled="isRowDisabled(slotProps.data)"
              :aria-label="
                formatSelectRowAriaLabel(resolveRowAriaLabel(slotProps.data))
              "
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
                <span
                  v-for="action in column.actions ?? []"
                  :key="action.key"
                  class="generic-data-table__action-shell"
                  :title="
                    resolveActionTooltip(action, slotProps.data) || undefined
                  "
                >
                  <PrimeButton
                    type="button"
                    :icon="action.icon"
                    :label="action.label"
                    :severity="action.severity ?? 'secondary'"
                    text
                    size="small"
                    :class="resolveActionClass(action, slotProps.data)"
                    :aria-label="resolveActionAriaLabel(action, slotProps.data)"
                    :disabled="isActionDisabled(action, slotProps.data)"
                    @click.stop="onActionClick(action, slotProps.data)"
                  />
                </span>
              </div>

              <span
                v-else-if="column.type === 'boolean'"
                class="generic-data-table__boolean-cell"
                :aria-label="`${column.header}: ${resolveBooleanLabel(
                  column,
                  readValue(slotProps.data, column.field)
                )}`"
              >
                <PrimeTag
                  v-if="column.booleanTag !== false"
                  :value="
                    resolveBooleanLabel(
                      column,
                      readValue(slotProps.data, column.field)
                    )
                  "
                  :severity="
                    resolveBooleanSeverity(
                      column,
                      readValue(slotProps.data, column.field)
                    )
                  "
                />

                <span v-else>
                  {{
                    resolveBooleanLabel(
                      column,
                      readValue(slotProps.data, column.field)
                    )
                  }}
                </span>
              </span>

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
                :options="resolveColumnFilterOptions(column)"
                option-label="label"
                option-value="value"
                :loading="isColumnOptionsLoading(column.field)"
                show-clear
                :placeholder="column.header"
                class="generic-data-table__filter-input"
                @update:model-value="
                  (value) => onColumnFilterChange(column.field, value)
                "
              />
              <div
                v-if="resolveColumnOptionsError(column)"
                class="generic-data-table__filter-error"
                role="status"
                aria-live="polite"
              >
                <span>
                  {{ resolveColumnOptionsError(column)?.message }}
                </span>
                <PrimeButton
                  type="button"
                  icon="pi pi-refresh"
                  text
                  size="small"
                  severity="danger"
                  :label="GENERIC_DATA_TABLE_LOCALE.retryLabel"
                  @click="void onReloadColumnOptions(column.field)"
                />
              </div>
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
                  (value) =>
                    onColumnFilterChange(
                      column.field,
                      normalizeDateFilterValue(value)
                    )
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
        <slot
          v-if="providerState.error.value"
          name="error"
          :query="currentQuery"
          :message="resolvedErrorMessage"
          :error="providerState.error.value"
        >
          <div class="generic-data-table__state-message">
            {{ resolvedErrorMessage }}
          </div>
        </slot>

        <slot
          v-else
          name="empty"
          :query="currentQuery"
          :rows="resolvedRows"
          :message="resolvedEmptyMessage"
        >
          <div class="generic-data-table__state-message">
            {{ resolvedEmptyMessage }}
          </div>
        </slot>
      </template>

      <template #loading>
        <slot
          name="loading"
          :query="currentQuery"
          :message="resolvedLoadingMessage"
        >
          <div class="generic-data-table__state-message">
            {{ resolvedLoadingMessage }}
          </div>
        </slot>
      </template>
    </DataTable>

    <GenericDataTableCountBar
      v-if="showCountBarBottom"
      class="generic-data-table__count-bar"
      :baseline-total="resolvedBaselineTotal"
      :filtered-total="resolvedTotalRecords"
      :shown="resolvedRows.length"
      :has-filters="hasActiveFilters"
      :show-shown="countBarShowShown"
      :show-clear-filters-button="false"
      :clear-filters-label="clearFiltersLabel"
    >
      <slot
        name="count-bar"
        :query="currentQuery"
        :rows="resolvedRows"
        :filtered-total="resolvedTotalRecords"
        :baseline-total="resolvedBaselineTotal"
        :shown="resolvedRows.length"
        :has-filters="hasActiveFilters"
        :selection="selectionPayload"
        :option-errors="optionErrorSummary"
        :has-option-errors="hasOptionErrors"
        :clear-filters="onClearFilters"
        :clear-selection="onClearSelection"
        :refresh="onRefresh"
        :reload-filter-options="onReloadAllFilterOptions"
      ></slot>
    </GenericDataTableCountBar>
  </div>
</template>

<script setup lang="ts">
import { computed, useAttrs, useSlots } from 'vue'
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
import GenericDataTableCountBar from './GenericDataTableCountBar.vue'
import {
  buildGenericDataTableActionPayload,
  isGenericDataTableActionDisabled,
  resolveGenericDataTableActionAriaLabel,
  resolveGenericDataTableActionClass
} from './generic-data-table.actions'
import {
  GENERIC_DATA_TABLE_LOCALE,
  formatOptionSourceFailureLabel,
  formatSelectRowAriaLabel
} from './generic-data-table.locale'
import { exportDataTableCsv, prepareDataTablePrint } from './tableExport'
import type {
  GenericDataTableAction,
  GenericDataTableActionPayload,
  GenericDataTableCsvExportOptions,
  GenericDataTableExpose,
  GenericDataTableColumn,
  GenericDataTableFilterValue,
  GenericDataTableLoadPayload,
  GenericDataTablePreparedPrintPayload,
  GenericDataTablePrintOptions,
  GenericDataTableProviderErrorPayload,
  GenericDataTableProps,
  GenericDataTableQuery,
  GenericDataTableRefreshPayload,
  GenericDataTableRow,
  GenericDataTableSelectionPayload
} from './generic-data-table.types'
import { useGenericDataTableOptions } from './useGenericDataTableOptions'
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
    emptyMessage: GENERIC_DATA_TABLE_LOCALE.emptyMessage,
    loadingMessage: GENERIC_DATA_TABLE_LOCALE.loadingMessage,
    errorMessage: GENERIC_DATA_TABLE_LOCALE.errorMessage,
    globalFilterPlaceholder: GENERIC_DATA_TABLE_LOCALE.globalFilterPlaceholder,
    clearFiltersLabel: GENERIC_DATA_TABLE_LOCALE.clearFiltersLabel,
    refreshLabel: GENERIC_DATA_TABLE_LOCALE.refreshLabel,
    selectPageLabel: GENERIC_DATA_TABLE_LOCALE.selectPageLabel,
    selectFilteredLabel: GENERIC_DATA_TABLE_LOCALE.selectFilteredLabel,
    clearSelectionLabel: GENERIC_DATA_TABLE_LOCALE.clearSelectionLabel,
    showGlobalFilter: true,
    showClearFilters: true,
    showRefreshButton: false,
    showSelectionToolbar: true,
    showCountBar: false,
    showOptionErrorsState: true,
    countBarPosition: 'top',
    countBarShowShown: false,
    disabledRowSelectionScope: 'visible',
    disabledFilteredRowKeys: () => [],
    disabledFilteredRowsResolved: false,
    showPaginator: true
  }
)

const slots = useSlots()
const attrs = useAttrs()

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
  (event: 'refresh', payload: GenericDataTableRefreshPayload): void
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
  computed(() => props.columns),
  computed(() => props.query),
  computed(() => props.query?.size ?? 10)
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

const optionState = useGenericDataTableOptions({
  columns: computed(() => props.columns),
  query: currentQuery
})

const booleanFilterOptions = [
  { label: GENERIC_DATA_TABLE_LOCALE.booleanTrueLabel, value: true },
  { label: GENERIC_DATA_TABLE_LOCALE.booleanFalseLabel, value: false }
]

const resolvedRows = computed(() =>
  isProviderMode.value ? providerState.rows.value : props.rows
)

const buildRowKey = (row: GenericDataTableRow): string => {
  const value = row?.[props.rowKey]

  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

const selectionEnabled = computed(() => props.selectionMode === 'multiple')

const hasRowClickListener = computed(() => {
  return typeof attrs.onRowClick === 'function'
})

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

const resolvedDisabledSelectionScope = computed(() => {
  if (!props.rowDisabled) {
    return 'none' as const
  }

  return props.disabledRowSelectionScope
})

const disabledVisibleRowKeys = computed(() => {
  if (!props.rowDisabled) {
    return []
  }

  return resolvedRows.value
    .filter((row) => isRowDisabled(row))
    .map((row) => buildRowKey(row))
    .filter(Boolean)
})

const disabledSelectionKeys = computed(() => {
  if (resolvedDisabledSelectionScope.value === 'filtered') {
    return Array.from(
      new Set([
        ...props.disabledFilteredRowKeys,
        ...disabledVisibleRowKeys.value
      ])
    )
  }

  return disabledVisibleRowKeys.value
})

const disabledRowsResolved = computed(() => {
  if (resolvedDisabledSelectionScope.value === 'none') {
    return true
  }

  if (resolvedDisabledSelectionScope.value === 'filtered') {
    return props.disabledFilteredRowsResolved
  }

  return true
})

const canSelectAllFiltered = computed(() => {
  if (!selectionEnabled.value) {
    return false
  }

  if (!props.rowDisabled) {
    return true
  }

  if (
    isProviderMode.value &&
    resolvedDisabledSelectionScope.value === 'visible'
  ) {
    return false
  }

  if (resolvedDisabledSelectionScope.value === 'filtered') {
    return props.disabledFilteredRowsResolved
  }

  return true
})

const selectAllFilteredDisabledReason = computed(() => {
  if (canSelectAllFiltered.value) {
    return ''
  }

  if (
    isProviderMode.value &&
    resolvedDisabledSelectionScope.value === 'visible'
  ) {
    return GENERIC_DATA_TABLE_LOCALE.selectFilteredProviderModeReason
  }

  if (resolvedDisabledSelectionScope.value === 'filtered') {
    return GENERIC_DATA_TABLE_LOCALE.selectFilteredResolveDisabledReason
  }

  return GENERIC_DATA_TABLE_LOCALE.selectFilteredUnavailableReason
})

const selectionState = useGenericDataTableSelection({
  visibleRows: resolvedRows,
  query: currentQuery,
  filteredTotal: computed(() => resolvedTotalRecords.value),
  baselineTotal: computed(() => resolvedBaselineTotal.value),
  rowKeyField: computed(() => props.rowKey),
  enabled: selectionEnabled,
  disabledKeys: disabledSelectionKeys,
  disabledRowsResolved,
  rowDisabledSelectionScope: resolvedDisabledSelectionScope,
  allowSelectAllFiltered: canSelectAllFiltered,
  onChange: (payload) => emit('selection-change', payload)
})

const selectionPayload = computed(() => {
  if (!selectionEnabled.value) {
    return null
  }

  return selectionState.getSelectionPayload()
})

const resolvedEmptyMessage = computed(() => {
  return props.emptyMessage
})

const resolvedErrorMessage = computed(() => {
  if (!providerState.error.value) {
    return props.errorMessage
  }

  return providerState.error.value.message || props.errorMessage
})

const resolvedLoadingMessage = computed(() => props.loadingMessage)

const hasActiveFilters = computed(() => {
  if (currentQuery.value.globalFilter?.trim()) {
    return true
  }

  return Object.values(currentQuery.value.filters ?? {}).some((value) => {
    if (value === null || value === undefined) {
      return false
    }

    if (typeof value === 'string') {
      return value.trim().length > 0
    }

    return true
  })
})

const globalFilterValue = computed(() => {
  const value = primeFilters.value.global?.value
  return typeof value === 'string' ? value : ''
})

const hasToolbarMainSlot = computed(() => Boolean(slots['toolbar-main']))

const hasToolbarActionsSlot = computed(() => Boolean(slots['toolbar-actions']))

const hasOptionErrorsSlot = computed(() => Boolean(slots['option-errors']))

const hasCountBarSlot = computed(() => Boolean(slots['count-bar']))

const showToolbar = computed(
  () =>
    props.showGlobalFilter ||
    props.showClearFilters ||
    props.showRefreshButton ||
    showSelectionToolbar.value ||
    hasToolbarMainSlot.value ||
    hasToolbarActionsSlot.value
)

const showSelectionToolbar = computed(
  () => selectionEnabled.value && props.showSelectionToolbar
)

const showRefreshButton = computed(() => props.showRefreshButton)

const showCountBar = computed(() => props.showCountBar || hasCountBarSlot.value)

const shouldRenderOptionErrorsState = computed(
  () =>
    hasOptionErrorsSlot.value ||
    (props.showOptionErrorsState && hasOptionErrors.value)
)

const showCountBarTop = computed(() => {
  return (
    showCountBar.value &&
    (props.countBarPosition === 'top' || props.countBarPosition === 'both')
  )
})

const showCountBarBottom = computed(() => {
  return (
    showCountBar.value &&
    (props.countBarPosition === 'bottom' || props.countBarPosition === 'both')
  )
})

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

  if (
    props.rowDisabled &&
    isProviderMode.value &&
    resolvedDisabledSelectionScope.value === 'visible' &&
    !selectionState.allFiltered.value
  ) {
    const selectedCount = selectionState.selectedCount.value ?? 0

    if (selectedCount > 0) {
      return `${selectedCount} rows selected (filtered-wide selection disabled while hidden disabled rows are unresolved)`
    }
  }

  if (selectionState.allFiltered.value) {
    const selectedCount = selectionState.selectedCount.value
    const excludedCount = selectionState.unselectedKeys.value.length
    const disabledCount = selectionPayload.value?.disabledCount ?? 0

    if (selectedCount === null) {
      return excludedCount > 0
        ? `All filtered rows selected (${excludedCount} excluded)`
        : 'All filtered rows selected'
    }

    const disabledSuffix =
      disabledCount > 0 ? `, ${disabledCount} disabled` : ''

    return excludedCount > 0
      ? `${selectedCount} filtered rows selected (${excludedCount} excluded${disabledSuffix})`
      : `${selectedCount} filtered rows selected${disabledSuffix ? ` (${disabledCount} disabled)` : ''}`
  }

  const selectedCount = selectionState.selectedCount.value ?? 0

  if (selectedCount === 0) {
    return 'No rows selected'
  }

  return `${selectedCount} rows selected`
})

const resolvedSortField = computed<string | undefined>(() => {
  const querySortField = normalizedQuery.value.sortField

  if (!querySortField) {
    return undefined
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

const resolveColumnFilterOptions = (
  column: GenericDataTableColumn<GenericDataTableRow>
) => {
  return optionState.resolvedOptions.value[column.field] ?? []
}

const isColumnOptionsLoading = (field: string): boolean => {
  return optionState.loadingByField.value[field] === true
}

const resolveColumnOptionsError = (
  column: GenericDataTableColumn<GenericDataTableRow>
) => {
  return optionState.errorByField.value[column.field] ?? null
}

const optionErrorSummary = computed(() => {
  return props.columns
    .map((column) => {
      const error = resolveColumnOptionsError(column)

      if (!error) {
        return null
      }

      return {
        field: column.field,
        header: column.header,
        message: error.message
      }
    })
    .filter(
      (
        error
      ): error is {
        field: string
        header: string
        message: string
      } => Boolean(error)
    )
})

const hasOptionErrors = computed(() => optionErrorSummary.value.length > 0)

const optionErrorSummaryText = computed(() => {
  return optionErrorSummary.value
    .map((error) => `${error.header}: ${error.message}`)
    .join(' | ')
})

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

const resolveBooleanLabel = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  value: unknown
): string => {
  if (value === true) {
    return (
      column.booleanLabels?.trueLabel ??
      GENERIC_DATA_TABLE_LOCALE.booleanTrueLabel
    )
  }

  if (value === false) {
    return (
      column.booleanLabels?.falseLabel ??
      GENERIC_DATA_TABLE_LOCALE.booleanFalseLabel
    )
  }

  return column.booleanLabels?.nullLabel ?? '-'
}

const resolveBooleanSeverity = (
  column: GenericDataTableColumn<GenericDataTableRow>,
  value: unknown
): 'secondary' | 'success' | 'info' | 'warning' | 'danger' | 'contrast' => {
  if (value === true) {
    return column.booleanTagSeverity?.true ?? 'success'
  }

  if (value === false) {
    return column.booleanTagSeverity?.false ?? 'warning'
  }

  return column.booleanTagSeverity?.null ?? 'secondary'
}

const isRowDisabled = (row: GenericDataTableRow): boolean => {
  if (typeof props.rowDisabled === 'function') {
    return props.rowDisabled(row)
  }

  return props.rowDisabled === true
}

const getSelectableRows = (
  rows: GenericDataTableRow[]
): GenericDataTableRow[] => {
  return rows.filter((row) => !isRowDisabled(row))
}

const resolveRowAriaLabel = (row: GenericDataTableRow): string => {
  const rowKeyValue = row[props.rowKey]

  if (rowKeyValue === null || rowKeyValue === undefined) {
    return 'row'
  }

  return `row ${String(rowKeyValue)}`
}

const resolveActionTooltip = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  if (typeof action.tooltip === 'function') {
    return action.tooltip(row) ?? ''
  }

  return action.tooltip ?? ''
}

const resolveActionClass = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): Array<string | string[] | Record<string, boolean>> => {
  return resolveGenericDataTableActionClass(action, row, isRowDisabled(row))
}

const resolveActionAriaLabel = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): string => {
  return resolveGenericDataTableActionAriaLabel(
    action,
    row,
    resolveActionTooltip(action, row)
  )
}

const isActionDisabled = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): boolean => {
  return isGenericDataTableActionDisabled(action, row, isRowDisabled(row))
}

const onActionClick = (
  action: GenericDataTableAction<GenericDataTableRow>,
  row: GenericDataTableRow
): void => {
  const payload = buildGenericDataTableActionPayload(
    action,
    row,
    isRowDisabled(row)
  )

  if (!payload) {
    return
  }

  emit('action', payload)
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

const onGlobalFilterChange = (value: string | undefined): void => {
  emit('update:query', setFilterValue('global', value ?? null))
}

const normalizeDateFilterValue = (
  value: Date | Array<Date | null> | null | undefined
): Date | null => {
  if (Array.isArray(value)) {
    return value[0] ?? null
  }

  return value ?? null
}

const onClearFilters = (): void => {
  emit('update:query', clearFilters())
}

const onRefresh = async (): Promise<void> => {
  emit('refresh', {
    query: currentQuery.value,
    providerMode: isProviderMode.value
  })

  if (isProviderMode.value) {
    await providerState.reload()
  }
}

const onRowSelectionChange = (
  row: GenericDataTableRow,
  value: boolean | null | undefined
): void => {
  selectionState.setRowSelected(row, value === true)
}

const onToggleVisibleRows = (value: boolean | null | undefined): void => {
  if (value === true) {
    selectionState.selectAllPage(getSelectableRows(resolvedRows.value))
    return
  }

  selectionState.clearVisibleRows(getSelectableRows(resolvedRows.value))
}

const onSelectAllPage = (): void => {
  selectionState.selectAllPage(getSelectableRows(resolvedRows.value))
}

const onSelectAllFiltered = (): void => {
  selectionState.selectAllFiltered(getSelectableRows(resolvedRows.value))
}

const onClearSelection = (): void => {
  selectionState.clearSelection()
}

const onReloadColumnOptions = async (field: string): Promise<void> => {
  await optionState.reloadFilterOptions([field])
}

const onReloadAllFilterOptions = async (
  fields?: Array<keyof GenericDataTableRow & string>
): Promise<void> => {
  await optionState.reloadFilterOptions(fields)
}

const onRowClick = (event: DataTableRowClickEvent): void => {
  if (isRowDisabled(event.data as GenericDataTableRow)) {
    return
  }

  emit('row-click', event.data as GenericDataTableRow)
}

const resolveRowClass = (row: GenericDataTableRow): string[] => {
  const disabled = isRowDisabled(row)

  return [
    'generic-data-table__row',
    disabled ? 'generic-data-table__row--disabled' : '',
    !disabled && hasRowClickListener.value
      ? 'generic-data-table__row--clickable'
      : ''
  ].filter(Boolean)
}

const exportCsv = (
  rows?: GenericDataTableRow[],
  options?: GenericDataTableCsvExportOptions
): string => {
  return exportDataTableCsv(
    {
      columns: props.columns,
      rows: rows ?? resolvedRows.value
    },
    options
  )
}

const preparePrint = (
  rows?: GenericDataTableRow[],
  options?: GenericDataTablePrintOptions
): GenericDataTablePreparedPrintPayload<GenericDataTableRow> => {
  return prepareDataTablePrint(
    {
      columns: props.columns,
      rows: rows ?? resolvedRows.value
    },
    options
  )
}

defineExpose<GenericDataTableExpose<GenericDataTableRow>>({
  selectAllPage: (rows) => selectionState.selectAllPage(rows),
  selectAllFiltered: (rows) => selectionState.selectAllFiltered(rows),
  clearSelection: () => selectionState.clearSelection(),
  refreshVisibleRows: (rows) => selectionState.refreshVisibleRows(rows),
  getSelectionPayload: () => selectionState.getSelectionPayload(),
  refresh: () => onRefresh(),
  clearFilters: () => onClearFilters(),
  exportCsv,
  preparePrint,
  reloadFilterOptions: (fields) => optionState.reloadFilterOptions(fields)
})
</script>

<style scoped>
.generic-data-table {
  --gdt-space-3: var(--app-space-3, 0.75rem);
  --gdt-space-4: var(--app-space-4, 1rem);
  --gdt-text-muted: var(--app-text-muted, #5f6b76);
  display: flex;
  flex-direction: column;
  gap: var(--gdt-space-3);
}

.generic-data-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--gdt-space-3);
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
  color: var(--gdt-text-muted);
  font-size: 0.9rem;
}

.generic-data-table__option-errors-state {
  margin: 0;
}

.generic-data-table__option-errors-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(182, 58, 58, 0.28);
  background: rgba(182, 58, 58, 0.08);
}

.generic-data-table__option-errors-copy {
  display: grid;
  gap: 0.2rem;
  color: #8f2f2f;
  font-size: 0.82rem;
  line-height: 1.35;
}

.generic-data-table__count-bar {
  margin: 0;
}

.generic-data-table__filter-cell {
  min-width: 9rem;
}

.generic-data-table__filter-error {
  margin-top: 0.35rem;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
  color: #b63a3a;
  font-size: 0.78rem;
  line-height: 1.35;
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
  flex-wrap: wrap;
}

.generic-data-table__action-shell {
  display: inline-flex;
}

.generic-data-table__action-button--disabled {
  opacity: 0.55;
}

.generic-data-table__boolean-cell {
  display: inline-flex;
  align-items: center;
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
  padding: var(--gdt-space-4);
  text-align: center;
  color: var(--gdt-text-muted);
}

:deep(.generic-data-table .p-datatable-tbody > tr) {
  cursor: default;
}

:deep(
  .generic-data-table .p-datatable-tbody > tr.generic-data-table__row--clickable
) {
  cursor: pointer;
}

:deep(
  .generic-data-table .p-datatable-tbody > tr.generic-data-table__row--disabled
) {
  opacity: 0.6;
}
</style>
