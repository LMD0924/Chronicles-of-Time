<script setup>
import {ref, computed, onMounted, watch, nextTick, provide, onUnmounted} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { getUserInfo as getStoredUserInfo } from '@/utils/token.js'
import ThemeToggleButton from '@/components/ThemeToggleButton.vue';
import {
  ThemeType,
  getStoredTheme,
  setTheme,
  toggleTheme as toggleGlobalTheme,
  initTheme,
  onThemeChange
} from "@/utils/theme.js";
import { message } from "ant-design-vue";

// 导入子组件
import PlanManagement from '@/views/high/volunteer/PlanManagement.vue'
import RecommendPanel from '@/views/high/volunteer/RecommendPanel.vue'
import SimulationPanel from '@/views/high/volunteer/SimulationPanel.vue'
import SearchPanel from '@/views/high/volunteer/SearchPanel.vue'

const route = useRoute()
const router = useRouter()
const [MessageApi, contextHolder] = message.useMessage();

// 主题
const isDark = ref(getStoredTheme() === ThemeType.DARK)
provide('isDark', isDark)
const isScrolled = ref(false)
const showUserMenu = ref(false)

// 用户信息
const userId = ref(1)
const UserInfo = ref({})
const userInitial = computed(() => UserInfo.value.name?.charAt(0)?.toUpperCase() || 'U')

// 未读消息数
const unreadCount = ref(3)

// 志愿方案数据
const volunteerPlans = ref([])

// 标签页配置
const tabs = [
  { key: 'plan', label: '志愿方案', icon: '📝' },
  { key: 'recommend', label: '智能推荐', icon: '🎯' },
  { key: 'simulate', label: '模拟录取', icon: '🎲' },
  { key: 'search', label: '数据查询', icon: '🔍' }
]
const activeTab = ref('plan')
const componentMap = { plan: PlanManagement, recommend: RecommendPanel, simulate: SimulationPanel, search: SearchPanel }
const currentComponent = computed(() => componentMap[activeTab.value] || PlanManagement)

// 志愿方案更新
const handleUpdateCount = (plans) => { volunteerPlans.value = plans }

// 获取用户信息
const getUserInfo = async () => {
  try {
    const storedUser = getStoredUserInfo()
    if (storedUser && storedUser.id) {
      userId.value = storedUser.id
      UserInfo.value = storedUser
    } else {
      request.get('/user/getUserById', {}, (msg, data) => {
        UserInfo.value = data
        userId.value = data.id
      })
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 导航相关
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const closeUserMenu = () => {
  showUserMenu.value = false
}

const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu-container')
  if (userMenu && !userMenu.contains(event.target)) {
    closeUserMenu()
  }
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

// 初始化滚动动画观察器
const initScrollAnimation = () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
        observer.unobserve(entry.target)
      }
    })
  }, {
    threshold: 0.2,
    rootMargin: '0px 0px -50px 0px'
  })

  const animatedElements = document.querySelectorAll('.scroll-animate')
  animatedElements.forEach(el => observer.observe(el))
}

// 处理主题切换
const handleThemeChange = (theme) => {
  const newTheme = theme === ThemeType.DARK
  isDark.value = newTheme

  if (newTheme) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }

  nextTick(() => {
    const animatedElements = document.querySelectorAll('.scroll-animate')
    animatedElements.forEach(el => {
      el.classList.remove('animated')
      setTimeout(() => {
        if (el.getBoundingClientRect().top < window.innerHeight + 100) {
          el.classList.add('animated')
        }
      }, 50)
    })
  })
}

// 带过渡效果的导航
const navigateWithTransition = (path) => {
  router.push(path)
}

// 监听 URL 参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab && tabs.some(tab => tab.key === newTab)) {
    activeTab.value = newTab
  }
}, { immediate: true })

// 监听 activeTab 变化并同步到 URL
watch(activeTab, (newTab) => {
  router.replace({
    query: { ...route.query, tab: newTab }
  })
})

