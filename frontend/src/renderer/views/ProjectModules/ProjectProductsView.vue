<template>
  <ProjectModuleShell
    :title="t('ProjectMain.modules.productsTitle')"
    :project-code="currentProjectCode"
    :project-description="currentProjectDescription"
    :interaction-log="interactionLog"
  >
    <div class="project-module__example-shell app-stack app-stack--compact">
      <div class="project-module__example-copy">
        <strong>Product batch queue</strong>
        <span>
          Ejemplo real en la vista de products usando provider mode, seleccion
          multiple y payload batch listo para backend.
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
              Uses the same module without project-specific wrappers.
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
  type GenericDataTableRow,
  type GenericDataTableSelectionPayload
} from '@/renderer/components/genericDataTable/custom-data-table.public'
import ProjectModuleShell from './ProjectModuleShell.vue'
import {
  type ProductModuleRow,
  productColumns,
  productDataProvider
} from './projectModuleTables'
import { useProjectModulePage } from './useProjectModulePage'

const { t } = useI18n()
const { currentProjectCode, currentProjectDescription } = useProjectModulePage()
const interactionLog = ref('Open Products to exercise a real module route.')
const productQuery = ref<GenericDataTableQuery>({
  page: 0,
  size: 5,
  sortField: 'productName',
  sortOrder: 1,
  globalFilter: null,
  filters: {}
})

const productColumnsForTable = productColumns as unknown as Array<any>
const productDataProviderForTable =
  productDataProvider as unknown as GenericDataTableDataProvider<GenericDataTableRow>

const onProductQueryChange = (nextQuery: GenericDataTableQuery): void => {
  productQuery.value = nextQuery
  interactionLog.value = `Product query changed -> page ${nextQuery.page + 1}, filters ${JSON.stringify(nextQuery.filters ?? {})}`
}

const onProductAction = (
  payload: GenericDataTableActionPayload<GenericDataTableRow>
): void => {
  const row = payload.row as ProductModuleRow
  interactionLog.value = `Product action ${payload.actionKey} on ${row.productName}`
}

const onProductRefresh = (_payload: GenericDataTableRefreshPayload): void => {
  interactionLog.value = 'Product queue requested a manual refresh.'
}

const onProductSelectionChange = (
  payload: GenericDataTableSelectionPayload<GenericDataTableRow>
): void => {
  const selection =
    payload as GenericDataTableSelectionPayload<ProductModuleRow>
  interactionLog.value = `Batch selection updated -> strategy ${selection.batch.strategy}, selected ${selection.selectedCount ?? 0}`
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
