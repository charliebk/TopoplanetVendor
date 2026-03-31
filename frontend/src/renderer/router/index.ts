import { createRouter, createWebHashHistory } from 'vue-router'
import MainView from '@/renderer/views/MainView/MainView.vue'
import ProjectMain from '@/renderer/views/ProjectMain/ProjectMain.vue'
import ProjectModuleView from '@/renderer/views/ProjectModules/ProjectModuleView.vue'

const workspaceChromeMeta = {
  layout: 'fixed',
  toolbarVisibility: {
    language: true,
    theme: true,
    version: true,
    closeProject: true
  },
  footerVisibility: {
    currentProject: true,
    currentModule: true,
    database: true,
    status: true
  }
} as const

const minimalShellMeta = {
  hideNavbar: true,
  hideFooter: true,
  layout: 'liquid'
} as const

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: MainView,
      meta: {
        titleKey: 'title.main',
        ...minimalShellMeta
      }
    },
    {
      path: '/project',
      component: ProjectMain,
      meta: {
        titleKey: 'title.projectMain',
        ...workspaceChromeMeta
      }
    },
    {
      path: '/project/logs',
      component: ProjectModuleView,
      meta: {
        titleKey: 'title.projectLogs',
        ...workspaceChromeMeta,
        moduleTitleKey: 'ProjectMain.modules.logsTitle',
        moduleDescriptionKey: 'ProjectMain.modules.logsDescription'
      }
    },
    {
      path: '/project/settings',
      component: ProjectModuleView,
      meta: {
        titleKey: 'title.projectSettings',
        ...workspaceChromeMeta,
        moduleTitleKey: 'ProjectMain.modules.settingsTitle',
        moduleDescriptionKey: 'ProjectMain.modules.settingsDescription'
      }
    },
    {
      path: '/project/vendors',
      component: ProjectModuleView,
      meta: {
        titleKey: 'title.projectVendors',
        ...workspaceChromeMeta,
        moduleTitleKey: 'ProjectMain.modules.vendorsTitle',
        moduleDescriptionKey: 'ProjectMain.modules.vendorsDescription'
      }
    },
    {
      path: '/project/products',
      component: ProjectModuleView,
      meta: {
        titleKey: 'title.projectProducts',
        ...workspaceChromeMeta,
        moduleTitleKey: 'ProjectMain.modules.productsTitle',
        moduleDescriptionKey: 'ProjectMain.modules.productsDescription'
      }
    },
    {
      path: '/project/questions',
      component: ProjectModuleView,
      meta: {
        titleKey: 'title.projectQuestions',
        ...workspaceChromeMeta,
        moduleTitleKey: 'ProjectMain.modules.questionsTitle',
        moduleDescriptionKey: 'ProjectMain.modules.questionsDescription'
      }
    },
    {
      path: '/error',
      component: () => import('@/renderer/views/ErrorView/ErrorView.vue'),
      meta: {
        titleKey: 'title.error',
        ...minimalShellMeta
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})
