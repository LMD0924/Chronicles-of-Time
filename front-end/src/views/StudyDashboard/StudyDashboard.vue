<!--
  文件说明：拾光记前台应用数据驾驶舱页面组件，承载数据驾驶舱场景的界面展示、交互操作和数据承接。
-->
<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
      <Nav :isDark="isDark" :menuItems="menuItems"/>

      <main class="app-main">
        <div class="app-container">
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
      </main>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import PracticeCenter from '@/views/StudyDashboard/PracticeCenter.vue'
import MistakeBook from '@/views/StudyDashboard/MistakeBook.vue'
import ScoreAnalysis from '@/views/StudyDashboard/ScoreAnalysis.vue'
import QuestionBank from '@/views/StudyDashboard/QuestionBank.vue'
import AnswerRecords from '@/views/StudyDashboard/AnswerRecords.vue'
import Nav from '@/components/Nav.vue'

defineProps({
  studentId: { type: [String, Number], default: 1 }
})

const route = useRoute()
const router = useRouter()
const { isDark } = useTheme()

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
