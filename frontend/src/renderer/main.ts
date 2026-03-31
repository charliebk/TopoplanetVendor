import { createApp, watch } from 'vue'

import App from '@/renderer/App.vue'
import '@/renderer/styles/global.css'
import router from '@/renderer/router'
import vuetify from '@/renderer/plugins/vuetify'
import primevue from '@/renderer/plugins/primevue'
import i18n from '@/renderer/plugins/i18n'
import pinia from '@/renderer/plugins/pinia'
import { usePreferencesStore } from '@/renderer/stores/stores.exports'
import type {
  UiLocale,
  UiThemeMode
} from '@/renderer/stores/preferences/preferences.types'
import { applyPrimeVueTheme } from '@/renderer/utils/primevueTheme'

// Add API key defined in contextBridge to window object type
declare global {
  interface Window {
    mainApi?: any
  }
}

const app = createApp(App)

app.use(vuetify).use(i18n).use(router).use(pinia)
app.use(primevue)

const preferencesStore = usePreferencesStore(pinia)

const applyLocalePreference = (nextLocale: UiLocale): void => {
  i18n.global.locale.value = nextLocale
  vuetify.locale.current.value = nextLocale
}

const applyThemePreference = (
  nextThemeMode: UiThemeMode,
  previousThemeMode?: UiThemeMode
): void => {
  vuetify.theme.change(nextThemeMode)

  applyPrimeVueTheme(
    nextThemeMode,
    previousThemeMode,
    app.config.globalProperties.$primevue as
      | {
          changeTheme?: (
            currentTheme: string,
            newTheme: string,
            linkElementId: string,
            callback?: () => void
          ) => void
        }
      | undefined
  )
}

applyLocalePreference(preferencesStore.locale)
applyThemePreference(preferencesStore.themeMode)

watch(
  () => preferencesStore.locale,
  (nextLocale) => {
    applyLocalePreference(nextLocale)
  }
)

watch(
  () => preferencesStore.themeMode,
  (nextThemeMode, previousThemeMode) => {
    applyThemePreference(nextThemeMode, previousThemeMode)
  }
)

app.mount('#app')
