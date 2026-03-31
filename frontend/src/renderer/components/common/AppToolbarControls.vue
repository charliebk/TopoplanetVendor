<template>
  <div class="app-toolbar-controls">
    <button
      type="button"
      class="app-toolbar-controls__theme-button"
      :aria-label="themeToggleLabel"
      @click="toggleTheme"
    >
      <span class="app-toolbar-controls__theme-pill">
        <i
          :class="['pi', themeToggleIcon, 'app-toolbar-controls__theme-icon']"
          aria-hidden="true"
        ></i>
      </span>
    </button>

    <PrimeSelectButton
      v-model="selectedLocale"
      :options="languageOptions"
      option-label="label"
      option-value="value"
      :aria-label="t('MainView.toolbar.language')"
      class="app-toolbar-controls__language-toggle"
      @update:model-value="handleLocaleChange"
    />

    <PrimeTag
      :value="`v${appVersion}`"
      severity="contrast"
      rounded
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  usePreferencesStore,
  type UiLocale
} from '@/renderer/stores/stores.exports'
import { isSupportedUiLocale } from '@/renderer/stores/preferences/preferences.types'

defineProps<{
  appVersion: string
}>()

const preferencesStore = usePreferencesStore()
const { t } = useI18n()

const languageOptions: Array<{ label: string; value: UiLocale }> = [
  {
    label: 'ES',
    value: 'es'
  },
  {
    label: 'PT',
    value: 'pt'
  },
  {
    label: 'EN',
    value: 'en'
  }
]

const selectedLocale = computed<UiLocale>({
  get: () => preferencesStore.locale,
  set: (nextLocale) => {
    preferencesStore.setLocale(nextLocale)
  }
})

const isDarkTheme = computed(() => preferencesStore.themeMode === 'dark')
const themeToggleLabel = computed(() =>
  isDarkTheme.value
    ? t('MainView.toolbar.themeLight')
    : t('MainView.toolbar.themeDark')
)
const themeToggleIcon = computed(() =>
  isDarkTheme.value ? 'pi-sun' : 'pi-moon'
)

const toggleTheme = (): void => {
  preferencesStore.setThemeMode(isDarkTheme.value ? 'light' : 'dark')
}

const handleLocaleChange = (
  nextLocale: UiLocale | { value?: UiLocale }
): void => {
  const normalizedLocale =
    typeof nextLocale === 'object' && nextLocale !== null
      ? nextLocale.value
      : nextLocale

  if (!isSupportedUiLocale(normalizedLocale)) {
    return
  }

  preferencesStore.setLocale(normalizedLocale)
}
</script>

<style scoped>
.app-toolbar-controls {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: nowrap;
  gap: var(--app-space-2);
  white-space: nowrap;
}

.app-toolbar-controls__theme-button {
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.app-toolbar-controls__theme-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.9rem;
  min-height: 1.6rem;
  padding: 0.2rem 0.45rem;
  border: 1px solid var(--app-border-strong);
  border-radius: 999px;
  background: var(--app-surface-soft);
  color: var(--app-accent);
  font-size: var(--app-text-xs);
  font-weight: 700;
  letter-spacing: 0.01em;
  transition:
    border-color 160ms ease,
    background-color 160ms ease,
    transform 160ms ease,
    color 160ms ease;
}

.app-toolbar-controls__theme-button:hover .app-toolbar-controls__theme-pill {
  transform: translateY(-1px);
  border-color: color-mix(
    in srgb,
    var(--app-border-strong) 65%,
    var(--app-accent) 35%
  );
  background: var(--app-accent-soft);
}

.app-toolbar-controls__theme-icon {
  font-size: 0.78rem;
}

.app-toolbar-controls__language-toggle {
  align-self: center;
  flex: 0 0 auto;
}

:deep(.app-toolbar-controls__language-toggle .p-selectbutton) {
  display: inline-flex;
  flex-wrap: nowrap;
}

:deep(.app-toolbar-controls__language-toggle .p-button) {
  min-height: 1.8rem;
  padding: 0.25rem 0.58rem;
  font-size: var(--app-text-xs);
  font-weight: 700;
}

@media (max-width: 640px) {
  .app-toolbar-controls {
    justify-content: flex-start;
  }
}
</style>
