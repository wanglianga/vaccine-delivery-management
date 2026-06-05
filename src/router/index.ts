import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/batches',
  },
  {
    path: '/batches',
    name: 'batches',
    component: () => import('@/pages/BatchPage.vue'),
  },
  {
    path: '/distribution',
    name: 'distribution',
    component: () => import('@/pages/DistributionPage.vue'),
  },
  {
    path: '/transit',
    name: 'transit',
    component: () => import('@/pages/TransitPage.vue'),
  },
  {
    path: '/acceptance',
    name: 'acceptance',
    component: () => import('@/pages/AcceptancePage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
