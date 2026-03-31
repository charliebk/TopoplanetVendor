<script setup lang="ts">
/**
 * ListingCountBar.vue
 * -------------------------------------------------------
 * Barra compacta de métricas para tablas (CustomDataTable):
 *  - Total (baselineTotal): número global sin filtros si se conoce.
 *  - Resultados (filteredTotal): total tras aplicar filtros actuales.
 *  - Mostrados (shown): cantidad de filas que se están dibujando (modo CURSOR u otros contextos).
 *  - Excluidos (excluded): diferencia entre total base y filtrado cuando hay filtros activos.
 *  - Botón para limpiar filtros (emite 'clear-filters').
 *
 * Orden del código (patrón modelo):
 * 1) Importaciones.
 * 2) Definición de props con defaults.
 * 3) Definición de emits.
 * 4) Computeds derivados (total, excluded).
 * 5) Template con clases BEM-like y slot para contenido extra.
 */

// 1) Importaciones
import { computed } from 'vue'

// 2) Props (withDefaults para valores opcionales)
const props = withDefaults(defineProps<{
  baselineTotal: number | null
  filteredTotal: number
  shown?: number | null
  hasFilters: boolean
  showShown?: boolean
}>(), {
  shown: null,
  showShown: false,
})

// 3) Emits (único evento para limpiar filtros)
const emit = defineEmits<{ (e: 'clear-filters'): void }>()

// 4) Computeds derivados
/** Total preferente (baseline si existe, en otro caso el filtrado). */
const total = computed(() => props.baselineTotal ?? props.filteredTotal)
/** Cantidad excluida (diferencia entre total base y filtrado si hay filtros). */
const excluded = computed(() => {
  if (!props.hasFilters) return 0
  const base = total.value ?? 0
  const filtered = props.filteredTotal ?? 0
  return Math.max(0, Number(base) - Number(filtered))
})
</script>

<template>
  <div class="lcb-root">
    <!-- Métricas agrupadas -->
    <div class="lcb-count-inline">
      <span><strong>Total:</strong> {{ total }}</span>
      <span><strong>Resultados:</strong> {{ filteredTotal }}</span>
      <span v-if="showShown && shown !== null"><strong>Mostrados:</strong> {{ shown }}</span>
      <span><strong>Excluidos:</strong> {{ excluded }}</span>
    </div>

    <!-- Botón limpiar filtros (deshabilitado si no hay filtros activos) -->
    <button
      type="button"
      class="p-button p-button-text p-button-sm lcb-clear-btn"
      :disabled="!hasFilters"
      @click="emit('clear-filters')"
      aria-label="Limpiar filtros"
    >
      <i class="pi pi-filter-slash" />
      <span>Limpiar filtros</span>
    </button>

    <!-- Slot opcional para tags, badges u otra información contextual -->
    <slot />
  </div>
</template>

<style scoped>
.lcb-root { display: flex; gap: .5rem; align-items: center; justify-content: flex-end; flex-wrap: wrap; }
.lcb-count-inline { display: flex; gap: .4rem; align-items: center; font-size: .65rem; }
.lcb-clear-btn { display: inline-flex; align-items: center; gap: .25rem; padding: 0 .25rem; height: 1.5rem; font-size: .65rem; }
.lcb-clear-btn:disabled { opacity: .4; cursor: not-allowed; }
</style>