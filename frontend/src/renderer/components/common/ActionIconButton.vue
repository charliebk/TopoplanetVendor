<script setup lang="ts">
defineProps<{
  icon: string
  tooltip: string
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <v-btn
    icon
    color="primary"
    @click="emit('click')"
  >
    <v-icon :icon="icon" />
    <v-tooltip
      activator="parent"
      location="bottom"
    >
      {{ tooltip }}
    </v-tooltip>
  </v-btn>
</template>
