export interface GenericDataTableLocale {
  emptyMessage: string
  loadingMessage: string
  errorMessage: string
  globalFilterPlaceholder: string
  clearFiltersLabel: string
  refreshLabel: string
  selectPageLabel: string
  selectFilteredLabel: string
  clearSelectionLabel: string
  retryLabel: string
  retryAllLabel: string
  booleanTrueLabel: string
  booleanFalseLabel: string
  selectVisibleRowsAriaLabel: string
  selectRowAriaPrefix: string
  selectFilteredProviderModeReason: string
  selectFilteredResolveDisabledReason: string
  selectFilteredUnavailableReason: string
  countBarClearFiltersLabel: string
  countBarTotalLabel: string
  countBarResultsLabel: string
  countBarShownLabel: string
  countBarExcludedLabel: string
}

export const GENERIC_DATA_TABLE_LOCALE: GenericDataTableLocale = {
  emptyMessage: 'No data available',
  loadingMessage: 'Loading data...',
  errorMessage: 'Failed to load data',
  globalFilterPlaceholder: 'Filter all columns',
  clearFiltersLabel: 'Clear filters',
  refreshLabel: 'Refresh',
  selectPageLabel: 'Select page',
  selectFilteredLabel: 'Select filtered',
  clearSelectionLabel: 'Clear selection',
  retryLabel: 'Retry',
  retryAllLabel: 'Retry all',
  booleanTrueLabel: 'Yes',
  booleanFalseLabel: 'No',
  selectVisibleRowsAriaLabel: 'Select visible rows',
  selectRowAriaPrefix: 'Select',
  selectFilteredProviderModeReason:
    'Select filtered requires disabled rows resolved for the full filtered result in provider mode.',
  selectFilteredResolveDisabledReason:
    'Select filtered is blocked until disabledFilteredRowsResolved is true.',
  selectFilteredUnavailableReason:
    'Select filtered is not available for the current selection configuration.',
  countBarClearFiltersLabel: 'Clear filters',
  countBarTotalLabel: 'Total',
  countBarResultsLabel: 'Results',
  countBarShownLabel: 'Shown',
  countBarExcludedLabel: 'Excluded'
}

export const formatOptionSourceFailureLabel = (count: number): string => {
  return `${count} filter option source${count === 1 ? '' : 's'} failed`
}

export const formatSelectRowAriaLabel = (rowLabel: string): string => {
  return `${GENERIC_DATA_TABLE_LOCALE.selectRowAriaPrefix} ${rowLabel}`
}
