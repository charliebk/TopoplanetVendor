import { computed, ref, watch } from 'vue'
import type {
  GenericDataTableColumn,
  GenericDataTableFilterValue,
  GenericDataTableQuery,
  GenericDataTableRow
} from './generic-data-table.types'

type PrimeFilterMetaValue = {
  value: GenericDataTableFilterValue
  matchMode: 'contains' | 'equals'
}

type PrimeFilters = Record<string, PrimeFilterMetaValue>

const resolveMatchMode = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>
): 'contains' | 'equals' => {
  const filterType = column.filterType ?? column.type ?? 'text'
  return filterType === 'text' ? 'contains' : 'equals'
}

const buildPrimeFilters = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>,
  query: GenericDataTableQuery
): PrimeFilters => {
  const next: PrimeFilters = {
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
      value: query.filters?.[column.field] ?? null,
      matchMode: resolveMatchMode(column)
    }
  }

  return next
}

const buildFilterRecord = (
  filters: PrimeFilters
): Record<string, GenericDataTableFilterValue> => {
  const next: Record<string, GenericDataTableFilterValue> = {}

  for (const [field, filterMeta] of Object.entries(filters)) {
    if (field === 'global') {
      continue
    }

    if (filterMeta.value === '' || filterMeta.value === null) {
      continue
    }

    next[field] = filterMeta.value
  }

  return next
}

export const useGenericDataTableQuery = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>,
  query: GenericDataTableQuery | undefined,
  defaultRows: number
) => {
  const normalizedQuery = ref<GenericDataTableQuery>({
    page: query?.page ?? 0,
    size: query?.size ?? defaultRows,
    sortField: query?.sortField ?? null,
    sortOrder: query?.sortOrder ?? 1,
    globalFilter: query?.globalFilter ?? null,
    filters: { ...(query?.filters ?? {}) }
  })

  const primeFilters = ref<PrimeFilters>(
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

  const first = computed(
    () => normalizedQuery.value.page * normalizedQuery.value.size
  )

  const currentQuery = computed<GenericDataTableQuery>(() => ({
    page: normalizedQuery.value.page,
    size: normalizedQuery.value.size,
    sortField: normalizedQuery.value.sortField ?? null,
    sortOrder: normalizedQuery.value.sortOrder ?? 1,
    globalFilter: primeFilters.value.global?.value?.toString() || null,
    filters: buildFilterRecord(primeFilters.value)
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
    normalizedQuery.value = {
      ...normalizedQuery.value,
      page: 0,
      sortField,
      sortOrder
    }

    return currentQuery.value
  }

  const setFilterValue = (
    field: string,
    value: GenericDataTableFilterValue
  ): GenericDataTableQuery => {
    primeFilters.value = {
      ...primeFilters.value,
      [field]: {
        ...primeFilters.value[field],
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