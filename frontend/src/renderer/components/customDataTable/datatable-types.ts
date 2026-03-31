/**
 * datatable-types.ts
 * -------------------------------------------------------
 * Tipos base para el sistema de tablas reutilizable (CustomDataTable / CustomDataTable).
 *
 * Objetivos principales:
 * - Definir la forma de las columnas (ColumnDef) con soporte para filtros, orden, exportación y tipos especiales.
 * - Unificar el contrato de la query (TableQuery) para paginación/orden/filtros y exportaciones.
 * - Estandarizar acciones por fila y selección avanzada.
 *
 * Notas sobre columnas de tipo lista:
 * 1) filterOptions: lista estática ya mapeada [{ label, value }]. Úsala si el set es pequeño y fijo.
 * 2) optionItemsProvider: () => any[] (RECOMENDADO). Devuelves los items crudos (ya cargados por el store).
 *    La tabla los mapea automáticamente usando optionLabelField / optionValueField y añade la opción "Todos" si includeAllOption.
 *
 * Migración completada: se eliminó soporte legacy optionProvider.
 */

export type MatchMode = 'contains' | 'equals' | 'startsWith' | 'endsWith'
export type ColumnType = 'idIcon' | 'string' | 'number' | 'integer' | 'date' | 'boolean' | 'list' | 'actions' | 'select' | 'percent'

/** Acción declarativa para columnas de tipo 'actions'. */
export interface ColumnAction {
  key: string
  icon?: string
  label?: string
  tooltip?: string
  class?: string
  disabled?: boolean | ((row: any) => boolean)
}

/**
 * ColumnDef
 * -------------------------------------------------------
 * Definición de una columna de la tabla.
 * Agrupadas por bloques: base, filtros, listas, visualización, booleano, exportación, transformaciones.
 */
export interface ColumnDef {
  /** Clave primaria de la columna (campo en el row). */
  field: string
  /** Texto de cabecera. */
  header: string
  /** Tipo de dato / renderizado. */
  type?: ColumnType
  /** Permite ordenar. */
  sortable?: boolean
  /** Activa filtro (renderiza control en cabecera si filterDisplay='row'). */
  filter?: boolean
  /** Modo de comparación del filtro de texto/número. */
  matchMode?: MatchMode
  /** Alineación de celdas. */
  align?: 'left' | 'center' | 'right'
  /** Ancho fijo opcional. */
  width?: string
  /** Ancho mínimo. */
  minWidth?: string
  /** Ancho máximo. */
  maxWidth?: string
  /** Nombre del campo usado en backend (si difiere de field). */
  backendField?: string

  /** Nº de decimales para formateo en pantalla si type==='number'. */
  decimals?: number

  // === Filtros tipo lista (List) ===
  /** Opciones ya mapeadas (estáticas) para el filtro tipo lista. */
  filterOptions?: Array<{ label: string; value: any }>
  /** Campo del item que se usará como etiqueta (por defecto 'name'). */
  optionLabelField?: string
  /** Campo del item que se usará como valor (por defecto 'id'). */
  optionValueField?: string
  /** Incluir opción global (Todos) al inicio. */
  includeAllOption?: boolean
  /** Texto personalizado para la opción global. */
  includeAllLabel?: string
  /** Transformación personalizada de un item crudo a {label,value}. */
  optionTransform?: (raw: any, col: ColumnDef) => { label: any; value: any }
  /** Proveedor RECOMENDADO de items crudos ya cargados. */
  optionItemsProvider?: () => any[]

  // === Visualización ===
  /** Campo alternativo para mostrar (si difiere del field original). */
  displayField?: string
  /** Acciones por fila (solo si type='actions'). */
  actions?: ColumnAction[]

  // === Booleano ===
  /** Etiquetas para los valores booleanos (true/false/null). */
  booleanLabels?: { trueLabel?: string; falseLabel?: string; nullLabel?: string }
  /** Mostrar Tag PrimeVue en vez de texto simple. */
  booleanTag?: boolean
  /** Severidad de Tag según valor. */
  booleanTagSeverity?: { true?: string; false?: string; null?: string }

  // === Exportación / Impresión ===
  /** Incluida en exportación (por defecto true salvo acciones). */
  exportable?: boolean
  /** Texto de cabecera en export (por defecto header). */
  exportHeader?: string
  /** Clave de la propiedad a exportar (por defecto displayField || field). */
  exportKey?: string
  /** Función de formateo del valor al exportar. */
  exportFormat?: (value: any, row: any) => any

  // === Transformaciones de filtros ===
  /** Transforma el valor del filtro antes de enviar al backend. */
  paramTransform?: (value: any) => any

  /** Campo alternativo para mostrar en el Tag de idIcon (ej: originTooltip) */
  tooltipField?: string
  /** Clase del icono cuando type='idIcon'. Puede ser string fijo o función que recibe la fila. */
  idIconClass?: string | ((row: any) => string)
}

// === Proveedor de datos para DataTable (resultado normalizado) ===
export interface ProviderOk { rows: any[]; total: number; overallTotal?: number }
export interface ProviderFail { ok: false; problem: any }
export type ProviderResult =
  | ProviderOk
  | ProviderFail
  | { ok: true; data: { data: any[]; total: number; overallTotal?: number } }

/** Modelo interno de filtro (PrimeVue DataTable). */
export interface FilterModel { value: any; matchMode: MatchMode }

/**
 * TableQuery
 * -------------------------------------------------------
 * Contrato unificado de consulta de la tabla:
 * - page / size: paginación
 * - sortBy / sortDir: orden
 * - resto de campos: filtros dinámicos planos (name, enabled, mapTypeId, etc.)
 */
export interface TableQuery {
  page: number
  size: number
  sortBy?: string | null
  sortDir?: 'asc' | 'desc'
  [key: string]: any // filtros dinámicos
}

/** Payload de cambio de selección avanzada (selectAllFiltered + overrides). */
export type SelectionChangePayload = {
  allFiltered: boolean
  rows: any[]
}
