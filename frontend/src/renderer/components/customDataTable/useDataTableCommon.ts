/**
 * useDataTableCommon.ts
 * ---------------------------------------------------------------------------
 * Utilidades comunes para DataTables (pageable y cursorable): inicialización y
 * gestión de filtros, construcción de query para backend, helpers de booleanos,
 * carga de opciones para columnas tipo lista y binding de helpers a componentes.
 */

// =============== Importaciones y Tipos Base ===============
import { Ref, watch } from 'vue'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import type { ColumnDef, FilterModel, MatchMode, SelectionChangePayload, TableQuery } from './datatable-types'

// =============== Sección: Inicialización de filtros ===============
/**
 * initFilters
 * Inicializa modelos de filtro por columna según columnas y query inicial.
 * Emite 'filters-change' al finalizar.
 */
export function initFilters(
    columns: ColumnDef[],
    query: TableQuery | undefined,
    filtersRef: Ref<Record<string, FilterModel>>,
    emit: (e: 'filters-change', v: Record<string, FilterModel>) => void,
) {
  const base: Record<string, FilterModel> = {}
  for (const c of columns) {
    if (!c.filter) continue
    const mode: MatchMode = c.matchMode || 'contains'
    const initial = query && query[c.backendField || c.field] != null ? query[c.backendField || c.field] : null
    base[c.field] = { value: initial, matchMode: mode }
  }
  filtersRef.value = base
  emit('filters-change', filtersRef.value)
}

// =============== Sección: Resolución de componentes de filtro ===============
/** Determina el componente de entrada según tipo de columna. */
export function resolveFilterComp(col: ColumnDef) {
  switch (col.type) {
    case 'number': return InputNumber
    case 'date': return Calendar
    case 'boolean': return Dropdown
    case 'list': return Dropdown
    default: return InputText
  }
}

// =============== Sección: Helpers de formateo booleano ===============
/** Etiqueta a mostrar en celda para un valor booleano usando labels de la columna. */
export function formatBooleanCell(col: ColumnDef, value: any) {
  const labels = col.booleanLabels || {}
  if (value === true) return labels.trueLabel ?? 'Sí'
  if (value === false) return labels.falseLabel ?? 'No'
  return labels.nullLabel ?? '-'
}

/** Construye opciones de filtro para boolean (Dropdown). */
export function buildBooleanFilterOptions(col: ColumnDef) {
  const labels = col.booleanLabels || {}
  return [
    { label: labels.trueLabel ?? 'Sí', value: true },
    { label: labels.falseLabel ?? 'No', value: false },
  ]
}

// =============== Sección: Estado de filtros (consultas rápidas) ===============
/** Indica si hay algún filtro activo distinto de null/''. */
export function hasActiveFilters(filtersRef: Ref<Record<string, FilterModel>>) {
  return Object.values(filtersRef.value).some(f => f.value !== null && f.value !== '')
}

/** Limpia todos los filtros estableciendo value=null y emite 'filters-change'. */
export function clearAllFilters(
    filtersRef: Ref<Record<string, FilterModel>>,
    loadCb: (() => void) | undefined,
    emit: (e: 'filters-change', v: Record<string, FilterModel>) => void,
) {
  let changed = false
  for (const f of Object.values(filtersRef.value)) {
    if (f.value !== null && f.value !== '') { f.value = null; changed = true }
  }
  if (!changed && loadCb) loadCb()
  emit('filters-change', filtersRef.value)
}

/** Firma estable de filtros para detectar cambios (debounce). */
export function buildFilterSignature(filtersRef: Ref<Record<string, FilterModel>>) {
  return Object.entries(filtersRef.value)
      .map(([k, f]) => `${k}:${f.value === null || f.value === '' ? '' : String(f.value)}`)
      .join('|')
}

// =============== Sección: Carga de opciones para columnas de lista ===============
/**
 * ensureListOptions
 * Rellena col.filterOptions para columnas de tipo 'list' si están vacías.
 * Camino soportado:
 *  - optionItemsProvider: items ya cargados → se mapean directamente.
 * Añade la opción global 'Todos' si includeAllOption.
 */
export async function ensureListOptions(columns: ColumnDef[]) {
  const tasks = (columns || []).map(async (col) => {
    if (col.type !== 'list') return
    if (col.filterOptions && col.filterOptions.length > 0) return
    if (typeof col.optionItemsProvider === 'function') {
      try {
        const raw = col.optionItemsProvider() || []
        const labelKey = col.optionLabelField || 'name'
        const valueKey = col.optionValueField || 'id'
        const opts = (Array.isArray(raw) ? raw : []).map(it => ({ label: it?.[labelKey], value: it?.[valueKey] }))
        if (col.includeAllOption) opts.unshift({ label: col.includeAllLabel || 'Todos', value: null })
        col.filterOptions = opts
      } catch (e) {
        window.$logger?.error('[useDataTableCommon] optionItemsProvider error for', col.field, e)
      }
    }
  })
  await Promise.allSettled(tasks)
}

