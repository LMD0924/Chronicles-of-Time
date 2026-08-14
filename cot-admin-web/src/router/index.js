import messageApi from '@/utils/messageApi'
/**
 * 文件说明：拾光记后台管理系统路由与菜单脚本模块，封装路由与菜单相关的配置、状态、路由或工具逻辑。
 */
import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import Layout from '@/layout/AdminLayout.vue'
import { adminMenus, toRouteRecords } from './menus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

// 固定路由由登录页、后台布局、403 和兜底重定向组成；业务页面由菜单配置自动生成。
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



// 全局前置守卫负责主题恢复、登录校验、后台权限校验和页面缓存登记。
router.beforeEach((to) => {
  NProgress.start()
  const userStore = useUserStore()
  const appStore = useAppStore()
  appStore.applyTheme()


  if (to.meta.public) return true
  if (!userStore.isLogin) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  if (!userStore.isAdmin) {
    messageApi.warning('普通账号无后台访问权限')
    return '/403'
  }
  if (to.meta.keepAlive) appStore.addCachedView(to.name)
  return true
})


// 路由结束后统一更新浏览器标题，并关闭顶部进度条。
router.afterEach((to) => {
  document.title = `${to.meta.title || '后台'} - 拾光记后台管理系统`
  NProgress.done()
})

router.onError(() => NProgress.done())

export default router
