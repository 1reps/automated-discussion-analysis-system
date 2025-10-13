import { createRouter, createWebHistory } from 'vue-router'

import Recorder from '../components/Recorder.vue'
import ApiSample from '../components/ApiSample.vue'

const routes = [
  { path: '/', name: 'home', component: Recorder },
  { path: '/sample', name: 'api-sample', component: ApiSample },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