// =============== Sección: Construcción y sincronización de query ===============
/**
 * buildBaseQuery
 * Construye un TableQuery a partir de filtros + orden + base (page/size).
 * Conversión estándar según tipo:
 *  - list: strings numéricos → Number
 *  - boolean: normaliza true/false y representaciones string/num
 *  - number: convierte strings numéricos a Number (enteros/decimales, negativos)
 * Aplica paramTransform si existe y elimina parámetros nulos/vacíos.
 */
export function buildBaseQuery(
    columns: ColumnDef[],
    filtersRef: Ref<Record<string, FilterModel>>,
    sortField: Ref<string | null>,
    sortOrder: Ref<1 | -1>,
    base: TableQuery,
) {
  const q: TableQuery = {
    ...base,
    sortBy: sortField.value ?? undefined,
    sortDir: sortOrder.value === 1 ? 'asc' : 'desc',
  }

  for (const [k, f] of Object.entries(filtersRef.value)) {
    const val = f.value
    const col = columns.find(c => c.field === k)
    const backendKey = col?.backendField || k

    if (val === null || val === '') { delete (q as any)[backendKey]; continue }

    let out: any = val

    if (col?.type === 'list' || col?.filterOptions?.length) {
      out = (typeof val === 'string' && /^\d+$/.test(val)) ? Number(val) : val
    } else if (col?.type === 'boolean') {
      if (val === true || val === false) out = val
      else if (typeof val === 'string') {
        const lower = val.toLowerCase()
        if (lower === 'true') out = true
        else if (lower === 'false') out = false
        else out = null
      } else if (val === 1 || val === '1') out = true
      else if (val === 0 || val === '0') out = false
      else out = null
    } else if (col?.type === 'number') {
      if (typeof val === 'string' && val !== '') out = /^[-]?\d+(\.\d+)?$/.test(val) ? Number(val) : val
    }

    if (col?.paramTransform) out = col.paramTransform(out)
    if (out === null || out === '') delete (q as any)[backendKey]
    else (q as any)[backendKey] = out
  }

  return q
}

/** Sincroniza los FilterModel existentes con los valores actuales de la query externa. */
export function applyFiltersFromQuery(
    columns: ColumnDef[],
    q: TableQuery,
    filtersRef: Ref<Record<string, FilterModel>>,
) {
  for (const [k, f] of Object.entries(filtersRef.value)) {
    const backendKey = columns.find(c => c.field === k)?.backendField || k
    f.value = (q as any)[backendKey] ?? null
  }
}

// =============== Sección: Binding de helpers a componentes ===============
/**
 * useDataTableBindings
 * Devuelve un set de helpers ya vinculados a refs externas de componente.
 */
export function useDataTableBindings(opts: {
  columnsRef: Ref<ColumnDef[]>
  query: TableQuery | undefined
  filtersRef: Ref<Record<string, FilterModel>>
  emit: (e: 'filters-change', v: Record<string, FilterModel>) => void
  loadIfProvider?: () => void
}) {
  const { columnsRef, query, filtersRef, emit, loadIfProvider } = opts
  const boundInitFilters = () => initFilters(columnsRef.value, query, filtersRef, emit)

  // No necesitamos deep:true; sólo nos interesa cuando cambia el array de columnas.
  const startColumnsWatch = () => watch(columnsRef, boundInitFilters)

  return {
    resolveFilterComp,
    formatBooleanCell,
    buildBooleanFilterOptions,
    hasActiveFilters: () => hasActiveFilters(filtersRef),
    clearAllFilters: () => clearAllFilters(filtersRef, loadIfProvider, emit),
    buildFilterSignature: () => buildFilterSignature(filtersRef),
    initFilters: boundInitFilters,
    startColumnsWatch,
  }
}

// =============== Sección: Tipado de eventos emitidos por DataTable ===============
/**
 * DataTableEmits
 * Contrato de eventos emitidos por los componentes de tabla.
 */
export interface DataTableEmits {
  (e: 'update:query', v: TableQuery): void;
  (e: 'load', payload: { rows: any[]; total: number }): void;
  (e: 'filters-change', filters: Record<string, FilterModel>): void;
  (e: 'sort', evt: { sortField: string | null; sortOrder: 1 | -1 }): void;
  (e: 'page', evt: { first: number; rows: number }): void;
  (e: 'action', payload: { actionKey: string; row: any }): void;
  (e: 'selection-change', payload: SelectionChangePayload): void;
  (e: 'row-click', payload: { row: any; originalEvent?: any }): void;
}

