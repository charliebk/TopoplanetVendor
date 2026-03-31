export const SUPPORTED_UI_LOCALES = ['es', 'pt', 'en'] as const

export type UiLocale = (typeof SUPPORTED_UI_LOCALES)[number]

export const isSupportedUiLocale = (value: unknown): value is UiLocale =>
  typeof value === 'string' &&
  (SUPPORTED_UI_LOCALES as readonly string[]).includes(value)

export const SUPPORTED_THEME_MODES = ['light', 'dark'] as const

export type UiThemeMode = (typeof SUPPORTED_THEME_MODES)[number]

export interface PersistedUiPreferences {
  locale: UiLocale
  themeMode: UiThemeMode
  currentProjectId: number | null
}
