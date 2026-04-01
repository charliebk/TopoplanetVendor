import { computed, ref, watch } from 'vue'
import type {
  GenericDataTableRow,
  GenericDataTableSelectionBatchPayload,
  GenericDataTableSelectionOptions,
  GenericDataTableSelectionOverride,
  GenericDataTableSelectionPayload,
  GenericDataTableSelectionState
} from './generic-data-table.types'

const buildRowKey = <Row extends GenericDataTableRow>(
  row: Row,
  rowKeyField: string
): string => {
  const value = row?.[rowKeyField]

  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

export const useGenericDataTableSelection = <Row extends GenericDataTableRow>(
  opts: GenericDataTableSelectionOptions<Row>
): GenericDataTableSelectionState<Row> => {
  const allFiltered = ref(false)
  const overrides = ref<Record<string, GenericDataTableSelectionOverride<Row>>>(
    {}
  )

  const disabledKeys = computed(() =>
    Array.from(new Set(opts.disabledKeys.value))
  )

  const disabledKeySet = computed(() => new Set(disabledKeys.value))

  const selectableVisibleRows = computed(() => {
    return opts.visibleRows.value.filter(
      (row) => !disabledKeySet.value.has(rowKeyOf(row))
    )
  })

  const disabledCount = computed(() => {
    if (opts.rowDisabledSelectionScope.value === 'filtered') {
      return disabledKeys.value.length
    }

    return null
  })

  const selectableFilteredCount = computed(() => {
    if (opts.filteredTotal.value === null) {
      return null
    }

    if (opts.rowDisabledSelectionScope.value !== 'filtered') {
      return opts.filteredTotal.value
    }

    return Math.max(0, opts.filteredTotal.value - disabledKeys.value.length)
  })

  const rowKeyOf = (row: Row): string =>
    buildRowKey(row, opts.rowKeyField.value)

  const selectedRows = computed(() => {
    return Object.values(overrides.value).flatMap((override) => {
      if (override.mode !== 'selected' || !override.row) {
        return []
      }

      return [override.row]
    })
  })

  const selectedKeys = computed(() => {
    return Object.values(overrides.value)
      .filter((override) => override.mode === 'selected')
      .map((override) => override.key)
  })

  const unselectedKeys = computed(() => {
    return Object.values(overrides.value)
      .filter((override) => override.mode === 'unselected')
      .map((override) => override.key)
  })

  const isRowSelected = (row: Row): boolean => {
    const key = rowKeyOf(row)

    if (!key || disabledKeySet.value.has(key)) {
      return false
    }

    const override = overrides.value[key]

    if (override?.mode === 'selected') {
      return true
    }

    if (override?.mode === 'unselected') {
      return false
    }

    return allFiltered.value
  }

  const visibleSelectedRows = computed(() => {
    return opts.visibleRows.value.filter((row) => isRowSelected(row))
  })

  const allVisibleSelected = computed(() => {
    return (
      selectableVisibleRows.value.length > 0 &&
      selectableVisibleRows.value.every((row) => isRowSelected(row))
    )
  })

  const someVisibleSelected = computed(() => {
    return visibleSelectedRows.value.length > 0 && !allVisibleSelected.value
  })

  const selectedCount = computed(() => {
    if (allFiltered.value) {
      if (selectableFilteredCount.value === null) {
        return null
      }

      return Math.max(
        0,
        selectableFilteredCount.value - unselectedKeys.value.length
      )
    }

    return selectedKeys.value.length
  })

  const batchReason = computed(() => {
    if (!allFiltered.value || opts.allowSelectAllFiltered.value) {
      return null
    }

    if (opts.rowDisabledSelectionScope.value === 'visible') {
      return 'Disabled rows are only resolved for the visible page.'
    }

    if (opts.rowDisabledSelectionScope.value === 'filtered') {
      return 'Disabled rows are not resolved yet for the full filtered result.'
    }

    return 'Filtered batch selection is not available for the current state.'
  })

  const batchPayload = computed<GenericDataTableSelectionBatchPayload>(() => {
    const strategy = allFiltered.value ? 'filterQuery' : 'includeKeys'

    return {
      strategy,
      filterQuery: allFiltered.value ? opts.query.value : null,
      includeKeys: allFiltered.value ? [] : selectedKeys.value,
      excludeKeys: allFiltered.value ? unselectedKeys.value : [],
      disabledKeys: disabledKeys.value,
      ready: !allFiltered.value || opts.allowSelectAllFiltered.value,
      reason: batchReason.value
    }
  })

  const getSelectionPayload = (): GenericDataTableSelectionPayload<Row> => ({
    rowKeyField: opts.rowKeyField.value,
    query: opts.query.value,
    allFiltered: allFiltered.value,
    rowDisabledSelectionScope: opts.rowDisabledSelectionScope.value,
    disabledRowsResolved: opts.disabledRowsResolved.value,
    filteredTotal: opts.filteredTotal.value,
    baselineTotal: opts.baselineTotal.value,
    disabledCount: disabledCount.value,
    selectableFilteredCount: selectableFilteredCount.value,
    selectedCount: selectedCount.value,
    visibleSelectedCount: visibleSelectedRows.value.length,
    selectedRows: allFiltered.value
      ? visibleSelectedRows.value
      : selectedRows.value,
    selectedKeys: allFiltered.value ? [] : selectedKeys.value,
    disabledKeys: disabledKeys.value,
    unselectedKeys: unselectedKeys.value,
    batch: batchPayload.value,
    overrides: Object.values(overrides.value)
  })

  const emitChange = (): void => {
    if (!opts.enabled.value) {
      return
    }

    opts.onChange?.(getSelectionPayload())
  }

  const refreshVisibleRows = (rows: Row[] = opts.visibleRows.value): void => {
    if (!opts.enabled.value) {
      return
    }

    const nextOverrides = { ...overrides.value }
    let changed = false

    for (const row of rows) {
      const key = rowKeyOf(row)

      if (!key || !nextOverrides[key]) {
        continue
      }

      nextOverrides[key] = {
        ...nextOverrides[key],
        row
      }
      changed = true
    }

    if (changed) {
      overrides.value = nextOverrides
    }
  }

  const setRowSelected = (row: Row, selected: boolean): void => {
    if (!opts.enabled.value) {
      return
    }

    const key = rowKeyOf(row)

    if (!key) {
      return
    }

    if (disabledKeySet.value.has(key)) {
      return
    }

    const nextOverrides = { ...overrides.value }

    if (!allFiltered.value) {
      if (selected) {
        nextOverrides[key] = { key, mode: 'selected', row }
      } else {
        delete nextOverrides[key]
      }
    } else if (selected) {
      nextOverrides[key] = { key, mode: 'selected', row }
    } else {
      nextOverrides[key] = { key, mode: 'unselected', row }
    }

    overrides.value = nextOverrides
    emitChange()
  }

  const selectAllPage = (rows: Row[] = opts.visibleRows.value): void => {
    if (!opts.enabled.value) {
      return
    }

    allFiltered.value = false
    const nextOverrides = { ...overrides.value }

    for (const row of rows) {
      const key = rowKeyOf(row)

      if (!key || disabledKeySet.value.has(key)) {
        continue
      }

      nextOverrides[key] = { key, mode: 'selected', row }
    }

    overrides.value = nextOverrides
    emitChange()
  }

  const selectAllFiltered = (_rows: Row[] = opts.visibleRows.value): void => {
    if (!opts.enabled.value || !opts.allowSelectAllFiltered.value) {
      return
    }

    allFiltered.value = true
    overrides.value = {}
    emitChange()
  }

  const clearSelection = (): void => {
    allFiltered.value = false
    overrides.value = {}
    emitChange()
  }

  const clearVisibleRows = (rows: Row[] = opts.visibleRows.value): void => {
    if (!opts.enabled.value) {
      return
    }

    const nextOverrides = { ...overrides.value }

    for (const row of rows) {
      const key = rowKeyOf(row)

      if (!key || disabledKeySet.value.has(key)) {
        continue
      }

      if (!allFiltered.value) {
        delete nextOverrides[key]
      } else {
        nextOverrides[key] = { key, mode: 'unselected', row }
      }
    }

    overrides.value = nextOverrides
    emitChange()
  }

  watch(
    [
      opts.visibleRows,
      opts.query,
      opts.filteredTotal,
      opts.baselineTotal,
      opts.rowKeyField,
      opts.enabled,
      opts.disabledKeys,
      opts.disabledRowsResolved,
      opts.rowDisabledSelectionScope,
      opts.allowSelectAllFiltered
    ],
    (
      [visibleRows, , , , rowKeyField, enabled, , , , allowSelectAllFiltered],
      previousValues
    ) => {
      const previousRowKeyField = previousValues?.[4]

      if (!enabled) {
        if (allFiltered.value || Object.keys(overrides.value).length > 0) {
          allFiltered.value = false
          overrides.value = {}
          emitChange()
        }

        return
      }

      if (!allowSelectAllFiltered && allFiltered.value) {
        allFiltered.value = false
      }

      if (previousRowKeyField && previousRowKeyField !== rowKeyField) {
        allFiltered.value = false
        overrides.value = {}
      }

      refreshVisibleRows(visibleRows)
      emitChange()
    },
    { deep: true, immediate: true }
  )

  return {
    selectedRows,
    allFiltered,
    overrides,
    selectedKeys,
    unselectedKeys,
    visibleSelectedRows,
    allVisibleSelected,
    someVisibleSelected,
    selectedCount,
    isRowSelected,
    setRowSelected,
    selectAllPage,
    selectAllFiltered,
    clearSelection,
    clearVisibleRows,
    refreshVisibleRows,
    getSelectionPayload
  }
}
