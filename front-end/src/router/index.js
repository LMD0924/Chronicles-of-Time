import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
        {
          path:'/',
          name:'登录',
          component:()=>import('@/views/auth/login.vue')
        },
    {
      path:'/test',
      name:'测试',
      component:()=>import('@/views/test.vue')
    }
  ],
})

export default router
