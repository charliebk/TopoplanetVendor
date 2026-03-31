<template>
  <div class="cdt">
    <DataTable
        v-if="columnsReady"
        :value="internalData"
        :paginator="!isCursor"
        :rows="nonUndefinedRows"
        :totalRecords="isCursor ? filteredTotal : internalTotal"
        :first="!isCursor ? internalPage * nonUndefinedRows : 0"
        :lazy="!!dataProvider || !!store"
        :filterDisplay="filterDisplay"
        :showGridlines="showGridlines"
        :stripedRows="stripedRows"
        :rowHover="rowHover"
        v-model:filters="filters"
        :rowsPerPageOptions="!isCursor ? rowsPerPageOptions : undefined"
        :loading="store?.loadingList ?? false"
        @page="onInternalPage"
        @sort="onInternalSort"
        @row-click="onInternalRowClick"
    >
      <!-- HEADER: barra de conteo -->
      <template
          v-if="showCountFooter && (showCountFooterPosition === 'start' || showCountFooterPosition === 'both')"
          #header
      >
        <ListingCountBar
            :baselineTotal="displayTotal"
            :filteredTotal="filteredTotal"
            :shown="isCursor ? resultsCount : null"
            :showShown="isCursor"
            :hasFilters="hasFiltersActive"
            @clear-filters="clearAllFilters"
        >
          <slot
              name="count-header"
              :total="internalTotal"
              :baselineTotal="displayTotal"
              :excluded="Math.max(0, Number(displayTotal) - Number(filteredTotal))"
          />
        </ListingCountBar>
      </template>

      <!-- COLUMNAS dinámicas -->
      <template v-for="col in columns" :key="col.field">
        <Column
            :field="col.field"
            :header="col.header"
            :sortable="col.sortable !== false && !['actions','select','idIcon'].includes(col.type || 'string')"
            :filter="col.filter === true && !['actions','select','idIcon'].includes(col.type || 'string')"
            :dataType="col.type === 'number' || col.type === 'integer' || col.type === 'percent' ? 'numeric' : col.type === 'date' ? 'date' : 'text'"
            :filterMatchMode="col.matchMode ?? 'contains'"
            :style="col.width || col.minWidth || col.maxWidth ? { width: col.width, minWidth: col.minWidth || col.width, maxWidth: col.maxWidth } : undefined"
            :headerStyle="{ textAlign: col.align || 'left', width: col.width }"
            :bodyStyle="{ textAlign: col.align || 'left', width: col.width }"
        >
          <!-- BODY por tipo de columna -->
          <template #body="{ data }">
            <!-- ✅ Checkbox: NO debe disparar row-click -->
            <div v-if="col.type === 'select'" class="cdt-select-cell" @click.stop>
              <Checkbox
                  :binary="true"
                  :modelValue="isRowSelected(data)"
                  @update:modelValue="val => setRowSelected(data, !!val)"
              />
            </div>

            <!-- ID con icono + tag en hover -->
            <div v-else-if="col.type === 'idIcon'" class="cdt-id-cell">
              <i class="pi cdt-id-icon" :class="resolveIdIconClass(col, data)" />
              <Tag class="cdt-id-tag" :value="data[col.tooltipField || col.field]" />
            </div>

            <!-- ✅ Acciones: NO deben disparar row-click -->
            <div v-else-if="col.type === 'actions'" class="cdt-actions" @click.stop>
              <Button
                  v-for="act in col.actions || []"
                  :key="act.key"
                  :icon="act.icon"
                  :label="act.label"
                  :class="act.class"
                  :disabled="typeof act.disabled === 'function' ? act.disabled(data) : act.disabled"
                  @click.stop="emit('action', { actionKey: act.key, row: data })"
              />
            </div>

            <!-- Booleano formateado -->
            <template v-else-if="col.type === 'boolean'">
              <Tag
                  v-if="col.booleanTag"
                  :value="formatBooleanCell(col, data[col.field])"
                  :severity="data[col.field] === true ? col.booleanTagSeverity?.true || 'success' : data[col.field] === false ? col.booleanTagSeverity?.false || 'warning' : col.booleanTagSeverity?.null || 'info'"
              />
              <span v-else>{{ formatBooleanCell(col, data[col.field]) }}</span>
            </template>

            <!-- Fecha amigable -->
            <span v-else-if="col.type === 'date'">
              {{ formatDateCell(col.displayField ? data[col.displayField] : data[col.field]) }}
            </span>

            <!-- Número con formateo de decimales -->
            <span v-else-if="col.type === 'number'">
              {{ formatNumberCell(col, col.displayField ? data[col.displayField] : data[col.field]) }}
            </span>

            <!-- Entero sin decimales -->
            <span v-else-if="col.type === 'integer'">
              {{ formatIntegerCell(col.displayField ? data[col.displayField] : data[col.field]) }}
            </span>

            <!-- Porcentaje con decimales y símbolo % -->
            <span v-else-if="col.type === 'percent'">
              {{ formatPercentCell(col, col.displayField ? data[col.displayField] : data[col.field]) }}
            </span>

            <!-- Resto de tipos -->
            <span v-else>
              {{ col.displayField ? data[col.displayField] : data[col.field] }}
            </span>
          </template>

          <!-- FILTRO por columna -->
          <template
              v-if="col.filter && !['actions','select','idIcon'].includes(col.type || 'string')"
              #filter="{ filterModel, filterCallback }"
          >
            <div v-if="filterModel" class="cdt-filter-cell">
              <Dropdown
                  v-if="col.filterOptions && col.filterOptions.length"
                  v-model="filterModel.value"
                  :options="col.filterOptions"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="--"
                  class="w-full"
                  @change="filterCallback()"
              />
              <Dropdown
                  v-else-if="col.type === 'boolean'"
                  v-model="filterModel.value"
                  :options="buildBooleanFilterOptions(col)"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="--"
                  class="w-full"
                  :showClear="true"
                  @change="filterCallback()"
              />
              <InputText
                  v-else-if="col.type === 'number'"
                  v-model="filterModel.value"
                  class="w-full"
                  inputmode="numeric"
                  @update:modelValue="filterCallback()"
              />
              <component
                  v-else
                  :is="resolveFilterComp(col)"
                  v-model="filterModel.value"
                  class="w-full"
                  :selectionMode="col.type === 'date' ? 'range' : undefined"
                  :placeholder="col.header"
                  @update:modelValue="filterCallback()"
              />
            </div>
          </template>
        </Column>
      </template>

      <!-- FOOTER: barra de conteo en paginador -->
      <template
          v-if="showCountFooter && (showCountFooterPosition === 'end' || showCountFooterPosition === 'both')"
          #paginatorend
      >
        <ListingCountBar
            :baselineTotal="displayTotal"
            :filteredTotal="filteredTotal"
            :shown="isCursor ? resultsCount : null"
            :showShown="isCursor"
            :hasFilters="hasFiltersActive"
            @clear-filters="clearAllFilters"
        >
          <slot
              name="count-footer"
              :total="internalTotal"
              :baselineTotal="displayTotal"
              :excluded="Math.max(0, Number(displayTotal) - Number(filteredTotal))"
          />
        </ListingCountBar>
      </template>

      <template #empty>
        <div class="p-3 text-center text-color-secondary">Sin datos</div>
      </template>
      <template #loading>
        <div class="p-3 text-center">Cargando…</div>
      </template>
    </DataTable>

    <!-- NAV CURSOR (modo CURSOR) -->
    <div v-if="isCursor" class="cdt-cursor-pager">
      <Button class="p-button-sm" icon="pi pi-angle-double-left" label="Primera" @click="goFirst" :disabled="disableFirst" />
      <Button class="p-button-sm" icon="pi pi-angle-left" label="Anterior" @click="goPrev" :disabled="disablePrev" />
      <Button class="p-button-sm" icon="pi pi-angle-right" label="Siguiente" @click="goNext" :disabled="disableNext" />
      <Button class="p-button-sm" icon="pi pi-angle-double-right" label="Final" @click="goEnd" :disabled="disableEnd" />
      <span class="cdt-cursor-info">
        Página {{ cursorCurrentIndex + 1 }} de {{ approxPages }} (≈)
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed, defineExpose, nextTick } from 'vue'
import DataTable, { type DataTableSortEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Checkbox from 'primevue/checkbox'

import ListingCountBar from './ListingCountBar.vue'
import type { ColumnDef, ProviderResult, TableQuery } from './datatable-types'
import {
  useDataTableBindings,
  buildBaseQuery,
  ensureListOptions,
  applyFiltersFromQuery,
  type DataTableEmits,
} from './useDataTableCommon'
import { useDataTableBase } from './useDataTableBase'
import { useTableSelection } from './useTableSelection'
import { useUiStore } from '@/process/ManagerUI/store/ui.store'

interface ListingStoreLike {
  items?: any[]
  meta?: {
    hasNext?: boolean
    hasPrevious?: boolean
    nextCursor?: string | null
    totalElements?: number | null
    filteredElements?: number | null
  } | null
  totalElements?: number
  loadingList?: boolean

  setMode?(m: 'PAGE' | 'CURSOR'): void
  setSort?(field: string | null, dir: 'asc' | 'desc'): void
  setFilter?(f: Record<string, any>): void
  resetFilter?(): void

  fetchPage?(pageIndex: number): Promise<void>
  fetchFirstCursorPage?(pageSize?: number): Promise<void>
  fetchNextCursorPage?(pageSize?: number): Promise<void>

  fetchList?(query: Record<string, any>): Promise<void>
}

const props = withDefaults(defineProps<{
  columns: ColumnDef[]
  mode: 'PAGE' | 'CURSOR'
  dataProvider?: (params: Record<string, any>) => Promise<ProviderResult>
  store?: ListingStoreLike
  query?: TableQuery
  rows?: number
  rowsPerPageOptions?: number[]
  filterDisplay?: 'row' | 'menu'
  showGridlines?: boolean
  stripedRows?: boolean
  rowHover?: boolean
  debounceMs?: number
  refreshKey?: number
  debugParams?: boolean
  showCountFooter?: boolean
  showCountFooterPosition?: 'start' | 'end' | 'both'
  rowKeyField?: string
  useGlobalLoading?: boolean

  /** ✅ NUEVO: filtro fijo SIEMPRE aplicado (ej: { cdauRoadId }) */
  baseFilter?: Record<string, any>
}>(), {
  mode: 'PAGE',
  rows: 10,
  rowsPerPageOptions: () => [10, 20, 50],
  filterDisplay: 'row',
  showGridlines: true,
  stripedRows: false,
  rowHover: true,
  debounceMs: 300,
  debugParams: false,
  showCountFooter: true,
  showCountFooterPosition: 'start',
  query: () => ({ page: 0, size: 10 }),
  rowKeyField: 'id',
  useGlobalLoading: false,

  baseFilter: () => ({}),
})

/**
 * ✅ IMPORTANTE: te rompí el listener en el template porque no estaba declarado.
 * Forzamos que 'selection-change' esté en emits para que Vue/Volar no se queje.
 */
type ExtendedEmits = DataTableEmits & {
  (e: 'selection-change', payload: any): void
}
const emit = defineEmits<ExtendedEmits>()

const uiStore = useUiStore()
const isCursor = computed(() => props.mode === 'CURSOR')
const usingStore = computed(() => !!props.store)
const columnsReady = ref(true)

const baseState = useDataTableBase(props.query, props.rows)
const { internalRows, sortField, sortOrder, internalData, internalTotal, baselineTotal, filters } = baseState

const internalPage = ref(props.query?.page ?? 0)
const nonUndefinedRows = computed(() => internalRows.value || 0)

let prevFilterSignature = ''
let isEmittingQuery = false
let isSyncingFromQuery = false
let loadInProgress = false

const {
  selectedRows,
  selectionAllFiltered,
  isRowSelected,
  setRowSelected,
  selectAllPage,
  selectAllFiltered,
  deselectAll,
  refreshSelectionWith,
} = useTableSelection(
    internalData as any,
    internalTotal as any,
    props.rowKeyField,
    (e, payload) => emit(e as any, payload as any),
)

defineExpose({
  selectedRows,
  selectionAllFiltered,
  selectAllFiltered,
  selectAllPage,
  deselectAll,
})

const {
  initFilters: initFiltersBound,
  startColumnsWatch,
  resolveFilterComp,
  formatBooleanCell,
  buildBooleanFilterOptions,
  hasActiveFilters,
  clearAllFilters: clearAllFiltersBound,
  buildFilterSignature,
} = useDataTableBindings({
  columnsRef: computed(() => props.columns),
  query: props.query,
  filtersRef: filters as any,
  emit: (e: any, v: any) => emit(e, v),
})

initFiltersBound()
startColumnsWatch()
prevFilterSignature = buildFilterSignature()

// ====== NUEVO: claves reservadas (no se pisan con baseFilter) ======
const RESERVED_KEYS = new Set(['page', 'size', 'cursor', 'sortBy', 'sortDir', 'mode', 'end'])

function applyBaseFilterToQuery(q: TableQuery): TableQuery {
  const bf = props.baseFilter || {}
  for (const [k, v] of Object.entries(bf)) {
    if (RESERVED_KEYS.has(k)) continue
        ;(q as any)[k] = v
  }
  return q
}

function computeTableQuery(base: TableQuery): TableQuery {
  const q = buildBaseQuery(props.columns, filters as any, sortField as any, sortOrder as any, base)
  if (props.debugParams) window.$logger?.info('[CustomDataTable] query params:', q)
  return q
}

function buildQueryParams(): TableQuery {
  const base: TableQuery = isCursor.value
      ? {
        page: cursorCurrentIndex.value,
        size: internalRows.value || 0,
        mode: 'CURSOR',
        ...(cursorCurrentToken.value ? { cursor: cursorCurrentToken.value } : {}),
      }
      : {
        page: internalPage.value,
        size: internalRows.value || 0,
        mode: 'PAGE',
      }

  // ✅ AQUÍ el cambio: inyectar baseFilter SIN romper tu sistema de filtros
  return applyBaseFilterToQuery(computeTableQuery(base))
}

function buildFilterObject(): Record<string, any> {
  const q = buildQueryParams()
  const out: Record<string, any> = {}
  for (const [k, v] of Object.entries(q)) {
    if (RESERVED_KEYS.has(k)) continue
    out[k] = v
  }
  return out
}

function formatDateCell(value: any): string {
  if (value == null) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatNumberCell(col: any, value: any): string {
  if (value == null || value === '') return ''
  const num = Number(value)
  if (Number.isNaN(num)) return String(value)
  const decimals = typeof col.decimals === 'number' ? col.decimals : 2
  return window.$formatter ? window.$formatter.formatNumber(num, decimals) : num.toFixed(decimals)
}

function formatIntegerCell(value: any): string {
  if (value == null || value === '') return ''

  // A veces el backend ya manda el número renderizado como string (p.ej. "97.821.126" o "97.821.126,00").
  // Para IDs queremos SIEMPRE un entero plano sin separadores ni decimales.
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return ''
    // Mantiene solo dígitos (y un '-' inicial si existiera).
    const isNeg = trimmed.startsWith('-')
    const digits = trimmed.replace(/\D+/g, '')
    if (!digits) return String(value)
    return (isNeg ? '-' : '') + digits
  }

  const num = Number(value)
  if (Number.isNaN(num)) return String(value)
  return Math.round(num).toString()
}

function formatPercentCell(col: any, value: any): string {
  if (value == null || value === '') return ''
  const num = Number(value)
  if (Number.isNaN(num)) return String(value)
  const decimals = typeof col.decimals === 'number' ? col.decimals : 1
  const base = window.$formatter ? window.$formatter.formatNumber(num, decimals) : num.toFixed(decimals)
  return `${base}%`
}

/** ✅ FIX2: baselineTotal debe venir SIEMPRE de meta.totalElements cuando exista */
function updateBaselineTotal(overall: any) {
  if (typeof overall === 'number' && !Number.isNaN(overall) && overall >= 0) {
    baselineTotal.value = overall as any
  }
}

async function loadIfProvider() {
  if (loadInProgress) return
  const shouldShowGlobal = props.useGlobalLoading
  loadInProgress = true
  if (shouldShowGlobal) uiStore.show()

  try {
    if (usingStore.value) {
      await loadFromStore()
      return
    }
    if (!props.dataProvider) return

    const query = buildQueryParams()
    const resp = await props.dataProvider(query)

    if (!('ok' in resp) || !resp.ok) {
      internalData.value = []
      internalTotal.value = 0
      emit('load', { rows: [], total: 0 })
      return
    }

    const d: any = resp.data

    if (!isCursor.value) {
      const rows = d.content as any[]
      let overall = d.meta?.totalElements ?? d.totalElements
      if (overall == null || overall < 0) overall = undefined
      let filtered = d.meta?.filteredElements
      if (filtered == null || filtered < 0) filtered = overall as number | undefined
      if (filtered == null || filtered < 0) filtered = rows.length

      internalData.value = rows
      internalTotal.value = filtered

      // ✅ FIX2: baselineTotal SIEMPRE que el backend lo traiga
      updateBaselineTotal(overall)

      refreshSelectionWith(rows)
      emit('load', { rows, total: filtered })
      return
    }

    const rows = d.content as any[]
    let overall = d.meta?.totalElements ?? d.totalElements
    if (overall == null || overall < 0) overall = undefined
    let filtered = d.meta?.filteredElements
    if (filtered == null || filtered < 0) filtered = overall as number | undefined
    if (filtered == null || filtered < 0) filtered = rows.length
    const nextCursor = d.meta?.nextCursor ?? null
    const hasNextLocal = !!d.meta?.hasNext

    cursorHasNext.value = hasNextLocal
    ensureCursorArraysSize()
    cursorNextTokens.value[cursorCurrentIndex.value] = nextCursor
    cursorPagesData.value[cursorCurrentIndex.value] = rows

    internalData.value = rows
    internalTotal.value = filtered

    // ✅ FIX2: baselineTotal SIEMPRE que el backend lo traiga
    updateBaselineTotal(overall)

    refreshSelectionWith(rows)
    emit('load', { rows, total: filtered })
  } catch (e) {
    window.$logger?.error('[CustomDataTable] load error', e)
    internalData.value = []
    internalTotal.value = 0
    emit('load', { rows: [], total: 0 })
  } finally {
    if (shouldShowGlobal) uiStore.hide()
    loadInProgress = false
  }
}

async function loadFromStore() {
  const s = props.store as ListingStoreLike | undefined
  if (!s) return

  // ✅ modo fetchList (nuevo)
  if (typeof s.fetchList === 'function') {
    const q = buildQueryParams()
    const filterObj: Record<string, any> = {}
    for (const [k, v] of Object.entries(q)) {
      if (RESERVED_KEYS.has(k)) continue
      filterObj[k] = v
    }
    const opts: any = {
      mode: q.mode,
      page: q.page,
      size: q.size,
      cursor: (q as any).cursor ?? null,
      sortBy: q.sortBy ?? null,
      sortDir: (q as any).sortDir ?? 'asc',
      filter: filterObj,
    }
    await s.fetchList(opts)
    syncFromStore(s)
    return
  }

  // ✅ legacy: setFilter/setSort/fetchPage...
  s.setMode?.(isCursor.value ? 'CURSOR' : 'PAGE')
  s.setSort?.(sortField.value ?? null, sortOrder.value === 1 ? 'asc' : 'desc')

  const fObj = buildFilterObject()
  if (Object.keys(fObj).length === 0) s.resetFilter?.()
  else s.setFilter?.(fObj)

  if (!isCursor.value) {
    await s.fetchPage?.(internalPage.value)
  } else {
    if (cursorCurrentIndex.value === 0) await s.fetchFirstCursorPage?.(internalRows.value)
    else await s.fetchNextCursorPage?.(internalRows.value)
  }
  syncFromStore(s)
}

function syncFromStore(s: ListingStoreLike) {
  const rows = Array.isArray(s.items) ? s.items : []
  let overall: number | undefined | null = s.meta?.totalElements ?? s.totalElements ?? null
  if (overall == null || overall < 0) overall = undefined
  let filtered: number | undefined | null = s.meta?.filteredElements
  if (filtered == null || filtered < 0) filtered = overall
  if (filtered == null || filtered < 0) filtered = rows.length
  const filteredNum = filtered as number

  internalData.value = rows
  internalTotal.value = filteredNum

  // ✅ FIX2: baselineTotal SIEMPRE que exista totalElements
  updateBaselineTotal(overall)

  if (!isCursor.value) {
    // nada más (baseline ya está corregido arriba)
  } else {
    cursorHasNext.value = !!s.meta?.hasNext
    ensureCursorArraysSize()
    cursorPagesData.value[cursorCurrentIndex.value] = rows
  }

  refreshSelectionWith(rows)
  emit('load', { rows, total: filteredNum })
}

// ===== Cursor state =====
const cursorPagesData = ref<any[][]>([])
const cursorNextTokens = ref<(string | null)[]>([])
const cursorCurrentIndex = ref(0)
const cursorHasNext = ref(false)
const cursorCurrentToken = ref<string | null>(null)

function resetCursorState() {
  cursorPagesData.value = []
  cursorNextTokens.value = [null]
  cursorCurrentIndex.value = 0
  cursorHasNext.value = false
  cursorCurrentToken.value = null
}

function ensureCursorArraysSize() {
  while (cursorNextTokens.value.length <= cursorCurrentIndex.value) cursorNextTokens.value.push(null)
  while (cursorPagesData.value.length <= cursorCurrentIndex.value) cursorPagesData.value.push([])
}

const nextTokenForCurrent = computed(() => cursorNextTokens.value[cursorCurrentIndex.value] ?? null)
const disableFirst = computed(() => cursorCurrentIndex.value === 0)
const disablePrev = computed(() => cursorCurrentIndex.value === 0)
const disableNext = computed(() => (usingStore.value ? !(props.store?.meta?.hasNext) : !nextTokenForCurrent.value))
const disableEnd = computed(() => (usingStore.value ? !(props.store?.meta?.hasNext) : !cursorHasNext.value))

function goFirst() {
  if (!isCursor.value || cursorCurrentIndex.value === 0) return
  if (usingStore.value) {
    const s = props.store as ListingStoreLike | undefined
    if (!s) return
    cursorCurrentIndex.value = 0
    resetCursorState()
    void (async () => {
      await s.fetchFirstCursorPage?.(internalRows.value)
      syncFromStore(s)
    })()
    return
  }
  resetCursorState()
  void loadIfProvider()
}

function goPrev() {
  if (!isCursor.value || cursorCurrentIndex.value === 0) return
  if (usingStore.value) {
    cursorCurrentIndex.value--
    const pageData = cursorPagesData.value[cursorCurrentIndex.value]
    if (pageData && pageData.length) {
      internalData.value = pageData
      refreshSelectionWith(pageData)
    } else {
      void loadIfProvider()
    }
    return
  }
  cursorCurrentIndex.value--
  cursorCurrentToken.value = cursorCurrentIndex.value === 0 ? null : cursorNextTokens.value[cursorCurrentIndex.value - 1]
  const page = cursorPagesData.value[cursorCurrentIndex.value] || []
  internalData.value = page
  refreshSelectionWith(page)
}

async function goNext() {
  if (!isCursor.value) return
  if (usingStore.value) {
    const s = props.store as ListingStoreLike | undefined
    if (!s || !s.meta?.hasNext) return
    cursorCurrentIndex.value++
    await s.fetchNextCursorPage?.(internalRows.value)
    syncFromStore(s)
    return
  }
  const nextToken = cursorNextTokens.value[cursorCurrentIndex.value]
  if (!nextToken) return
  cursorCurrentIndex.value++
  cursorCurrentToken.value = nextToken
  if (cursorPagesData.value[cursorCurrentIndex.value]) {
    const page = cursorPagesData.value[cursorCurrentIndex.value]
    internalData.value = page
    refreshSelectionWith(page)
    return
  }
  void loadIfProvider()
}

async function loadTail() {
  if (!props.dataProvider || !isCursor.value) return
  const shouldShowGlobal = props.useGlobalLoading
  if (shouldShowGlobal) uiStore.show()
  try {
    const base: TableQuery = { page: cursorCurrentIndex.value, size: internalRows.value || 0, mode: 'CURSOR' }
    const q: any = computeTableQuery(base)
    delete q.cursor
    q.end = true

    // ✅ también aplicamos baseFilter aquí, por consistencia
    applyBaseFilterToQuery(q)

    const resp = await props.dataProvider(q)
    if (!('ok' in resp) || !resp.ok) {
      internalData.value = []
      internalTotal.value = 0
      emit('load', { rows: [], total: 0 })
      return
    }

    const d: any = resp.data
    const rows = d.content as any[]
    let overall = d.meta?.totalElements ?? d.totalElements
    if (overall == null || overall < 0) overall = undefined
    let filtered = d.meta?.filteredElements
    if (filtered == null || filtered < 0) filtered = overall as number | undefined
    if (filtered == null || filtered < 0) filtered = rows.length

    const nextCursor = d.meta?.nextCursor ?? null
    cursorHasNext.value = !!d.meta?.hasNext
    ensureCursorArraysSize()
    cursorNextTokens.value[cursorCurrentIndex.value] = nextCursor
    cursorPagesData.value[cursorCurrentIndex.value] = rows
    internalData.value = rows
    internalTotal.value = filtered

    // ✅ FIX2: baselineTotal SIEMPRE que el backend lo traiga
    updateBaselineTotal(overall)

    refreshSelectionWith(rows)
    emit('load', { rows, total: filtered })
  } catch (e) {
    window.$logger?.error('[CustomDataTable] tail load error', e)
    internalData.value = []
    internalTotal.value = 0
    emit('load', { rows: [], total: 0 })
  } finally {
    if (shouldShowGlobal) uiStore.hide()
  }
}

async function goEnd() {
  if (!isCursor.value) return
  if (usingStore.value) {
    const s = props.store as ListingStoreLike | undefined
    if (!s || !s.meta?.hasNext) return
    cursorCurrentIndex.value = 0
    await s.fetchFirstCursorPage?.(internalRows.value)
    syncFromStore(s)
    let guard = 0
    while (s.meta?.hasNext && guard < 1000) {
      cursorCurrentIndex.value++
      await s.fetchNextCursorPage?.(internalRows.value)
      guard++
    }
    syncFromStore(s)
    return
  }
  cursorCurrentIndex.value = Math.max(0, approxPages.value - 1)
  cursorCurrentToken.value = null
  await loadTail()
}

// ===== Counts =====
const hasFiltersActive = computed(() => hasActiveFilters())
const displayTotal = computed<number>(() => {
  const base = baselineTotal.value ?? internalTotal.value ?? 0
  return base < 0 ? resultsCount.value : base
})
const filteredTotal = computed<number>(() => {
  const raw = hasFiltersActive.value ? internalTotal.value : displayTotal.value
  if (raw == null || raw < 0) return resultsCount.value
  return raw
})
const resultsCount = computed<number>(() => (internalData.value ? internalData.value.length : 0))
const approxPages = computed(() => {
  const total = Number(filteredTotal.value ?? 0)
  const baseSize = props.query?.size ?? props.rows ?? internalRows.value
  const pageSize = Number(baseSize || 1)
  if (pageSize <= 0) return 1
  return Math.max(1, Math.ceil(total / pageSize))
})

function clearAllFilters() {
  clearAllFiltersBound()
  if (isCursor.value) resetCursorState()
  else internalPage.value = 0
}

// ===== List options watchers =====
function mapListOptionsFromItems(items: any[], col: ColumnDef) {
  const labelKey = col.optionLabelField || 'name'
  const valueKey = col.optionValueField || 'id'
  const mapped = (Array.isArray(items) ? items : []).map((it: any) => {
    if (it && it.label !== undefined && it.value !== undefined) return { label: it.label, value: it.value }
    return { label: it?.[labelKey], value: it?.[valueKey] }
  })
  if (col.includeAllOption) mapped.unshift({ label: col.includeAllLabel || 'Todos', value: null })
  return mapped
}

const optionItemsPrevSig = new Map<string, string>()
const optionItemsStops = new Map<string, () => void>()

function rebuildOptionItemsWatchers() {
  optionItemsStops.forEach(stop => { try { stop() } catch {} })
  optionItemsStops.clear()

  for (const col of props.columns) {
    if (col.type === 'list' && col.filter === true && typeof col.optionItemsProvider === 'function') {
      const stop = watch(
          () => (col.optionItemsProvider ? col.optionItemsProvider() : null),
          items => {
            if (!items) return
            const mapped = mapListOptionsFromItems(items as any[], col)
            const sig = JSON.stringify(mapped.map(o => [o.label, o.value]))
            if (sig === optionItemsPrevSig.get(col.field)) return
            nextTick(() => {
              col.filterOptions = mapped
              optionItemsPrevSig.set(col.field, sig)
            })
          },
          { immediate: true },
      )
      optionItemsStops.set(col.field, stop)
    }
  }
}

// ===== PrimeVue events =====
function onInternalSort(e: DataTableSortEvent) {
  const sf = (e.sortField ?? null) as string | null
  const soRaw = e.sortOrder ?? 1
  const so = soRaw === -1 ? -1 : 1
  sortField.value = sf
  sortOrder.value = so
  if (isCursor.value) resetCursorState()
  emit('sort', { sortField: sortField.value, sortOrder: sortOrder.value })
  void loadIfProvider()
}

function onInternalPage(e: { first: number; rows: number }) {
  if (isCursor.value) return
  internalPage.value = Math.floor(e.first / e.rows)
  internalRows.value = e.rows
  emit('page', e)
  void loadIfProvider()
}

/** ✅ ESTE es el evento bueno: entrar en fila */
function onInternalRowClick(e: any) {
  const row = e?.data
  if (!row) return
  emit('row-click', { row, originalEvent: e?.originalEvent ?? e })
}

onMounted(async () => {
  await ensureListOptions(props.columns)
  if (props.dataProvider || props.store) await loadIfProvider()
  rebuildOptionItemsWatchers()
})

watch(
    () => props.query,
    async q => {
      if (!q || isEmittingQuery) {
        isEmittingQuery = false
        return
      }
      isSyncingFromQuery = true
      internalRows.value = q.size ?? internalRows.value ?? (props.rows || 10)
      sortField.value = q.sortBy ?? null
      sortOrder.value = (q.sortDir ?? 'asc') === 'asc' ? 1 : -1
      applyFiltersFromQuery(props.columns, q, filters as any)
      prevFilterSignature = buildFilterSignature()
      if (isCursor.value) resetCursorState()
      else internalPage.value = q.page ?? 0
      await loadIfProvider()
      isSyncingFromQuery = false
    },
    { deep: true },
)

watch(
    filters,
    () => {
      if (isSyncingFromQuery) return
      const newSig = buildFilterSignature()
      if (newSig === prevFilterSignature) return
      prevFilterSignature = newSig
      const q = buildQueryParams()
      isEmittingQuery = true
      emit('update:query', q)
      void loadIfProvider()
    },
    { deep: true },
)

watch(
    () => props.refreshKey,
    () => {
      if (isCursor.value) resetCursorState()
      else internalPage.value = 0
      void loadIfProvider()
    },
)

/** ✅ NUEVO: cuando cambie baseFilter recargamos y volvemos a page 0 */
watch(
    () => props.baseFilter,
    () => {
      baselineTotal.value = null // ✅ FIX2: evita baseline “fantasma” de otro contexto
      if (isCursor.value) resetCursorState()
      else internalPage.value = 0
      void loadIfProvider()
    },
    { deep: true },
)

function resolveIdIconClass(col: ColumnDef, row: any): string {
  const val = col.idIconClass
  if (typeof val === 'function') {
    try {
      return val(row) || 'pi-lock'
    } catch {
      return 'pi-lock'
    }
  }
  return val || 'pi-lock'
}
</script>

<style scoped>
.cdt { position: relative; }
/* UX: indicar que la fila es clicable */
:deep(.p-datatable-tbody > tr) { cursor: pointer; }
.cdt-filter-cell { display: flex; flex-direction: column; gap: .25rem; }
.cdt-actions { display: flex; gap: .4rem; justify-content: center; }
.cdt-select-cell { display: flex; justify-content: center; align-items: center; }
.cdt-id-cell { position: relative; display: flex; justify-content: center; align-items: center; }
.cdt-id-icon { font-size: 1rem; opacity: .8; }
/* Colores específicos para iconos de origen */
.cdt-id-icon.text-green-500 { color: #22c55e; }
.cdt-id-icon.text-red-500 { color: #ef4444; }
.cdt-id-tag { position: absolute; top: 100%; margin-top: .25rem; opacity: 0; pointer-events: none; transition: opacity .15s ease-in-out; white-space: nowrap; z-index: 10; }
.cdt-id-cell:hover .cdt-id-tag { opacity: 1; }
.cdt-cursor-pager { display: flex; gap: .5rem; align-items: center; flex-wrap: wrap; padding: .5rem .25rem; }
.cdt-cursor-info { font-size: .7rem; opacity: .7; }
</style>
