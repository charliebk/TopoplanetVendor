import { computed, ref, watch, type ComputedRef, type Ref } from 'vue'
import type {
  GenericDataTableColumn,
  GenericDataTableOption,
  GenericDataTableOptionLoadError,
  GenericDataTableOptionProviderContext,
  GenericDataTableQuery,
  GenericDataTableRow
} from './generic-data-table.types'

interface UseGenericDataTableOptionsConfig<Row extends GenericDataTableRow> {
  columns: ComputedRef<Array<GenericDataTableColumn<Row>>>
  query: ComputedRef<GenericDataTableQuery>
}

interface GenericDataTableOptionsController<Row extends GenericDataTableRow> {
  resolvedOptions: ComputedRef<Record<string, GenericDataTableOption[]>>
  loadingByField: Ref<Record<string, boolean>>
  errorByField: Ref<Record<string, GenericDataTableOptionLoadError | null>>
  reloadFilterOptions: (fields?: Array<keyof Row & string>) => Promise<void>
}

const defaultOptionLabelField = 'name'
const defaultOptionValueField = 'id'
const defaultIncludeAllLabel = 'All'

const isMappedOption = (value: unknown): value is GenericDataTableOption => {
  if (!value || typeof value !== 'object') {
    return false
  }

  return 'label' in value && 'value' in value
}

const normalizeOptionValue = (
  value: unknown
): string | number | boolean | null => {
  if (
    typeof value === 'string' ||
    typeof value === 'number' ||
    typeof value === 'boolean' ||
    value === null
  ) {
    return value
  }

  return value === undefined ? null : String(value)
}

const mapRawOption = <Row extends GenericDataTableRow>(
  raw: unknown,
  column: GenericDataTableColumn<Row>,
  context: GenericDataTableOptionProviderContext<Row>
): GenericDataTableOption => {
  if (column.optionTransform) {
    return column.optionTransform(raw, column, context)
  }

  if (isMappedOption(raw)) {
    return {
      label: String(raw.label ?? ''),
      value: normalizeOptionValue(raw.value)
    }
  }

  if (raw && typeof raw === 'object') {
    const rawRecord = raw as Record<string, unknown>
    const labelField = column.optionLabelField ?? defaultOptionLabelField
    const valueField = column.optionValueField ?? defaultOptionValueField

    return {
      label: String(rawRecord[labelField] ?? ''),
      value: normalizeOptionValue(rawRecord[valueField])
    }
  }

  return {
    label: String(raw ?? ''),
    value: normalizeOptionValue(raw)
  }
}

const withAllOption = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>,
  options: GenericDataTableOption[]
): GenericDataTableOption[] => {
  if (!column.includeAllOption) {
    return options
  }

  return [
    {
      label: column.includeAllLabel ?? defaultIncludeAllLabel,
      value: null
    },
    ...options.filter((option) => option.value !== null)
  ]
}

export const useGenericDataTableOptions = <Row extends GenericDataTableRow>(
  config: UseGenericDataTableOptionsConfig<Row>
): GenericDataTableOptionsController<Row> => {
  const rawItemsByField = ref<Record<string, unknown[]>>({})
  const loadingByField = ref<Record<string, boolean>>({})
  const errorByField = ref<
    Record<string, GenericDataTableOptionLoadError | null>
  >({})
  const requestTokenByField = ref<Record<string, number>>({})

  const providerColumns = computed(() => {
    return config.columns.value.filter(
      (column) => typeof column.optionItemsProvider === 'function'
    )
  })

  const loadColumnOptions = async (
    column: GenericDataTableColumn<Row>
  ): Promise<void> => {
    if (typeof column.optionItemsProvider !== 'function') {
      return
    }

    const field = column.field
    const nextToken = (requestTokenByField.value[field] ?? 0) + 1
    requestTokenByField.value = {
      ...requestTokenByField.value,
      [field]: nextToken
    }
    loadingByField.value = {
      ...loadingByField.value,
      [field]: true
    }
    errorByField.value = {
      ...errorByField.value,
      [field]: null
    }

    const context: GenericDataTableOptionProviderContext<Row> = {
      query: config.query.value,
      column
    }

    try {
      const items = await Promise.resolve(column.optionItemsProvider(context))

      if (requestTokenByField.value[field] !== nextToken) {
        return
      }

      rawItemsByField.value = {
        ...rawItemsByField.value,
        [field]: Array.isArray(items) ? items : []
      }
    } catch (error) {
      if (requestTokenByField.value[field] === nextToken) {
        rawItemsByField.value = {
          ...rawItemsByField.value,
          [field]: []
        }
        errorByField.value = {
          ...errorByField.value,
          [field]: {
            message:
              error instanceof Error && error.message
                ? error.message
                : 'Filter options could not be loaded.',
            cause: error
          }
        }
      }
    } finally {
      if (requestTokenByField.value[field] === nextToken) {
        loadingByField.value = {
          ...loadingByField.value,
          [field]: false
        }
      }
    }
  }

  const reloadFilterOptions = async (
    fields?: Array<keyof Row & string>
  ): Promise<void> => {
    const targetFields = new Set(fields ?? [])
    const columnsToLoad = providerColumns.value.filter((column) => {
      return targetFields.size === 0 || targetFields.has(column.field)
    })

    await Promise.all(columnsToLoad.map((column) => loadColumnOptions(column)))
  }

  watch(
    providerColumns,
    (columns) => {
      void Promise.all(
        columns
          .filter(
            (column) => (column.optionReloadStrategy ?? 'mount') !== 'manual'
          )
          .map((column) => loadColumnOptions(column))
      )
    },
    { immediate: true, deep: true }
  )

  watch(
    config.query,
    () => {
      void Promise.all(
        providerColumns.value
          .filter(
            (column) =>
              (column.optionReloadStrategy ?? 'mount') === 'query-change'
          )
          .map((column) => loadColumnOptions(column))
      )
    },
    { deep: true }
  )

  const resolvedOptions = computed<Record<string, GenericDataTableOption[]>>(
    () => {
      return Object.fromEntries(
        config.columns.value.map((column) => {
          const context: GenericDataTableOptionProviderContext<Row> = {
            query: config.query.value,
            column
          }
          const baseOptions = column.optionItemsProvider
            ? (rawItemsByField.value[column.field] ?? []).map((item) =>
                mapRawOption(item, column, context)
              )
            : [...(column.filterOptions ?? [])]

          return [column.field, withAllOption(column, baseOptions)]
        })
      )
    }
  )

  return {
    resolvedOptions,
    loadingByField,
    errorByField,
    reloadFilterOptions
  }
}
