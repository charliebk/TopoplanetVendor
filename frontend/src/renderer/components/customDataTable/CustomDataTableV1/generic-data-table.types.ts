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

export interface GenericDataTableProviderRequest<
  Row extends GenericDataTableRow
> {
  query: GenericDataTableQuery
  columns: Array<GenericDataTableColumn<Row>>
}

export interface GenericDataTableProviderSuccess<
  Row extends GenericDataTableRow
> {
  ok: true
  rows: Row[]
  totalRecords: number
  overallTotal?: number | null
}

export interface GenericDataTableProviderFailure {
  ok: false
  message: string
  cause?: unknown
}

export type GenericDataTableProviderResult<Row extends GenericDataTableRow> =
  | GenericDataTableProviderSuccess<Row>
  | GenericDataTableProviderFailure

export type GenericDataTableDataProvider<Row extends GenericDataTableRow> = (
  request: GenericDataTableProviderRequest<Row>
) => Promise<GenericDataTableProviderResult<Row>>

export interface GenericDataTableLoadPayload<Row extends GenericDataTableRow> {
  query: GenericDataTableQuery
  rows: Row[]
  totalRecords: number
  overallTotal: number | null
  baselineTotal: number | null
}

export interface GenericDataTableProviderErrorPayload {
  query: GenericDataTableQuery
  message: string
  cause?: unknown
}

export interface GenericDataTableRefreshPayload {
  query: GenericDataTableQuery
  providerMode: boolean
}

export interface GenericDataTableCountBarProps {
  baselineTotal: number | null
  filteredTotal: number
  shown?: number | null
  hasFilters: boolean
  showShown?: boolean
  showClearFiltersButton?: boolean
  clearFiltersLabel?: string
  totalLabel?: string
  resultsLabel?: string
  shownLabel?: string
  excludedLabel?: string
}

export interface GenericDataTableExportSource<Row extends GenericDataTableRow> {
  columns: Array<GenericDataTableColumn<Row>>
  rows: Row[]
}

export interface GenericDataTableCsvExportOptions {
  delimiter?: string
  lineBreak?: '\n' | '\r\n'
  includeBom?: boolean
}

export interface GenericDataTablePrintOptions {
  title?: string | null
}

export interface GenericDataTablePreparedExportColumn<
  Row extends GenericDataTableRow
> {
  field: keyof Row & string
  exportKey: string
  header: string
}

export interface GenericDataTablePreparedPrintRow<
  Row extends GenericDataTableRow
> {
  row: Row
  values: Record<string, string>
}

export interface GenericDataTablePreparedPrintPayload<
  Row extends GenericDataTableRow
> {
  title: string | null
  generatedAt: string
  columns: Array<GenericDataTablePreparedExportColumn<Row>>
  rows: Array<GenericDataTablePreparedPrintRow<Row>>
}

export type GenericDataTableSelectionMode = 'none' | 'multiple'

export type GenericDataTableSelectionOverrideMode = 'selected' | 'unselected'

export interface GenericDataTableSelectionOverride<
  Row extends GenericDataTableRow
> {
  key: string
  mode: GenericDataTableSelectionOverrideMode
  row?: Row
}

export interface GenericDataTableSelectionBatchPayload {
  strategy: 'includeKeys' | 'filterQuery'
  filterQuery: GenericDataTableQuery | null
  includeKeys: string[]
  excludeKeys: string[]
  disabledKeys: string[]
  ready: boolean
  reason: string | null
}

export interface GenericDataTableSelectionPayload<
  Row extends GenericDataTableRow
> {
  rowKeyField: string
  query: GenericDataTableQuery
  allFiltered: boolean
  rowDisabledSelectionScope: 'none' | 'visible' | 'filtered'
  disabledRowsResolved: boolean
  filteredTotal: number | null
  baselineTotal: number | null
  disabledCount: number | null
  selectableFilteredCount: number | null
  selectedCount: number | null
  visibleSelectedCount: number
  selectedRows: Row[]
  selectedKeys: string[]
  disabledKeys: string[]
  unselectedKeys: string[]
  batch: GenericDataTableSelectionBatchPayload
  overrides: Array<GenericDataTableSelectionOverride<Row>>
}

