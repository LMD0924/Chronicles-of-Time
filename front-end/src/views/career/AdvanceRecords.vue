<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const router = useRouter()
const { isDark } = useTheme()
const STORAGE = 'career_advance_v1'

const okrs = ref([
  { id: 1, objective: '本年度晋升/加薪', keyResults: ['完成 2 个核心项目', '带教 1 名新人', '绩效达到 A'], progress: 30 },
])
const projects = ref([])
const milestones = ref([])

const load = async () => {
  const saved = localStorage.getItem(STORAGE)
  if (saved) {
    const data = JSON.parse(saved)
    if (data.okrs) okrs.value = data.okrs
  }
  try {
    const res = await request.get('/resume/getCompleteResume')
    if (res.code === 200 && res.data?.projects) {
      projects.value = res.data.projects.slice(0, 6)
    }
  } catch (_) {}
  try {
    const m = await request.get('/growth/milestones')
    if (m.code === 200) milestones.value = (m.data || []).slice(0, 8)
  } catch (_) {}
}

const save = () => localStorage.setItem(STORAGE, JSON.stringify({ okrs: okrs.value }))

const addOkr = () => {
  okrs.value.push({
    id: Date.now(),
    objective: '新目标',
    keyResults: ['关键结果 1'],
    progress: 0,
  })
  save()
}

onMounted(load)
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <div>
          <p class="app-section-label mb-2">职场</p>
          <h1 class="app-page-title">进阶之路</h1>
          <p class="app-page-desc">OKR、项目成果与里程碑（25 岁+）</p>
        </div>

        <div class="app-card-surface p-6 space-y-4">
          <div class="flex justify-between items-center">
            <h3 class="font-semibold">年度 OKR</h3>
            <button type="button" class="app-btn-secondary text-sm" @click="addOkr">+ 添加</button>
          </div>
          <div v-for="o in okrs" :key="o.id" class="p-4 rounded-xl border" :class="isDark ? 'border-dark-border bg-zinc-900/50' : 'border-zinc-100 bg-zinc-50'">
            <p class="font-medium mb-2">{{ o.objective }}</p>
            <ul class="text-sm text-zinc-600 space-y-1 mb-2">
              <li v-for="(kr, i) in o.keyResults" :key="i">· {{ kr }}</li>
            </ul>
            <div class="h-1.5 rounded-full bg-zinc-200 dark:bg-zinc-700">
              <div class="h-full app-gradient-bar rounded-full" :style="{ width: (o.progress || 0) + '%' }" />
            </div>
            <input v-model.number="o.progress" type="range" min="0" max="100" class="w-full mt-2" @change="save" />
          </div>
        </div>

        <div class="grid lg:grid-cols-2 gap-6">
          <div class="app-card-surface p-5">
            <h3 class="font-semibold mb-3">简历项目成果</h3>
            <div v-for="p in projects" :key="p.id" class="py-3 border-b last:border-0 text-sm">
              <p class="font-medium">{{ p.projectName }}</p>
              <p class="text-zinc-500 text-xs">{{ p.projectRole }} · {{ p.startDate }} - {{ p.endDate || '至今' }}</p>
            </div>
            <p v-if="!projects.length" class="text-zinc-500 text-sm">在简历中填写项目后在此展示</p>
            <button type="button" class="app-btn-primary text-sm mt-3" @click="router.push('/Resume')">编辑简历</button>
          </div>
          <div class="app-card-surface p-5">
            <h3 class="font-semibold mb-3">成长里程碑</h3>
            <ul class="space-y-2 text-sm">
              <li v-for="m in milestones" :key="m.id" class="py-2 border-b last:border-0">
                <span class="app-pill-tag text-xs mb-1">里程碑</span>
                <p>{{ m.examName || m.activityName || m.competitionName || '重要时刻' }}</p>
                <p class="text-zinc-400 text-xs">{{ m.recordDate }} · {{ m.stage }}</p>
              </li>
            </ul>
            <button type="button" class="app-btn-secondary text-sm mt-3" @click="router.push('/GrowthHub')">成长贯通</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
