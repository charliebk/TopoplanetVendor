import { ref, watch, type ComputedRef, type Ref } from 'vue'
import type {
  GenericDataTableColumn,
  GenericDataTableDataProvider,
  GenericDataTableLoadPayload,
  GenericDataTableProviderErrorPayload,
  GenericDataTableProviderFailure,
  GenericDataTableProviderState,
  GenericDataTableQuery,
  GenericDataTableRow
} from './generic-data-table.types'

const hasActiveFilters = (query: GenericDataTableQuery): boolean => {
  if (query.globalFilter && query.globalFilter.trim().length > 0) {
    return true
  }

  return Object.values(query.filters ?? {}).some((value) => {
    if (value === null) {
      return false
    }

    if (typeof value === 'string') {
      return value.trim().length > 0
    }

    return true
  })
}

const buildFailurePayload = (
  query: GenericDataTableQuery,
  message: string,
  cause?: unknown
): GenericDataTableProviderErrorPayload => ({
  query,
  message,
  cause
})

export const useGenericDataTableProvider = <
  Row extends GenericDataTableRow
>(opts: {
  columns: ComputedRef<Array<GenericDataTableColumn<Row>>>
  query: ComputedRef<GenericDataTableQuery>
  dataProvider: ComputedRef<GenericDataTableDataProvider<Row> | undefined>
  enabled: ComputedRef<boolean>
  onLoad?: (payload: GenericDataTableLoadPayload<Row>) => void
  onError?: (payload: GenericDataTableProviderErrorPayload) => void
}): GenericDataTableProviderState<Row> => {
  const rows = ref<Row[]>([]) as Ref<Row[]>
  const totalRecords = ref(0)
  const overallTotal = ref<number | null>(null)
  const baselineTotal = ref<number | null>(null)
  const loading = ref(false)
  const error = ref<GenericDataTableProviderFailure | null>(null)

  const reload = async (): Promise<void> => {
    if (!opts.enabled.value || !opts.dataProvider.value) {
      return
    }

    loading.value = true
    error.value = null

    try {
      const result = await opts.dataProvider.value({
        query: opts.query.value,
        columns: opts.columns.value
      })

      if (!result.ok) {
        rows.value = []
        totalRecords.value = 0
        error.value = result
        opts.onError?.(
          buildFailurePayload(opts.query.value, result.message, result.cause)
        )
        return
      }

      rows.value = result.rows
      totalRecords.value = result.totalRecords

      const resolvedOverallTotal = result.overallTotal ?? result.totalRecords
      overallTotal.value = resolvedOverallTotal

      if (!hasActiveFilters(opts.query.value)) {
        baselineTotal.value = resolvedOverallTotal
      } else if (baselineTotal.value === null) {
        baselineTotal.value = resolvedOverallTotal
      }

      opts.onLoad?.({
        query: opts.query.value,
        rows: result.rows,
        totalRecords: result.totalRecords,
        overallTotal: resolvedOverallTotal,
        baselineTotal: baselineTotal.value
      })
    } catch (cause) {
      rows.value = []
      totalRecords.value = 0
      error.value = {
        ok: false,
        message: 'Failed to load data',
        cause
      }
      opts.onError?.(
        buildFailurePayload(opts.query.value, 'Failed to load data', cause)
      )
    } finally {
      loading.value = false
    }
  }

  watch(
    [opts.query, opts.columns, opts.dataProvider, opts.enabled],
    async ([, , , enabled]) => {
      if (!enabled) {
        rows.value = []
        totalRecords.value = 0
        overallTotal.value = null
        baselineTotal.value = null
        error.value = null
        loading.value = false
        return
      }

      await reload()
    },
    { deep: true, immediate: true }
  )

  return {
    rows,
    totalRecords,
    overallTotal,
    baselineTotal,
    loading,
    error,
    reload
  }
}
