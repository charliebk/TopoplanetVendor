<template>
  <section class="app-page project-module">
    <div class="app-page__shell project-module__shell app-stack">
      <PrimeCard class="app-panel project-module__panel">
        <template #title>
          <span>{{ t(titleKey) }}</span>
        </template>
        <template #subtitle>
          {{ currentProjectCode }}
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <p class="app-copy app-copy--muted">
              {{ currentProjectDescription }}
            </p>

            <div
              v-if="isVendorRoute"
              class="project-module__example-shell app-stack app-stack--compact"
            >
              <div class="project-module__example-copy">
                <strong>Vendor review list</strong>
                <span>
                  Ejemplo real en la vista de vendors usando provider mode,
                  toolbar, count bar y acciones por fila.
                </span>
              </div>

              <GenericDataTable
                :columns="vendorColumnsForTable"
                :rows="[]"
                :query="vendorQuery"
                :data-provider="vendorDataProviderForTable"
                empty-message="No vendors match the current filters"
                loading-message="Loading vendors..."
                error-message="Vendor list could not be loaded"
                global-filter-placeholder="Filter vendors"
                show-refresh-button
                refresh-label="Reload vendors"
                show-count-bar
                count-bar-position="bottom"
                @update:query="onVendorQueryChange"
                @action="onVendorAction"
                @refresh="onVendorRefresh"
              >
                <template #toolbar-main="slotProps">
                  <div class="project-module__table-toolbar">
                    <span class="project-module__table-pill">
                      {{ slotProps.filteredTotal }} vendors in review
                    </span>
                    <span class="project-module__table-note">
                      Real route example for Sprint 10 portability hardening.
                    </span>
                  </div>
                </template>
              </GenericDataTable>
            </div>

            <div
              v-else-if="isProductRoute"
              class="project-module__example-shell app-stack app-stack--compact"
            >
              <div class="project-module__example-copy">
                <strong>Product batch queue</strong>
                <span>
                  Ejemplo real en la vista de products usando provider mode,
                  seleccion multiple y payload batch listo para backend.
                </span>
              </div>

              <GenericDataTable
                :columns="productColumnsForTable"
                :rows="[]"
                :query="productQuery"
                :data-provider="productDataProviderForTable"
                selection-mode="multiple"
                empty-message="No products match the current filters"
                loading-message="Loading products..."
                error-message="Product queue could not be loaded"
                global-filter-placeholder="Filter products"
                show-refresh-button
                refresh-label="Reload products"
                show-count-bar
                count-bar-position="both"
                count-bar-show-shown
                @update:query="onProductQueryChange"
                @action="onProductAction"
                @refresh="onProductRefresh"
                @selection-change="onProductSelectionChange"
              >
                <template #toolbar-main="slotProps">
                  <div class="project-module__table-toolbar">
                    <span class="project-module__table-pill">
                      {{ slotProps.filteredTotal }} products queued
                    </span>
                    <span class="project-module__table-note">
                      Uses the same V1 module without project-specific wrappers.
                    </span>
                  </div>
                </template>
              </GenericDataTable>
            </div>

            <div
              v-else
              class="project-module__placeholder"
            >
              {{ t(descriptionKey) }}
            </div>

            <div
              v-if="hasInteractionLog"
              class="project-module__log"
            >
              <span class="project-module__log-title">Last table event</span>
              <pre class="project-module__log-code">{{
                moduleInteractionLog
              }}</pre>
            </div>
          </div>
        </template>
      </PrimeCard>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  GenericDataTable,
  type GenericDataTableActionPayload,
  type GenericDataTableColumn,
  type GenericDataTableDataProvider,
  type GenericDataTableQuery,
  type GenericDataTableRefreshPayload,
  type GenericDataTableRow,
  type GenericDataTableSelectionPayload
} from '@/renderer/components/genericDataTable/custom-data-table.public'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import type { CoreProjectResponse } from '@/renderer/stores/stores.exports'

interface VendorModuleRow extends GenericDataTableRow {
  id: number
  vendorName: string
  country: string
  tier: 'Strategic' | 'Qualified' | 'Watchlist'
  score: number
}

interface ProductModuleRow extends GenericDataTableRow {
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

const vendorColumns: Array<GenericDataTableColumn<VendorModuleRow>> = [
  {
    field: 'vendorName',
    header: 'Vendor',
    filterable: true,
    sortable: true
  },
  {
    field: 'country',
    header: 'Country',
    filterable: true,
    sortable: true
  },
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
      {
        key: 'inspect',
        icon: 'pi pi-search',
        label: 'Inspect'
      },
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

const productColumns: Array<GenericDataTableColumn<ProductModuleRow>> = [
  {
    field: 'productName',
    header: 'Product',
    filterable: true,
    sortable: true
  },
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

const vendorColumnsForTable = vendorColumns as unknown as Array<
  GenericDataTableColumn<GenericDataTableRow>
>
const productColumnsForTable = productColumns as unknown as Array<
  GenericDataTableColumn<GenericDataTableRow>
>

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const currentProject = ref<CoreProjectResponse | null>(null)
const vendorQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'vendorName',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})
const productQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'productName',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})
const moduleInteractionLog = ref(
  'Open Vendors or Products to exercise a real V1 table route.'
)

