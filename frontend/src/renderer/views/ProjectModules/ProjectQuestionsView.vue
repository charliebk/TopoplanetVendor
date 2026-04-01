<template>
  <ProjectModuleShell
    :title="t('ProjectMain.modules.questionsTitle')"
    :project-code="currentProjectCode"
    :project-description="currentProjectDescription"
  >
    <div class="project-module-placeholder">
      {{ t('ProjectMain.modules.questionsDescription') }}
    </div>
  </ProjectModuleShell>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ProjectModuleShell from './ProjectModuleShell.vue'
import { useProjectModulePage } from './useProjectModulePage'

const { t } = useI18n()
const { currentProjectCode, currentProjectDescription } = useProjectModulePage()
</script>

<style scoped>
.project-module-placeholder {
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
