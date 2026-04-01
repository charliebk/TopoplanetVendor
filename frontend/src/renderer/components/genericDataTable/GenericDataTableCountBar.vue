<template>
  <div class="generic-data-table-count-bar">
    <div class="generic-data-table-count-bar__metrics">
      <span class="generic-data-table-count-bar__metric">
        <strong>{{ totalLabel }}:</strong> {{ totalValue }}
      </span>

      <span class="generic-data-table-count-bar__metric">
        <strong>{{ resultsLabel }}:</strong> {{ filteredTotal }}
      </span>

      <span
        v-if="showShown && shown !== null"
        class="generic-data-table-count-bar__metric"
      >
        <strong>{{ shownLabel }}:</strong> {{ shown }}
      </span>

      <span
        v-if="hasFilters"
        class="generic-data-table-count-bar__metric"
      >
        <strong>{{ excludedLabel }}:</strong> {{ excludedValue }}
      </span>
    </div>

    <div class="generic-data-table-count-bar__actions">
      <PrimeButton
        v-if="showClearFiltersButton"
        type="button"
        icon="pi pi-filter-slash"
        text
        severity="secondary"
        :label="clearFiltersLabel"
        :disabled="!hasFilters"
        @click="emit('clear-filters')"
      />

      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PrimeButton from 'primevue/button'
import { GENERIC_DATA_TABLE_LOCALE } from './generic-data-table.locale'
import type { GenericDataTableCountBarProps } from './generic-data-table.types'

const props = withDefaults(defineProps<GenericDataTableCountBarProps>(), {
  shown: null,
  showShown: false,
  showClearFiltersButton: false,
  clearFiltersLabel: GENERIC_DATA_TABLE_LOCALE.countBarClearFiltersLabel,
  totalLabel: GENERIC_DATA_TABLE_LOCALE.countBarTotalLabel,
  resultsLabel: GENERIC_DATA_TABLE_LOCALE.countBarResultsLabel,
  shownLabel: GENERIC_DATA_TABLE_LOCALE.countBarShownLabel,
  excludedLabel: GENERIC_DATA_TABLE_LOCALE.countBarExcludedLabel
})

const emit = defineEmits<{
  (event: 'clear-filters'): void
}>()

const totalValue = computed(() => props.baselineTotal ?? props.filteredTotal)

const excludedValue = computed(() => {
  if (!props.hasFilters) {
    return 0
  }

  return Math.max(0, totalValue.value - props.filteredTotal)
})
</script>

<style scoped>
.generic-data-table-count-bar {
  --gdt-space-3: var(--app-space-3, 0.75rem);
  --gdt-space-4: var(--app-space-4, 1rem);
  --gdt-font-size-sm: 0.74rem;
  --gdt-border-subtle: var(--app-border-subtle, rgba(15, 23, 42, 0.12));
  --gdt-radius-panel: var(--app-radius-panel, 0.75rem);
  --gdt-surface-soft: var(--app-surface-soft, #f8fafc);
  --gdt-text-muted: var(--app-text-muted, #5f6b76);
  --gdt-text-strong: var(--app-text-strong, #1f2937);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--gdt-space-3);
  flex-wrap: wrap;
  padding: var(--gdt-space-3) var(--gdt-space-4);
  border: 1px solid var(--gdt-border-subtle);
  border-radius: var(--gdt-radius-panel);
  background: var(--gdt-surface-soft);
}

.generic-data-table-count-bar__metrics {
  display: inline-flex;
  align-items: center;
  gap: 0.85rem;
  flex-wrap: wrap;
  color: var(--gdt-text-muted);
  font-size: var(--gdt-font-size-sm);
}

.generic-data-table-count-bar__metric strong {
  color: var(--gdt-text-strong);
}

.generic-data-table-count-bar__actions {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
}

:deep(.generic-data-table-count-bar .p-button .p-button-label) {
  font-size: var(--gdt-font-size-sm) !important;
}
</style>
