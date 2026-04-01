import { describe, expect, it } from 'vitest'
import type {
  GenericDataTableAction,
  GenericDataTableRow
} from '../generic-data-table.types'
import {
  buildGenericDataTableActionPayload,
  isGenericDataTableActionDisabled,
  resolveGenericDataTableActionAriaLabel,
  resolveGenericDataTableActionClass
} from '../generic-data-table.actions'

interface VendorRow extends GenericDataTableRow {
  id: number
  name: string
  archived: boolean
}

describe('generic data table action helpers', () => {
  const row: VendorRow = {
    id: 7,
    name: 'Northwind',
    archived: false
  }

  it('blocks actions when either the row or the action is disabled', () => {
    const action: GenericDataTableAction<VendorRow> = {
      key: 'archive',
      label: 'Archive',
      disabled: (currentRow) => currentRow.archived
    }

    expect(
      isGenericDataTableActionDisabled<VendorRow>(action, row, false)
    ).toBe(false)
    expect(isGenericDataTableActionDisabled<VendorRow>(action, row, true)).toBe(
      true
    )
    expect(
      buildGenericDataTableActionPayload<VendorRow>(action, row, false)
    ).toEqual({
      actionKey: 'archive',
      row
    })
    expect(
      buildGenericDataTableActionPayload<VendorRow>(action, row, true)
    ).toBeNull()
  })

  it('prefers tooltip text for aria labels and adds disabled class state', () => {
    const action: GenericDataTableAction<VendorRow> = {
      key: 'inspect',
      label: 'Inspect',
      class: 'vendor-action',
      tooltip: (currentRow) => `Inspect ${currentRow.name}`,
      disabled: true
    }

    expect(
      resolveGenericDataTableActionAriaLabel<VendorRow>(
        action,
        row,
        'Inspect Northwind'
      )
    ).toBe('Inspect Northwind')

    expect(
      resolveGenericDataTableActionClass<VendorRow>(action, row, false)
    ).toEqual(
      expect.arrayContaining([
        'generic-data-table__action-button',
        'generic-data-table__action-button--inspect',
        'vendor-action',
        {
          'generic-data-table__action-button--disabled': true
        }
      ])
    )
  })
})
