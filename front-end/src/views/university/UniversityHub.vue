<!--
  文件说明：拾光记前台应用大学阶段页面组件，承载大学阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { useUniversityData } from '@/composables/useUniversityData'
import Nav from '@/components/Nav.vue'
import SemesterSchedule from '@/views/university/features/SemesterSchedule.vue'
import GraduationGap from '@/views/university/features/GraduationGap.vue'
import GpaSimulator from '@/views/university/features/GpaSimulator.vue'
import MajorCompare from '@/views/university/features/MajorCompare.vue'
import CertificateArchive from '@/views/university/features/CertificateArchive.vue'
import ThesisBoard from '@/views/university/features/ThesisBoard.vue'
import ElectiveCommunity from '@/views/university/features/ElectiveCommunity.vue'
import CampusOrganizations from '@/views/university/features/CampusOrganizations.vue'

const route = useRoute()
const router = useRouter()
const { isDark } = useTheme()
const { majors, currentMajorId, loadMajors } = useUniversityData()

const tabs = [
  { key: 'schedule', label: '学期课表', icon: '📅' },
  { key: 'gap', label: '毕业缺口', icon: '📋' },
  { key: 'gpa', label: 'GPA 模拟', icon: '🧮' },
  { key: 'compare', label: '方案对比', icon: '⚖️' },
  { key: 'cert', label: '证书档案', icon: '🏅' },
  { key: 'thesis', label: '论文看板', icon: '📄' },
  { key: 'tree', label: '课程树', icon: '🌳', external: '/CourseTree' },
  { key: 'community', label: '选课参考', icon: '💬' },
  { key: 'campus', label: '社团学生会', icon: '🏛️' },
]

const activeTab = ref(route.query.tab || 'schedule')

const menuItems = [
  { key: 'hub', label: '大学中心', icon: '🎓', path: '/UniversityHub' },
  { key: 'tree', label: '课程树', icon: '🌳', path: '/CourseTree' },
  { key: 'paper', label: '写论文', icon: '📝', path: '/Paper' },
  { key: 'campus', label: '社团学生会', icon: '🏛️', path: '/UniversityHub?tab=campus' },
]

const currentComponent = computed(() => {
  const map = {
    schedule: SemesterSchedule,
    gap: GraduationGap,
    gpa: GpaSimulator,
    compare: MajorCompare,
    cert: CertificateArchive,
    thesis: ThesisBoard,
    community: ElectiveCommunity,
    campus: CampusOrganizations,
  }
  return map[activeTab.value]
})

const switchTab = (key, external) => {
  if (external) {
    router.push(external)
    return
  }
  activeTab.value = key
  router.replace({ query: { ...route.query, tab: key } })
}

watch(() => route.query.tab, (t) => {
  if (t) activeTab.value = t
})

onMounted(() => loadMajors())
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <div class="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-4">
          <div>
            <p class="app-section-label mb-2">大学</p>
            <h1 class="app-page-title">学业中心</h1>
            <p class="app-page-desc">课表、毕业分析、GPA 模拟、方案对比与论文进度</p>
          </div>
          <select
            v-model="currentMajorId"
            class="px-4 py-2.5 rounded-xl text-sm border focus:ring-2 focus:ring-brand-500/50 min-w-[180px]"
            :class="isDark ? 'bg-dark-surface border-dark-border text-zinc-100' : 'bg-white border-zinc-200'"
          >
            <option v-for="m in majors" :key="m.id" :value="m.id">{{ m.name }}</option>
          </select>
        </div>

        <div class="app-nav-pill flex flex-wrap gap-1 !flex md:!flex">
          <button
            v-for="t in tabs"
            :key="t.key"
            type="button"
            class="app-nav-item"
            :class="activeTab === t.key && !t.external ? 'app-nav-item-active' : 'app-nav-item-inactive'"
            @click="switchTab(t.key, t.external)"
          >
            <span class="flex items-center gap-1.5"><span>{{ t.icon }}</span>{{ t.label }}</span>
          </button>
        </div>

        <component
          :is="currentComponent"
          v-if="currentComponent && (currentMajorId || activeTab === 'campus')"
          :major-id="currentMajorId"
          :is-dark="isDark"
        />
        <div v-else-if="!currentMajorId" class="app-card-surface p-8 text-center text-zinc-500">
          请先在「准备」页绑定专业，或添加专业数据
        </div>
      </div>
    </main>
  </div>
</template>
