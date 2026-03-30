<template>
  <v-app-bar
    color="primary"
    density="compact"
  >
    <v-app-bar-title>{{ resolveLabel(titleKey) }}</v-app-bar-title>
    <template #append>
      <v-btn
        v-for="menu in headerMenus"
        :key="menu.path"
        :prepend-icon="menu.icon"
        variant="text"
        :class="{ active: isCurrentRoute(menu.path) }"
        @click="handleRoute(menu.path)"
      >
        {{ resolveLabel(menu.text) }}
      </v-btn>
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { mdiHome } from '@mdi/js'

const router = useRouter()
const route: any = useRoute()
const titleKey: string = (route?.meta?.titleKey || 'title.main') as string

const { t } = useI18n()

const handleRoute = (path: string): void => {
  router.push(path)
}

const isCurrentRoute = (path: string): boolean => {
  return path === route.path
}

const resolveLabel = (text: string): string => {
  if (text.includes('.')) {
    return t(text)
  }
  return text
}

const headerMenus: {
  icon: string
  text: string
  path: string
}[] = [
  {
    icon: mdiHome,
    text: 'title.main',
    path: '/'
  }
]
</script>

<style scoped>
.v-btn {
  opacity: 0.4;
}
.active {
  opacity: 1 !important;
}
</style>
