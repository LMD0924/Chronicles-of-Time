<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const { isDark } = useTheme()
const router = useRouter()
const loading = ref(false)
const activeFilter = ref('all')
const goals = ref([])
const tasks = ref([])
const interviews = ref([])
const reviews = ref([])

const menuItems = [
  { key: 'work', label: '工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'timeline', label: '成长时间线', icon: '🗓️', path: '/CareerTimeline' },
  { key: 'toolkit', label: '入职工具箱', icon: '🧰', path: '/CareerToolkit' },
  { key: 'interview', label: '模拟面试', icon: '🎙️', path: '/InterviewLab' },
]

const filters = [
  { key: 'all', label: '全部动态' },
  { key: 'goal', label: '职业目标' },
  { key: 'task', label: '工作任务' },
  { key: 'interview', label: '面试安排' },
  { key: 'review', label: '工作复盘' },
]

const statusText = {
  ACTIVE: '进行中', DONE: '已完成', ARCHIVED: '已归档', TODO: '待处理', DOING: '推进中',
  PREPARING: '准备中', WAITING: '等待反馈', PASSED: '已通过', FAILED: '未通过',
  DAILY: '日复盘', WEEKLY: '周复盘', MONTHLY: '月复盘', PROJECT: '项目复盘',
}

const getData = (res, fallback) => res?.code === 200 ? (res.data ?? fallback) : fallback

const events = computed(() => {
  const result = []
  if (activeFilter.value === 'all' || activeFilter.value === 'goal') {
    goals.value.forEach((item) => result.push({
      key: `goal-${item.id}`,
      type: 'goal',
      icon: '🎯',
      label: '职业目标',
      title: item.goalName || '未命名目标',
      description: item.metric || item.notes || '为自己设定一个可执行的职业方向。',
      date: item.targetDate || item.startDate || item.createdAt,
      status: statusText[item.status] || item.status || '进行中',
      progress: Number(item.progress || 0),
    }))
  }
  if (activeFilter.value === 'all' || activeFilter.value === 'task') {
    tasks.value.forEach((item) => result.push({
      key: `task-${item.id}`,
      type: 'task',
      icon: item.status === 'DONE' ? '✅' : '📌',
      label: '工作任务',
      title: item.taskName || '未命名任务',
      description: item.notes || item.outcome || '把任务拆成今天可以完成的一步。',
      date: item.dueDate || item.createdAt,
      status: statusText[item.status] || item.status || '待处理',
      progress: item.status === 'DONE' ? 100 : item.status === 'DOING' ? 50 : 0,
    }))
  }
  if (activeFilter.value === 'all' || activeFilter.value === 'interview') {
    interviews.value.forEach((item) => result.push({
      key: `interview-${item.id}`,
      type: 'interview',
      icon: '🎙️',
      label: '面试安排',
      title: `${item.companyName || '目标公司'} · ${item.positionName || '目标岗位'}`,
      description: item.preparationNotes || item.feedback || `${item.interviewRound || '面试'}，提前准备岗位案例。`,
      date: item.interviewDate || item.createdAt,
      status: statusText[item.status] || item.status || '准备中',
      progress: Number(item.confidenceScore || 0),
    }))
  }
  if (activeFilter.value === 'all' || activeFilter.value === 'review') {
    reviews.value.forEach((item) => result.push({
      key: `review-${item.id}`,
      type: 'review',
      icon: '📝',
      label: '工作复盘',
      title: `${statusText[item.reviewType] || '工作'}复盘`,
      description: item.wins || item.learnings || item.problems || '记录一次工作复盘。',
      date: item.reviewDate || item.createdAt,
      status: item.nextActions ? '已有下一步' : '待补行动',
      progress: item.nextActions ? 100 : 0,
    }))
  }
  return result.sort((a, b) => String(b.date || '').localeCompare(String(a.date || '')))
})

const stats = computed(() => {
  const completedTasks = tasks.value.filter((item) => item.status === 'DONE').length
  const activeGoals = goals.value.filter((item) => item.status === 'ACTIVE').length
  const upcomingInterviews = interviews.value.filter((item) => item.status === 'PREPARING' || item.status === 'WAITING').length
  return [
    { label: '时间线节点', value: events.value.length, tone: 'primary' },
    { label: '进行中目标', value: activeGoals, tone: 'violet' },
    { label: '任务完成率', value: tasks.value.length ? `${Math.round(completedTasks / tasks.value.length * 100)}%` : '0%', tone: 'green' },
    { label: '待准备面试', value: upcomingInterviews, tone: 'orange' },
  ]
})

const nextActions = computed(() => {
  const current = events.value.filter((item) => item.type !== 'review' || item.progress === 0)
  return current.slice(0, 4)
})

const formatDate = (value) => value ? String(value).replace('T', ' ').slice(0, 10) : '待定日期'
const typeClass = (type) => `event-${type}`

const loadAll = async () => {
  loading.value = true
  try {
    const [goalRes, taskRes, interviewRes, reviewRes] = await Promise.allSettled([
      request.get('/workplace/goals'),
      request.get('/workplace/tasks'),
      request.get('/workplace/interviews'),
      request.get('/workplace/reviews'),
    ])
    goals.value = goalRes.status === 'fulfilled' ? getData(goalRes.value, []) : []
    tasks.value = taskRes.status === 'fulfilled' ? getData(taskRes.value, []) : []
    interviews.value = interviewRes.status === 'fulfilled' ? getData(interviewRes.value, []) : []
    reviews.value = reviewRes.status === 'fulfilled' ? getData(reviewRes.value, []) : []
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="timeline-hero">
          <div>
            <p class="app-section-label mb-2">Career Timeline</p>
            <h1 class="app-page-title">成长时间线</h1>
            <p class="app-page-desc">把目标、任务、面试和复盘放到同一条线上，看清自己正在往哪里走。</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <button type="button" class="app-btn-secondary" :disabled="loading" @click="loadAll">{{ loading ? '同步中...' : '同步数据' }}</button>
            <button type="button" class="app-btn-primary" @click="router.push('/CareerToolkit')">打开工具箱</button>
          </div>
        </header>

        <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <article v-for="item in stats" :key="item.label" class="timeline-stat" :class="`stat-${item.tone}`">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <i></i>
          </article>
        </section>

        <section class="timeline-layout">
          <div class="timeline-main app-card-surface p-5 sm:p-6">
            <div class="mb-6 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div><h2 class="section-title">职场动态</h2><p class="section-subtitle">按日期倒序整理你的关键节点</p></div>
              <div class="filter-bar" role="tablist" aria-label="动态筛选">
                <button v-for="filter in filters" :key="filter.key" type="button" :class="{ active: activeFilter === filter.key }" @click="activeFilter = filter.key">{{ filter.label }}</button>
              </div>
            </div>

            <div v-if="events.length" class="event-list">
              <article v-for="event in events" :key="event.key" class="event-row">
                <div class="event-rail"><span :class="typeClass(event.type)">{{ event.icon }}</span><i></i></div>
                <div class="event-card">
                  <div class="flex flex-wrap items-start justify-between gap-3">
                    <div class="min-w-0"><p class="event-label">{{ event.label }} · {{ formatDate(event.date) }}</p><h3>{{ event.title }}</h3><p class="event-description">{{ event.description }}</p></div>
                    <span class="event-status">{{ event.status }}</span>
                  </div>
                  <div class="event-progress"><span :style="{ width: `${Math.min(100, event.progress)}%` }"></span></div>
                </div>
              </article>
            </div>
            <div v-else class="empty-state"><span>🧭</span><h3>时间线还在等待第一条记录</h3><p>先创建一个目标、任务或面试安排，成长轨迹就会自动出现。</p><button type="button" class="app-btn-primary" @click="router.push('/WorkRecords')">去添加记录</button></div>
          </div>

          <aside class="timeline-aside space-y-5">
            <section class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><div><h2 class="section-title">下一步</h2><p class="section-subtitle">优先处理最靠前的节点</p></div><span class="aside-icon">⚡</span></div>
              <div v-if="nextActions.length" class="next-list"><button v-for="item in nextActions" :key="`next-${item.key}`" type="button" class="next-item" @click="router.push('/WorkRecords')"><span :class="typeClass(item.type)">{{ item.icon }}</span><span><strong>{{ item.title }}</strong><small>{{ formatDate(item.date) }} · {{ item.status }}</small></span><b>›</b></button></div>
              <p v-else class="empty-text">暂无待处理节点。</p>
            </section>
            <section class="timeline-tip">
              <span>💡</span><div><h3>给新人的建议</h3><p>每周只保留 1 个主目标，任务写成可以在今天完成的动作，复盘才会真正产生下一步。</p></div>
            </section>
          </aside>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.timeline-hero { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 18px; }
.timeline-stat { position: relative; overflow: hidden; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); padding: 18px; }
.timeline-stat span { color: var(--app-text-muted); font-size: 12px; }.timeline-stat strong { display: block; margin-top: 6px; color: var(--app-text); font-size: 28px; font-weight: 850; }.timeline-stat i { position: absolute; right: -14px; bottom: -20px; width: 72px; height: 72px; border-radius: 50%; background: rgb(var(--theme-primary-rgb) / .1); }
.stat-violet i { background: rgb(139 92 246 / .12); }.stat-green i { background: rgb(16 185 129 / .12); }.stat-orange i { background: rgb(245 158 11 / .14); }
.timeline-layout { display: grid; gap: 20px; grid-template-columns: minmax(0, 1.55fr) minmax(280px, .65fr); }.section-title { color: var(--app-text); font-size: 18px; font-weight: 800; }.section-subtitle { margin-top: 3px; color: var(--app-text-muted); font-size: 12px; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 6px; }.filter-bar button { border: 1px solid var(--app-border); border-radius: 999px; padding: 7px 11px; color: var(--app-text-muted); font-size: 12px; transition: 180ms ease; }.filter-bar button:hover, .filter-bar button.active { border-color: rgb(var(--theme-primary-rgb) / .4); background: rgb(var(--theme-primary-rgb) / .1); color: var(--theme-primary); }
.event-list { position: relative; display: grid; gap: 4px; }.event-row { display: grid; grid-template-columns: 44px minmax(0, 1fr); gap: 10px; }.event-rail { position: relative; display: grid; justify-items: center; }.event-rail > span { position: relative; z-index: 1; display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid var(--app-border); border-radius: 10px; background: var(--app-card-solid); font-size: 16px; }.event-rail > i { position: absolute; top: 34px; bottom: -8px; width: 1px; background: var(--app-border); }.event-row:last-child .event-rail > i { display: none; }.event-card { margin-bottom: 12px; border: 1px solid var(--app-border); border-radius: 8px; background: color-mix(in srgb, var(--app-card-solid) 94%, var(--theme-primary) 6%); padding: 14px; transition: 180ms ease; }.event-card:hover { border-color: rgb(var(--theme-primary-rgb) / .35); transform: translateX(3px); }.event-label { color: var(--theme-primary); font-size: 11px; font-weight: 750; }.event-card h3 { margin-top: 3px; color: var(--app-text); font-size: 15px; font-weight: 750; }.event-description { margin-top: 5px; color: var(--app-text-muted); font-size: 12px; line-height: 1.65; }.event-status { flex-shrink: 0; border-radius: 999px; background: rgb(var(--theme-primary-rgb) / .1); padding: 4px 8px; color: var(--theme-primary); font-size: 11px; }.event-progress { height: 3px; margin-top: 12px; overflow: hidden; border-radius: 2px; background: var(--app-border); }.event-progress span { display: block; height: 100%; border-radius: inherit; background: var(--theme-primary); transition: width 500ms ease; }.event-task span { background: rgb(245 158 11 / .12); }.event-interview span { background: rgb(59 130 246 / .12); }.event-review span { background: rgb(16 185 129 / .12); }
.next-list { display: grid; gap: 8px; }.next-item { display: grid; width: 100%; grid-template-columns: 30px minmax(0, 1fr) 16px; align-items: center; gap: 8px; border: 1px solid var(--app-border); border-radius: 8px; background: transparent; padding: 9px; text-align: left; transition: 180ms ease; }.next-item:hover { border-color: rgb(var(--theme-primary-rgb) / .35); background: rgb(var(--theme-primary-rgb) / .06); }.next-item > span:first-child { display: grid; width: 28px; height: 28px; place-items: center; border-radius: 8px; background: rgb(var(--theme-primary-rgb) / .1); }.next-item strong, .next-item small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.next-item strong { color: var(--app-text); font-size: 12px; }.next-item small { margin-top: 3px; color: var(--app-text-muted); font-size: 10px; }.next-item b { color: var(--app-text-muted); font-size: 19px; font-weight: 400; }.aside-icon { display: grid; width: 30px; height: 30px; place-items: center; border-radius: 9px; background: rgb(245 158 11 / .12); }.timeline-tip { display: flex; gap: 12px; border: 1px solid rgb(59 130 246 / .2); border-radius: 8px; background: rgb(59 130 246 / .07); padding: 16px; }.timeline-tip > span { font-size: 20px; }.timeline-tip h3 { color: var(--app-text); font-size: 13px; font-weight: 800; }.timeline-tip p { margin-top: 5px; color: var(--app-text-muted); font-size: 12px; line-height: 1.7; }.empty-state { display: grid; justify-items: center; gap: 8px; padding: 70px 20px; text-align: center; }.empty-state > span { font-size: 32px; }.empty-state h3 { color: var(--app-text); font-weight: 800; }.empty-state p { color: var(--app-text-muted); font-size: 13px; }.empty-text { color: var(--app-text-muted); font-size: 13px; text-align: center; }
@media (max-width: 900px) { .timeline-layout { grid-template-columns: 1fr; } }
</style>
