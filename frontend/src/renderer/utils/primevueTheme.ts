import type { UiThemeMode } from '@/renderer/stores/preferences/preferences.types'

export const PRIMEVUE_THEME_LINK_ID = 'theme-link'

const PRIMEVUE_THEME_NAME_BY_MODE: Record<UiThemeMode, string> = {
  light: 'aura-light-green',
  dark: 'aura-dark-green'
}

const PRIMEVUE_THEME_PATH_BY_MODE: Record<UiThemeMode, string> = {
  light: '/themes/aura-light-green/theme.css',
  dark: '/themes/aura-dark-green/theme.css'
}

interface PrimeVueRuntime {
  changeTheme?: (
    currentTheme: string,
    newTheme: string,
    linkElementId: string,
    callback?: () => void
  ) => void
}

const updateThemeLink = (mode: UiThemeMode): void => {
  const themeLinkElement = document.getElementById(
    PRIMEVUE_THEME_LINK_ID
  ) as HTMLLinkElement | null

  if (!themeLinkElement) {
    return
  }

  themeLinkElement.href = PRIMEVUE_THEME_PATH_BY_MODE[mode]
}

export const applyPrimeVueTheme = (
  nextThemeMode: UiThemeMode,
  previousThemeMode?: UiThemeMode,
  primeVue?: PrimeVueRuntime
): void => {
  if (!previousThemeMode || !primeVue?.changeTheme) {
    updateThemeLink(nextThemeMode)
    return
  }

  primeVue.changeTheme(
    PRIMEVUE_THEME_NAME_BY_MODE[previousThemeMode],
    PRIMEVUE_THEME_NAME_BY_MODE[nextThemeMode],
    PRIMEVUE_THEME_LINK_ID,
    () => updateThemeLink(nextThemeMode)
  )
}
