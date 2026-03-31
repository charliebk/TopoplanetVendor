import type { ComputedRef, Ref } from 'vue'

export type GenericDataTableRow = Record<string, unknown>

export type GenericDataTableMatchMode =
  | 'contains'
  | 'equals'
  | 'startsWith'
  | 'endsWith'

export type GenericDataTableColumnType =
  | 'text'
  | 'number'
  | 'integer'
  | 'date'
  | 'boolean'
  | 'list'
  | 'select'
  | 'percent'
  | 'idIcon'
  | 'actions'

export type GenericDataTableFilterType =
  | 'text'
  | 'number'
  | 'date'
  | 'boolean'
  | 'list'
  | 'select'

export type GenericDataTableFilterValue =
  | string
  | number
  | boolean
  | Date
  | null

export interface GenericDataTableOption {
  label: string
  value: string | number | boolean | null
}

export interface GenericDataTableAction<Row extends GenericDataTableRow> {
  key: string
  icon: string
  label?: string
  tooltip?: string
  class?: string
  disabled?: boolean | ((row: Row) => boolean)
  severity?:
    | 'secondary'
    | 'success'
    | 'info'
    | 'warning'
    | 'help'
    | 'danger'
    | 'contrast'
}

export interface GenericDataTableColumn<Row extends GenericDataTableRow> {
  field: keyof Row & string
  header: string
  type?: GenericDataTableColumnType
  sortable?: boolean
  filterable?: boolean
  filterType?: GenericDataTableFilterType
  matchMode?: GenericDataTableMatchMode
  filterOptions?: GenericDataTableOption[]
  width?: string
  minWidth?: string
  maxWidth?: string
  align?: 'left' | 'center' | 'right'
  displayField?: keyof Row & string
  backendField?: string
  paramTransform?: (value: GenericDataTableFilterValue, row?: Row) => unknown
  tooltipField?: keyof Row & string
  idIconClass?: string | ((row: Row) => string)
  decimals?: number
  format?: (value: unknown, row: Row) => string
  actions?: Array<GenericDataTableAction<Row>>
}

export interface GenericDataTableQuery {
  page: number
  size: number
  sortField?: string | null
  sortOrder?: 1 | -1
  globalFilter?: string | null
  filters?: Record<string, GenericDataTableFilterValue>
}

export interface GenericDataTableProps<Row extends GenericDataTableRow> {
  columns: Array<GenericDataTableColumn<Row>>
  rows: Row[]
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
}

export interface GenericDataTableLoadResult<Row extends GenericDataTableRow> {
  rows: Row[]
  totalRecords: number
}

export interface GenericDataTableActionPayload<
  Row extends GenericDataTableRow
> {
  actionKey: string
  row: Row
}

export type GenericDataTableUpdateQueryPayload = GenericDataTableQuery

export type GenericDataTableRowClickPayload<Row extends GenericDataTableRow> =
  Row

export type GenericDataTableEventName = 'update:query' | 'row-click' | 'action'

export interface GenericDataTableEmits<Row extends GenericDataTableRow> {
  (event: 'update:query', payload: GenericDataTableUpdateQueryPayload): void
  (event: 'row-click', payload: GenericDataTableRowClickPayload<Row>): void
  (event: 'action', payload: GenericDataTableActionPayload<Row>): void
}

export type GenericDataTableQueryChangeHandler = (
  payload: GenericDataTableUpdateQueryPayload
) => void

export type GenericDataTableRowClickHandler<Row extends GenericDataTableRow> = (
  payload: GenericDataTableRowClickPayload<Row>
) => void

export type GenericDataTableActionHandler<Row extends GenericDataTableRow> = (
  payload: GenericDataTableActionPayload<Row>
) => void

export interface GenericDataTablePrimeFilter {
  value: GenericDataTableFilterValue
  matchMode: GenericDataTableMatchMode
}

export type GenericDataTablePrimeFilters = Record<
  string,
  GenericDataTablePrimeFilter
>

export interface GenericDataTableQueryController {
  normalizedQuery: Ref<GenericDataTableQuery>
  primeFilters: Ref<GenericDataTablePrimeFilters>
  first: ComputedRef<number>
  currentQuery: ComputedRef<GenericDataTableQuery>
  setPage: (page: number, size: number) => GenericDataTableQuery
  setSort: (
    sortField: string | null,
    sortOrder: 1 | -1
  ) => GenericDataTableQuery
  setFilterValue: (
    field: string,
    value: GenericDataTableFilterValue
  ) => GenericDataTableQuery
  clearFilters: () => GenericDataTableQuery
}
