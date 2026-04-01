import type {
  GenericDataTableColumn,
  GenericDataTableCsvExportOptions,
  GenericDataTableExportSource,
  GenericDataTablePreparedExportColumn,
  GenericDataTablePreparedPrintPayload,
  GenericDataTablePreparedPrintRow,
  GenericDataTablePrintOptions,
  GenericDataTableRow
} from './generic-data-table.types'

const defaultCsvOptions: Required<GenericDataTableCsvExportOptions> = {
  delimiter: ',',
  lineBreak: '\r\n',
  includeBom: true
}

const isExportableColumn = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>
): boolean => {
  if (column.type === 'actions') {
    return false
  }

  return column.exportable !== false
}

const resolveExportColumns = <Row extends GenericDataTableRow>(
  columns: Array<GenericDataTableColumn<Row>>
): Array<GenericDataTablePreparedExportColumn<Row>> => {
  return columns.filter(isExportableColumn).map((column) => ({
    field: column.field,
    exportKey: column.exportKey ?? column.displayField ?? column.field,
    header: column.exportHeader ?? column.header
  }))
}

const resolveBooleanLabel = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>,
  value: unknown
): string => {
  if (value === true) {
    return column.booleanLabels?.trueLabel ?? 'Yes'
  }

  if (value === false) {
    return column.booleanLabels?.falseLabel ?? 'No'
  }

  return column.booleanLabels?.nullLabel ?? '-'
}

const stringifyExportValue = (value: unknown): string => {
  if (value === null || value === undefined) {
    return ''
  }

  if (typeof value === 'string') {
    return value
  }

  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }

  if (value instanceof Date) {
    return value.toLocaleDateString()
  }

  return String(value)
}

const formatFallbackExportValue = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>,
  value: unknown,
  row: Row
): string => {
  if (column.format) {
    return column.format(value, row)
  }

  if (column.type === 'boolean') {
    return resolveBooleanLabel(column, value)
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
      return stringifyExportValue(value)
    }

    return dateValue.toLocaleDateString()
  }

  return stringifyExportValue(value)
}

const resolveCellText = <Row extends GenericDataTableRow>(
  column: GenericDataTableColumn<Row>,
  row: Row,
  exportKey: string
): string => {
  const rawValue = row[exportKey]

  if (column.exportFormat) {
    return stringifyExportValue(column.exportFormat(rawValue, row))
  }

  return formatFallbackExportValue(column, rawValue, row)
}

const escapeCsvCell = (value: string, delimiter: string): string => {
  const escapedValue = value.replace(/"/g, '""')
  const needsQuotes =
    escapedValue.includes('"') ||
    escapedValue.includes('\n') ||
    escapedValue.includes('\r') ||
    escapedValue.includes(delimiter)

  return needsQuotes ? `"${escapedValue}"` : escapedValue
}

export const prepareDataTablePrint = <Row extends GenericDataTableRow>(
  source: GenericDataTableExportSource<Row>,
  options?: GenericDataTablePrintOptions
): GenericDataTablePreparedPrintPayload<Row> => {
  const columns = resolveExportColumns(source.columns)
  const rows: Array<GenericDataTablePreparedPrintRow<Row>> = source.rows.map(
    (row) => {
      const values = Object.fromEntries(
        columns.map((column) => {
          const sourceColumn = source.columns.find(
            (candidate) => candidate.field === column.field
          )

          if (!sourceColumn) {
            return [column.exportKey, '']
          }

          return [
            column.exportKey,
            resolveCellText(sourceColumn, row, column.exportKey)
          ]
        })
      )

      return {
        row,
        values
      }
    }
  )

  return {
    title: options?.title ?? null,
    generatedAt: new Date().toISOString(),
    columns,
    rows
  }
}

export const exportDataTableCsv = <Row extends GenericDataTableRow>(
  source: GenericDataTableExportSource<Row>,
  options?: GenericDataTableCsvExportOptions
): string => {
  const resolvedOptions = {
    ...defaultCsvOptions,
    ...options
  }
  const printPayload = prepareDataTablePrint(source)
  const headerLine = printPayload.columns
    .map((column) => escapeCsvCell(column.header, resolvedOptions.delimiter))
    .join(resolvedOptions.delimiter)
  const dataLines = printPayload.rows.map((row) => {
    return printPayload.columns
      .map((column) => {
        return escapeCsvCell(
          row.values[column.exportKey] ?? '',
          resolvedOptions.delimiter
        )
      })
      .join(resolvedOptions.delimiter)
  })
  const prefix = resolvedOptions.includeBom ? '\uFEFF' : ''

  return `${prefix}${[headerLine, ...dataLines].join(resolvedOptions.lineBreak)}`
}
