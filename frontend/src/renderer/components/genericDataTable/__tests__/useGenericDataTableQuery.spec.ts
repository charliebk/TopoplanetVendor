import { nextTick, ref } from 'vue'
import { describe, expect, it } from 'vitest'
import type {
  GenericDataTableColumn,
  GenericDataTableQuery,
  GenericDataTableRow
} from '../generic-data-table.types'
import { useGenericDataTableQuery } from '../useGenericDataTableQuery'

interface AuditRow extends GenericDataTableRow {
  id: number
  status: string
  amount: number
  createdAt: string
}

describe('useGenericDataTableQuery', () => {
  it('normalizes backend fields, numbers, dates and global filters', () => {
    const columns = ref<Array<GenericDataTableColumn<AuditRow>>>([
      {
        field: 'status',
        header: 'Status',
        filterable: true,
        backendField: 'statusId',
        type: 'list'
      },
      {
        field: 'amount',
        header: 'Amount',
        filterable: true,
        filterType: 'number'
      },
      {
        field: 'createdAt',
        header: 'Created',
        filterable: true,
        filterType: 'date'
      }
    ])

    const externalQuery = ref<GenericDataTableQuery>({
      page: 1,
      size: 25,
      sortField: 'statusId',
      sortOrder: 1,
      globalFilter: null,
      filters: {
        statusId: 'approved'
      }
    })

    const controller = useGenericDataTableQuery<AuditRow>(
      columns,
      externalQuery,
      10
    )

    expect(controller.first.value).toBe(25)
    expect(controller.primeFilters.value.status.value).toBe('approved')

    const sortedQuery = controller.setSort('status', -1)
    expect(sortedQuery.sortField).toBe('statusId')
    expect(sortedQuery.sortOrder).toBe(-1)

    const numberQuery = controller.setFilterValue('amount', '42')
    expect(numberQuery.filters?.amount).toBe(42)

    const dateQuery = controller.setFilterValue(
      'createdAt',
      new Date(2026, 2, 31)
    )
    expect(dateQuery.filters?.createdAt).toBe('2026-03-31')

    const globalQuery = controller.setFilterValue('global', 'delta')
    expect(globalQuery.globalFilter).toBe('delta')

    const clearedQuery = controller.clearFilters()
    expect(clearedQuery.page).toBe(0)
    expect(clearedQuery.globalFilter).toBeNull()
    expect(clearedQuery.filters).toEqual({})
  })

  it('reacts to external query and column changes', async () => {
    const columns = ref<Array<GenericDataTableColumn<AuditRow>>>([
      {
        field: 'status',
        header: 'Status',
        filterable: true,
        backendField: 'statusId'
      }
    ])

    const externalQuery = ref<GenericDataTableQuery>({
      page: 0,
      size: 10,
      sortField: null,
      sortOrder: 1,
      globalFilter: null,
      filters: {
        statusId: 'draft'
      }
    })

    const controller = useGenericDataTableQuery<AuditRow>(
      columns,
      externalQuery,
      10
    )

    externalQuery.value = {
      page: 2,
      size: 20,
      sortField: 'statusId',
      sortOrder: -1,
      globalFilter: 'legacy',
      filters: {
        statusId: 'blocked'
      }
    }

    await nextTick()

    expect(controller.normalizedQuery.value.page).toBe(2)
    expect(controller.currentQuery.value.globalFilter).toBe('legacy')
    expect(controller.primeFilters.value.status.value).toBe('blocked')

    columns.value = [
      {
        field: 'status',
        header: 'Status',
        filterable: true,
        backendField: 'stateCode'
      }
    ]

    externalQuery.value = {
      ...externalQuery.value,
      filters: {
        stateCode: 'archived'
      }
    }

    await nextTick()

    expect(controller.primeFilters.value.status.value).toBe('archived')
  })
})
