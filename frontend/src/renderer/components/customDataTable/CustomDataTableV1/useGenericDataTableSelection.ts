import { computed, ref, watch } from 'vue'
import type {
  GenericDataTableRow,
  GenericDataTableSelectionOptions,
  GenericDataTableSelectionOverride,
  GenericDataTableSelectionPayload,
  GenericDataTableSelectionState
} from './generic-data-table.types'
import { computed, ref, watch } from 'vue'
import type {
  GenericDataTableRow,
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
      opts.visibleRows.value.length > 0 &&
      opts.visibleRows.value.every((row) => isRowSelected(row))
    )
  })

  const someVisibleSelected = computed(() => {
    return visibleSelectedRows.value.length > 0 && !allVisibleSelected.value
  })

  const selectedCount = computed(() => {
    if (allFiltered.value) {
      if (opts.filteredTotal.value === null) {
        return null
      }

      return Math.max(0, opts.filteredTotal.value - unselectedKeys.value.length)
    }

    return selectedKeys.value.length
  })

  const getSelectionPayload = (): GenericDataTableSelectionPayload<Row> => ({
    rowKeyField: opts.rowKeyField.value,
    query: opts.query.value,
    allFiltered: allFiltered.value,
    filteredTotal: opts.filteredTotal.value,
    baselineTotal: opts.baselineTotal.value,
    selectedCount: selectedCount.value,
    visibleSelectedCount: visibleSelectedRows.value.length,
    selectedRows: allFiltered.value
      ? visibleSelectedRows.value
      : selectedRows.value,
    selectedKeys: allFiltered.value ? [] : selectedKeys.value,
    unselectedKeys: unselectedKeys.value,
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

      if (!key) {
        continue
      }

      nextOverrides[key] = { key, mode: 'selected', row }
    }

    overrides.value = nextOverrides
    emitChange()
  }

  const selectAllFiltered = (rows: Row[] = opts.visibleRows.value): void => {
    if (!opts.enabled.value) {
      return
    }

    allFiltered.value = true
    const nextOverrides: Record<
      string,
      GenericDataTableSelectionOverride<Row>
    > = {}

    for (const row of rows) {
      const key = rowKeyOf(row)

      if (!key) {
        continue
      }

      nextOverrides[key] = { key, mode: 'selected', row }
    }

    overrides.value = nextOverrides
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

      if (!key) {
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
      opts.enabled
    ],
    ([visibleRows, , , , rowKeyField, enabled], previousValues) => {
      const previousRowKeyField = previousValues?.[4]

      if (!enabled) {
        if (allFiltered.value || Object.keys(overrides.value).length > 0) {
          allFiltered.value = false
          overrides.value = {}
          emitChange()
        }

        return
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
