<template>
  <section class="app-page project-module">
    <div class="app-page__shell project-module__shell app-stack">
      <PrimeCard class="app-panel project-module__panel">
        <template #title>
          <span>{{ title }}</span>
        </template>
        <template #subtitle>
          {{ projectCode }}
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <p class="app-copy app-copy--muted">
              {{ projectDescription }}
            </p>

            <slot></slot>

            <div
              v-if="interactionLog"
              class="project-module__log"
            >
              <span class="project-module__log-title">Last table event</span>
              <pre class="project-module__log-code">{{ interactionLog }}</pre>
            </div>
          </div>
        </template>
      </PrimeCard>
    </div>
  </section>
</template>

<script setup lang="ts">
import PrimeCard from 'primevue/card'

defineProps<{
  title: string
  projectCode: string
  projectDescription: string
  interactionLog?: string
}>()
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

.project-module__log {
  display: grid;
  gap: 0.35rem;
  padding: 0.9rem 1rem;
  border-radius: var(--app-radius-panel);
  background: rgba(16, 24, 40, 0.04);
  border: 1px solid rgba(16, 24, 40, 0.08);
}

.project-module__log-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--app-text-strong);
}

.project-module__log-code {
  margin: 0;
  white-space: pre-wrap;
  font-size: 0.8rem;
  color: var(--app-text-muted);
}
</style>
