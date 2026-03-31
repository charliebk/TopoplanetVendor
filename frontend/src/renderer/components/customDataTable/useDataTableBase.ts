// useDataTableBase.ts
/**
 * useDataTableBase
 * -------------------------------------------------------
 * Composable base para el estado interno de una tabla reutilizable.
 * Proporciona refs reactivas para:
 *  - internalRows: tamaño de página actual.
 *  - sortField / sortOrder: ordenación activa.
 *  - internalData: filas actuales en memoria (página o cursor).
 *  - internalTotal: total filtrado devuelto por backend.
 *  - baselineTotal: total global sin filtros (se fija tras primera carga sin filtros).
 *  - filters: estructura de filtros PrimeVue (FilterModel por campo).
 *
 * No gestiona carga de datos; se complementa con otros composables (useDataTableBindings,
 * lógica de CustomDataTable, etc.). Mantenerlo pequeño y centrado en estado.
 */
import { ref } from 'vue'
import type { FilterModel, TableQuery } from './datatable-types'

/**
 * Estructura tipada del estado base devuelto.
 */
export interface DataTableBaseState {
  internalRows: ReturnType<typeof ref<number>>
  sortField: ReturnType<typeof ref<string | null>>
  sortOrder: ReturnType<typeof ref<1 | -1>>
  internalData: ReturnType<typeof ref<any[]>>
  internalTotal: ReturnType<typeof ref<number>>
  baselineTotal: ReturnType<typeof ref<number | null>>
  filters: ReturnType<typeof ref<Record<string, FilterModel>>>
  resetBaseState: (query?: TableQuery) => void
}

/**
 * Crea y devuelve el estado base de la tabla.
 * @param initialQuery Query inicial (page/size/sort) para seed del estado.
 * @param defaultRows Tamaño de página por defecto si initialQuery no define size.
 */
export function useDataTableBase(initialQuery: TableQuery | undefined, defaultRows: number): DataTableBaseState {
  const internalRows = ref<number>(initialQuery?.size ?? defaultRows)
  const sortField = ref<string | null>(initialQuery?.sortBy ?? null)
  const sortOrder = ref<1 | -1>((initialQuery?.sortDir ?? 'asc') === 'asc' ? 1 : -1)
  const internalData = ref<any[]>([])
  const internalTotal = ref<number>(0)
  const baselineTotal = ref<number | null>(null)
  const filters = ref<Record<string, FilterModel>>({})

  /**
   * resetBaseState
   * Reestablece los valores principales (sin tocar filtros actuales) según nueva query opcional.
   * Útil cuando se reinicia cursor o se fuerza un cambio de modo.
   */
  function resetBaseState(q?: TableQuery) {
    if (q) {
      internalRows.value = q.size ?? internalRows.value
      sortField.value = q.sortBy ?? sortField.value
      sortOrder.value = (q.sortDir ?? (sortOrder.value === 1 ? 'asc' : 'desc')) === 'asc' ? 1 : -1
    }
    internalData.value = []
    internalTotal.value = 0
    baselineTotal.value = null
  }

  return { internalRows, sortField, sortOrder, internalData, internalTotal, baselineTotal, filters, resetBaseState }
}

// Tipo derivado (compatibilidad si se importaba anteriormente)
export type UseDataTableBaseReturn = DataTableBaseState
