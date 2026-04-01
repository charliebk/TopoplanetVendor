import type {
  GenericDataTableQuery,
  GenericDataTableRow,
  GenericDataTableSelectionBatchPayload,
  GenericDataTableSelectionPayload
} from './generic-data-table.types'

const cloneQuery = (
  query: GenericDataTableQuery | null
): GenericDataTableQuery | null => {
  if (!query) {
    return null
  }

  return {
    ...query,
    filters: query.filters ? { ...query.filters } : {}
  }
}

const deriveReason = <Row extends GenericDataTableRow>(
  selection: GenericDataTableSelectionPayload<Row>,
  ready: boolean
): string | null => {
  if (ready) {
    return null
  }

  if (selection.rowDisabledSelectionScope === 'visible') {
    return 'Disabled rows are only resolved for the visible page.'
  }

  if (selection.rowDisabledSelectionScope === 'filtered') {
    return 'Disabled rows are not resolved yet for the full filtered result.'
  }

  return 'Filtered batch selection is not available for the current state.'
}

export const toBatchRequest = <Row extends GenericDataTableRow>(
  selection: GenericDataTableSelectionPayload<Row> | null | undefined
): GenericDataTableSelectionBatchPayload | null => {
  if (!selection) {
    return null
  }

  const strategy = selection.allFiltered ? 'filterQuery' : 'includeKeys'
  const ready =
    !selection.allFiltered ||
    selection.rowDisabledSelectionScope === 'none' ||
    selection.disabledRowsResolved

  return {
    strategy,
    filterQuery: selection.allFiltered ? cloneQuery(selection.query) : null,
    includeKeys: selection.allFiltered ? [] : [...selection.selectedKeys],
    excludeKeys: selection.allFiltered ? [...selection.unselectedKeys] : [],
    disabledKeys: [...selection.disabledKeys],
    ready,
    reason: deriveReason(selection, ready)
  }
}
