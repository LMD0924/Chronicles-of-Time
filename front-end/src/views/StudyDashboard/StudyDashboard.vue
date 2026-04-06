<template>
  <div :class="[isDark ? 'bg-black' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <Nav :isDark="isDark" :menuItems="menuItems"/>

      <!-- 主内容区 -->
      <div class="pt-24 pb-16" :class="isDark ? 'bg-black' : ''">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, shallowRef, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PracticeCenter from '@/views/StudyDashboard/PracticeCenter.vue'
import MistakeBook from '@/views/StudyDashboard/MistakeBook.vue'
import ScoreAnalysis from '@/views/StudyDashboard/ScoreAnalysis.vue'
import QuestionBank from '@/views/StudyDashboard/QuestionBank.vue'
import AnswerRecords from '@/views/StudyDashboard/AnswerRecords.vue'
import Nav from '@/components/Nav.vue'

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
  }
]

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

/* 隐藏滚动条 */
.animate-fadeIn::-webkit-scrollbar {
  display: none;
}
.animate-fadeIn {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
