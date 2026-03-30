<template>
  <v-container>
    <v-row
      density="compact"
      class="text-center align-center"
    >
      <v-col
        cols="12"
        md="5"
      >
        <v-img
          data-testid="main-logo"
          alt="logo"
          draggable="false"
          class="ma-auto h-auto w-sm-50 w-md-100"
          src="images/vutron-logo.webp"
        />
      </v-col>
      <v-col
        cols="12"
        md="7"
      >
        <h2 class="my-4">
          {{ t('desc.welcome-title') }}
        </h2>
        <p>{{ t('desc.welcome-desc') }}</p>
        <p class="my-4">
          App Version: <strong>{{ appVersion }}</strong>
        </p>
        <p class="my-2">
          <strong>Backend message:</strong> {{ helloWorldMessage }}
        </p>
        <p class="my-2">
          <PrimeTag
            value="PrimeVue OK"
            severity="success"
          />
        </p>
        <p v-if="selectedFile">
          {{
            t('desc.selected-file', {
              filePath: selectedFile
            })
          }}
        </p>
        <v-row class="my-4">
          <v-col>
            <ActionIconButton
              :icon="mdiBrightness6"
              :tooltip="t('menu.change-theme')"
              @click="handleChangeTheme"
            />
          </v-col>
          <v-col>
            <ActionIconButton
              :icon="mdiFileDocument"
              :tooltip="t('menu.documentation')"
              @click="handleOpenDocument"
            />
          </v-col>
          <v-col>
            <ActionIconButton
              :icon="mdiGithub"
              :tooltip="t('menu.github')"
              @click="handleOpenGitHub"
            />
          </v-col>
          <v-col>
            <ActionIconButton
              :icon="mdiFolderOpen"
              :tooltip="t('menu.open-file')"
              @click="handleOpenFile"
            />
          </v-col>
          <v-col cols="12">
            <v-select
              data-testid="select-language"
              :model-value="locale"
              density="compact"
              :label="t('menu.change-language')"
              :items="languages"
              @update:model-value="handleChangeLanguage"
            >
              {{ t('menu.change-language') }}
            </v-select>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useTheme } from 'vuetify'
import { openExternal, openFile } from '@/renderer/utils'
import { onMounted, ref } from 'vue'
import {
  mdiBrightness6,
  mdiFileDocument,
  mdiFolderOpen,
  mdiGithub
} from '@mdi/js'
import { useAppMessageStore } from '@/renderer/stores'
import ActionIconButton from '@/renderer/components/common/ActionIconButton.vue'

const { t, locale, availableLocales } = useI18n()
const theme = useTheme()
const languages = ref(['en'])
const appVersion = ref('Unknown')
const selectedFile = ref('')
const helloWorldMessage = ref('Loading message from backend...')
const appMessageStore = useAppMessageStore()

const loadHelloWorldMessage = async (): Promise<void> => {
  try {
    const appMessage = await appMessageStore.fetchHelloWorldMessage()
    helloWorldMessage.value = appMessage.value
  } catch {
    helloWorldMessage.value = 'Backend unavailable. Could not load HELLO_WORLD.'
  }
}

const getApplicationVersionFromMainProcess = (): void => {
  window.mainApi.invoke('msgRequestGetVersion').then((result: string) => {
    appVersion.value = result
  })
}

const handleChangeTheme = (): void => {
  theme.change(theme.global.current.value.dark ? 'light' : 'dark')
}

const handleChangeLanguage = (val): void => {
  locale.value = val
}

const handleOpenDocument = async (): Promise<void> => {
  await openExternal('https://vutron.cdget.com')
}

const handleOpenGitHub = async (): Promise<void> => {
  await openExternal('https://github.com/jooy2/vutron')
}

const handleOpenFile = async () => {
  const dialogResult = await openFile('text')
  if (!dialogResult.canceled) {
    selectedFile.value = dialogResult.filePaths[0]
  }
}

onMounted((): void => {
  languages.value = availableLocales
  getApplicationVersionFromMainProcess()
  loadHelloWorldMessage()
})
</script>
