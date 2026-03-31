<template>
  <section class="project-hub app-page">
    <div class="project-hub__shell app-page__shell">
      <div class="project-hub__intro">
        <div class="project-hub__brand">
          <img
            :src="appLogoSrc"
            alt="Application logo"
            class="project-hub__logo app-state-art"
          />
          <div>
            <p class="project-hub__eyebrow app-page__eyebrow">
              {{ appNameLabel }}
            </p>
            <h1 class="project-hub__title app-page__title">
              {{ t('MainView.hero.title') }}
            </h1>
            <p class="project-hub__subtitle app-page__subtitle">
              {{ t('MainView.hero.subtitle') }}
            </p>
          </div>
        </div>
        <AppToolbarControls
          :app-version="appVersion"
          class="project-hub__toolbar"
        />
      </div>

      <div class="project-hub__grid app-grid-two-columns">
        <div class="project-hub__main-column app-stack">
          <PrimeCard class="project-panel app-panel project-panel--start">
            <template #title>
              <span>
                {{ startSectionTitle }}
              </span>
            </template>
            <template #subtitle>
              {{ t('MainView.sections.startSubtitle') }}
            </template>
            <template #content>
              <div class="project-panel__menu app-list">
                <button
                  v-for="action in startActions"
                  :key="action.id"
                  type="button"
                  class="project-list-item app-list-item project-list-item--action"
                >
                  <span class="project-list-item__main app-list-item__main">
                    <i
                      :class="[
                        'pi',
                        action.icon,
                        'project-list-item__icon',
                        'app-icon-muted'
                      ]"
                      aria-hidden="true"
                    ></i>
                    <span>
                      <span
                        class="project-list-item__title app-list-item__title"
                      >
                        {{ action.label }}
                      </span>
                      <span class="project-list-item__meta app-list-item__meta">
                        {{ action.helper }}
                      </span>
                    </span>
                  </span>
                  <i
                    class="pi pi-angle-right project-list-item__arrow app-icon-muted"
                    aria-hidden="true"
                  ></i>
                </button>
              </div>

              <PrimeDivider />

              <div class="project-panel__section-header app-section-heading">
                <h2>{{ t('MainView.sections.projectsTitle') }}</h2>
                <PrimeTag
                  :value="
                    t('MainView.sections.projectsCount', {
                      count: projects.length
                    })
                  "
                  severity="success"
                />
              </div>

              <div class="project-panel__list app-list">
                <div
                  v-if="isLoadingProjects"
                  class="project-panel__feedback app-copy app-copy--muted"
                >
                  {{ t('MainView.projects.loading') }}
                </div>
                <div
                  v-else-if="projectsLoadError"
                  class="project-panel__feedback app-copy app-copy--muted"
                >
                  {{ t('MainView.projects.loadError') }}
                </div>
                <div
                  v-else-if="projects.length === 0"
                  class="project-panel__feedback app-copy app-copy--muted"
                >
                  {{ t('MainView.projects.empty') }}
                </div>
                <button
                  v-for="project in projects"
                  :key="project.id"
                  type="button"
                  class="project-list-item app-list-item"
                  @click="openProject(project.id)"
                >
                  <span>
                    <span class="project-list-item__title app-list-item__title">
                      {{ project.name }}
                    </span>
                    <span class="project-list-item__meta app-list-item__meta">
                      {{ project.code }} · {{ project.description }}
                    </span>
                  </span>
                  <PrimeTag
                    :value="project.status"
                    :severity="project.statusSeverity"
                  />
                </button>
              </div>
            </template>
          </PrimeCard>
        </div>
        <div class="project-hub__side-column app-stack">
          <PrimeCard class="project-panel app-panel tutorial-panel">
            <template #title>
              <span>
                {{ tutorialSectionTitle }}
              </span>
            </template>
            <template #subtitle>
              {{ t('MainView.sections.tutorialsSubtitle') }}
            </template>
            <template #content>
              <div class="tutorial-list app-list">
                <article
                  v-for="tutorial in tutorials"
                  :key="tutorial.id"
                  class="tutorial-card"
                >
                  <div class="tutorial-card__header">
                    <PrimeTag
                      :value="tutorial.level"
                      :severity="tutorial.severity"
                    />
                    <span class="tutorial-card__duration">
                      {{ tutorial.duration }}
                    </span>
                  </div>
                  <h2>{{ tutorial.title }}</h2>
                  <p>{{ tutorial.description }}</p>
                  <PrimeButton
                    :label="tutorial.cta"
                    icon="pi pi-play"
                    text
                    class="tutorial-card__button"
                  />
                </article>
              </div>
            </template>
          </PrimeCard>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  useCoreProjectStore,
  usePreferencesStore
} from '@/renderer/stores/stores.exports'
import { RENDERER_ASSETS } from '@/renderer/utils'
import { APP_BRANDING } from '@/shared/branding'
import AppToolbarControls from '@/renderer/components/common/AppToolbarControls.vue'

