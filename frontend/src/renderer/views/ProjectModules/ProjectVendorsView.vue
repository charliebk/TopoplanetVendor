<template>
  <ProjectModuleShell
    :title="t('ProjectMain.modules.vendorsTitle')"
    :project-code="currentProjectCode"
    :project-description="currentProjectDescription"
    :interaction-log="interactionLog"
  >
    <div class="project-module__example-shell app-stack app-stack--compact">
      <div class="project-module__example-copy">
        <strong>Vendor review list</strong>
        <span>
          Ejemplo real en la vista de vendors usando provider mode, toolbar,
          count bar y acciones por fila.
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
  </ProjectModuleShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  GenericDataTable,
  type GenericDataTableActionPayload,
  type GenericDataTableDataProvider,
  type GenericDataTableQuery,
  type GenericDataTableRefreshPayload,
  type GenericDataTableRow
} from '@/renderer/components/genericDataTable/custom-data-table.public'
import ProjectModuleShell from './ProjectModuleShell.vue'
import {
  type VendorModuleRow,
  vendorColumns,
  vendorDataProvider
} from './projectModuleTables'
import { useProjectModulePage } from './useProjectModulePage'

const { t } = useI18n()
const { currentProjectCode, currentProjectDescription } = useProjectModulePage()
const interactionLog = ref('Open Vendors to exercise a real module route.')
const vendorQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'vendorName',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const vendorColumnsForTable = vendorColumns as unknown as Array<any>
const vendorDataProviderForTable =
  vendorDataProvider as unknown as GenericDataTableDataProvider<GenericDataTableRow>

const onVendorQueryChange = (nextQuery: GenericDataTableQuery): void => {
  vendorQuery.value = nextQuery
  interactionLog.value = `Vendor query changed -> page ${nextQuery.page + 1}, filters ${JSON.stringify(nextQuery.filters ?? {})}`
}

const onVendorAction = (
  payload: GenericDataTableActionPayload<GenericDataTableRow>
): void => {
  const row = payload.row as VendorModuleRow
  interactionLog.value = `Vendor action ${payload.actionKey} on ${row.vendorName}`
}

const onVendorRefresh = (_payload: GenericDataTableRefreshPayload): void => {
  interactionLog.value = 'Vendor table requested a manual refresh.'
}
</script>

<style scoped>
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
</style>
