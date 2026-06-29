<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const { isDark } = useTheme()

const tabs = [
  { key: 'timeline', label: '成长贯通', icon: '⏳' },
  { key: 'milestones', label: '里程碑', icon: '🏆' },
  { key: 'ai', label: 'AI 建议', icon: '🤖' },
]

const activeTab = ref(route.query.tab || 'timeline')
const timeline = ref([])
const milestones = ref([])
const advice = ref([])
const majorId = ref(null)
const loading = ref(false)

const menuItems = [
  { key: 'growth', label: '成长贯通', icon: '🔗', path: '/GrowthHub' },
  { key: 'records', label: '记录拾光', icon: '📖', path: '/Records' },
  { key: 'high', label: '高中中心', icon: '🏫', path: '/HighSchoolHub' },
  { key: 'uni', label: '大学中心', icon: '🎓', path: '/UniversityHub' },
]

const switchTab = (key) => {
  activeTab.value = key
  router.replace({ query: { tab: key } })
}

const loadTimeline = async () => {
  const items = []
  const g = await request.post('/growth/list', { page: 1, size: 30 })
  if (g.code === 200) {
    (g.data?.records || g.data?.list || []).forEach((r) => {
      items.push({ type: 'growth', stage: r.stage, title: r.examName || r.activityName || '成长记录', date: r.recordDate, link: '/Records' })
    })
  }
  try {
    const p = await request.get('/student-course/progress', { majorId: majorId.value })
    if (p.code === 200 && p.data) {
      items.push({ type: 'university', stage: '大学', title: `毕业进度 ${p.data.progressPercent}%`, date: new Date().toISOString().slice(0, 10), link: '/UniversityHub?tab=gap' })
    }
  } catch (_) {}
  items.sort((a, b) => (b.date || '').localeCompare(a.date || ''))
  timeline.value = items
}

const loadMilestones = async () => {
  const res = await request.get('/growth/milestones')
  if (res.code === 200) milestones.value = res.data || []
}

const loadAdvice = async () => {
  const tips = []
  const u0 = await request.get('/user/getUserById')
  const uid0 = u0?.data?.id || 1
  const weak = await request.get('/score/weak-subject', { userId: uid0 }).catch(() => null)
  if (weak?.code === 200 && weak.data) {
    tips.push({ title: '薄弱科目', content: `建议优先复习：${weak.data}`, priority: 'high' })
  }
  if (majorId.value) {
    const gap = await request.get('/student-course/gap-analysis', { majorId: majorId.value })
    if (gap.code === 200 && gap.data?.missingCompulsory?.length) {
      tips.push({ title: '毕业缺口', content: `还有 ${gap.data.missingCompulsory.length} 门必修未通过`, priority: 'high', link: '/UniversityHub?tab=gap' })
    }
  }
  const u = await request.get('/user/getUserById')
  const uid = u?.data?.id || 1
  const m = await request.get(`/mistake/list/${uid}`).catch(() => null)
  if (m?.code === 200 && m.data?.length) {
    tips.push({ title: '错题本', content: `共有 ${m.data.length} 道错题待巩固`, priority: 'medium', link: '/StudyDashboard?tab=mistake' })
  }
  if (!tips.length) tips.push({ title: '保持节奏', content: '继续记录成长与修读课程，系统将给出更精准建议', priority: 'low' })
  advice.value = tips
}

const loadAll = async () => {
  loading.value = true
  const ui = await request.get('/userInfo/getCurrent').catch(() => null)
  if (ui?.code === 200) majorId.value = ui.data?.majorId
  await Promise.all([loadTimeline(), loadMilestones(), loadAdvice()])
  loading.value = false
}

watch(activeTab, (t) => {
  if (t === 'milestones') loadMilestones()
  if (t === 'ai') loadAdvice()
})

onMounted(loadAll)
watch(() => route.query.tab, (t) => { if (t) activeTab.value = t })
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <div>
          <p class="app-section-label mb-2">跨学段</p>
          <h1 class="app-page-title">成长贯通</h1>
          <p class="app-page-desc">高中 → 志愿 → 大学课程 → 论文，一条时间线串联</p>
        </div>
        <div class="flex flex-wrap gap-1 app-nav-pill !flex">
          <button
            v-for="t in tabs"
            :key="t.key"
            type="button"
            class="app-nav-item"
            :class="activeTab === t.key ? 'app-nav-item-active' : 'app-nav-item-inactive'"
            @click="switchTab(t.key)"
          >
            {{ t.icon }} {{ t.label }}
          </button>
        </div>

        <div v-if="loading" class="text-zinc-500 text-center py-8">加载中...</div>

        <div v-else-if="activeTab === 'timeline'" class="relative pl-8 border-l-2 border-brand-200 dark:border-brand-800 space-y-6">
          <div v-for="(item, i) in timeline" :key="i" class="relative">
            <span class="absolute -left-[25px] w-3 h-3 rounded-full bg-brand-500" />
            <div class="app-card-surface p-4 cursor-pointer hover:ring-2 hover:ring-brand-200 transition-all" @click="router.push(item.link)">
              <span class="text-xs text-brand-600">{{ item.stage }}</span>
              <p class="font-medium">{{ item.title }}</p>
              <p class="text-xs text-zinc-500">{{ item.date }}</p>
            </div>
          </div>
          <p v-if="!timeline.length" class="text-zinc-500">暂无贯通记录</p>
        </div>

        <div v-else-if="activeTab === 'milestones'" class="grid md:grid-cols-2 gap-4">
          <div v-for="m in milestones" :key="m.id" class="app-card-surface p-4">
            <span v-if="m.isMilestone" class="app-pill-tag mb-2">里程碑</span>
            <p class="font-medium">{{ m.examName || m.activityName || m.competitionName || '重要时刻' }}</p>
            <p class="text-sm text-zinc-500">{{ m.recordDate }} · {{ m.stage }}</p>
          </div>
          <p v-if="!milestones.length" class="text-zinc-500 col-span-2 text-center">在记录中标记「里程碑」后自动出现在此</p>
        </div>

        <div v-else-if="activeTab === 'ai'" class="space-y-3">
          <div
            v-for="(a, i) in advice"
            :key="i"
            class="app-card-surface p-4 flex justify-between items-start cursor-pointer"
            @click="a.link && router.push(a.link)"
          >
            <div>
              <p class="font-semibold text-brand-700 dark:text-brand-300">{{ a.title }}</p>
              <p class="text-sm text-zinc-600 mt-1">{{ a.content }}</p>
            </div>
            <span class="text-xs px-2 py-0.5 rounded-full" :class="a.priority === 'high' ? 'bg-red-100 text-red-600' : 'bg-zinc-100 text-zinc-600'">{{ a.priority }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