const titleKey = computed(() => {
  return (route.meta.moduleTitleKey ||
    'ProjectMain.modules.defaultTitle') as string
})

const descriptionKey = computed(() => {
  return (route.meta.moduleDescriptionKey ||
    'ProjectMain.modules.defaultDescription') as string
})

const isVendorRoute = computed(() => route.path === '/project/vendors')
const isProductRoute = computed(() => route.path === '/project/products')
const hasInteractionLog = computed(
  () => isVendorRoute.value || isProductRoute.value
)

const currentProjectCode = computed(() => {
  return currentProject.value?.code ?? t('ProjectMain.fallbackCode')
})

const currentProjectDescription = computed(() => {
  return (
    currentProject.value?.description ?? t('ProjectMain.fallbackDescription')
  )
})

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

const vendorDataProvider: GenericDataTableDataProvider<
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

const productDataProvider: GenericDataTableDataProvider<
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

const vendorDataProviderForTable =
  vendorDataProvider as unknown as GenericDataTableDataProvider<GenericDataTableRow>
const productDataProviderForTable =
  productDataProvider as unknown as GenericDataTableDataProvider<GenericDataTableRow>

const onVendorQueryChange = (nextQuery: GenericDataTableQuery): void => {
  vendorQuery.value = nextQuery
  moduleInteractionLog.value = `Vendor query changed -> page ${nextQuery.page + 1}, filters ${JSON.stringify(nextQuery.filters ?? {})}`
}

const onVendorAction = (
  payload: GenericDataTableActionPayload<GenericDataTableRow>
): void => {
  const row = payload.row as VendorModuleRow
  moduleInteractionLog.value = `Vendor action ${payload.actionKey} on ${row.vendorName}`
}

const onVendorRefresh = (_payload: GenericDataTableRefreshPayload): void => {
  moduleInteractionLog.value = 'Vendor table requested a manual refresh.'
}

const onProductQueryChange = (nextQuery: GenericDataTableQuery): void => {
  productQuery.value = nextQuery
  moduleInteractionLog.value = `Product query changed -> page ${nextQuery.page + 1}, filters ${JSON.stringify(nextQuery.filters ?? {})}`
}

const onProductAction = (
  payload: GenericDataTableActionPayload<GenericDataTableRow>
): void => {
  const row = payload.row as ProductModuleRow
  moduleInteractionLog.value = `Product action ${payload.actionKey} on ${row.productName}`
}

const onProductRefresh = (_payload: GenericDataTableRefreshPayload): void => {
  moduleInteractionLog.value = 'Product queue requested a manual refresh.'
}

const onProductSelectionChange = (
  payload: GenericDataTableSelectionPayload<GenericDataTableRow>
): void => {
  const selection =
    payload as GenericDataTableSelectionPayload<ProductModuleRow>
  moduleInteractionLog.value = `Batch selection updated -> strategy ${selection.batch.strategy}, selected ${selection.selectedCount ?? 0}`
}

const loadCurrentProject = async (): Promise<void> => {
  const selectedProjectId = preferencesStore.currentProjectId

  if (selectedProjectId === null) {
    await router.replace('/')
    return
  }

  try {
    currentProject.value =
      await coreProjectStore.getCoreProjectById(selectedProjectId)
  } catch (error) {
    console.error('Failed to load current project module', error)
    preferencesStore.setCurrentProjectId(null)
    await router.replace('/')
  }
}

onMounted((): void => {
  void loadCurrentProject()
})
</script>

<style scoped>
.project-module {
  background:
    radial-gradient(
      circle at top left,
      rgba(32, 97, 61, 0.08),
      transparent 26%
    ),
    linear-gradient(
      180deg,
      var(--app-page-gradient-top) 0%,
      var(--app-page-gradient-bottom) 100%
    );
}

.project-module__shell {
  gap: var(--app-space-5);
}

.project-module__panel {
  min-height: 280px;
}

.project-module__example-shell {
  gap: 1rem;
}

.project-module__example-copy {
  display: grid;
  gap: 0.25rem;
  color: var(--app-text-muted);
}

.project-module__table-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.project-module__table-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: rgba(32, 97, 61, 0.12);
  color: #20613d;
  font-size: 0.82rem;
  font-weight: 600;
}

.project-module__table-note {
  color: var(--app-text-muted);
  font-size: 0.82rem;
}

.project-module__placeholder {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--app-border-strong);
  border-radius: var(--app-radius-panel);
  background: var(--app-surface-soft);
  color: var(--app-text-muted);
  text-align: center;
  padding: var(--app-space-6);
}

.project-module__log {
  display: grid;
  gap: 0.35rem;
  padding: 0.9rem 1rem;
  border-radius: var(--app-radius-panel);
  background: rgba(16, 24, 40, 0.04);
  border: 1px solid rgba(16, 24, 40, 0.08);
}

.project-module__log-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--app-text-strong);
}

.project-module__log-code {
  margin: 0;
  white-space: pre-wrap;
  font-size: 0.8rem;
  color: var(--app-text-muted);
}
</style>