export interface GenericDataTableSelectionController<
  Row extends GenericDataTableRow
> {
  isRowSelected: (row: Row) => boolean
  setRowSelected: (row: Row, selected: boolean) => void
  selectAllPage: (rows?: Row[]) => void
  selectAllFiltered: (rows?: Row[]) => void
  clearSelection: () => void
  clearVisibleRows: (rows?: Row[]) => void
  refreshVisibleRows: (rows?: Row[]) => void
  getSelectionPayload: () => GenericDataTableSelectionPayload<Row>
}

export type GenericDataTableExpose<Row extends GenericDataTableRow> = Pick<
  GenericDataTableSelectionController<Row>,
  | 'selectAllPage'
  | 'selectAllFiltered'
  | 'clearSelection'
  | 'refreshVisibleRows'
  | 'getSelectionPayload'
> & {
  refresh: () => Promise<void>
  clearFilters: () => void
  exportCsv: (
    rows?: Row[],
    options?: GenericDataTableCsvExportOptions
  ) => string
  preparePrint: (
    rows?: Row[],
    options?: GenericDataTablePrintOptions
  ) => GenericDataTablePreparedPrintPayload<Row>
}

export interface GenericDataTableToolbarSlotPayload<
  Row extends GenericDataTableRow
> {
  query: GenericDataTableQuery
  rows: Row[]
  filteredTotal: number
  baselineTotal: number | null
  hasFilters: boolean
  selection: GenericDataTableSelectionPayload<Row> | null
  clearFilters: () => void
  clearSelection: () => void
  refresh: () => Promise<void>
}

export interface GenericDataTableCountBarSlotPayload<
  Row extends GenericDataTableRow
> extends GenericDataTableToolbarSlotPayload<Row> {
  shown: number
}

export interface GenericDataTableSelectionOptions<
  Row extends GenericDataTableRow
> {
  visibleRows: ComputedRef<Row[]>
  query: ComputedRef<GenericDataTableQuery>
  filteredTotal: ComputedRef<number | null>
  baselineTotal: ComputedRef<number | null>
  rowKeyField: ComputedRef<string>
  enabled: ComputedRef<boolean>
  disabledKeys: ComputedRef<string[]>
  disabledRowsResolved: ComputedRef<boolean>
  rowDisabledSelectionScope: ComputedRef<'none' | 'visible' | 'filtered'>
  allowSelectAllFiltered: ComputedRef<boolean>
  onChange?: (payload: GenericDataTableSelectionPayload<Row>) => void
}

export type GenericDataTableClassValue =
  | string
  | string[]
  | Record<string, boolean>

export interface GenericDataTableBooleanLabels {
  trueLabel?: string
  falseLabel?: string
  nullLabel?: string
}

export interface GenericDataTableBooleanTagSeverity {
  true?: 'secondary' | 'success' | 'info' | 'warning' | 'danger' | 'contrast'
  false?: 'secondary' | 'success' | 'info' | 'warning' | 'danger' | 'contrast'
  null?: 'secondary' | 'success' | 'info' | 'warning' | 'danger' | 'contrast'
}

