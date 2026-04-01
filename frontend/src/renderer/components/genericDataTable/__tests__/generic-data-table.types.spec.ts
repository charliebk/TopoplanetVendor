import { describe, expectTypeOf, it } from 'vitest'
import type {
  GenericDataTableColumn,
  GenericDataTableExpose,
  GenericDataTablePreparedPrintPayload,
  GenericDataTableQuery,
  GenericDataTableRow,
  GenericDataTableSelectionPayload,
  GenericDataTableToolbarSlotPayload
} from '../custom-data-table.public'

interface VendorRow extends GenericDataTableRow {
  id: number
  name: string
  active: boolean
}

describe('CustomDataTable public types', () => {
  it('keeps row-specific columns and expose methods aligned', () => {
    const columns: Array<GenericDataTableColumn<VendorRow>> = [
      {
        field: 'name',
        header: 'Vendor',
        filterable: true
      },
      {
        field: 'active',
        header: 'Active',
        type: 'boolean'
      }
    ]

    expectTypeOf(columns[0]).toMatchTypeOf<GenericDataTableColumn<VendorRow>>()

    expectTypeOf<
      GenericDataTableExpose<VendorRow>['exportCsv']
    >().returns.toEqualTypeOf<string>()
    expectTypeOf<
      GenericDataTableExpose<VendorRow>['preparePrint']
    >().returns.toEqualTypeOf<GenericDataTablePreparedPrintPayload<VendorRow>>()
    expectTypeOf<
      GenericDataTableExpose<VendorRow>['getSelectionPayload']
    >().returns.toEqualTypeOf<GenericDataTableSelectionPayload<VendorRow>>()
    expectTypeOf<
      GenericDataTableToolbarSlotPayload<VendorRow>['rows']
    >().toEqualTypeOf<VendorRow[]>()
    expectTypeOf<
      GenericDataTableToolbarSlotPayload<VendorRow>['selection']
    >().toEqualTypeOf<GenericDataTableSelectionPayload<VendorRow> | null>()
    expectTypeOf<
      GenericDataTableSelectionPayload<VendorRow>['selectedRows']
    >().toEqualTypeOf<VendorRow[]>()
    expectTypeOf<GenericDataTableQuery['page']>().toEqualTypeOf<number>()
  })
})
