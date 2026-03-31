<template>
  <section class="app-page project-module">
    <div class="app-page__shell project-module__shell app-stack">
      <PrimeCard class="app-panel project-module__panel">
        <template #title>
          <span>{{ t(titleKey) }}</span>
        </template>
        <template #subtitle>
          {{ currentProjectCode }}
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <p class="app-copy app-copy--muted">
              {{ currentProjectDescription }}
            </p>
            <div class="project-module__placeholder">
              {{ t(descriptionKey) }}
            </div>
          </div>
        </template>
      </PrimeCard>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import type { CoreProjectResponse } from '@/renderer/stores/stores.exports'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const currentProject = ref<CoreProjectResponse | null>(null)

const titleKey = computed(() => {
  return (route.meta.moduleTitleKey ||
    'ProjectMain.modules.defaultTitle') as string
})

const descriptionKey = computed(() => {
  return (route.meta.moduleDescriptionKey ||
    'ProjectMain.modules.defaultDescription') as string
})

const currentProjectCode = computed(() => {
  return currentProject.value?.code ?? t('ProjectMain.fallbackCode')
})

const currentProjectDescription = computed(() => {
  return (
    currentProject.value?.description ?? t('ProjectMain.fallbackDescription')
  )
})

const loadCurrentProject = async (): Promise<void> => {
  const selectedProjectId = preferencesStore.currentProjectId

  if (selectedProjectId === null) {
    await router.replace('/')
    return
  }

  try {
    currentProject.value =
      await coreProjectStore.getCoreProjectById(selectedProjectId)
  } catch (error) {
    console.error('Failed to load current project module', error)
    preferencesStore.setCurrentProjectId(null)
    await router.replace('/')
  }
}

onMounted((): void => {
  void loadCurrentProject()
})
</script>

<style scoped>
.project-module {
  background:
    radial-gradient(
      circle at top left,
      rgba(32, 97, 61, 0.08),
      transparent 26%
    ),
    linear-gradient(
      180deg,
      var(--app-page-gradient-top) 0%,
      var(--app-page-gradient-bottom) 100%
    );
}

.project-module__shell {
  gap: var(--app-space-5);
}

.project-module__panel {
  min-height: 280px;
}

.project-module__placeholder {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--app-border-strong);
  border-radius: var(--app-radius-panel);
  background: var(--app-surface-soft);
  color: var(--app-text-muted);
  text-align: center;
  padding: var(--app-space-6);
}
</style>
