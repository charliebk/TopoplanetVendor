import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import type { CoreProjectResponse } from '@/renderer/stores/stores.exports'

export const useProjectModulePage = () => {
  const router = useRouter()
  const { t } = useI18n()
  const coreProjectStore = useCoreProjectStore()
  const preferencesStore = usePreferencesStore()
  const currentProject = ref<CoreProjectResponse | null>(null)

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

  return {
    currentProject,
    currentProjectCode,
    currentProjectDescription,
    loadCurrentProject
  }
}
