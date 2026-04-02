import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// Token 获取函数
const getToken = () => {
  return sessionStorage.getItem('token') || localStorage.getItem('token')
}

// 不需要登录的白名单路径
const whiteList = ['/', '/login']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '欢迎页面',
      component: () => import('@/views/auth/welcome.vue')
    },
    {
      path: '/login',
      name: '登录',
      component: () => import('@/views/auth/login.vue')
    },
    {
      path: '/home',
      name: '首页',
      component: () => import('@/views/auth/home.vue')
    },
    {
      path:'/PersonalProfile',
      name:'个人档案',
      component: () => import('@/views/auth/PersonalProfile.vue')
    },
    {
      path:'/Resume',
      name:'简历',
      component: () => import('@/views/auth/Resume.vue')
    },
    {
      path:'/HighRecords',
      name:'高中记录',
      component: () => import('@/views/high/HighRecords.vue')
    },
    {
      path:'/CourseSelection',
      name:'选科',
      component: () => import('@/views/high/CourseSelection/CourseSelection.vue')
    },
    {
      path: '/test',
      name: '测试',
      component: () => import('@/views/test.vue')
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()

  // 白名单路径直接放行
  if (whiteList.includes(to.path)) {
    next()
    return
  }

  // 其他所有路径都需要登录
  if (!token) {
    ElMessage.warning('请先登录')
    next('/login')
  } else {
    next()
  }
})

export default router
