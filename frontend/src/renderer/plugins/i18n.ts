import { createI18n } from 'vue-i18n'
import commonEn from '@/renderer/locales/Common/en.json'
import commonEs from '@/renderer/locales/Common/es.json'
import commonPt from '@/renderer/locales/Common/pt.json'
import mainViewEn from '@/renderer/locales/MainView/en.json'
import mainViewEs from '@/renderer/locales/MainView/es.json'
import mainViewPt from '@/renderer/locales/MainView/pt.json'
import projectMainEn from '@/renderer/locales/ProjectMain/en.json'
import projectMainEs from '@/renderer/locales/ProjectMain/es.json'
import projectMainPt from '@/renderer/locales/ProjectMain/pt.json'
import errorViewEn from '@/renderer/locales/ErrorView/en.json'
import errorViewEs from '@/renderer/locales/ErrorView/es.json'
import errorViewPt from '@/renderer/locales/ErrorView/pt.json'
import { getCurrentLocale } from '@/renderer/utils'
import {
  isSupportedUiLocale,
  type UiLocale
} from '@/renderer/stores/preferences/preferences.types'

const resolveLocale = (): UiLocale => {
  const currentLocale = getCurrentLocale()

  return isSupportedUiLocale(currentLocale) ? currentLocale : 'en'
}

const en = {
  ...commonEn,
  MainView: mainViewEn,
  ProjectMain: projectMainEn,
  ErrorView: errorViewEn
}

const es = {
  ...commonEs,
  MainView: mainViewEs,
  ProjectMain: projectMainEs,
  ErrorView: errorViewEs
}

const pt = {
  ...commonPt,
  MainView: mainViewPt,
  ProjectMain: projectMainPt,
  ErrorView: errorViewPt
}

export default createI18n({
  locale: resolveLocale(),
  fallbackLocale: 'en',
  globalInjection: true,
  silentTranslationWarn: process.env.NODE_ENV !== 'development',
  messages: {
    en,
    es,
    pt
  }
})
