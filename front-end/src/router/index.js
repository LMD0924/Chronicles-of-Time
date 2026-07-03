/**
 * 鏂囦欢璇存槑锛氭嬀鍏夎鍓嶅彴搴旂敤璺敱涓庤彍鍗曡剼鏈ā鍧楋紝灏佽璺敱涓庤彍鍗曠浉鍏崇殑閰嶇疆銆佺姸鎬併€佽矾鐢辨垨宸ュ叿閫昏緫銆? */
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// Token 鑾峰彇鍑芥暟
const getToken = () => {
  return sessionStorage.getItem('token') || localStorage.getItem('token')
}

// 涓嶉渶瑕佺櫥褰曠殑鐧藉悕鍗曡矾寰?const whiteList = ['/', '/login']

// 鍓嶅彴璺敱闆嗕腑绠＄悊楂樹腑銆佸ぇ瀛︺€佽亴鍦哄拰鎴愰暱闃舵椤甸潰锛屼繚鎸佹嬀鍏夎涓荤珯鍏ュ彛娓呮櫚銆?const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '娆㈣繋椤甸潰',
      component: () => import('@/views/auth/welcome.vue')
    },
    {
      path: '/login',
      name: '鐧诲綍',
      component: () => import('@/views/auth/login.vue')
    },
    {
      path: '/home',
      name: '棣栭〉',
      component: () => import('@/views/auth/home.vue')
    },
    {
      path:'/PersonalProfile',
      name:'涓汉妗ｆ',
      component: () => import('@/views/auth/PersonalProfile.vue')
    },
    {
      path:'/Resume',
      name:'绠€鍘?,
      component: () => import('@/views/auth/Resume.vue')
    },
    {
      path:'/Settings',
      name:'璁剧疆',
      component: () => import('@/views/auth/Settings.vue')
    },
    {
      path:'/Records',
      name:'璁板綍鎷惧厜',
      component: () => import('@/views/Records.vue')
    },
    {
      path:'/CourseSelection',
      name:'閫夌',
      component: () => import('@/views/high/CourseSelection/CourseSelection.vue')
    },
    {
      path:'/StudyDashboard',
      name:'瀛︿範浠〃鐩?,
      component: () => import('@/views/StudyDashboard/StudyDashboard.vue')
    },
    {
      path:'/Volunteer',
      name:'蹇楁効濉姤',
      component: () => import('@/views/high/volunteer/volunteer.vue')
    },
    {
      path:'/Publish',
      name:'鍙戝竷',
      component: () => import('@/views/content/publish.vue')
    },
    {
      path:'/List',
      name:'鍒楄〃',
      component: () => import('@/views/content/List.vue')
    },
    {
      path:'/View/:id',
      name:'鏌ョ湅',
      component: () => import('@/views/content/View.vue')
    },
    {
      path:'/PrePare',
      name:'鍑嗗',
      component: () => import('@/views/university/PrePare.vue')
    },
    {
      path:'/GraphView',
      name:'瀛︿範鍥捐氨',
      component: () => import('@/views/StudyDashboard/GraphView.vue')
    },
    {
      path:'/ContentKnowledgeGraph',
      name:'鏂囩珷鍥捐氨',
      component: () => import('@/views/content/ContentKnowledgeGraph.vue')
    },
    //===============澶у======================
    {
      path:'/Paper',
      name:'璁烘枃',
      component: () => import('@/views/university/paper/Paper.vue')
    },
    {
      path: '/test',
      name: '娴嬭瘯',
      component: () => import('@/views/test.vue')
    }
  ],
})

// 璺敱瀹堝崼

// 璺敱鍒囨崲鏃剁粺涓€澶勭悊椤甸潰鏍囬銆佺櫥褰曟€佹垨涓婚鎭㈠绛夋í鍒囬€昏緫銆?router.beforeEach((to, from, next) => {
  const token = getToken()

  // 鐧藉悕鍗曡矾寰勭洿鎺ユ斁琛?  if (whiteList.includes(to.path)) {
    next()
    return
  }

  // 鍏朵粬鎵€鏈夎矾寰勯兘闇€瑕佺櫥褰?  if (!token) {
    ElMessage.warning('璇峰厛鐧诲綍')
    next('/login')
  } else {
    next()
  }
})

export default router

