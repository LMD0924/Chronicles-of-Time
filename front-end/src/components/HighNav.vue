<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStoredTheme, ThemeType } from '@/utils/theme'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()
const isScrolled = ref(false)
const showStudySubmenu = ref(false)

const activeNav = computed(() => {
  const path = route.path
  if (path.includes('/CourseSelection') || path.includes('/course-selection')) return 'courseSelection'
  if (path.includes('/HighRecords') || path.includes('/records')) return 'growth'
  if (path.includes('/StudyDashboard')) return 'StudyDashboard'
  return ''
})

const studyDashboardTabs = [
  { key: 'practice', label: '实战练习', icon: '⚡', path: '/StudyDashboard' },
  { key: 'answerRecords', label: '答题记录', icon: '📋', path: '/StudyDashboard?tab=answerRecords' },
  { key: 'mistake', label: '错题本', icon: '📖', path: '/StudyDashboard?tab=mistake' },
  { key: 'analysis', label: '成绩分析', icon: '📊', path: '/StudyDashboard?tab=analysis' },
  { key: 'questionBank', label: '题库管理', icon: '📚', path: '/StudyDashboard?tab=questionBank' }
]

const toggleStudySubmenu = (e) => {
  e.stopPropagation()
  if (activeNav.value !== 'StudyDashboard') {
    navigateTo('StudyDashboard')
  }
  showStudySubmenu.value = !showStudySubmenu.value
}

const navigateToStudyTab = (tabKey) => {
  router.push({
    path: '/StudyDashboard',
    query: { tab: tabKey }
  })
  showStudySubmenu.value = false
}

// 点击外部关闭子菜单
const handleClickOutside = (e) => {
  if (!e.target.closest('.study-submenu-container')) {
    showStudySubmenu.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})

const navigateTo = (target) => {
  switch (target) {
    case 'growth':
      router.push('/HighRecords')
      break
    case 'courseSelection':
      router.push('/CourseSelection')
      break
    case 'home':
      router.push('/home')
      break
    case 'StudyDashboard':
      router.push('/StudyDashboard')
      break
  }
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})
</script>

<template>
    <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
      <div class="flex items-center justify-between h-16 lg:h-20">
        <!-- 左侧留空 -->
        <div class="w-20"></div>

        <!-- 中间导航菜单 -->
        <div class="flex items-center justify-center gap-2 shadow-lg rounded-full">
          <button
            @click="navigateTo('growth')"
            class="relative px-6 py-2 rounded-full text-base font-medium transition-all duration-300 overflow-hidden group"
            :class="[
          activeNav === 'growth'
            ? isDark
              ? 'bg-white/30 text-white shadow-lg'
              : 'bg-white/30 text-gray-700 shadow-lg'
            : isDark
              ? 'text-gray-300 hover:text-white hover:bg-white/30'
              : 'text-gray-700 hover:text-indigo-600 hover:bg-white/30'
        ]"
          >
        <span class="relative flex items-center gap-2 z-10">
          <span class="text-lg">📚</span>
          <span>高中拾光</span>
        </span>
          </button>

          <button
            @click="navigateTo('courseSelection')"
            class="relative px-6 py-2 rounded-full text-base font-medium transition-all duration-300 overflow-hidden group"
            :class="[
          activeNav === 'courseSelection'
            ? isDark
              ? 'bg-white/30 text-white shadow-lg'
              : 'bg-white/30 text-gray-700 shadow-lg'
            : isDark
              ? 'text-gray-300 hover:text-indigo-600 hover:bg-gray-800'
              : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-100'
        ]"
          >
        <span class="relative flex items-center gap-2 z-10">
          <span class="text-lg">🎯</span>
          <span>选科意向</span>
        </span>
          </button>

          <div class="relative study-submenu-container">
            <button
              @click="toggleStudySubmenu"
              class="relative px-6 py-2 rounded-full text-base font-medium transition-all duration-300 overflow-hidden group"
              :class="[
            (activeNav === 'StudyDashboard' || showStudySubmenu)
              ? isDark
                ? 'bg-white/30 text-white shadow-lg'
                : 'bg-white/30 text-gray-700 shadow-lg'
              : isDark
                ? 'text-gray-300 hover:text-indigo-600 hover:bg-gray-800'
                : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-100'
          ]"
            >
              <span class="relative flex items-center gap-2 z-10">
                <span class="text-lg">📚</span>
                <span>温故而知新</span>
              </span>
            </button>

            <!-- 子菜单 - 横着显示 -->
            <div
              v-if="showStudySubmenu"
              class="absolute top-full left-1/2 -translate-x-1/2 mt-2 bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 overflow-hidden z-50"
            >
              <div class="p-2 flex gap-2">
                <button
                  v-for="tab in studyDashboardTabs"
                  :key="tab.key"
                  @click="navigateToStudyTab(tab.key)"
                  class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 whitespace-nowrap"
                  :class="[
                    route.query.tab === tab.key || (!route.query.tab && tab.key === 'practice')
                      ? 'bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400'
                      : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                  ]"
                >
                  <span class="text-lg">{{ tab.icon }}</span>
                  <span>{{ tab.label }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧返回首页按钮 -->
        <div class="w-40 h-12 flex justify-end">
          <button
            @click="navigateTo('home')"
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
            <span>返回首页</span>
          </button>
        </div>
      </div>
    </div>
</template>

<style scoped>
/* 导航栏样式已在模板中通过内联类定义 */
</style>
