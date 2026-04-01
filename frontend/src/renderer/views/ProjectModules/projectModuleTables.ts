import type {
  GenericDataTableColumn,
  GenericDataTableDataProvider,
  GenericDataTableQuery,
  GenericDataTableRow
} from '@/renderer/components/genericDataTable/custom-data-table.public'

export interface VendorModuleRow extends GenericDataTableRow {
  id: number
  vendorName: string
  country: string
  tier: 'Strategic' | 'Qualified' | 'Watchlist'
  score: number
}

export interface ProductModuleRow extends GenericDataTableRow {
  id: number
  productName: string
  category: 'Hardware' | 'Services' | 'Software'
  stock: number
  status: 'Ready' | 'Review' | 'Blocked'
}

const vendorCatalog: VendorModuleRow[] = [
  {
    id: 101,
    vendorName: 'Andes Supply',
    country: 'Chile',
    tier: 'Strategic',
    score: 94
  },
  {
    id: 102,
    vendorName: 'Boreal Components',
    country: 'Canada',
    tier: 'Qualified',
    score: 88
  },
  {
    id: 103,
    vendorName: 'Cobalt Industrial',
    country: 'Spain',
    tier: 'Watchlist',
    score: 63
  },
  {
    id: 104,
    vendorName: 'Delta Freight',
    country: 'Mexico',
    tier: 'Qualified',
    score: 81
  },
  {
    id: 105,
    vendorName: 'Estrella Metals',
    country: 'Peru',
    tier: 'Strategic',
    score: 91
  },
  {
    id: 106,
    vendorName: 'Fjord Systems',
    country: 'Norway',
    tier: 'Watchlist',
    score: 57
  }
]

const productCatalog: ProductModuleRow[] = [
  {
    id: 201,
    productName: 'Vendor Portal',
    category: 'Software',
    stock: 120,
    status: 'Ready'
  },
  {
    id: 202,
    productName: 'Dock Scanner',
    category: 'Hardware',
    stock: 18,
    status: 'Review'
  },
  {
    id: 203,
    productName: 'Onboarding Audit',
    category: 'Services',
    stock: 42,
    status: 'Ready'
  },
  {
    id: 204,
    productName: 'Risk Console',
    category: 'Software',
    stock: 7,
    status: 'Blocked'
  },
  {
    id: 205,
    productName: 'Field Tablet',
    category: 'Hardware',
    stock: 33,
    status: 'Review'
  },
  {
    id: 206,
    productName: 'Training Bundle',
    category: 'Services',
    stock: 64,
    status: 'Ready'
  }
]

export const vendorColumns: Array<GenericDataTableColumn<VendorModuleRow>> = [
  { field: 'vendorName', header: 'Vendor', filterable: true, sortable: true },
  { field: 'country', header: 'Country', filterable: true, sortable: true },
  {
    field: 'tier',
    header: 'Tier',
    filterable: true,
    type: 'list',
    sortable: true,
    filterOptions: [
      { label: 'Strategic', value: 'Strategic' },
      { label: 'Qualified', value: 'Qualified' },
      { label: 'Watchlist', value: 'Watchlist' }
    ]
  },
  {
    field: 'score',
    header: 'Score',
    filterable: true,
    type: 'integer',
    sortable: true
  },
  {
    field: 'id',
    header: 'Actions',
    type: 'actions',
    actions: [
      { key: 'inspect', icon: 'pi pi-search', label: 'Inspect' },
      {
        key: 'flag',
        icon: 'pi pi-flag',
        label: 'Flag',
        severity: 'warning',
        disabled: (row) => row.tier === 'Strategic'
      }
    ]
  }
]

export const productColumns: Array<GenericDataTableColumn<ProductModuleRow>> = [
  { field: 'productName', header: 'Product', filterable: true, sortable: true },
  {
    field: 'category',
    header: 'Category',
    filterable: true,
    type: 'list',
    sortable: true,
    filterOptions: [
      { label: 'Hardware', value: 'Hardware' },
      { label: 'Services', value: 'Services' },
      { label: 'Software', value: 'Software' }
    ]
  },
  {
    field: 'stock',
    header: 'Stock',
    type: 'integer',
    filterable: true,
    sortable: true
  },
  {
    field: 'status',
    header: 'Status',
    type: 'list',
    filterable: true,
    sortable: true,
    filterOptions: [
      { label: 'Ready', value: 'Ready' },
      { label: 'Review', value: 'Review' },
      { label: 'Blocked', value: 'Blocked' }
    ]
  },
  {
    field: 'id',
    header: 'Actions',
    type: 'actions',
    actions: [
      {
        key: 'restock',
        icon: 'pi pi-plus-circle',
        label: 'Restock',
        severity: 'success',
        disabled: (row) => row.status === 'Blocked'
      }
    ]
  }
]

const compareValues = (left: unknown, right: unknown): number => {
  if (typeof left === 'number' && typeof right === 'number') {
    return left - right
  }

  return String(left ?? '').localeCompare(String(right ?? ''), undefined, {
    sensitivity: 'base'
  })
}

const filterRows = <Row extends GenericDataTableRow>(
  rows: Row[],
  query: GenericDataTableQuery,
  textFields: string[]
): Row[] => {
  return rows.filter((row) => {
    const matchesGlobal =
      !query.globalFilter ||
      textFields.some((field) =>
        String(row[field] ?? '')
          .toLowerCase()
          .includes(query.globalFilter?.toLowerCase() ?? '')
      )

    if (!matchesGlobal) {
      return false
    }

    return Object.entries(query.filters ?? {}).every(([field, value]) => {
      if (value === null || value === undefined || value === '') {
        return true
      }

      return (
        String(row[field] ?? '').toLowerCase() === String(value).toLowerCase()
      )
    })
  })
}

const sortRows = <Row extends GenericDataTableRow>(
  rows: Row[],
  query: GenericDataTableQuery
): Row[] => {
  if (!query.sortField) {
    return rows
  }

  return [...rows].sort((left, right) => {
    const result = compareValues(
      left[query.sortField as keyof Row],
      right[query.sortField as keyof Row]
    )

    return query.sortOrder === -1 ? result * -1 : result
  })
}

const paginateRows = <Row extends GenericDataTableRow>(
  rows: Row[],
  query: GenericDataTableQuery
): Row[] => {
  const start = query.page * query.size
  return rows.slice(start, start + query.size)
}

export const vendorDataProvider: GenericDataTableDataProvider<
  VendorModuleRow
> = async ({ query }) => {
  const filteredRows = sortRows(
    filterRows(vendorCatalog, query, ['vendorName', 'country', 'tier']),
    query
  )

  return {
    ok: true,
    rows: paginateRows(filteredRows, query),
    totalRecords: filteredRows.length,
    overallTotal: vendorCatalog.length
  }
}

export const productDataProvider: GenericDataTableDataProvider<
  ProductModuleRow
> = async ({ query }) => {
  const filteredRows = sortRows(
    filterRows(productCatalog, query, ['productName', 'category', 'status']),
    query
  )

  return {
    ok: true,
    rows: paginateRows(filteredRows, query),
    totalRecords: filteredRows.length,
    overallTotal: productCatalog.length
  }
}
