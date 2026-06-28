<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const router = useRouter()
const { isDark } = useTheme()
const STORAGE = 'work_onboarding_v1'

const checklist = ref([
  { id: 1, label: '完善个人简历', done: false, link: '/Resume' },
  { id: 2, label: '绑定大学/专业信息', done: false, link: '/PrePare' },
  { id: 3, label: '记录首周工作日志', done: false, link: '/Records' },
  { id: 4, label: '设定 90 天学习目标', done: false },
  { id: 5, label: '了解团队与汇报关系', done: false },
])
const workLogs = ref([])
const newLog = ref('')
const skills = ref(['沟通表达', 'Excel/PPT', '业务理解', '时间管理'])
const progress = computed(() => {
  const d = checklist.value.filter((c) => c.done).length
  return Math.round((d / checklist.value.length) * 100)
})

const load = () => {
  const saved = localStorage.getItem(STORAGE)
  if (saved) {
    const data = JSON.parse(saved)
    if (data.checklist) checklist.value = data.checklist
    if (data.logs) workLogs.value = data.logs
  }
  loadGrowth()
}

const save = () => {
  localStorage.setItem(STORAGE, JSON.stringify({ checklist: checklist.value, logs: workLogs.value }))
}

const loadGrowth = async () => {
  try {
    const res = await request.post('/growth/list', { page: 1, size: 10, stage: '职场' })
    const list = res.data?.records || res.data?.list || res.data || []
    workLogs.value = [
      ...workLogs.value,
      ...list.map((r) => ({
        id: 'g-' + r.id,
        text: r.activityName || r.studyNotes || r.competitionName || '职场记录',
        date: r.recordDate,
        fromGrowth: true,
      })),
    ].slice(0, 15)
  } catch (_) {}
}

const toggleCheck = (item) => {
  item.done = !item.done
  save()
}

const addLog = () => {
  if (!newLog.value.trim()) return
  workLogs.value.unshift({ id: Date.now(), text: newLog.value, date: new Date().toISOString().slice(0, 10) })
  newLog.value = ''
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
          <h1 class="app-page-title">职场新人</h1>
          <p class="app-page-desc">入职适应、工作日志与基础能力清单（22–25 岁）</p>
        </div>

        <div class="app-card-surface p-6">
          <div class="flex justify-between mb-3">
            <h3 class="font-semibold">入职适应进度</h3>
            <span class="text-brand-600 font-bold">{{ progress }}%</span>
          </div>
          <div class="h-2 rounded-full bg-zinc-100 dark:bg-zinc-800 mb-4">
            <div class="h-full app-gradient-bar rounded-full" :style="{ width: progress + '%' }" />
          </div>
          <ul class="space-y-2">
            <li v-for="item in checklist" :key="item.id" class="flex items-center gap-3 text-sm">
              <input type="checkbox" :checked="item.done" @change="toggleCheck(item)" />
              <span :class="item.done ? 'line-through text-zinc-400' : ''">{{ item.label }}</span>
              <button v-if="item.link" type="button" class="text-brand-600 text-xs ml-auto" @click="router.push(item.link)">前往</button>
            </li>
          </ul>
        </div>

        <div class="grid lg:grid-cols-2 gap-6">
          <div class="app-card-surface p-5">
            <h3 class="font-semibold mb-3">工作日志</h3>
            <div class="flex gap-2 mb-3">
              <input v-model="newLog" placeholder="记录今日收获或问题..." class="flex-1 px-3 py-2 rounded-lg border text-sm" />
              <button type="button" class="app-btn-primary text-sm" @click="addLog">添加</button>
            </div>
            <ul class="space-y-2 text-sm max-h-64 overflow-y-auto">
              <li v-for="log in workLogs" :key="log.id" class="py-2 border-b last:border-0">
                <span class="text-zinc-400 text-xs">{{ log.date }}</span>
                <p>{{ log.text }}</p>
              </li>
            </ul>
          </div>
          <div class="app-card-surface p-5">
            <h3 class="font-semibold mb-3">建议优先提升</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="s in skills" :key="s" class="app-pill-tag">{{ s }}</span>
            </div>
            <button type="button" class="app-btn-secondary text-sm mt-4" @click="router.push('/StudyDashboard')">去学习中心练基本功</button>
            <button type="button" class="app-btn-primary text-sm mt-2 w-full" @click="router.push('/Resume')">完善简历</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
