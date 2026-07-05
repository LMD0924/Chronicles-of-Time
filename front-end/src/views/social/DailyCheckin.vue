<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { getStoredTheme, ThemeType } from '@/utils/theme.js'

const isDark = ref(getStoredTheme() === ThemeType.DARK)
const loading = ref(false)
const checking = ref(false)
const summary = ref({
  totalLoginDays: 0,
  continuousLoginDays: 0,
  maxContinuousLoginDays: 0,
  totalOnlineSeconds: 0,
  todayOnlineSeconds: 0,
  medalScore: 0,
  checkedInToday: false,
  medals: [],
  newlyAwarded: [],
})
let heartbeatTimer = null

const menuItems = [
  { key: 'checkin', label: '每日打卡', icon: '✅', path: '/DailyCheckin' },
  { key: 'chat', label: '在线聊天', icon: '💬', path: '/Chat' },
  { key: 'records', label: '记录拾光', icon: '📝', path: '/Records' },
]

const statCards = computed(() => [
  { label: '累计登录', value: summary.value.totalLoginDays || 0, suffix: '天', tone: 'emerald' },
  { label: '连续登录', value: summary.value.continuousLoginDays || 0, suffix: '天', tone: 'blue' },
  { label: '今日在线', value: formatDuration(summary.value.todayOnlineSeconds), suffix: '', tone: 'violet' },
  { label: '活跃积分', value: summary.value.medalScore || 0, suffix: '分', tone: 'amber' },
])

const progressText = computed(() => {
  if (summary.value.checkedInToday) return '今日已完成打卡'
  return '今日还未打卡'
})

const formatDuration = (seconds = 0) => {
  const safe = Number(seconds || 0)
  const hours = Math.floor(safe / 3600)
  const minutes = Math.floor((safe % 3600) / 60)
  if (hours > 0) return `${hours}h ${minutes}m`
  return `${minutes}m`
}

const fetchSummary = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/summary')
    summary.value = { ...summary.value, ...(res.data || {}) }
  } finally {
    loading.value = false
  }
}

const checkIn = async () => {
  checking.value = true
  try {
    const res = await request.post('/activity/checkin', {})
    summary.value = { ...summary.value, ...(res.data || {}) }
    const count = summary.value.newlyAwarded?.length || 0
    ElMessage.success(count ? `打卡成功，获得 ${count} 枚新勋章` : '打卡成功')
  } finally {
    checking.value = false
  }
}

const heartbeat = async () => {
  try {
    const res = await request.post('/activity/heartbeat', { activeSeconds: 60 })
    summary.value = { ...summary.value, ...(res.data || {}) }
  } catch {
    clearInterval(heartbeatTimer)
  }
}

onMounted(() => {
  fetchSummary()
  heartbeat()
  heartbeatTimer = setInterval(heartbeat, 60000)
})

