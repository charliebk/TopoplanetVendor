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
import type { GenericDataTableCountBarProps } from './generic-data-table.types'

const props = withDefaults(defineProps<GenericDataTableCountBarProps>(), {
  shown: null,
  showShown: false,
  showClearFiltersButton: false,
  clearFiltersLabel: 'Clear filters',
  totalLabel: 'Total',
  resultsLabel: 'Results',
  shownLabel: 'Shown',
  excludedLabel: 'Excluded'
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--app-space-3);
  flex-wrap: wrap;
  padding: var(--app-space-3) var(--app-space-4);
  border: 1px solid var(--app-border-subtle);
  border-radius: var(--app-radius-panel);
  background: var(--app-surface-soft);
}

.generic-data-table-count-bar__metrics {
  display: inline-flex;
  align-items: center;
  gap: 0.85rem;
  flex-wrap: wrap;
  color: var(--app-text-muted);
  font-size: 0.85rem;
}

.generic-data-table-count-bar__metric strong {
  color: var(--app-text-strong);
}

.generic-data-table-count-bar__actions {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
}
</style>
