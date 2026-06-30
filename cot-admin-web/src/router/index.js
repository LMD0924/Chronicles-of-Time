import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { ElMessage } from 'element-plus'
import Layout from '@/layout/AdminLayout.vue'
import { adminMenus, toRouteRecords } from './menus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const constantRoutes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { title: '管理员登录', public: true } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: toRouteRecords(adminMenus),
  },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/ForbiddenView.vue'), meta: { title: '无权限', public: true } },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
})

router.beforeEach((to) => {
  NProgress.start()
  const userStore = useUserStore()
  const appStore = useAppStore()
  appStore.applyTheme()

  if (to.meta.public) return true
  if (!userStore.isLogin) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  if (!userStore.isAdmin) {
    ElMessage.warning('普通账号无后台访问权限')
    return '/403'
  }
  if (to.meta.keepAlive) appStore.addCachedView(to.name)
  return true
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '后台'} - 拾光记后台管理系统`
  NProgress.done()
})

router.onError(() => NProgress.done())

export default router