type TagSeverity =
  | 'success'
  | 'info'
  | 'warning'
  | 'danger'
  | 'secondary'
  | 'contrast'

interface StartAction {
  id: string
  label: string
  helper: string
  icon: string
}

interface ProjectListItem {
  id: number
  code: string
  name: string
  description: string
  status: string
  statusSeverity: Exclude<TagSeverity, 'contrast'>
}

interface TutorialItem {
  id: number
  title: string
  description: string
  level: string
  duration: string
  cta: string
  severity: Exclude<TagSeverity, 'contrast'>
}

const appVersion = ref('0.0.0')
const router = useRouter()
const coreProjectStore = useCoreProjectStore()
const preferencesStore = usePreferencesStore()
const appLogoSrc = RENDERER_ASSETS.logo
const appNameLabel = APP_BRANDING.applicationName
const { t } = useI18n()
const isLoadingProjects = ref(false)
const projectsLoadError = ref<string | null>(null)
const projectItems = ref<ProjectListItem[]>([])
const startSectionTitle = computed(() => t('MainView.sections.startTitle'))
const tutorialSectionTitle = computed(() =>
  t('MainView.sections.tutorialsTitle')
)

const startActions = computed<StartAction[]>(() => [
  {
    id: 'add-project',
    label: t('MainView.actions.addProject'),
    helper: t('MainView.actions.addProjectHelper'),
    icon: 'pi-plus-circle'
  },
  {
    id: 'export-project',
    label: t('MainView.actions.export'),
    helper: t('MainView.actions.exportHelper'),
    icon: 'pi-upload'
  },
  {
    id: 'import-project',
    label: t('MainView.actions.import'),
    helper: t('MainView.actions.importHelper'),
    icon: 'pi-download'
  }
])

const projects = computed<ProjectListItem[]>(() => projectItems.value)

const tutorials = computed<TutorialItem[]>(() => [
  {
    id: 1,
    title: t('MainView.tutorials.gettingStartedTitle'),
    description: t('MainView.tutorials.gettingStartedDescription'),
    level: t('MainView.tutorials.beginner'),
    duration: '4 min',
    cta: t('MainView.tutorials.openTutorial'),
    severity: 'success'
  },
  {
    id: 2,
    title: t('MainView.tutorials.importTitle'),
    description: t('MainView.tutorials.importDescription'),
    level: t('MainView.tutorials.operations'),
    duration: '6 min',
    cta: t('MainView.tutorials.viewFlow'),
    severity: 'info'
  },
  {
    id: 3,
    title: t('MainView.tutorials.workflowTitle'),
    description: t('MainView.tutorials.workflowDescription'),
    level: t('MainView.tutorials.model'),
    duration: '7 min',
    cta: t('MainView.tutorials.exploreGuide'),
    severity: 'warning'
  }
])

