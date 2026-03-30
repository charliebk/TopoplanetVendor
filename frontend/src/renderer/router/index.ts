import { createRouter, createWebHashHistory } from 'vue-router'
import MainView from '@/renderer/views/MainView/MainView.vue'

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
