export type GenericDataTableRow = Record<string, unknown>

export type GenericDataTableColumnType =
  | 'text'
  | 'number'
  | 'boolean'
  | 'select'
  | 'actions'

export type GenericDataTableFilterType =
  | 'text'
  | 'number'
  | 'boolean'
  | 'select'

export type GenericDataTableFilterValue = string | number | boolean | null

export interface GenericDataTableOption {
  label: string
  value: string | number | boolean | null
}

export interface GenericDataTableAction<Row extends GenericDataTableRow> {
  key: string
  icon: string
  label?: string
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
  filterOptions?: GenericDataTableOption[]
  width?: string
  align?: 'left' | 'center' | 'right'
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