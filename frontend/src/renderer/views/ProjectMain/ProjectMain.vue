<template>
  <section class="app-page project-main">
    <div class="app-page__shell project-main__shell app-stack">
      <PrimeCard class="app-panel project-main__panel">
        <template #title>
          <span>{{ currentProjectName }}</span>
        </template>
        <template #subtitle>
          {{ currentProjectCode }}
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <p class="app-copy app-copy--muted">
              {{ currentProjectDescription }}
            </p>
            <div class="project-main__demo-intro">
              <span class="project-main__demo-badge">Sprint 9 Demo</span>
              <p class="app-copy app-copy--muted">
                Tabla temporal para validar opciones dinamicas en filtros tipo
                lista, mapeo desde items crudos, recarga dependiente de query y
                error visual explicito cuando falla un provider remoto en la V1.
              </p>
            </div>
          </div>
        </template>
      </PrimeCard>

      <PrimeCard class="app-panel project-main__panel">
        <template #title>
          <span>CustomDataTable Dynamic Options Demo</span>
        </template>
        <template #subtitle>
          Provider mode with dynamic list filters, batch selection and export
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <GenericDataTable
              ref="demoTable"
              :columns="demoColumnsForTable"
              :rows="[]"
              :query="demoQuery"
              :data-provider="demoDataProviderForTable"
              :show-option-errors-state="false"
              selection-mode="multiple"
              empty-message="No demo audits match the current filters"
              loading-message="Reloading demo audits..."
              error-message="Demo audits could not be loaded"
              global-filter-placeholder="Filter demo audits"
              show-refresh-button
              refresh-label="Reload audits"
              show-count-bar
              count-bar-position="both"
              count-bar-show-shown
              :row-disabled="isDemoRowDisabledForTable"
              disabled-row-selection-scope="filtered"
              :disabled-filtered-row-keys="demoDisabledFilteredRowKeys"
              disabled-filtered-rows-resolved
              @update:query="onDemoQueryChange"
              @row-click="onDemoRowClickForTable"
              @action="onDemoActionForTable"
              @selection-change="onDemoSelectionChangeForTable"
              @refresh="onDemoRefresh"
            >
              <template #toolbar-main="slotProps">
                <div class="project-main__toolbar-group">
                  <span class="project-main__toolbar-pill">
                    {{ slotProps.filteredTotal }} filtered audits
                  </span>
                  <span class="project-main__toolbar-note">
                    Origin filter options reload automatically when the category
                    filter changes; category options come from raw store-like
                    items and can fail explicitly to validate the dedicated slot
                    plus the inline error UI.
                  </span>
                </div>
              </template>

              <template #toolbar-actions>
                <PrimeButton
                  type="button"
                  :icon="
                    demoFailOriginOptions
                      ? 'pi pi-check'
                      : 'pi pi-exclamation-triangle'
                  "
                  text
                  :severity="demoFailOriginOptions ? 'success' : 'danger'"
                  :label="
                    demoFailOriginOptions
                      ? 'Restore origin provider'
                      : 'Simulate origin provider failure'
                  "
                  @click="void onToggleOriginOptionFailure()"
                />
                <PrimeButton
                  type="button"
                  icon="pi pi-sync"
                  text
                  severity="secondary"
                  label="Reload filter options"
                  @click="void onReloadDemoFilterOptions()"
                />
                <PrimeButton
                  type="button"
                  icon="pi pi-inbox"
                  text
                  severity="secondary"
                  label="Snapshot selection"
                  @click="onRefreshSelectionApi"
                />
              </template>

              <template #option-errors="slotProps">
                <div
                  v-if="slotProps.hasOptionErrors"
                  class="project-main__option-errors-panel"
                >
                  <div class="project-main__option-errors-copy">
                    <strong>
                      {{ slotProps.optionErrors.length }} dynamic filter
                      source{{ slotProps.optionErrors.length === 1 ? '' : 's' }}
                      failed
                    </strong>
                    <span>
                      {{ formatOptionErrorSummary(slotProps.optionErrors) }}
                    </span>
                  </div>
                  <PrimeButton
                    type="button"
                    icon="pi pi-refresh"
                    text
                    size="small"
                    severity="danger"
                    label="Retry failed filters"
                    @click="void slotProps.reloadFilterOptions()"
                  />
                </div>
              </template>

              <template #count-bar="slotProps">
                <span class="project-main__toolbar-note">
                  Visible {{ slotProps.shown }} of
                  {{ slotProps.filteredTotal }} filtered
                </span>
                <span class="project-main__toolbar-note">
                  Refreshes: {{ demoRefreshCount }}
                </span>
              </template>
            </GenericDataTable>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title">
                  Last interaction
                </span>
                <span class="project-main__query-caption">
                  Row clicks ignore disabled rows and action buttons never
                  bubble
                </span>
              </div>
              <pre class="project-main__query-code">{{
                demoInteractionLog
              }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title">
                  Dynamic option provider state
                </span>
                <span class="project-main__query-caption">
                  Toggle the origin provider failure and open the Origin filter
                  to inspect the dedicated `option-errors` slot plus the inline
                  retry/error UI.
                </span>
              </div>
              <pre class="project-main__query-code">{{
                demoOptionProviderStatus
              }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title">
                  Emitted backend query
                </span>
                <span class="project-main__query-caption">
                  Temporary inspection block for Sprint 7 validation
                </span>
              </div>
              <pre class="project-main__query-code">{{ demoQueryPreview }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div
                class="project-main__query-header project-main__query-header--split"
              >
                <div>
                  <span class="project-main__query-title">
                    Selection payload
                  </span>
                  <span class="project-main__query-caption">
                    Raw selection state plus a derived `batch` block ready for
                    backend endpoints
                  </span>
                </div>
                <PrimeButton
                  type="button"
                  icon="pi pi-refresh"
                  text
                  severity="secondary"
                  label="Refresh selection API"
                  @click="onRefreshSelectionApi"
                />
              </div>
              <pre class="project-main__query-code">{{
                demoSelectionPreview
              }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title"> CSV export </span>
                <span class="project-main__query-caption">
                  Uses the public API to export the full filtered result without
                  remapping columns; the actions column is skipped automatically
                </span>
              </div>
              <pre class="project-main__query-code">{{ demoCsvPreview }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title"> Print payload </span>
                <span class="project-main__query-caption">
                  Structured print-ready payload built from the same column
                  contract
                </span>
              </div>
              <pre class="project-main__query-code">{{ demoPrintPreview }}</pre>
            </div>

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title"> Batch request </span>
                <span class="project-main__query-caption">
                  Real example using `toBatchRequest(selection)` from the public
                  API
                </span>
              </div>
              <pre class="project-main__query-code">{{
                demoBatchRequestPreview
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
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import type { CoreProjectResponse } from '@/renderer/stores/stores.exports'
import {
  exportDataTableCsv,
  GenericDataTable,
  prepareDataTablePrint,
  toBatchRequest,
  type GenericDataTableActionPayload,
  type GenericDataTableActionHandler,
  type GenericDataTableColumn,
  type GenericDataTableDataProvider,
  type GenericDataTableExpose,
  type GenericDataTableQuery,
  type GenericDataTableQueryChangeHandler,
  type GenericDataTableRefreshHandler,
  type GenericDataTableRow,
  type GenericDataTableRowClickHandler,
  type GenericDataTableSelectionChangeHandler,
  type GenericDataTableSelectionPayload
} from '@/renderer/components/genericDataTable/custom-data-table.public'

const router = useRouter()
const { t } = useI18n()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const currentProject = ref<CoreProjectResponse | null>(null)
const demoTable = ref<GenericDataTableExpose<DemoAuditRow> | null>(null)

type DemoAuditRow = {
  id: number
  auditCode: string
  ownerName: string
  originId: number
  originName: string
  originDescription: string
  originActive: boolean
  categoryId: number
  categoryName: string
  reviewedAt: string
  compliance: number
}

const demoQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'reviewedAt',
  sortOrder: -1,
  globalFilter: null,
  filters: {}
})
const demoSelection =
  ref<GenericDataTableSelectionPayload<DemoAuditRow> | null>(null)
const demoRefreshCount = ref(0)
const demoInteractionLog = ref('No interaction registered yet.')
const demoFailOriginOptions = ref(false)

const demoCategoryCatalog = [
  { id: 1, name: 'Safety' },
  { id: 2, name: 'Quality' },
  { id: 3, name: 'Compliance' }
]

const demoOriginCatalog = [
  { id: 1, name: 'Sensor Hub', categoryIds: [1, 3], source: 'live' },
  { id: 2, name: 'Legacy Import', categoryIds: [2, 3], source: 'legacy' },
  { id: 3, name: 'Mobile Capture', categoryIds: [1, 2], source: 'mobile' },
  { id: 4, name: 'Partner Feed', categoryIds: [1, 2], source: 'partner' }
]

const demoDataset: DemoAuditRow[] = [
  {
    id: 1,
    auditCode: 'AUD-AX-101',
    ownerName: 'Alicia Torres',
    originId: 1,
    originName: 'Sensor Hub',
    originDescription: 'Primary sensor origin for field inspections',
    originActive: true,
    categoryId: 1,
    categoryName: 'Safety',
    reviewedAt: '2026-03-25',
    compliance: 96.4
  },
  {
    id: 2,
    auditCode: 'AUD-BX-142',
    ownerName: 'Bruno Salas',
    originId: 2,
    originName: 'Legacy Import',
    originDescription: 'Imported from a legacy integration batch',
    originActive: false,
    categoryId: 2,
    categoryName: 'Quality',
    reviewedAt: '2026-03-21',
    compliance: 82.1
  },
  {
    id: 3,
    auditCode: 'AUD-CX-177',
    ownerName: 'Carla Nunez',
    originId: 1,
    originName: 'Sensor Hub',
    originDescription: 'Primary sensor origin for field inspections',
    originActive: true,
    categoryId: 3,
    categoryName: 'Compliance',
    reviewedAt: '2026-03-19',
    compliance: 88.6
  },
  {
    id: 4,
    auditCode: 'AUD-DX-188',
    ownerName: 'Diego Ramos',
    originId: 3,
    originName: 'Mobile Capture',
    originDescription: 'Captured from the mobile field application',
    originActive: true,
    categoryId: 2,
    categoryName: 'Quality',
    reviewedAt: '2026-03-14',
    compliance: 91.2
  },
  {
    id: 5,
    auditCode: 'AUD-EX-203',
    ownerName: 'Elena Varela',
    originId: 4,
    originName: 'Partner Feed',
    originDescription: 'Data received from a certified partner feed',
    originActive: true,
    categoryId: 1,
    categoryName: 'Safety',
    reviewedAt: '2026-03-11',
    compliance: 78.9
  },
  {
    id: 6,
    auditCode: 'AUD-FX-244',
    ownerName: 'Fabian Soto',
    originId: 2,
    originName: 'Legacy Import',
    originDescription: 'Imported from a legacy integration batch',
    originActive: false,
    categoryId: 3,
    categoryName: 'Compliance',
    reviewedAt: '2026-03-08',
    compliance: 69.5
  },
  {
    id: 7,
    auditCode: 'AUD-GX-278',
    ownerName: 'Gabriela Mena',
    originId: 3,
    originName: 'Mobile Capture',
    originDescription: 'Captured from the mobile field application',
    originActive: true,
    categoryId: 1,
    categoryName: 'Safety',
    reviewedAt: '2026-03-05',
    compliance: 94.8
  },
  {
    id: 8,
    auditCode: 'AUD-HX-302',
    ownerName: 'Hector Vidal',
    originId: 4,
    originName: 'Partner Feed',
    originDescription: 'Data received from a certified partner feed',
    originActive: true,
    categoryId: 2,
    categoryName: 'Quality',
    reviewedAt: '2026-03-01',
    compliance: 86.7
  }
]

const demoColumns: Array<GenericDataTableColumn<DemoAuditRow>> = [
  {
    field: 'auditCode',
    header: 'Audit Code',
    exportHeader: 'Audit code',
    sortable: true,
    filterable: true,
    matchMode: 'endsWith',
    backendField: 'auditCode',
    minWidth: '10rem'
  },
  {
    field: 'ownerName',
    header: 'Owner',
    sortable: true,
    filterable: true,
    matchMode: 'startsWith',
    backendField: 'ownerName',
    minWidth: '12rem'
  },
  {
    field: 'originId',
    header: 'Origin',
    type: 'idIcon',
    sortable: true,
    filterable: true,
    filterType: 'list',
    displayField: 'originName',
    exportHeader: 'Origin',
    backendField: 'originId',
    tooltipField: 'originDescription',
    idIconClass: (row) =>
      row.originActive ? 'pi pi-check-circle' : 'pi pi-ban',
    optionItemsProvider: ({ query }) => {
      if (demoFailOriginOptions.value) {
        throw new Error(
          'Origin filter options could not be loaded from the remote provider.'
        )
      }

      const selectedCategoryId = query.filters?.categoryId

      if (typeof selectedCategoryId !== 'number') {
        return demoOriginCatalog
      }

      return demoOriginCatalog.filter((origin) =>
        origin.categoryIds.includes(selectedCategoryId)
      )
    },
    optionTransform: (raw) => {
      const origin = raw as {
        id: number
        name: string
        source: string
      }

      return {
        label: `${origin.name} (${origin.source})`,
        value: origin.id
      }
    },
    includeAllOption: true,
    includeAllLabel: 'All origins',
    optionReloadStrategy: 'query-change',
    minWidth: '13rem'
  },
  {
    field: 'categoryId',
    header: 'Category',
    type: 'list',
    sortable: true,
    filterable: true,
    displayField: 'categoryName',
    backendField: 'categoryId',
    optionItemsProvider: () => demoCategoryCatalog,
    optionLabelField: 'name',
    optionValueField: 'id',
    includeAllOption: true,
    includeAllLabel: 'All categories',
    minWidth: '11rem'
  },
  {
    field: 'originActive',
    header: 'Origin Status',
    type: 'boolean',
    sortable: true,
    filterable: true,
    exportHeader: 'Origin source',
    backendField: 'originActive',
    booleanLabels: {
      trueLabel: 'Active source',
      falseLabel: 'Legacy source',
      nullLabel: 'Unknown'
    },
    booleanTag: true,
    booleanTagSeverity: {
      true: 'success',
      false: 'warning',
      null: 'secondary'
    },
    exportFormat: (value) => (value === true ? 'Active' : 'Legacy'),
    align: 'center',
    minWidth: '11rem'
  },
  {
    field: 'reviewedAt',
    header: 'Reviewed At',
    type: 'date',
    sortable: true,
    filterable: true,
    exportHeader: 'Reviewed date',
    backendField: 'reviewedAt',
    paramTransform: (value) => {
      if (!(value instanceof Date)) {
        return value
      }

      const year = value.getFullYear()
      const month = `${value.getMonth() + 1}`.padStart(2, '0')
      const day = `${value.getDate()}`.padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    minWidth: '11rem'
  },
  {
    field: 'compliance',
    header: 'Compliance',
    type: 'percent',
    sortable: true,
    filterable: true,
    matchMode: 'equals',
    decimals: 1,
    exportHeader: 'Compliance (%)',
    exportFormat: (value) => {
      const numericValue = Number(value)

      if (Number.isNaN(numericValue)) {
        return ''
      }

      return numericValue.toFixed(1)
    },
    align: 'right',
    minWidth: '9rem'
  },
  {
    field: 'id',
    header: 'Actions',
    type: 'actions',
    align: 'center',
    minWidth: '7rem',
    actions: [
      {
        key: 'inspect',
        icon: 'pi pi-search',
        label: 'Inspect',
        tooltip: 'Open the audit detail panel',
        class:
          'project-main__action-button project-main__action-button--inspect'
      }
    ]
  }
]

const demoColumnsForTable = demoColumns as unknown as Array<
  GenericDataTableColumn<GenericDataTableRow>
>

const currentProjectName = computed(() => {
  return currentProject.value?.name ?? t('ProjectMain.fallbackTitle')
})

const currentProjectCode = computed(() => {
  return currentProject.value?.code ?? t('ProjectMain.fallbackCode')
})

const currentProjectDescription = computed(() => {
  return (
    currentProject.value?.description ?? t('ProjectMain.fallbackDescription')
  )
})

const demoQueryPreview = computed(() =>
  JSON.stringify(demoQuery.value, null, 2)
)

const demoOptionProviderStatus = computed(() => {
  return JSON.stringify(
    {
      originProviderFailureEnabled: demoFailOriginOptions.value,
      recommendedCheck:
        'Open the Origin filter dropdown to see the inline error and retry action when the remote provider fails.'
    },
    null,
    2
  )
})

const formatOptionErrorSummary = (
  errors: Array<{ header: string; message: string } | null>
): string => {
  return errors
    .filter(
      (error): error is { header: string; message: string } => error !== null
    )
    .map((error) => `${error.header}: ${error.message}`)
    .join(' | ')
}

const demoFilteredRows = computed(() => applyDemoQuery(demoQuery.value))

const demoDisabledFilteredRowKeys = computed(() => {
  return demoFilteredRows.value
    .filter((row) => isDemoRowDisabled(row))
    .map((row) => String(row.id))
})

const demoSelectionPreview = computed(() => {
  if (!demoSelection.value) {
    return 'No selection payload emitted yet.'
  }

  return JSON.stringify(demoSelection.value, null, 2)
})

const demoBatchRequestPreview = computed(() => {
  const batchRequest = toBatchRequest(demoSelection.value)

  if (!batchRequest) {
    return 'No batch request available yet.'
  }

  return JSON.stringify(batchRequest, null, 2)
})

const demoCsvPreview = computed(() => {
  return exportDataTableCsv(
    {
      columns: demoColumns,
      rows: demoFilteredRows.value
    },
    {
      includeBom: false
    }
  )
})

const demoPrintPreview = computed(() => {
  return JSON.stringify(
    prepareDataTablePrint(
      {
        columns: demoColumns,
        rows: demoFilteredRows.value
      },
      {
        title: 'Filtered demo audits'
      }
    ),
    null,
    2
  )
})

const isDemoRowDisabled = (row: DemoAuditRow): boolean => !row.originActive

const applyMatchMode = (
  candidate: unknown,
  filterValue: unknown,
  matchMode: 'contains' | 'equals' | 'startsWith' | 'endsWith'
): boolean => {
  if (candidate === null || candidate === undefined) {
    return false
  }

  if (matchMode === 'equals') {
    return String(candidate).toLowerCase() === String(filterValue).toLowerCase()
  }

  const candidateText = String(candidate).toLowerCase()
  const filterText = String(filterValue).toLowerCase()

  if (matchMode === 'startsWith') {
    return candidateText.startsWith(filterText)
  }

  if (matchMode === 'endsWith') {
    return candidateText.endsWith(filterText)
  }

  return candidateText.includes(filterText)
}

const applyDemoQuery = (query: GenericDataTableQuery): DemoAuditRow[] => {
  const filters = query.filters ?? {}
  const globalFilter = query.globalFilter?.trim().toLowerCase()

  let nextRows = [...demoDataset]

  if (globalFilter) {
    nextRows = nextRows.filter((row) => {
      return [
        row.auditCode,
        row.ownerName,
        row.originName,
        row.categoryName,
        row.reviewedAt
      ].some((value) => String(value).toLowerCase().includes(globalFilter))
    })
  }

  for (const [queryField, filterValue] of Object.entries(filters)) {
    const column = demoColumns.find(
      (candidate) => (candidate.backendField ?? candidate.field) === queryField
    )

    if (!column || filterValue === null || filterValue === '') {
      continue
    }

    nextRows = nextRows.filter((row) => {
      const backendKey = (column.backendField ??
        column.field) as keyof DemoAuditRow
      const rawValue = row[backendKey]
      const matchMode =
        column.matchMode ??
        (column.filterType === 'text' || column.type === 'text'
          ? 'contains'
          : 'equals')

      if (column.type === 'date') {
        return String(rawValue) === String(filterValue)
      }

      if (
        column.type === 'list' ||
        column.type === 'select' ||
        column.type === 'number' ||
        column.type === 'integer' ||
        column.type === 'percent'
      ) {
        const numericRawValue = Number(rawValue)
        const numericFilterValue = Number(filterValue)

        if (
          Number.isFinite(numericRawValue) &&
          Number.isFinite(numericFilterValue)
        ) {
          return numericRawValue === numericFilterValue
        }

        return String(rawValue) === String(filterValue)
      }

      return applyMatchMode(rawValue, filterValue, matchMode)
    })
  }

  const sortField = query.sortField ?? 'reviewedAt'
  const sortOrder = query.sortOrder ?? 1

  nextRows.sort((left, right) => {
    const leftValue = left[sortField as keyof DemoAuditRow]
    const rightValue = right[sortField as keyof DemoAuditRow]

    if (leftValue === rightValue) {
      return 0
    }

    return leftValue > rightValue ? sortOrder : -sortOrder
  })

  return nextRows
}

const demoDataProvider: GenericDataTableDataProvider<DemoAuditRow> = async ({
  query
}) => {
  const filteredRows = applyDemoQuery(query)
  const start = query.page * query.size
  const end = start + query.size

  return {
    ok: true,
    rows: filteredRows.slice(start, end),
    totalRecords: filteredRows.length,
    overallTotal: demoDataset.length
  }
}

const demoDataProviderForTable =
  demoDataProvider as unknown as GenericDataTableDataProvider<GenericDataTableRow>

const onDemoQueryChange: GenericDataTableQueryChangeHandler = (nextQuery) => {
  demoQuery.value = nextQuery
}

const onDemoRowClick: GenericDataTableRowClickHandler<DemoAuditRow> = (row) => {
  demoInteractionLog.value = `row-click -> ${row.auditCode}`
}

const isDemoRowDisabledForTable = (row: GenericDataTableRow): boolean => {
  return isDemoRowDisabled(row as DemoAuditRow)
}

const onDemoRowClickForTable = (row: GenericDataTableRow): void => {
  onDemoRowClick(row as DemoAuditRow)
}

const onDemoAction: GenericDataTableActionHandler<DemoAuditRow> = (payload) => {
  demoInteractionLog.value = `${payload.actionKey} -> ${payload.row.auditCode}`
}

const onDemoActionForTable = (
  payload: GenericDataTableActionPayload<GenericDataTableRow>
): void => {
  onDemoAction({
    actionKey: payload.actionKey,
    row: payload.row as DemoAuditRow
  })
}

const onDemoSelectionChange: GenericDataTableSelectionChangeHandler<
  DemoAuditRow
> = (payload) => {
  demoSelection.value = payload
}

const onDemoSelectionChangeForTable = (
  payload: GenericDataTableSelectionPayload<GenericDataTableRow>
): void => {
  onDemoSelectionChange(
    payload as GenericDataTableSelectionPayload<DemoAuditRow>
  )
}

const onRefreshSelectionApi = (): void => {
  demoTable.value?.refreshVisibleRows()
  demoSelection.value = demoTable.value?.getSelectionPayload() ?? null
}

const onReloadDemoFilterOptions = async (): Promise<void> => {
  await demoTable.value?.reloadFilterOptions(['categoryId', 'originId'])
  demoInteractionLog.value = 'reload-filter-options -> categoryId, originId'
}

const onToggleOriginOptionFailure = async (): Promise<void> => {
  demoFailOriginOptions.value = !demoFailOriginOptions.value
  await demoTable.value?.reloadFilterOptions(['originId'])
  demoInteractionLog.value = demoFailOriginOptions.value
    ? 'origin-option-provider -> forced failure'
    : 'origin-option-provider -> restored'
}

const onDemoRefresh: GenericDataTableRefreshHandler = () => {
  demoRefreshCount.value += 1
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
    console.error('Failed to load current project', error)
    preferencesStore.setCurrentProjectId(null)
    await router.replace('/')
  }
}

onMounted((): void => {
  void loadCurrentProject()
})
</script>

<style scoped>
.project-main {
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

.project-main__shell {
  gap: var(--app-space-5);
}

.project-main__panel {
  min-height: 280px;
}

.project-main__demo-intro {
  display: grid;
  gap: var(--app-space-3);
}

.project-main__demo-badge {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: rgba(32, 97, 61, 0.12);
  color: var(--app-text-strong);
  font-size: 0.8rem;
  font-weight: 600;
}

.project-main__toolbar-pill {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  background: rgba(32, 97, 61, 0.12);
  color: var(--app-text-strong);
  font-size: 0.8rem;
  font-weight: 600;
}

.project-main__toolbar-group {
  display: grid;
  gap: 0.35rem;
}

.project-main__toolbar-note {
  color: var(--app-text-muted);
  font-size: 0.82rem;
}

.project-main__option-errors-panel {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--app-space-3);
  padding: var(--app-space-3) var(--app-space-4);
  border-radius: var(--app-radius-panel);
  border: 1px solid rgba(182, 58, 58, 0.28);
  background: rgba(182, 58, 58, 0.08);
}

.project-main__option-errors-copy {
  display: grid;
  gap: 0.2rem;
  color: #8f2f2f;
  font-size: 0.82rem;
  line-height: 1.4;
}

.project-main__action-button--inspect {
  color: #275efe;
}

.project-main__action-button--approve {
  color: #1f7a4d;
}

.project-main__action-button--archive {
  color: #b63a3a;
}

.project-main__query-panel {
  display: grid;
  gap: var(--app-space-2);
  padding: var(--app-space-4);
  border-radius: var(--app-radius-panel);
  border: 1px solid var(--app-border-subtle);
  background: var(--app-surface-soft);
}

.project-main__query-header {
  display: grid;
  gap: 0.15rem;
}

.project-main__query-header--split {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--app-space-3);
  flex-wrap: wrap;
}

.project-main__query-title {
  font-weight: 600;
  color: var(--app-text-strong);
}

.project-main__query-caption {
  color: var(--app-text-muted);
  font-size: 0.85rem;
}

.project-main__query-code {
  margin: 0;
  overflow: auto;
  padding: var(--app-space-4);
  border-radius: calc(var(--app-radius-panel) * 0.8);
  background: rgba(15, 23, 42, 0.06);
  color: var(--app-text-strong);
  font-size: 0.85rem;
  line-height: 1.5;
}
</style>