onUnmounted(() => {
  clearInterval(heartbeatTimer)
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-white" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />

    <main class="mx-auto max-w-7xl px-4 pb-16 pt-28 sm:px-6 lg:px-8">
      <section class="checkin-hero">
        <div>
          <p class="text-sm font-semibold text-emerald-600">Activity Center</p>
          <h1 class="mt-2 text-4xl font-bold tracking-tight md:text-5xl">每日打卡与在线勋章</h1>
          <p class="mt-3 max-w-2xl text-slate-500 dark:text-slate-300">
            记录登录天数、连续登录和在线时长，系统会根据活跃算法自动发放勋章。
          </p>
        </div>
        <button
          class="rounded-xl bg-emerald-600 px-6 py-3 font-semibold text-white shadow-lg shadow-emerald-600/20 transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="checking || summary.checkedInToday"
          @click="checkIn"
        >
          {{ summary.checkedInToday ? '今日已打卡' : checking ? '打卡中...' : '立即打卡' }}
        </button>
      </section>

      <section class="mt-8 grid gap-4 md:grid-cols-4">
        <article v-for="card in statCards" :key="card.label" class="stat-card" :class="`tone-${card.tone}`">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}<em>{{ card.suffix }}</em></strong>
        </article>
      </section>

      <section class="mt-8 grid gap-6 lg:grid-cols-[0.85fr_1.15fr]">
        <article class="rounded-2xl border border-white/70 bg-white p-6 shadow-sm dark:border-white/10 dark:bg-slate-900">
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-xl font-semibold">今日状态</h2>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ progressText }}</p>
            </div>
            <span class="rounded-full px-3 py-1 text-sm font-medium" :class="summary.checkedInToday ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'">
              {{ summary.checkedInToday ? '已完成' : '待完成' }}
            </span>
          </div>
          <div class="mt-8 space-y-5">
            <div>
              <div class="mb-2 flex justify-between text-sm">
                <span>今日在线</span>
                <span>{{ formatDuration(summary.todayOnlineSeconds) }}</span>
              </div>
              <div class="h-3 rounded-full bg-slate-100 dark:bg-slate-800">
                <div class="h-3 rounded-full bg-emerald-500" :style="{ width: Math.min((summary.todayOnlineSeconds || 0) / 3600 * 100, 100) + '%' }"></div>
              </div>
            </div>
            <div>
              <div class="mb-2 flex justify-between text-sm">
                <span>最长连续</span>
                <span>{{ summary.maxContinuousLoginDays || 0 }} 天</span>
              </div>
              <div class="h-3 rounded-full bg-slate-100 dark:bg-slate-800">
                <div class="h-3 rounded-full bg-blue-500" :style="{ width: Math.min((summary.maxContinuousLoginDays || 0) / 30 * 100, 100) + '%' }"></div>
              </div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4 text-sm dark:bg-slate-800">
              累计在线：<strong>{{ formatDuration(summary.totalOnlineSeconds) }}</strong>
              <span class="mx-2 text-slate-300">/</span>
              最近在线：{{ summary.lastSeenAt ? summary.lastSeenAt.replace('T', ' ') : '暂无' }}
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-white/70 bg-white p-6 shadow-sm dark:border-white/10 dark:bg-slate-900">
          <div class="flex items-center justify-between">
            <h2 class="text-xl font-semibold">我的勋章</h2>
            <span class="text-sm text-slate-500">{{ summary.medals?.length || 0 }} 枚</span>
          </div>
          <div v-if="loading" class="py-16 text-center text-slate-400">加载中...</div>
          <div v-else-if="!summary.medals?.length" class="py-16 text-center text-slate-400">还没有勋章，完成打卡后会自动获得。</div>
          <div v-else class="mt-5 grid gap-3 sm:grid-cols-2">
            <div v-for="medal in summary.medals" :key="medal.id" class="medal-card">
              <div class="medal-icon" :style="{ color: medal.color || '#2563eb' }">🏅</div>
              <div>
                <h3 class="font-semibold">{{ medal.name }}</h3>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ medal.description }}</p>
                <p class="mt-2 text-xs text-slate-400">达成值：{{ medal.sourceValue }}</p>
              </div>
            </div>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<style scoped>
.checkin-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(236, 253, 245, 0.86));
  padding: 32px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.dark .checkin-hero {
  border-color: rgba(255, 255, 255, 0.08);
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.98), rgba(6, 78, 59, 0.56));
}

.stat-card {
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: white;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.dark .stat-card {
  border-color: rgba(255, 255, 255, 0.08);
  background: #0f172a;
}

.stat-card span {
  color: #64748b;
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  line-height: 1;
}

.stat-card em {
  margin-left: 4px;
  font-size: 13px;
  font-style: normal;
}

.tone-emerald strong { color: #059669; }
.tone-blue strong { color: #2563eb; }
.tone-violet strong { color: #7c3aed; }
.tone-amber strong { color: #d97706; }

.medal-card {
  display: flex;
  gap: 14px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.86);
  padding: 14px;
}

.dark .medal-card {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(30, 41, 59, 0.72);
}

.medal-icon {
  display: grid;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  place-items: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 22px;
}

@media (max-width: 768px) {
  .checkin-hero {
    align-items: stretch;
    flex-direction: column;
    padding: 24px;
  }
}
</style>
