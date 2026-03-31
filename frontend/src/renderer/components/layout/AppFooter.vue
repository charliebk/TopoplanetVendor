<template>
  <footer class="app-footer">
    <button
      v-if="footerVisibility.currentProject"
      type="button"
      class="app-footer__item"
      @click="router.push('/project')"
    >
      <i
        class="pi pi-folder-open"
        aria-hidden="true"
      ></i>
      <span>
        {{
          t('workspace.currentProject', {
            project: currentProjectLabel
          })
        }}
      </span>
    </button>
    <span
      v-if="footerVisibility.currentModule"
      class="app-footer__item"
    >
      <i
        class="pi pi-window-maximize"
        aria-hidden="true"
      ></i>
      <span>{{ currentModuleLabel }}</span>
    </span>
    <span
      v-if="footerVisibility.database"
      class="app-footer__item"
    >
      <i
        class="pi pi-database"
        aria-hidden="true"
      ></i>
      <span>{{ t('workspace.localDatabase') }}</span>
    </span>
    <span
      v-if="footerVisibility.status"
      class="app-footer__item app-footer__item--end"
    >
      <i
        class="pi pi-info-circle"
        aria-hidden="true"
      ></i>
      <span>{{ t('workspace.ready') }}</span>
    </span>
  </footer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import type { CoreProjectResponse } from '@/renderer/stores/stores.exports'

interface FooterVisibilityOptions {
  currentProject: boolean
  currentModule: boolean
  database: boolean
  status: boolean
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const currentProject = ref<CoreProjectResponse | null>(null)

const footerVisibility = computed<FooterVisibilityOptions>(() => {
  const routeOptions =
    (route.meta.footerVisibility as Partial<FooterVisibilityOptions>) ?? {}

  return {
    currentProject: routeOptions.currentProject ?? true,
    currentModule: routeOptions.currentModule ?? true,
    database: routeOptions.database ?? true,
    status: routeOptions.status ?? true
  }
})

const currentProjectLabel = computed(() => {
  if (currentProject.value) {
    return `${currentProject.value.code} · ${currentProject.value.name}`
  }

  if (preferencesStore.currentProjectId !== null) {
    return t('workspace.loadingProject')
  }

  return t('workspace.noProject')
})

const currentModuleLabel = computed(() => {
  const titleKey = (route.meta.titleKey || 'title.projectMain') as string
  return t(titleKey)
})

const loadCurrentProject = async (): Promise<void> => {
  const selectedProjectId = preferencesStore.currentProjectId

  if (selectedProjectId === null) {
    currentProject.value = null
    return
  }

  try {
    currentProject.value =
      await coreProjectStore.getCoreProjectById(selectedProjectId)
  } catch (error) {
    console.error('Failed to load current project for footer', error)
    currentProject.value = null
  }
}

onMounted((): void => {
  void loadCurrentProject()
})

watch(
  () => preferencesStore.currentProjectId,
  () => {
    void loadCurrentProject()
  }
)
</script>
