import type {
  GenericDataTableAction,
  GenericDataTableActionPayload,
  GenericDataTableClassValue,
  GenericDataTableRow
} from './generic-data-table.types'

const toClassList = (
  resolvedClass: GenericDataTableClassValue | null | undefined
): GenericDataTableClassValue[] => {
  return resolvedClass ? [resolvedClass] : []
}

export const isGenericDataTableActionDisabled = <
  Row extends GenericDataTableRow
>(
  action: GenericDataTableAction<Row>,
  row: Row,
  rowDisabled: boolean
): boolean => {
  if (rowDisabled) {
    return true
  }

  if (typeof action.disabled === 'function') {
    return action.disabled(row)
  }

  return action.disabled === true
}

export const resolveGenericDataTableActionClass = <
  Row extends GenericDataTableRow
>(
  action: GenericDataTableAction<Row>,
  row: Row,
  rowDisabled: boolean
): Array<string | GenericDataTableClassValue | Record<string, boolean>> => {
  const resolvedClass =
    typeof action.class === 'function' ? action.class(row) : action.class

  return [
    'generic-data-table__action-button',
    `generic-data-table__action-button--${action.key}`,
    {
      'generic-data-table__action-button--disabled':
        isGenericDataTableActionDisabled(action, row, rowDisabled)
    },
    ...toClassList(resolvedClass)
  ]
}

export const resolveGenericDataTableActionAriaLabel = <
  Row extends GenericDataTableRow
>(
  action: GenericDataTableAction<Row>,
  row: Row,
  tooltip: string | null | undefined
): string => {
  return tooltip || action.label || action.key
}

export const buildGenericDataTableActionPayload = <
  Row extends GenericDataTableRow
>(
  action: GenericDataTableAction<Row>,
  row: Row,
  rowDisabled: boolean
): GenericDataTableActionPayload<Row> | null => {
  if (isGenericDataTableActionDisabled(action, row, rowDisabled)) {
    return null
  }

  return {
    actionKey: action.key,
    row
  }
}
