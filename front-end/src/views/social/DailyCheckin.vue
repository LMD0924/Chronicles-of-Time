<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Calendar as CalendarCheck, ChatDotRound, Check, EditPen, MagicStick, Star, Timer, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { getStoredTheme, ThemeType } from '@/utils/theme.js'

const router = useRouter()
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
  level: 1,
  levelName: '初见',
  growthExperience: 0,
  currentLevelExperience: 0,
  nextLevelExperience: 120,
  levelProgress: 0,
  publishedArticleCount: 0,
  completedPracticeCount: 0,
  growthTasks: [],
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
  { label: '累计打卡', value: summary.value.totalLoginDays || 0, suffix: '天', icon: CalendarCheck },
  { label: '最长连续', value: summary.value.maxContinuousLoginDays || 0, suffix: '天', icon: Trophy },
  { label: '发表文章', value: summary.value.publishedArticleCount || 0, suffix: '篇', icon: EditPen },
  { label: '完成练习', value: summary.value.completedPracticeCount || 0, suffix: '次', icon: MagicStick },
])

const taskIcons = { CalendarCheck, EditPen, MagicStick, Timer }
const completedTaskCount = computed(() => summary.value.growthTasks?.filter(task => task.completed).length || 0)
const currentLevelExperience = computed(() => Math.max(0,
  (summary.value.growthExperience || 0) - (summary.value.currentLevelExperience || 0)))
const nextLevelExperience = computed(() => Math.max(1,
  (summary.value.nextLevelExperience || 1) - (summary.value.currentLevelExperience || 0)))

const progressText = computed(() => {
  if (summary.value.checkedInToday) return '今天的足迹已经留下'
  return '完成打卡，开启今天的成长任务'
})

const goTask = (path) => {
  if (path) router.push(path)
}

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
  <div class="app-shell app-page-bg min-h-screen" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />

    <main class="growth-page mx-auto max-w-7xl px-4 pb-16 pt-28 sm:px-6 lg:px-8">
      <header class="page-heading">
        <div>
          <span class="section-kicker"><Star /> 成长中心</span>
          <h1>今天也积累一点点</h1>
          <p>{{ progressText }}</p>
        </div>
        <div class="heading-actions">
          <button class="soft-action" type="button" @click="router.push('/Chat')">
            <ChatDotRound />
            <span>在线聊天</span>
          </button>
          <button
            class="checkin-button"
            type="button"
            :disabled="checking || summary.checkedInToday"
            @click="checkIn"
          >
            <Check v-if="summary.checkedInToday" />
            <CalendarCheck v-else />
            <span>{{ summary.checkedInToday ? '今日已打卡' : checking ? '打卡中...' : '立即打卡' }}</span>
          </button>
        </div>
      </header>

      <section class="growth-overview">
        <div class="level-block">
          <span class="level-mark"><Trophy /></span>
          <div>
            <small>当前等级</small>
            <div class="level-title"><strong>Lv.{{ summary.level || 1 }}</strong><span>{{ summary.levelName }}</span></div>
          </div>
        </div>
        <div class="experience-block">
          <div class="experience-copy">
            <span>成长经验</span>
            <strong>{{ currentLevelExperience }} / {{ nextLevelExperience }}</strong>
          </div>
          <div class="experience-track" role="progressbar" :aria-valuenow="summary.levelProgress || 0" aria-valuemin="0" aria-valuemax="100">
            <i :style="{ width: `${summary.levelProgress || 0}%` }"></i>
          </div>
          <p>累计 {{ summary.growthExperience || 0 }} EXP，再前进一点就会升级。</p>
        </div>
        <div class="today-score">
          <small>今日任务</small>
          <strong>{{ completedTaskCount }}<span>/{{ summary.growthTasks?.length || 4 }}</span></strong>
        </div>
      </section>

      <section class="stats-grid">
        <article
          v-for="(card, index) in statCards"
          :key="card.label"
          class="growth-stat motion-card"
          :style="{ '--delay': `${index * 70}ms` }"
        >
          <span class="stat-icon"><component :is="card.icon" /></span>
          <div>
            <small>{{ card.label }}</small>
            <strong>{{ card.value }}<em>{{ card.suffix }}</em></strong>
          </div>
        </article>
      </section>

      <section class="content-grid">
        <div class="task-section">
          <div class="section-heading">
            <div>
              <span class="section-kicker">Daily quests</span>
              <h2>今日成长任务</h2>
            </div>
            <span class="task-count">{{ completedTaskCount }} 项完成</span>
          </div>
          <div class="task-list">
            <button
              v-for="task in summary.growthTasks"
              :key="task.key"
              class="growth-task motion-card"
              :class="{ completed: task.completed }"
              type="button"
              @click="goTask(task.actionPath)"
            >
              <span class="task-icon">
                <Check v-if="task.completed" />
                <component :is="taskIcons[task.icon] || Star" v-else />
              </span>
              <span class="task-copy">
                <strong>{{ task.title }}</strong>
                <small>{{ task.description }}</small>
                <span class="task-progress"><i :style="{ width: `${Math.min((task.current || 0) / Math.max(task.target || 1, 1) * 100, 100)}%` }"></i></span>
              </span>
              <span class="task-reward">+{{ task.rewardExperience }} EXP</span>
              <ArrowRight class="task-arrow" />
            </button>
          </div>
        </div>

        <aside class="medal-section">
          <div class="section-heading">
            <div>
              <span class="section-kicker">Collection</span>
              <h2>我的勋章</h2>
            </div>
            <span class="task-count">{{ summary.medals?.length || 0 }} 枚</span>
          </div>
          <div v-if="loading" class="empty-panel">加载中...</div>
          <div v-else-if="!summary.medals?.length" class="empty-panel">
            <Trophy />
            <strong>第一枚勋章正在路上</strong>
            <span>完成每日打卡后就有机会获得。</span>
          </div>
          <div v-else class="medal-list">
            <article v-for="medal in summary.medals" :key="medal.id" class="medal-item motion-card">
              <span class="medal-icon" :style="{ color: medal.color || 'var(--theme-primary)' }"><Trophy /></span>
              <div>
                <strong>{{ medal.name }}</strong>
                <p>{{ medal.description }}</p>
                <small>达成值 {{ medal.sourceValue }}</small>
              </div>
            </article>
          </div>
        </aside>
      </section>
    </main>
  </div>
