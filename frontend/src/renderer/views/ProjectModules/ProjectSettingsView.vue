<template>
  <ProjectModuleShell
    :title="t('ProjectMain.modules.settingsTitle')"
    :project-code="currentProjectCode"
    :project-description="currentProjectDescription"
  >
    <div class="project-settings app-stack app-stack--compact">
      <div
        class="project-settings__tabs"
        role="tablist"
        aria-label="Settings data tabs"
      >
        <button
          type="button"
          class="project-settings__tab"
          :class="{
            'project-settings__tab--active': activeTab === 'categories'
          }"
          role="tab"
          :aria-selected="activeTab === 'categories'"
          @click="activeTab = 'categories'"
        >
          Categories
        </button>

        <button
          type="button"
          class="project-settings__tab"
          :class="{
            'project-settings__tab--active': activeTab === 'requirementLevels'
          }"
          role="tab"
          :aria-selected="activeTab === 'requirementLevels'"
          @click="activeTab = 'requirementLevels'"
        >
          Requirement Levels
        </button>
      </div>

      <div
        v-if="activeTab === 'categories'"
        class="project-settings__panel app-stack app-stack--compact"
      >
        <div class="project-settings__intro">
          <strong>Categories</strong>
          <span>
            Loaded from the active project and filtered directly against the
            backend category store.
          </span>
        </div>

        <GenericDataTable
          :columns="categoryColumnsForTable"
          :rows="[]"
          :query="categoryQuery"
          :data-provider="categoryDataProviderForTable"
          empty-message="No categories match the current filters"
          loading-message="Loading categories..."
          error-message="Category list could not be loaded"
          global-filter-placeholder="Filter categories"
          show-refresh-button
          refresh-label="Reload categories"
          show-count-bar
          count-bar-position="bottom"
          @update:query="onCategoryQueryChange"
          @refresh="onCategoryRefresh"
        >
          <template #toolbar-main="slotProps">
            <div class="project-settings__toolbar">
              <span class="project-settings__pill">
                {{ slotProps.filteredTotal }} categories
              </span>
              <span class="project-settings__note">
                Project scoped data from the active backend project.
              </span>
            </div>
          </template>
        </GenericDataTable>
      </div>

      <div
        v-else
        class="project-settings__panel app-stack app-stack--compact"
      >
        <div class="project-settings__intro">
          <strong>Requirement Levels</strong>
          <span>
            Loaded from the active project and filtered against the backend
            requirement level store.
          </span>
        </div>

        <GenericDataTable
          :columns="requirementLevelColumnsForTable"
          :rows="[]"
          :query="requirementLevelQuery"
          :data-provider="requirementLevelDataProviderForTable"
          empty-message="No requirement levels match the current filters"
          loading-message="Loading requirement levels..."
          error-message="Requirement level list could not be loaded"
          global-filter-placeholder="Filter requirement levels"
          show-refresh-button
          refresh-label="Reload requirement levels"
          show-count-bar
          count-bar-position="bottom"
          @update:query="onRequirementLevelQueryChange"
          @refresh="onRequirementLevelRefresh"
        >
          <template #toolbar-main="slotProps">
            <div class="project-settings__toolbar">
              <span class="project-settings__pill">
                {{ slotProps.filteredTotal }} requirement levels
              </span>
              <span class="project-settings__note">
                Same reusable table, separate dataset and route-specific code.
              </span>
            </div>
          </template>
        </GenericDataTable>
      </div>
    </div>
  </ProjectModuleShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  GenericDataTable,
  type GenericDataTableDataProvider,
  type GenericDataTableQuery,
  type GenericDataTableRefreshPayload,
  type GenericDataTableRow,
  type GenericDataTableColumn
} from '@/renderer/components/genericDataTable/custom-data-table.public'
import {
  useCategoryStore,
  useRequirementLevelStore,
  type CategoryResponse,
  type RequirementLevelResponse
} from '@/renderer/stores/stores.exports'
import ProjectModuleShell from './ProjectModuleShell.vue'
import { useProjectModulePage } from './useProjectModulePage'

const { t } = useI18n()
const { currentProject, currentProjectCode, currentProjectDescription } =
  useProjectModulePage()
