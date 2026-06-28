<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import SubjectComboTree from '@/views/high/features/SubjectComboTree.vue'
import TargetUniversityPath from '@/views/high/features/TargetUniversityPath.vue'
import SubjectRadarPanel from '@/views/high/features/SubjectRadarPanel.vue'
import ReviewPlan from '@/views/high/features/ReviewPlan.vue'
import QualityRecords from '@/views/high/features/QualityRecords.vue'
import StrongBaseChecklist from '@/views/high/features/StrongBaseChecklist.vue'
import ParentReadOnlyView from '@/views/high/features/ParentReadOnlyView.vue'

const route = useRoute()
const router = useRouter()
const { isDark } = useTheme()

const tabs = [
  { key: 'combo', label: '选科组合', icon: '🌳' },
  { key: 'target', label: '目标院校', icon: '🎯' },
  { key: 'radar', label: '学科雷达', icon: '📡' },
  { key: 'plan', label: '复习计划', icon: '📆' },
  { key: 'quality', label: '综评记录', icon: '📝' },
  { key: 'strong', label: '强基清单', icon: '✅' },
  { key: 'parent', label: '家长视图', icon: '👪' },
  { key: 'selection', label: '选科中心', icon: '📚', external: '/CourseSelection' },
  { key: 'volunteer', label: '志愿填报', icon: '🎓', external: '/Volunteer' },
]

const activeTab = ref(route.query.tab || 'combo')
const menuItems = [
  { key: 'hub', label: '高中中心', icon: '🏫', path: '/HighSchoolHub' },
  { key: 'selection', label: '选科', icon: '📚', path: '/CourseSelection' },
  { key: 'volunteer', label: '志愿', icon: '🎓', path: '/Volunteer' },
  { key: 'study', label: '学习', icon: '⚡', path: '/StudyDashboard?tab=practice' },
]

const currentComponent = computed(() => {
  const map = {
    combo: SubjectComboTree,
    target: TargetUniversityPath,
    radar: SubjectRadarPanel,
    plan: ReviewPlan,
    quality: QualityRecords,
    strong: StrongBaseChecklist,
    parent: ParentReadOnlyView,
  }
  return map[activeTab.value]
})

const switchTab = (key, external) => {
  if (external) { router.push(external); return }
  activeTab.value = key
  router.replace({ query: { ...route.query, tab: key } })
}

watch(() => route.query.tab, (t) => { if (t) activeTab.value = t })
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <div>
          <p class="app-section-label mb-2">高中</p>
          <h1 class="app-page-title">升学中心</h1>
          <p class="app-page-desc">选科、志愿、学科能力与复习规划一站管理</p>
        </div>
        <div class="flex flex-wrap gap-1 app-nav-pill !flex">
          <button
            v-for="t in tabs"
            :key="t.key"
            type="button"
            class="app-nav-item"
            :class="activeTab === t.key && !t.external ? 'app-nav-item-active' : 'app-nav-item-inactive'"
            @click="switchTab(t.key, t.external)"
          >
            {{ t.icon }} {{ t.label }}
          </button>
        </div>
        <component :is="currentComponent" v-if="currentComponent" :is-dark="isDark" />
      </div>
    </main>
  </div>
</template>
