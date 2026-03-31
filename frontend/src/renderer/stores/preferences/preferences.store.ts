import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentLocale } from '@/renderer/utils'
import type {
  PersistedUiPreferences,
  UiLocale,
  UiThemeMode
} from './preferences.types'
import { isSupportedUiLocale } from './preferences.types'

const STORAGE_KEY = 'topoplanet-renderer-preferences'

const resolveLocale = (): UiLocale => {
  const currentLocale = getCurrentLocale()

  return isSupportedUiLocale(currentLocale) ? currentLocale : 'en'
}

const readPersistedPreferences = (): PersistedUiPreferences | null => {
  if (typeof window === 'undefined') {
    return null
  }

  const rawPreferences = window.localStorage.getItem(STORAGE_KEY)

  if (!rawPreferences) {
    return null
  }

  try {
    const parsedPreferences = JSON.parse(
      rawPreferences
    ) as Partial<PersistedUiPreferences>

    if (
      isSupportedUiLocale(parsedPreferences.locale) &&
      (parsedPreferences.themeMode === 'light' ||
        parsedPreferences.themeMode === 'dark')
    ) {
      return {
        locale: parsedPreferences.locale,
        themeMode: parsedPreferences.themeMode,
        currentProjectId:
          typeof parsedPreferences.currentProjectId === 'number'
            ? parsedPreferences.currentProjectId
            : null
      }
    }
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
  }

  return null
}

export const usePreferencesStore = defineStore('renderer-preferences', () => {
  const persistedPreferences = readPersistedPreferences()
  const locale = ref<UiLocale>(persistedPreferences?.locale ?? resolveLocale())
  const themeMode = ref<UiThemeMode>(persistedPreferences?.themeMode ?? 'light')
  const currentProjectId = ref<number | null>(
    persistedPreferences?.currentProjectId ?? null
  )

  const persistPreferences = (): void => {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        locale: locale.value,
        themeMode: themeMode.value,
        currentProjectId: currentProjectId.value
      })
    )
  }

  const setLocale = (nextLocale: unknown): void => {
    if (!isSupportedUiLocale(nextLocale)) {
      return
    }

    if (locale.value === nextLocale) {
      return
    }

    locale.value = nextLocale
    persistPreferences()
  }

  const setThemeMode = (nextThemeMode: UiThemeMode): void => {
    if (themeMode.value === nextThemeMode) {
      return
    }

    themeMode.value = nextThemeMode
    persistPreferences()
  }

  const setCurrentProjectId = (nextProjectId: number | null): void => {
    if (currentProjectId.value === nextProjectId) {
      return
    }

    currentProjectId.value = nextProjectId
    persistPreferences()
  }

  return {
    locale,
    themeMode,
    currentProjectId,
    setLocale,
    setThemeMode,
    setCurrentProjectId
  }
})