const categoryStore = useCategoryStore()
const requirementLevelStore = useRequirementLevelStore()

type SettingsTab = 'categories' | 'requirementLevels'

const activeTab = ref<SettingsTab>('categories')

const categoryQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'code',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const requirementLevelQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 10,
  sortField: 'code',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const categoryColumns: Array<GenericDataTableColumn<CategoryResponse>> = [
  {
    field: 'code',
    header: 'Code',
    filterable: true,
    sortable: true,
    minWidth: '8rem'
  },
  {
    field: 'name',
    header: 'Name',
    filterable: true,
    sortable: true,
    minWidth: '12rem'
  },
  {
    field: 'description',
    header: 'Description',
    filterable: true,
    sortable: true,
    minWidth: '16rem'
  },
  {
    field: 'factor',
    header: 'Factor',
    type: 'number',
    filterable: true,
    sortable: true,
    align: 'right',
    minWidth: '7rem'
  },
  {
    field: 'updatedAt',
    header: 'Updated At',
    type: 'date',
    filterable: true,
    sortable: true,
    minWidth: '11rem'
  }
]

const requirementLevelColumns: Array<
  GenericDataTableColumn<RequirementLevelResponse>
> = [
  {
    field: 'code',
    header: 'Code',
    filterable: true,
    sortable: true,
    minWidth: '8rem'
  },
  {
    field: 'name',
    header: 'Name',
    filterable: true,
    sortable: true,
    minWidth: '12rem'
  },
  {
    field: 'description',
    header: 'Description',
    filterable: true,
    sortable: true,
    minWidth: '16rem'
  },
  {
    field: 'factor',
    header: 'Factor',
    type: 'number',
    filterable: true,
    sortable: true,
    align: 'right',
    minWidth: '7rem'
  },
  {
    field: 'updatedAt',
    header: 'Updated At',
    type: 'date',
    filterable: true,
    sortable: true,
    minWidth: '11rem'
  }
]

const categoryColumnsForTable = categoryColumns as unknown as Array<
  GenericDataTableColumn<GenericDataTableRow>
>
const requirementLevelColumnsForTable =
  requirementLevelColumns as unknown as Array<
    GenericDataTableColumn<GenericDataTableRow>
  >

const includesFilter = (rowValue: unknown, filterValue: unknown): boolean => {
  return String(rowValue ?? '')
    .toLowerCase()
    .includes(String(filterValue ?? '').toLowerCase())
}

const equalsFilter = (rowValue: unknown, filterValue: unknown): boolean => {
  if (filterValue === null || filterValue === undefined || filterValue === '') {
    return true
  }

  const numericRowValue = Number(rowValue)
  const numericFilterValue = Number(filterValue)

  if (Number.isFinite(numericRowValue) && Number.isFinite(numericFilterValue)) {
    return numericRowValue === numericFilterValue
  }

  return (
    String(rowValue ?? '').toLowerCase() ===
    String(filterValue ?? '').toLowerCase()
  )
}

const compareValues = (left: unknown, right: unknown): number => {
  if (typeof left === 'number' && typeof right === 'number') {
    return left - right
  }

  return String(left ?? '').localeCompare(String(right ?? ''), undefined, {
    sensitivity: 'base'
  })
}

const buildPagedResult = <Row extends GenericDataTableRow>(
  rows: Row[],
  query: GenericDataTableQuery,
  textFields: string[],
  exactFields: string[],
  dateFields: string[]
) => {
  let filteredRows = [...rows]

  if (query.globalFilter?.trim()) {
    const globalFilter = query.globalFilter.trim().toLowerCase()
    filteredRows = filteredRows.filter((row) => {
      return textFields.some((field) =>
        String(row[field] ?? '')
          .toLowerCase()
          .includes(globalFilter)
      )
    })
  }

  for (const [field, value] of Object.entries(query.filters ?? {})) {
    if (value === null || value === undefined || value === '') {
      continue
    }

    filteredRows = filteredRows.filter((row) => {
      if (dateFields.includes(field)) {
        return String(row[field] ?? '').startsWith(String(value))
      }

      if (exactFields.includes(field)) {
        return equalsFilter(row[field], value)
      }

      return includesFilter(row[field], value)
    })
  }

  const sortField = query.sortField

  if (sortField) {
    filteredRows.sort((left, right) => {
      const result = compareValues(
        left[sortField as keyof Row],
        right[sortField as keyof Row]
      )

      return query.sortOrder === -1 ? result * -1 : result
    })
  }

  const start = query.page * query.size
  const pagedRows = filteredRows.slice(start, start + query.size)

  return {
    rows: pagedRows,
    totalRecords: filteredRows.length,
    overallTotal: rows.length
  }
}