</template>

<style scoped>
.growth-page {
  color: var(--app-text);
}

.page-heading,
.growth-overview,
.level-block,
.experience-copy,
.heading-actions,
.section-heading,
.growth-task,
.growth-stat {
  display: flex;
  align-items: center;
}

.page-heading {
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  animation: rise-in 560ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.page-heading h1 {
  margin-top: 6px;
  color: var(--app-text);
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 800;
  line-height: 1.12;
}

.page-heading p {
  margin-top: 8px;
  color: var(--app-text-muted);
}

.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.section-kicker svg {
  width: 14px;
  height: 14px;
}

.heading-actions {
  gap: 10px;
}

.soft-action,
.checkin-button {
  display: inline-flex;
  min-height: 42px;
  align-items: center;
  gap: 8px;
  border-radius: 8px;
  padding: 0 16px;
  font-weight: 750;
  transition: transform 280ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 280ms ease, border-color 200ms ease;
}

.soft-action {
  border: 1px solid var(--app-border);
  background: var(--app-card-solid);
  color: var(--app-text-secondary);
}

.checkin-button {
  border: 1px solid var(--theme-primary);
  background: var(--theme-primary);
  color: white;
  box-shadow: 0 14px 28px -20px rgb(var(--theme-primary-rgb) / 0.9);
}

.soft-action:hover,
.checkin-button:not(:disabled):hover {
  transform: translateY(-2px);
}

.checkin-button:disabled {
  cursor: default;
  opacity: 0.72;
}

.soft-action svg,
.checkin-button svg {
  width: 18px;
  height: 18px;
}

.growth-overview {
  display: grid;
  grid-template-columns: minmax(180px, 0.75fr) minmax(280px, 1.5fr) minmax(120px, 0.5fr);
  gap: 24px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--app-card-solid) 90%, var(--theme-primary) 10%);
  padding: 24px;
  box-shadow: var(--app-elevation);
  animation: rise-in 620ms 70ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.level-block {
  gap: 12px;
}

.level-mark,
.stat-icon,
.task-icon,
.medal-icon {
  display: grid;
  place-items: center;
  border-radius: 8px;
}

.level-mark {
  width: 48px;
  height: 48px;
  background: var(--theme-primary);
  color: white;
  box-shadow: 0 12px 24px -18px rgb(var(--theme-primary-rgb) / 0.9);
}

.level-mark svg {
  width: 24px;
  height: 24px;
}

.level-block small,
.today-score small,
.growth-stat small,
.experience-block p {
  color: var(--app-text-muted);
  font-size: 12px;
}

.level-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 3px;
}

.level-title strong {
  font-size: 24px;
}

.level-title span {
  color: var(--theme-primary);
  font-size: 13px;
  font-weight: 800;
}

.experience-copy {
  justify-content: space-between;
  gap: 12px;
  color: var(--app-text-secondary);
  font-size: 13px;
}

.experience-track,
.task-progress {
  overflow: hidden;
  border-radius: 4px;
  background: rgb(var(--theme-primary-rgb) / 0.12);
}

.experience-track {
  height: 9px;
  margin: 9px 0 7px;
}

