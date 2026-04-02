<script setup>
import { ref, computed, onMounted } from 'vue'
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

const activeNav = computed(() => {
  const path = route.path
  if (path.includes('/CourseSelection') || path.includes('/course-selection')) return 'courseSelection'
  if (path.includes('/HighRecords') || path.includes('/records')) return 'growth'
  return ''
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
  <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-500 mb-12" :class="[
      isScrolled
        ? isDark
          ? 'bg-black/95 backdrop-blur-xl border-b border-gray-800'
          : 'bg-white/95 backdrop-blur-xl shadow-lg border-b border-gray-100'
        : 'bg-transparent'
    ]">
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
              : 'text-gray-700 hover:text-indigo-600 hover:bg-white/50'
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
        </div>

        <!-- 右侧返回首页按钮 -->
        <div class="w-40 h-12 flex justify-end">
          <button
            @click="navigateTo('home')"
            class="shadow-lg group flex items-center gap-2 px-4 py-1 rounded-full text-sm font-medium transition-all duration-300"
            :class="[
          isDark
            ? 'text-gray-300 hover:text-white hover:bg-gray-800'
            : 'text-gray-500 hover:text-indigo-600 hover:bg-gray-200'
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
  </nav>
</template>

<style scoped>
/* 导航栏样式已在模板中通过内联类定义 */
</style>