onMounted(() => {
  getUserInfo()

  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  window.addEventListener('scroll', handleScroll)
  window.addEventListener('click', handleClickOutside)

  setTimeout(initScrollAnimation, 100)

  const stopListen = onThemeChange((theme) => {
    handleThemeChange(theme)
  })

  onUnmounted(() => {
    stopListen()
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <contextHolder></contextHolder>
  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <!-- 导航栏 -->
      <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-500" :class="[
        isScrolled
          ? isDark
            ? 'bg-black/95 backdrop-blur-xl border-b border-gray-800'
            : 'bg-white/95 backdrop-blur-xl shadow-lg border-b border-gray-100'
          : 'bg-transparent'
      ]">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <div class="flex items-center justify-between h-16 lg:h-20">
            <!-- 返回按钮 -->
            <div class="w-40 h-12 flex justify-start">
              <button
                @click="navigateWithTransition('HighRecords')"
                class="shadow-lg group flex items-center gap-2 px-4 py-1 rounded-full text-sm font-medium transition-all duration-300"
                :class="[
                  isDark
                    ? 'text-gray-300 hover:text-white hover:bg-gray-500'
                    : 'text-gray-500 hover:text-indigo-600 hover:bg-gray-500'
                ]"
              >
                <svg class="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                </svg>
                <span>返回</span>
              </button>
            </div>

            <!-- 标签页导航（居中） -->
            <div class="flex-1 flex justify-center">
              <div :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'hidden md:flex items-center gap-1 rounded-full p-1 shadow-sm']">
                <button
                  v-for="tab in tabs"
                  :key="tab.key"
                  @click="activeTab = tab.key"
                  class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group"
                  :class="[
                    activeTab === tab.key
                      ? isDark ? 'text-white shadow-lg' : 'text-black shadow-lg'
                      : isDark ? 'text-gray-400 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'
                  ]"
                >
                  <span v-if="activeTab === tab.key" class="absolute inset-0 bg-gradient-to-r rounded-full shadow-md" :class="isDark ? 'bg-gray-600' : ''"></span>
                  <span class="relative flex items-center gap-2 z-10">
                    <span class="text-base">{{ tab.icon }}</span>
                    <span>{{ tab.label }}</span>
                  </span>
                </button>
              </div>
            </div>

            <!-- 用户信息（右侧） -->
            <div class="flex items-center gap-4 user-menu-container relative">
              <!-- 用户名和头像 -->
              <div class="hidden md:flex items-center gap-2 cursor-pointer group" @click="toggleUserMenu">
                <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-indigo-200 group-hover:border-indigo-400 transition-colors">
                  <img :src="UserInfo.avatar" alt="User Avatar">
                </div>
                <span :class="[isDark ? 'text-gray-300 group-hover:text-indigo-400' : 'text-gray-700 group-hover:text-indigo-600', 'text-sm font-medium transition-colors']">{{UserInfo.name}}</span>
              </div>

              <!-- 下拉菜单 -->
              <div v-if="showUserMenu" :class="[isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100', 'absolute top-full right-0 mt-2 w-48 rounded-lg shadow-xl border overflow-hidden z-50']">
                <div class="py-2">
                  <button @click="navigateWithTransition('PersonalProfile')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>👤</span>
                    <span>个人档案</span>
                  </button>
                  <button @click="navigateWithTransition('Resume')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>👤</span>
                    <span>个人简历</span>
                  </button>
                  <button @click="navigateWithTransition('settings')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>⚙️</span>
                    <span>设置</span>
                  </button>
                  <div :class="[isDark ? 'border-gray-800' : 'border-gray-100', 'border-t my-1']"></div>
                  <button @click="navigateWithTransition('logout')" :class="[isDark ? 'text-red-400 hover:bg-gray-800' : 'text-red-600 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>🚪</span>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>

              <!-- 主题切换按钮（最右侧） -->
              <ThemeToggleButton />

              <!-- 移动端菜单按钮（仅移动端显示） -->
              <button :class="[isDark ? 'bg-gray-800 text-gray-300' : 'bg-gray-100 text-gray-600', 'md:hidden w-9 h-9 rounded-lg flex items-center justify-center']">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </nav>

      <!-- 主内容区 -->
      <div class="pt-24 pb-16">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <!-- 移动端标签页 -->
          <div class="md:hidden mb-6">
            <div :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex flex-wrap gap-1 rounded-2xl p-1 shadow-sm']">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                @click="activeTab = tab.key"
                class="flex-1 min-w-[70px] px-3 py-2 rounded-xl text-sm font-medium transition-all duration-300"
                :class="[
                  activeTab === tab.key
                    ? isDark ? 'text-white shadow-lg bg-gray-600' : 'text-black shadow-lg bg-white'
                    : isDark ? 'text-gray-400' : 'text-gray-600'
                ]"
              >
                <span class="text-base">{{ tab.icon }}</span>
              </button>
            </div>
          </div>

          <!-- 动态组件 -->
          <div class="scroll-animate">
            <keep-alive>
              <component
                :is="currentComponent"
                :user-id="userId"
                :volunteer-plans="volunteerPlans"
                @update-count="handleUpdateCount"
              />
            </keep-alive>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.1); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-float {
  animation: float 20s ease-in-out infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-5000 {
  animation-delay: 5s;
}

/* 滚动动画样式 */
.scroll-animate {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.scroll-animate.animated {
  opacity: 1;
  transform: translateY(0);
}

/* 过渡动画 */
.transition-colors {
  transition-property: background-color, border-color, color, fill, stroke;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}
</style>