const categoryDataProvider = async ({
  query
}: {
  query: GenericDataTableQuery
}) => {
  const projectId = currentProject.value?.id

  if (!projectId) {
    return {
      ok: true as const,
      rows: [],
      totalRecords: 0,
      overallTotal: 0
    }
  }

  try {
    const rows = await categoryStore.listCategories({
      coreProjectId: projectId
    })
    const result = buildPagedResult(
      rows,
      query,
      ['code', 'name', 'description'],
      ['factor'],
      ['updatedAt']
    )

    return {
      ok: true as const,
      rows: result.rows,
      totalRecords: result.totalRecords,
      overallTotal: result.overallTotal
    }
  } catch (error) {
    return {
      ok: false as const,
      message:
        error instanceof Error ? error.message : 'Failed to load categories',
      cause: error
    }
  }
}

const requirementLevelDataProvider = async ({
  query
}: {
  query: GenericDataTableQuery
}) => {
  const projectId = currentProject.value?.id

  if (!projectId) {
    return {
      ok: true as const,
      rows: [],
      totalRecords: 0,
      overallTotal: 0
    }
  }

  try {
    const rows = await requirementLevelStore.listRequirementLevels({
      coreProjectId: projectId
    })
    const result = buildPagedResult(
      rows,
      query,
      ['code', 'name', 'description'],
      ['factor'],
      ['updatedAt']
    )

    return {
      ok: true as const,
      rows: result.rows,
      totalRecords: result.totalRecords,
      overallTotal: result.overallTotal
    }
  } catch (error) {
    return {
      ok: false as const,
      message:
        error instanceof Error
          ? error.message
          : 'Failed to load requirement levels',
      cause: error
    }
  }
}

const categoryDataProviderForTable =
  categoryDataProvider as GenericDataTableDataProvider<GenericDataTableRow>
const requirementLevelDataProviderForTable =
  requirementLevelDataProvider as GenericDataTableDataProvider<GenericDataTableRow>

const onCategoryQueryChange = (nextQuery: GenericDataTableQuery): void => {
  categoryQuery.value = nextQuery
}

const onRequirementLevelQueryChange = (
  nextQuery: GenericDataTableQuery
): void => {
  requirementLevelQuery.value = nextQuery
}

const onCategoryRefresh = (_payload: GenericDataTableRefreshPayload): void => {
  categoryQuery.value = { ...categoryQuery.value }
}

const onRequirementLevelRefresh = (
  _payload: GenericDataTableRefreshPayload
): void => {
  requirementLevelQuery.value = { ...requirementLevelQuery.value }
}
</script>

<style scoped>
.project-settings {
  gap: 1rem;
}

.project-settings__tabs {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.project-settings__tab {
  border: 1px solid rgba(32, 97, 61, 0.16);
  background: rgba(32, 97, 61, 0.06);
  color: var(--app-text-muted);
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.project-settings__tab--active {
  background: rgba(32, 97, 61, 0.14);
  color: #20613d;
  border-color: rgba(32, 97, 61, 0.26);
}

.project-settings__panel {
  gap: 1rem;
}

.project-settings__intro {
  display: grid;
  gap: 0.25rem;
  color: var(--app-text-muted);
}

.project-settings__toolbar {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.project-settings__pill {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: rgba(32, 97, 61, 0.12);
  color: #20613d;
  font-size: 0.82rem;
  font-weight: 600;
}

.project-settings__note {
  color: var(--app-text-muted);
  font-size: 0.82rem;
}
</style>