.experience-track i,
.task-progress i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--theme-primary);
  transition: width 800ms cubic-bezier(0.16, 1, 0.3, 1);
}

.today-score {
  border-left: 1px solid var(--app-border);
  padding-left: 24px;
  text-align: right;
}

.today-score strong {
  display: block;
  margin-top: 4px;
  color: var(--theme-primary);
  font-size: 30px;
}

.today-score strong span {
  color: var(--app-text-muted);
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.growth-stat {
  gap: 12px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  padding: 16px;
  animation: rise-in 520ms var(--delay) cubic-bezier(0.16, 1, 0.3, 1) both;
}

.stat-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  background: rgb(var(--theme-primary-rgb) / 0.1);
  color: var(--theme-primary);
}

.stat-icon svg {
  width: 19px;
  height: 19px;
}

.growth-stat strong {
  display: block;
  margin-top: 3px;
  font-size: 22px;
}

.growth-stat em {
  margin-left: 3px;
  color: var(--app-text-muted);
  font-size: 12px;
  font-style: normal;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(300px, 0.75fr);
  gap: 28px;
  margin-top: 36px;
}

.section-heading {
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-heading h2 {
  margin-top: 3px;
  font-size: 20px;
  font-weight: 800;
}

.task-count {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: rgb(var(--theme-primary-rgb) / 0.07);
  padding: 5px 9px;
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 750;
}

.task-list,
.medal-list {
  display: grid;
  gap: 10px;
}

.growth-task {
  width: 100%;
  gap: 12px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  padding: 14px;
  text-align: left;
  transition: transform 320ms cubic-bezier(0.16, 1, 0.3, 1), border-color 220ms ease, box-shadow 320ms ease;
}

.growth-task:hover {
  border-color: var(--app-border-strong);
  box-shadow: var(--app-elevation);
  transform: translateX(4px);
}

.growth-task.completed {
  background: color-mix(in srgb, var(--app-card-solid) 92%, var(--theme-secondary) 8%);
}

.task-icon {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  background: rgb(var(--theme-primary-rgb) / 0.1);
  color: var(--theme-primary);
}

.completed .task-icon {
  background: var(--theme-primary);
  color: white;
  animation: task-pop 460ms cubic-bezier(0.16, 1, 0.3, 1);
}

.task-icon svg,
.medal-icon svg {
  width: 19px;
  height: 19px;
}

.task-copy {
  display: grid;
  min-width: 0;
  flex: 1;
}

.task-copy strong {
  color: var(--app-text);
  font-size: 14px;
}

.task-copy small {
  overflow: hidden;
  margin-top: 2px;
  color: var(--app-text-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-progress {
  width: min(180px, 100%);
  height: 4px;
  margin-top: 8px;
}

.task-reward {
  color: var(--theme-primary);
  font-size: 11px;
  font-weight: 800;
  white-space: nowrap;
}

.task-arrow {
  width: 16px;
  color: var(--app-text-muted);
  transition: transform 240ms ease;
}

.growth-task:hover .task-arrow {
  transform: translateX(3px);
}

.medal-section {
  min-width: 0;
}

.medal-item {
  display: flex;
  gap: 12px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  padding: 14px;
}

.medal-icon {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  background: rgb(var(--theme-primary-rgb) / 0.09);
}

.medal-item strong {
  font-size: 14px;
}

.medal-item p,
.medal-item small {
  margin-top: 3px;
  color: var(--app-text-muted);
  font-size: 12px;
}

.empty-panel {
  display: grid;
  min-height: 220px;
  place-items: center;
  align-content: center;
  gap: 8px;
  border: 1px dashed var(--app-border-strong);
  border-radius: 8px;
  color: var(--app-text-muted);
  text-align: center;
}

.empty-panel svg {
  width: 30px;
  color: var(--theme-primary);
}

.empty-panel strong {
  color: var(--app-text-secondary);
}

.empty-panel span {
  font-size: 12px;
}

.motion-card:hover {
  will-change: transform;
}

@keyframes rise-in {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes task-pop {
  0% { transform: scale(0.74) rotate(-8deg); }
  65% { transform: scale(1.08) rotate(3deg); }
  100% { transform: scale(1) rotate(0); }
}

@media (max-width: 900px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .growth-overview,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .today-score {
    border-top: 1px solid var(--app-border);
    border-left: 0;
    padding-top: 16px;
    padding-left: 0;
    text-align: left;
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .heading-actions,
  .soft-action,
  .checkin-button {
    width: 100%;
  }

  .heading-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .soft-action,
  .checkin-button {
    justify-content: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .task-reward {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .page-heading,
  .growth-overview,
  .growth-stat,
  .completed .task-icon {
    animation: none;
  }

  .growth-task:hover,
  .soft-action:hover,
  .checkin-button:not(:disabled):hover {
    transform: none;
  }
}
</style>
