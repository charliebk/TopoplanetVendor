import { createRouter, createWebHashHistory } from 'vue-router'
import MainView from '@/renderer/views/MainView/MainView.vue'
import ProjectMain from '@/renderer/views/ProjectMain/ProjectMain.vue'

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: MainView,
      meta: {
        titleKey: 'title.main'
      }
    },
    {
      path: '/project',
      component: ProjectMain,
      meta: {
        titleKey: 'title.projectMain'
      }
    },
    {
      path: '/error',
      component: () => import('@/renderer/views/ErrorView/ErrorView.vue'),
      meta: {
        titleKey: 'title.error'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})
