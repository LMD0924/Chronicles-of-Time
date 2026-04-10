<script setup>
import {ref, computed, onMounted, watch, nextTick, provide, onUnmounted} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { getUserInfo as getStoredUserInfo } from '@/utils/token.js'
import Nav from '@/components/Nav.vue'
import { getStoredTheme, ThemeType } from '@/utils/theme'
// 导入子组件
import PlanManagement from '@/views/high/volunteer/PlanManagement.vue'
import RecommendPanel from '@/views/high/volunteer/RecommendPanel.vue'
import SimulationPanel from '@/views/high/volunteer/SimulationPanel.vue'
import SearchPanel from '@/views/high/volunteer/SearchPanel.vue'

const route = useRoute()
const router = useRouter()

// 主题
const isDark = ref(getStoredTheme() === ThemeType.DARK)

// 用户信息
const userId = ref(1)
const UserInfo = ref({})

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

// 导航菜单配置
const menuItems = [
  {
    key: 'CourseSelection',
    label: '明确目标',
    icon: '🎯',
    path: '/CourseSelection'
  },
  {
    key: 'Volunteer',
    label: '规划未来',
    icon: '🎓',
    path: '/Volunteer'
  },
  {
    key: 'StudyDashboard',
    label: '温故而知新',
    icon: '📚',
    children: [
      { key: 'practice', label: '实战练习', icon: '⚡', path: '/StudyDashboard?tab=practice' },
      { key: 'mistake', label: '错题本', icon: '📖', path: '/StudyDashboard?tab=mistake' },
      { key: 'analysis', label: '成绩分析', icon: '📊', path: '/StudyDashboard?tab=analysis' },
      { key: 'questionBank', label: '题库管理', icon: '📚', path: '/StudyDashboard?tab=questionBank' },
      { key: 'answerRecords', label: '答题记录', icon: '✍️', path: '/StudyDashboard?tab=answerRecords' }
    ]
  },
  {
    key: '个人图谱分析',
    label: '图谱',
    icon: '👤',
    path: '/GraphView'
  }
]

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
})
</script>

<template>
  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <Nav :isDark="isDark" :menuItems="menuItems"/>

      <!-- 主内容区 -->
      <div class="pt-24 pb-16" :class="isDark ? 'bg-black' : ''">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <!-- 标签页导航 -->
          <div class="mb-6">
            <div :class="[isDark ? 'bg-black backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex flex-wrap gap-1 rounded-2xl p-1 shadow-sm']">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                @click="activeTab = tab.key"
                class="relative px-4 py-2 rounded-xl text-sm font-medium transition-all duration-300 overflow-hidden group"
                :class="[
                  activeTab === tab.key
                    ? isDark ? 'text-white shadow-lg' : 'text-black shadow-lg'
                    : isDark ? 'text-gray-400 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'
                ]"
              >
                <span v-if="activeTab === tab.key" class="absolute inset-0 bg-gradient-to-r rounded-full shadow-md" :class="isDark ? 'bg-gray-800' : ''"></span>
                <span class="relative flex items-center gap-2 z-10">
                  <span class="text-base">{{ tab.icon }}</span>
                  <span>{{ tab.label }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- 动态组件 -->
          <div>
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
