<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/renderer/components/layout/AppFooter.vue'
import AppNavbar from '@/renderer/components/layout/AppNavbar.vue'

const route = useRoute()

const showNavbar = computed(() => route.meta.hideNavbar !== true)
const showFooter = computed(() => route.meta.hideFooter !== true)
const mainLayoutClass = computed(() => {
  return route.meta.layout === 'fixed' ? 'layout-fixed' : 'layout-liquid'
})
</script>

<template>
  <div
    id="app"
    class="app-shell"
  >
    <AppNavbar v-if="showNavbar" />
    <main
      class="app-main-content"
      :class="mainLayoutClass"
    >
      <RouterView class="view-wrapper" />
    </main>
    <AppFooter v-if="showFooter" />
  </div>
</template>
