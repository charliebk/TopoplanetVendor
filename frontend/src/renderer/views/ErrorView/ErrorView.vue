<template>
  <section class="app-page app-page--centered app-page--narrow error-view">
    <div class="app-page__shell">
      <PrimeCard class="app-panel error-view__panel">
        <template #title>
          <span>
            {{ t('ErrorView.title') }}
          </span>
        </template>
        <template #subtitle>
          {{ t('ErrorView.subtitle') }}
        </template>
        <template #content>
          <div class="app-stack app-stack--compact">
            <img
              :src="appIconSrc"
              alt="Application icon"
              class="app-state-art app-state-art--compact error-view__icon"
            />
            <p class="app-copy">
              {{ t('ErrorView.body') }}
            </p>
            <div class="app-action-row">
              <PrimeButton
                :label="t('ErrorView.backHome')"
                icon="pi pi-home"
                @click="goHome"
              />
            </div>
          </div>
        </template>
      </PrimeCard>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { RENDERER_ASSETS } from '@/renderer/utils'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const appIconSrc = RENDERER_ASSETS.icon
const { t } = useI18n()

const goHome = (): void => {
  router.push('/')
}
</script>

<style scoped>
.error-view {
  background:
    radial-gradient(circle at top, rgba(21, 101, 192, 0.1), transparent 28%),
    linear-gradient(180deg, #f4f7f1 0%, #edf1eb 100%);
}

.error-view__panel {
  background: rgba(255, 255, 255, 0.96);
}

.error-view__icon {
  margin-bottom: 0.2rem;
}
</style>
