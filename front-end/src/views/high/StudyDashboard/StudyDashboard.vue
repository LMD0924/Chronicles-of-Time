<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-100 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-slate-900 dark:to-gray-900 relative overflow-hidden">
    <!-- 装饰性背景元素 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 bg-purple-300/20 dark:bg-purple-600/10 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-blue-300/20 dark:bg-blue-600/10 rounded-full blur-3xl"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-purple-400/10 via-transparent to-blue-400/10 dark:from-purple-600/10 dark:to-blue-600/10 rounded-full blur-3xl"></div>
    </div>
    
    <HighNav></HighNav>
    <!-- 主内容区 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 relative z-10">
      <!-- 动态组件 -->
      <div class="animate-fadeIn">
        <keep-alive>
          <component
            :is="currentComponent"
            :is-dark="isDark"
            :student-id="studentId"
          />
        </keep-alive>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, shallowRef, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PracticeCenter from '@/views/high/StudyDashboard/PracticeCenter.vue'
import MistakeBook from '@/views/high/StudyDashboard/MistakeBook.vue'
import ScoreAnalysis from '@/views/high/StudyDashboard/ScoreAnalysis.vue'
import QuestionBank from '@/views/high/StudyDashboard/QuestionBank.vue'
import AnswerRecords from '@/views/high/StudyDashboard/AnswerRecords.vue'
import HighNav from '@/components/HighNav.vue'

const props = defineProps({
  studentId: { type: [String, Number], default: 1 }
})

const route = useRoute()
const router = useRouter()

// 主题
const isDark = ref(false)

const toggleDarkMode = () => {
  isDark.value = !isDark.value
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const activeTab = ref('practice')

const currentComponent = computed(() => {
  const map = {
    practice: PracticeCenter,
    answerRecords: AnswerRecords,
    mistake: MistakeBook,
    analysis: ScoreAnalysis,
    questionBank: QuestionBank
  }
  return map[activeTab.value] || PracticeCenter
})

const userInitial = computed(() => {
  return '学'
})

// 监听 URL 参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab
  }
}, { immediate: true })

// 监听 activeTab 变化并同步到 URL
watch(activeTab, (newTab) => {
  router.replace({
    query: { ...route.query, tab: newTab }
  })
})
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.4s ease-out forwards;
}
</style>