export interface GenericDataTableAction<Row extends GenericDataTableRow> {
  key: string
  icon?: string
  label?: string
  tooltip?: string | ((row: Row) => string | null | undefined)
  class?:
    | GenericDataTableClassValue
    | ((row: Row) => GenericDataTableClassValue | null | undefined)
  disabled?: boolean | ((row: Row) => boolean)
  severity?:
    | 'secondary'
    | 'success'
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
  booleanLabels?: GenericDataTableBooleanLabels
  booleanTag?: boolean
  booleanTagSeverity?: GenericDataTableBooleanTagSeverity
  decimals?: number
  format?: (value: unknown, row: Row) => string
  exportable?: boolean
  exportHeader?: string
  exportKey?: string
  exportFormat?: (value: unknown, row: Row) => unknown
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
  selectionMode?: GenericDataTableSelectionMode
  rowKey?: string
  totalRecords?: number
  rowsPerPageOptions?: number[]
  filterDisplay?: 'row' | 'menu'
  showGridlines?: boolean
  stripedRows?: boolean
  rowHover?: boolean
  emptyMessage?: string
  loadingMessage?: string
  errorMessage?: string
  globalFilterPlaceholder?: string
  clearFiltersLabel?: string
  refreshLabel?: string
  selectPageLabel?: string
  selectFilteredLabel?: string
  clearSelectionLabel?: string
  showGlobalFilter?: boolean
  showClearFilters?: boolean
  showRefreshButton?: boolean
  showSelectionToolbar?: boolean
  showCountBar?: boolean
  countBarPosition?: 'top' | 'bottom' | 'both'
  countBarShowShown?: boolean
  rowDisabled?: boolean | ((row: Row) => boolean)
  disabledRowSelectionScope?: 'visible' | 'filtered'
  disabledFilteredRowKeys?: string[]
  disabledFilteredRowsResolved?: boolean
  showPaginator?: boolean
  dataProvider?: GenericDataTableDataProvider<Row>
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

export type GenericDataTableEventName =
  | 'update:query'
  | 'row-click'
  | 'action'
  | 'load'
  | 'refresh'
  | 'selection-change'
  | 'provider-error'

export interface GenericDataTableEmits<Row extends GenericDataTableRow> {
  (event: 'update:query', payload: GenericDataTableUpdateQueryPayload): void
  (event: 'row-click', payload: GenericDataTableRowClickPayload<Row>): void
  (event: 'action', payload: GenericDataTableActionPayload<Row>): void
  (event: 'load', payload: GenericDataTableLoadPayload<Row>): void
  (event: 'refresh', payload: GenericDataTableRefreshPayload): void
  (
    event: 'selection-change',
    payload: GenericDataTableSelectionPayload<Row>
  ): void
  (event: 'provider-error', payload: GenericDataTableProviderErrorPayload): void
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

export type GenericDataTableLoadHandler<Row extends GenericDataTableRow> = (
  payload: GenericDataTableLoadPayload<Row>
) => void

export type GenericDataTableProviderErrorHandler = (
  payload: GenericDataTableProviderErrorPayload
) => void

export type GenericDataTableRefreshHandler = (
  payload: GenericDataTableRefreshPayload
) => void

export type GenericDataTableSelectionChangeHandler<
  Row extends GenericDataTableRow
> = (payload: GenericDataTableSelectionPayload<Row>) => void

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

export interface GenericDataTableProviderState<
  Row extends GenericDataTableRow
> {
  rows: Ref<Row[]>
  totalRecords: Ref<number>
  overallTotal: Ref<number | null>
  baselineTotal: Ref<number | null>
  loading: Ref<boolean>
  error: Ref<GenericDataTableProviderFailure | null>
  reload: () => Promise<void>
}

export interface GenericDataTableSelectionState<
  Row extends GenericDataTableRow
> extends GenericDataTableSelectionController<Row> {
  selectedRows: Ref<Row[]>
  allFiltered: Ref<boolean>
  overrides: Ref<Record<string, GenericDataTableSelectionOverride<Row>>>
  selectedKeys: ComputedRef<string[]>
  unselectedKeys: ComputedRef<string[]>
  visibleSelectedRows: ComputedRef<Row[]>
  allVisibleSelected: ComputedRef<boolean>
  someVisibleSelected: ComputedRef<boolean>
  selectedCount: ComputedRef<number | null>
}
