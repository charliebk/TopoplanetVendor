import { ref, computed, type Ref } from 'vue'

export type SelectionOverride = {
    forced: 'selected' | 'unselected'
    row?: any
}

export interface SelectionChangePayload {
    allFiltered: boolean
    rows: any[]
    totalFiltered?: number
}

/**
 * useTableSelection
 * Gestiona la selección "híbrida":
 * - Selección manual (página actual).
 * - Selección masiva (Select All Filtered) con exclusiones.
 */
export function useTableSelection(
    internalData: Ref<any[]>,
    internalTotal: Ref<number>,
    rowKeyField: string = 'id',
    emit: (e: 'selection-change', payload: SelectionChangePayload) => void
) {

    const selectionAllFiltered = ref(false)
    const selectionOverrides = ref<Record<string, SelectionOverride>>({})

    function rowKeyOf(row: any): string {
        const val = row?.[rowKeyField]
        return String(val)
    }

    const selectedRows = computed<any[]>(() => {
        const out: any[] = []
        for (const ov of Object.values(selectionOverrides.value)) {
            if (ov.forced === 'selected' && ov.row) out.push(ov.row)
        }
        return out
    })

    function isRowSelected(row: any): boolean {
        const key = rowKeyOf(row)
        const ov = selectionOverrides.value[key]
        if (ov?.forced === 'selected') return true
        if (ov?.forced === 'unselected') return false
        return selectionAllFiltered.value
    }

    function emitSelectionChange() {
        const payload: SelectionChangePayload = {
            allFiltered: selectionAllFiltered.value,
            rows: selectedRows.value,
            totalFiltered: internalTotal.value,
        }
        emit('selection-change', payload)
    }

    function setRowSelected(row: any, checked: boolean) {
        const key = rowKeyOf(row)
        const copy = { ...selectionOverrides.value }

        if (!selectionAllFiltered.value) {
            if (checked) copy[key] = { forced: 'selected', row }
            else delete copy[key]
        } else {
            if (checked) {
                if (copy[key]?.forced === 'unselected') delete copy[key]
            } else {
                copy[key] = { forced: 'unselected', row }
            }
        }
        selectionOverrides.value = copy
        emitSelectionChange()
    }

    function selectAllPage() {
        const copy = { ...selectionOverrides.value }
        selectionAllFiltered.value = false
        for (const row of internalData.value || []) {
            const key = rowKeyOf(row)
            copy[key] = { forced: 'selected', row }
        }
        selectionOverrides.value = copy
        emitSelectionChange()
    }

    function selectAllFilteredFn() {
        selectionAllFiltered.value = true
        const copy: Record<string, SelectionOverride> = {}
        // Marcamos visualmente las visibles como seleccionadas
        for (const row of internalData.value || []) {
            const key = rowKeyOf(row)
            copy[key] = { forced: 'selected', row }
        }
        selectionOverrides.value = copy
        emitSelectionChange()
    }

    function deselectAll() {
        selectionAllFiltered.value = false
        selectionOverrides.value = {}
        emitSelectionChange()
    }

    function refreshSelectionWith(rows: any[]) {
        const copy = { ...selectionOverrides.value }
        let changed = false
        for (const r of rows) {
            const key = rowKeyOf(r)
            const ov = copy[key]
            if (ov && ov.forced === 'selected') {
                copy[key] = { ...ov, row: r }
                changed = true
            }
        }
        if (changed) selectionOverrides.value = copy
    }

    return {
        selectedRows,
        selectionAllFiltered,
        selectionOverrides,
        isRowSelected,
        setRowSelected,
        selectAllPage,
        selectAllFiltered: selectAllFilteredFn,
        deselectAll,
        refreshSelectionWith
    }
}
