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
              <span class="project-main__demo-badge">Sprint 3 Demo</span>
              <p class="app-copy app-copy--muted">
                Tabla temporal para validar filtros `list`, `date`, `idIcon`,
                `startsWith`, `endsWith` y `paramTransform` con carga lazy
                mockeada.
              </p>
            </div>
          </div>
        </template>
      </PrimeCard>

      <PrimeCard class="app-panel project-main__panel">
        <template #title>
          <span>CustomDataTableV1 Filter Demo</span>
        </template>
        <template #subtitle>
          Mock lazy loading with backend-style query output
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <GenericDataTable
              :columns="demoColumns"
              :rows="demoRows"
              :query="demoQuery"
              :loading="demoLoading"
              :lazy="true"
              :total-records="demoTotalRecords"
              empty-message="No demo audits match the current filters"
              global-filter-placeholder="Filter demo audits"
              @update:query="onDemoQueryChange"
              @row-click="onDemoRowClick"
            />

            <div class="project-main__query-panel">
              <div class="project-main__query-header">
                <span class="project-main__query-title"
                  >Emitted backend query</span
                >
                <span class="project-main__query-caption">
                  Temporary inspection block for Sprint 3 validation
                </span>
              </div>
              <pre class="project-main__query-code">{{ demoQueryPreview }}</pre>
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
  GenericDataTable,
  type GenericDataTableColumn,
  type GenericDataTableQuery,
  type GenericDataTableQueryChangeHandler,
  type GenericDataTableRowClickHandler
} from '@/renderer/components/customDataTable/CustomDataTableV1/custom-data-table-v1.public'

const router = useRouter()
const { t } = useI18n()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const currentProject = ref<CoreProjectResponse | null>(null)

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

const demoLoading = ref(false)
const demoRows = ref<DemoAuditRow[]>([])
const demoTotalRecords = ref(0)
const demoQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'reviewedAt',
  sortOrder: -1,
  globalFilter: null,
  filters: {}
})

const categoryOptions = [
  { label: 'Safety', value: 1 },
  { label: 'Quality', value: 2 },
  { label: 'Compliance', value: 3 }
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
    filterType: 'text',
    displayField: 'originName',
    backendField: 'originName',
    tooltipField: 'originDescription',
    idIconClass: (row) =>
      row.originActive ? 'pi pi-check-circle' : 'pi pi-ban',
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
    filterOptions: categoryOptions,
    minWidth: '11rem'
  },
  {
    field: 'reviewedAt',
    header: 'Reviewed At',
    type: 'date',
    sortable: true,
    filterable: true,
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
    align: 'right',
    minWidth: '9rem'
  }
]

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

const loadDemoRows = async (query: GenericDataTableQuery): Promise<void> => {
  demoLoading.value = true

  try {
    const filteredRows = applyDemoQuery(query)
    const start = query.page * query.size
    const end = start + query.size

    demoTotalRecords.value = filteredRows.length
    demoRows.value = filteredRows.slice(start, end)
  } finally {
    demoLoading.value = false
  }
}

const onDemoQueryChange: GenericDataTableQueryChangeHandler = (nextQuery) => {
  demoQuery.value = nextQuery
  void loadDemoRows(nextQuery)
}

const onDemoRowClick: GenericDataTableRowClickHandler<DemoAuditRow> = (row) => {
  console.info('Demo row clicked', row)
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
  void loadDemoRows(demoQuery.value)
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
