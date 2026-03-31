import { computed, ref, watch } from 'vue'
import type {
  GenericDataTableColumn,
  GenericDataTableFilterValue,
  GenericDataTableMatchMode,
  GenericDataTablePrimeFilters,
  GenericDataTableQueryController,
  GenericDataTableQuery,
  GenericDataTableRow
} from './generic-data-table.types'

const resolveBackendField = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>
): string => column.backendField ?? column.field

const resolveMatchMode = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>
): GenericDataTableMatchMode => {
  if (column.matchMode) {
    return column.matchMode
  }

  const filterType = column.filterType ?? column.type ?? 'text'
  return filterType === 'text' ? 'contains' : 'equals'
}

const normalizeFilterValue = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row> | undefined,
  value: GenericDataTableFilterValue
): unknown => {
  if (!column) {
    return value
  }

  if (column.paramTransform) {
    return column.paramTransform(value)
  }

  const filterType = column.filterType ?? column.type ?? 'text'

  if (value === null || value === '') {
    return null
  }

  if (filterType === 'date' && value instanceof Date) {
    const year = value.getFullYear()
    const month = `${value.getMonth() + 1}`.padStart(2, '0')
    const day = `${value.getDate()}`.padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  if (
    (filterType === 'number' ||
      filterType === 'select' ||
      filterType === 'list') &&
    typeof value === 'string' &&
    /^-?\d+(\.\d+)?$/.test(value)
  ) {
    return Number(value)
  }

  return value
}

const buildPrimeFilters = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>,
  query: GenericDataTableQuery
): GenericDataTablePrimeFilters => {
  const next: GenericDataTablePrimeFilters = {
    global: {
      value: query.globalFilter ?? null,
      matchMode: 'contains'
    }
  }

  for (const column of columns) {
    if (!column.filterable) {
      continue
    }

    next[column.field] = {
      value: query.filters?.[resolveBackendField(column)] ?? null,
      matchMode: resolveMatchMode(column)
    }
  }

  return next
}

const buildFilterRecord = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>,
  filters: GenericDataTablePrimeFilters
): Record<string, GenericDataTableFilterValue> => {
  const next: Record<string, GenericDataTableFilterValue> = {}

  for (const [field, filterMeta] of Object.entries(filters)) {
    if (field === 'global') {
      continue
    }

    if (filterMeta.value === '' || filterMeta.value === null) {
      continue
    }

    const column = columns.find((candidate) => candidate.field === field)
    const queryField = column ? resolveBackendField(column) : field
    const normalizedValue = normalizeFilterValue(column, filterMeta.value)

    if (normalizedValue === '' || normalizedValue === null) {
      continue
    }

    next[queryField] = normalizedValue as GenericDataTableFilterValue
  }

  return next
}

export const useGenericDataTableQuery = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>,
  query: GenericDataTableQuery | undefined,
  defaultRows: number
): GenericDataTableQueryController => {
  const normalizedQuery = ref<GenericDataTableQuery>({
    page: query?.page ?? 0,
    size: query?.size ?? defaultRows,
    sortField: query?.sortField ?? null,
    sortOrder: query?.sortOrder ?? 1,
    globalFilter: query?.globalFilter ?? null,
    filters: { ...(query?.filters ?? {}) }
  })

  const primeFilters = ref<GenericDataTablePrimeFilters>(
    buildPrimeFilters(columns, normalizedQuery.value)
  )

  watch(
    () => query,
    (nextQuery) => {
      normalizedQuery.value = {
        page: nextQuery?.page ?? 0,
        size: nextQuery?.size ?? defaultRows,
        sortField: nextQuery?.sortField ?? null,
        sortOrder: nextQuery?.sortOrder ?? 1,
        globalFilter: nextQuery?.globalFilter ?? null,
        filters: { ...(nextQuery?.filters ?? {}) }
      }
      primeFilters.value = buildPrimeFilters(columns, normalizedQuery.value)
    },
    { deep: true }
  )

  watch(
    () => columns,
    () => {
      primeFilters.value = buildPrimeFilters(columns, normalizedQuery.value)
    },
    { deep: true }
  )

  const first = computed(
    () => normalizedQuery.value.page * normalizedQuery.value.size
  )

  const currentQuery = computed<GenericDataTableQuery>(() => ({
    page: normalizedQuery.value.page,
    size: normalizedQuery.value.size,
    sortField: normalizedQuery.value.sortField ?? null,
    sortOrder: normalizedQuery.value.sortOrder ?? 1,
    globalFilter:
      typeof primeFilters.value.global?.value === 'string'
        ? primeFilters.value.global.value
        : null,
    filters: buildFilterRecord(columns, primeFilters.value)
  }))

  const setPage = (page: number, size: number): GenericDataTableQuery => {
    normalizedQuery.value = {
      ...normalizedQuery.value,
      page,
      size
    }

    return currentQuery.value
  }

  const setSort = (
    sortField: string | null,
    sortOrder: 1 | -1
  ): GenericDataTableQuery => {
    const column = columns.find((candidate) => candidate.field === sortField)

    normalizedQuery.value = {
      ...normalizedQuery.value,
      page: 0,
      sortField: column ? resolveBackendField(column) : sortField,
      sortOrder
    }

    return currentQuery.value
  }

  const setFilterValue = (
    field: string,
    value: GenericDataTableFilterValue
  ): GenericDataTableQuery => {
    const currentFilter = primeFilters.value[field] ?? {
      value: null,
      matchMode: 'contains' as GenericDataTableMatchMode
    }

    primeFilters.value = {
      ...primeFilters.value,
      [field]: {
        ...currentFilter,
        value
      }
    }

    normalizedQuery.value = {
      ...normalizedQuery.value,
      page: 0
    }

    return currentQuery.value
  }

  const clearFilters = (): GenericDataTableQuery => {
    primeFilters.value = buildPrimeFilters(columns, {
      ...normalizedQuery.value,
      page: 0,
      globalFilter: null,
      filters: {}
    })

    normalizedQuery.value = {
      ...normalizedQuery.value,
      page: 0,
      globalFilter: null,
      filters: {}
    }

    return currentQuery.value
  }

  return {
    normalizedQuery,
    primeFilters,
    first,
    currentQuery,
    setPage,
    setSort,
    setFilterValue,
    clearFilters
  }
}
