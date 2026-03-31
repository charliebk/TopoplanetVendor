<template>
  <PrimeMenubar
    :model="menuItems"
    class="app-menubar"
  >
    <template #start>
      <button
        type="button"
        class="app-menubar__brand"
        @click="router.push('/project')"
      >
        <img
          :src="appLogoSrc"
          :alt="APP_BRANDING.applicationName"
          class="app-menubar__brand-mark"
        />
        <span class="app-menubar__brand-name">
          {{ APP_BRANDING.applicationName }}
        </span>
      </button>
    </template>

    <template #end>
      <AppToolbarControls
        :app-version="appVersion"
        :is-visible-language="toolbarVisibility.language"
        :is-visible-theme="toolbarVisibility.theme"
        :is-visible-version="toolbarVisibility.version"
        :is-visible-close-project="toolbarVisibility.closeProject"
        @close-project="closeCurrentProject"
      />
    </template>
  </PrimeMenubar>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppToolbarControls from '@/renderer/components/common/AppToolbarControls.vue'
import { RENDERER_ASSETS } from '@/renderer/utils'
import { APP_BRANDING } from '@/shared/branding'
import { usePreferencesStore } from '@/renderer/stores/stores.exports'

interface ToolbarVisibilityOptions {
  language: boolean
  theme: boolean
  version: boolean
  closeProject: boolean
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const preferencesStore = usePreferencesStore()
const appVersion = ref('0.0.0')
const appLogoSrc = RENDERER_ASSETS.logo

const toolbarVisibility = computed<ToolbarVisibilityOptions>(() => {
  const routeOptions =
    (route.meta.toolbarVisibility as Partial<ToolbarVisibilityOptions>) ?? {}

  return {
    language: routeOptions.language ?? true,
    theme: routeOptions.theme ?? true,
    version: routeOptions.version ?? true,
    closeProject: routeOptions.closeProject ?? false
  }
})

const menuItems = computed(() => [
  {
    label: t('workspace.menus.project'),
    items: [
      {
        label: t('workspace.menus.dashboard'),
        icon: 'pi pi-home',
        command: () => router.push('/project')
      },
      {
        label: t('workspace.menus.logs'),
        icon: 'pi pi-list',
        command: () => router.push('/project/logs')
      },
      {
        label: t('workspace.menus.settings'),
        icon: 'pi pi-cog',
        command: () => router.push('/project/settings')
      }
    ]
  },
  {
    label: t('workspace.menus.modules'),
    items: [
      {
        label: t('workspace.menus.vendors'),
        icon: 'pi pi-briefcase',
        command: () => router.push('/project/vendors')
      },
      {
        label: t('workspace.menus.products'),
        icon: 'pi pi-box',
        command: () => router.push('/project/products')
      },
      {
        label: t('workspace.menus.questions'),
        icon: 'pi pi-question-circle',
        command: () => router.push('/project/questions')
      }
    ]
  }
])

const closeCurrentProject = async (): Promise<void> => {
  preferencesStore.setCurrentProjectId(null)
  await router.push('/')
}

const getApplicationVersionFromMainProcess = (): void => {
  window.mainApi.invoke('msgRequestGetVersion').then((result: string) => {
    appVersion.value = result
  })
}

onMounted((): void => {
  getApplicationVersionFromMainProcess()
})
</script>