const getApplicationVersionFromMainProcess = (): void => {
  window.mainApi.invoke('msgRequestGetVersion').then((result: string) => {
    appVersion.value = result
  })
}

const loadProjects = async (): Promise<void> => {
  isLoadingProjects.value = true
  projectsLoadError.value = null

  try {
    const response = await coreProjectStore.listCoreProjects()
    projectItems.value = response.map((project) => ({
      id: project.id,
      code: project.code,
      name: project.name,
      description: project.description ?? t('MainView.projects.noDescription'),
      status: t('MainView.projects.active'),
      statusSeverity: 'success'
    }))
  } catch (error) {
    console.error('Failed to load core projects', error)
    projectsLoadError.value = t('MainView.projects.loadError')
    projectItems.value = []
  } finally {
    isLoadingProjects.value = false
  }
}

const openProject = async (projectId: number): Promise<void> => {
  preferencesStore.setCurrentProjectId(projectId)
  await router.push('/project')
}

onMounted((): void => {
  getApplicationVersionFromMainProcess()
  void loadProjects()
})
</script>

<style scoped>
.project-hub {
  background:
    radial-gradient(
      circle at top left,
      var(--app-page-glow-top),
      transparent 30%
    ),
    radial-gradient(
      circle at bottom right,
      var(--app-page-glow-bottom),
      transparent 28%
    ),
    linear-gradient(
      180deg,
      var(--app-page-gradient-top) 0%,
      var(--app-page-gradient-bottom) 100%
    );
}

.project-hub__intro {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: var(--app-space-6);
}

.project-hub__brand {
  display: flex;
  align-items: flex-start;
  gap: var(--app-space-4);
}

.project-hub__toolbar {
  justify-content: flex-end;
}

.project-hub__subtitle {
  max-width: 760px;
}

.project-panel--start {
  background: linear-gradient(
    180deg,
    var(--app-surface-strong),
    var(--app-surface-soft)
  );
}

.tutorial-panel {
  background: var(--app-surface);
}

.project-panel__menu,
.project-panel__list,
.tutorial-list {
  gap: 0.45rem;
}

.project-list-item {
  background: var(--app-surface-soft);
}

.project-panel__feedback {
  padding: var(--app-space-4);
  border: 1px dashed var(--app-border-strong);
  border-radius: var(--app-radius-block);
  background: var(--app-surface);
}

.project-list-item--action {
  background: linear-gradient(
    180deg,
    var(--app-surface-soft) 0%,
    var(--app-surface-strong) 100%
  );
}

.project-list-item__icon,
.project-list-item__arrow {
  font-size: 1.1rem;
}

.tutorial-card {
  padding: 1rem 1rem 0.9rem;
  border: 1px solid var(--app-border-strong);
  border-radius: 22px;
  background: linear-gradient(
    180deg,
    var(--app-surface-soft) 0%,
    var(--app-surface-strong) 100%
  );
}

.tutorial-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--app-space-3);
  margin-bottom: 0.7rem;
}

.tutorial-card__duration {
  font-size: var(--app-text-sm);
  color: var(--app-text-muted);
}

.tutorial-card h2 {
  margin: 0 0 0.45rem;
  font-size: 1rem;
  color: var(--app-text-strong);
}

.tutorial-card p {
  margin: 0 0 0.45rem;
  font-size: var(--app-text-sm);
  line-height: var(--app-line-relaxed);
  color: var(--app-text);
}

:deep(.tutorial-card__button.p-button) {
  padding-left: 0;
  font-size: var(--app-text-sm);
}

@media (max-width: 1080px) {
  .project-hub__subtitle {
    max-width: none;
  }
}

@media (max-width: 640px) {
  .project-hub__intro {
    flex-direction: column;
  }

  .project-hub__brand {
    flex-direction: column;
  }

  .project-hub__toolbar {
    width: 100%;
    justify-content: flex-start;
  }

  .tutorial-card__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
