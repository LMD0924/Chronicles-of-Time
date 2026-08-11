<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Calendar as CalendarCheck, Check, EditPen, MagicStick, Refresh, Star, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { getStoredTheme, ThemeType } from '@/utils/theme.js'

const router = useRouter()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const loading = ref(true)
const refreshing = ref(false)
const error = ref('')
const userId = ref(null)
const now = new Date()
const padDatePart = (value) => String(value).padStart(2, '0')
const today = ref(now.getFullYear() + '-' + padDatePart(now.getMonth() + 1) + '-' + padDatePart(now.getDate()))

const summary = ref({
  checkedInToday: false,
  level: 1,
  levelName: '初见',
  levelProgress: 0,
  continuousLoginDays: 0,
  growthExperience: 0,
  growthTasks: [],
})
const tasks = ref([])
const goals = ref([])
const interviews = ref([])
const growthRecords = ref([])
const mistakes = ref([])

const menuItems = [
  { key: 'checkin', label: '每日打卡', icon: '✅', path: '/DailyCheckin' },
  { key: 'study', label: '学习中心', icon: '📚', path: '/StudyDashboard' },
  { key: 'work', label: '职场工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'records', label: '成长记录', icon: '📝', path: '/Records' },
]

const toList = (response, fallback = []) => {
  const data = response?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  return fallback
}
const dateOnly = (value) => value ? String(value).replace('T', ' ').slice(0, 10) : ''
const isBeforeToday = (value) => {
  const date = dateOnly(value)
  return Boolean(date && date < today.value)
}
const isWithinDays = (value, days) => {
  const date = dateOnly(value)
  if (!date) return false
  const diff = (new Date(date + 'T00:00:00') - new Date(today.value + 'T00:00:00')) / 86400000
  return diff >= 0 && diff <= days
}
const formatDate = (value) => {
  const date = dateOnly(value)
  if (!date) return '未定日期'
  if (date === today.value) return '今天'
  if (date < today.value) return '逾期 · ' + date
  return date
}
const getReviewDate = (item) => item.nextReviewDate || item.next_review_date || item.mistakeDate || item.mistake_date
const getGrowthDate = (item) => item.recordDate || item.record_date || item.createdAt || item.createTime

const dailyTasks = computed(() => summary.value.growthTasks || [])
const completedDailyTasks = computed(() => dailyTasks.value.filter((item) => item.completed).length)
const workTaskList = computed(() => [...tasks.value].sort((a, b) => dateOnly(a.dueDate || a.due_date).localeCompare(dateOnly(b.dueDate || b.due_date))))
const dueWorkTasks = computed(() => workTaskList.value.filter((item) => item.status !== 'DONE').slice(0, 6))
const overdueTasks = computed(() => workTaskList.value.filter((item) => item.status !== 'DONE' && isBeforeToday(item.dueDate || item.due_date)))
const dueMistakes = computed(() => [...mistakes.value]
  .filter((item) => !item.mastered && (!getReviewDate(item) || dateOnly(getReviewDate(item)) <= today.value))
  .sort((a, b) => dateOnly(getReviewDate(a)).localeCompare(dateOnly(getReviewDate(b))))
  .slice(0, 6))
const upcomingInterviews = computed(() => [...interviews.value]
  .filter((item) => item.status !== 'PASSED' && item.status !== 'CANCELLED' && isWithinDays(item.interviewDate || item.interview_date, 7))
  .sort((a, b) => dateOnly(a.interviewDate || a.interview_date).localeCompare(dateOnly(b.interviewDate || b.interview_date)))
  .slice(0, 4))
const activeGoals = computed(() => [...goals.value]
  .filter((item) => item.status !== 'DONE' && item.status !== 'ARCHIVED')
  .slice(0, 4))
const recentRecords = computed(() => [...growthRecords.value]
  .sort((a, b) => dateOnly(getGrowthDate(b)).localeCompare(dateOnly(getGrowthDate(a))))
  .slice(0, 5))
const completedTodayWorkTasks = computed(() => tasks.value.filter((item) =>
  item.status === 'DONE' && dateOnly(item.completedAt || item.updatedAt || item.updateTime) === today.value).length)
const completionRate = computed(() => {
  const total = dueWorkTasks.value.length + dueMistakes.value.length + dailyTasks.value.length + completedTodayWorkTasks.value
  const done = completedDailyTasks.value + completedTodayWorkTasks.value
  return total ? Math.min(100, Math.round((done / total) * 100)) : 0
})

const reminders = computed(() => {
  const items = []
  overdueTasks.value.slice(0, 3).forEach((item) => {
    items.push({ key: 'task-' + item.id, tone: 'danger', title: '任务已逾期：' + (item.taskName || '未命名任务'), description: '截止 ' + formatDate(item.dueDate || item.due_date), path: '/WorkRecords' })
  })
  dueMistakes.value.slice(0, 3).forEach((item) => {
    items.push({ key: 'mistake-' + item.id, tone: 'warning', title: '有 ' + (item.subjectName || '学习') + ' 错题待复习', description: item.knowledgePoint || '今天完成一次复习', path: '/StudyDashboard?tab=mistake' })
  })
  upcomingInterviews.value.slice(0, 2).forEach((item) => {
    items.push({ key: 'interview-' + item.id, tone: 'info', title: '面试安排：' + (item.companyName || '目标公司'), description: formatDate(item.interviewDate || item.interview_date), path: '/InterviewLab' })
  })
  if (!summary.value.checkedInToday) items.push({ key: 'checkin', tone: 'success', title: '今天还没有留下成长足迹', description: '完成每日打卡，开启今日任务', path: '/DailyCheckin' })
  return items.slice(0, 6)
})

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const userResponse = await request.get('/user/getUserById').catch(() => null)
    userId.value = userResponse?.data?.id || null
    const requests = [
      request.get('/activity/summary'),
      request.get('/workplace/tasks'),
      request.get('/workplace/goals'),
      request.get('/workplace/interviews'),
      request.post('/growth/list', { page: 1, size: 20 }),
      userId.value ? request.get('/mistake/list/' + userId.value) : Promise.resolve({ data: [] }),
    ]
    const [activityResult, taskResult, goalResult, interviewResult, growthResult, mistakeResult] = await Promise.allSettled(requests)
    if (activityResult.status === 'fulfilled') summary.value = { ...summary.value, ...(activityResult.value.data || {}) }
    tasks.value = taskResult.status === 'fulfilled' ? toList(taskResult.value) : []
    goals.value = goalResult.status === 'fulfilled' ? toList(goalResult.value) : []
    interviews.value = interviewResult.status === 'fulfilled' ? toList(interviewResult.value) : []
    growthRecords.value = growthResult.status === 'fulfilled' ? toList(growthResult.value) : []
    mistakes.value = mistakeResult.status === 'fulfilled' ? toList(mistakeResult.value) : []
    const dataResults = [activityResult, taskResult, goalResult, interviewResult, growthResult]
    if (userId.value) dataResults.push(mistakeResult)
    const failedCount = dataResults.filter((item) => item.status === 'rejected').length
    if (failedCount === dataResults.length) {
      error.value = '暂时无法加载成长数据，请检查后端服务后重试。'
    } else if (failedCount > 0) {
      error.value = '有 ' + failedCount + ' 项数据暂未同步，其余内容已正常展示。'
    }
  } catch (loadError) {
    console.error('加载今日工作台失败', loadError)
    error.value = '暂时无法加载今日工作台，请稍后重试。'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const refresh = async () => {
  refreshing.value = true
  await loadData()
}
const checkIn = async () => {
  if (summary.value.checkedInToday) return
  try {
    const response = await request.post('/activity/checkin', {})
    summary.value = { ...summary.value, ...(response.data || {}) }
    ElMessage.success('今天的成长足迹已留下')
  } catch (checkinError) {
    ElMessage.error(checkinError.message || '打卡失败，请稍后重试')
  }
}
const completeTask = async (item) => {
  try {
    await request.post('/workplace/tasks', { ...item, status: 'DONE' })
    ElMessage.success('任务已完成')
    await loadData()
  } catch (taskError) {
    ElMessage.error(taskError.message || '任务更新失败')
  }
}
const reviewMistake = async (item) => {
  try {
    await request.put('/mistake/review/' + item.id)
    ElMessage.success('已记录本次复习')
    await loadData()
  } catch (reviewError) {
    ElMessage.error(reviewError.message || '复习记录失败')
  }
}
const go = (path) => { if (path) router.push(path) }
const goDailyTask = (task) => go(task.actionPath || '/DailyCheckin')

onMounted(loadData)
</script>

<template>
  <div class="app-shell app-page-bg min-h-screen" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="today-hero">
          <div><p class="app-section-label mb-2">Today · Growth OS</p><h1 class="app-page-title">今天，先完成最重要的一步</h1><p class="app-page-desc">把学习、工作和成长记录放在同一个行动清单里。</p></div>
          <div class="flex flex-wrap gap-2">
            <button type="button" class="app-btn-secondary" :disabled="loading || refreshing" @click="refresh"><Refresh class="mr-1 inline-block h-4 w-4" />{{ refreshing ? '同步中...' : '同步数据' }}</button>
            <button type="button" class="app-btn-secondary" @click="router.push('/GrowthPlanner?view=plan')"><Trophy class="mr-1 inline-block h-4 w-4" />智能今日计划</button>
            <button type="button" class="app-btn-secondary" @click="router.push('/TaskMatrix?view=matrix')"><span class="mr-1">四</span>任务矩阵</button>
            <button type="button" class="app-btn-primary" :disabled="loading || summary.checkedInToday" @click="checkIn"><Check class="mr-1 inline-block h-4 w-4" />{{ summary.checkedInToday ? '今天已打卡' : '完成今日打卡' }}</button>
          </div>
        </header>

        <div v-if="error" class="app-card-surface border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700"><div class="flex flex-wrap items-center justify-between gap-3"><span>{{ error }}</span><button type="button" class="app-btn-secondary" @click="refresh">重试</button></div></div>

        <div v-if="loading" class="loading-panel app-card-surface"><Refresh class="h-6 w-6 animate-spin" /><strong>正在汇总今天的数据...</strong><span>部分服务暂时不可用时，其他内容仍会继续显示。</span></div>

        <section v-if="!loading" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <article class="today-stat app-card-surface"><span>今日任务</span><strong>{{ dailyTasks.length + dueWorkTasks.length }}</strong><small>{{ completedDailyTasks }} 项成长任务已完成</small></article>
          <article class="today-stat app-card-surface"><span>待复习错题</span><strong>{{ dueMistakes.length }}</strong><small>打开错题本继续巩固</small></article>
          <article class="today-stat app-card-surface"><span>连续打卡</span><strong>{{ summary.continuousLoginDays || 0 }}<em>天</em></strong><small>Lv.{{ summary.level || 1 }} · {{ summary.levelName || '初见' }}</small></article>
          <article class="today-stat app-card-surface"><span>行动完成度</span><strong>{{ completionRate }}<em>%</em></strong><small>完成一件，就离目标更近一点</small></article>
        </section>

        <section v-if="!loading" class="today-layout">
          <div class="space-y-5">
            <section class="app-card-surface p-5 sm:p-6">
              <div class="mb-5 flex flex-wrap items-end justify-between gap-3"><div><p class="app-section-label">Daily actions</p><h2 class="section-title">今日行动</h2><p class="section-subtitle">先处理有明确截止时间的事情</p></div><button type="button" class="app-btn-secondary !px-3 !py-2" @click="go('/WorkRecords')">管理任务</button></div>
              <div v-if="dailyTasks.length" class="mb-5 space-y-2">
                <button v-for="task in dailyTasks" :key="task.key" type="button" class="daily-task-row" @click="goDailyTask(task)"><span class="task-check" :class="{ done: task.completed }"><Check v-if="task.completed" /></span><span class="min-w-0 flex-1"><strong>{{ task.title }}</strong><small>{{ task.description }}</small></span><span class="task-exp">+{{ task.rewardExperience || 0 }} EXP</span><ArrowRight class="h-4 w-4 shrink-0" /></button>
              </div>
              <div v-if="dueWorkTasks.length" class="space-y-2">
                <article v-for="task in dueWorkTasks" :key="'work-' + task.id" class="action-row"><div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><strong>{{ task.taskName || '未命名任务' }}</strong><span class="status-pill">{{ task.status || 'TODO' }}</span></div><small>{{ task.notes || '完成后记得留下复盘记录' }} · {{ formatDate(task.dueDate || task.due_date) }}</small></div><button type="button" class="mini-action" @click="completeTask(task)">完成</button></article>
              </div>
              <div v-if="!dailyTasks.length && !dueWorkTasks.length" class="empty-panel"><Star class="mb-2 h-7 w-7" /><strong>今天还没有明确任务</strong><span>去职场工作台或学习中心添加一件可以完成的小事。</span><button type="button" class="app-btn-primary mt-2" @click="go('/WorkRecords')">添加任务</button></div>
            </section>

            <section class="app-card-surface p-5 sm:p-6">
              <div class="mb-5 flex flex-wrap items-end justify-between gap-3"><div><p class="app-section-label">Spaced review</p><h2 class="section-title">今日错题复习</h2><p class="section-subtitle">把遗忘变成下一次答对的机会</p></div><button type="button" class="app-btn-secondary !px-3 !py-2" @click="go('/StudyDashboard?tab=mistake')">打开错题本</button></div>
              <div v-if="dueMistakes.length" class="space-y-2">
                <article v-for="mistake in dueMistakes" :key="mistake.id" class="action-row"><div class="min-w-0 flex-1"><strong>{{ mistake.mistakeName || '未命名错题' }}</strong><small>{{ mistake.subjectName || '未分类' }} · {{ mistake.knowledgePoint || '待补充知识点' }} · {{ formatDate(getReviewDate(mistake)) }}</small></div><button type="button" class="mini-action" @click="reviewMistake(mistake)">复习</button></article>
              </div>
              <div v-else class="empty-panel"><MagicStick class="mb-2 h-7 w-7" /><strong>今天没有到期错题</strong><span>保持这个节奏，继续完成练习。</span></div>
            </section>
          </div>

          <aside class="space-y-5">
            <section class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><div><p class="app-section-label">Reminders</p><h2 class="section-title">提醒</h2></div><span class="text-2xl">🔔</span></div>
              <div v-if="reminders.length" class="space-y-2"><button v-for="item in reminders" :key="item.key" type="button" class="reminder-row" :class="'tone-' + item.tone" @click="go(item.path)"><span class="h-2 w-2 shrink-0 rounded-full bg-current"></span><span class="min-w-0 flex-1"><strong>{{ item.title }}</strong><small>{{ item.description }}</small></span><ArrowRight class="h-4 w-4 shrink-0" /></button></div>
              <div v-else class="empty-panel"><Check class="mb-2 h-7 w-7" /><strong>今天没有紧急提醒</strong><span>可以安心推进自己的节奏。</span></div>
            </section>

            <section class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><div><p class="app-section-label">Next 7 days</p><h2 class="section-title">近期安排</h2></div><CalendarCheck class="h-6 w-6 text-brand-500" /></div>
              <div v-if="upcomingInterviews.length" class="space-y-2"><button v-for="item in upcomingInterviews" :key="item.id" type="button" class="schedule-row" @click="go('/InterviewLab')"><span class="schedule-date">{{ formatDate(item.interviewDate || item.interview_date) }}</span><span class="min-w-0 flex-1"><strong>{{ item.companyName || '目标公司' }}</strong><small>{{ item.positionName || item.interviewRound || '面试准备' }}</small></span></button></div>
              <p v-else class="empty-text">未来 7 天没有面试安排。</p>
            </section>

            <section class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><div><p class="app-section-label">Focus</p><h2 class="section-title">当前目标</h2></div><Trophy class="h-6 w-6 text-brand-500" /></div>
              <div v-if="activeGoals.length" class="space-y-3"><button v-for="goal in activeGoals" :key="goal.id" type="button" class="goal-row" @click="go('/WorkRecords')"><span class="goal-progress"><i :style="{ width: Math.min(100, Number(goal.progress || 0)) + '%' }"></i></span><span class="min-w-0 flex-1"><strong>{{ goal.goalName || '未命名目标' }}</strong><small>{{ goal.metric || '还没有填写衡量指标' }}</small></span><ArrowRight class="h-4 w-4 shrink-0" /></button></div>
              <p v-else class="empty-text">还没有进行中的目标，先设一个本周主目标。</p>
            </section>
          </aside>
        </section>

        <section v-if="!loading" class="app-card-surface p-5 sm:p-6">
          <div class="mb-5 flex flex-wrap items-end justify-between gap-3"><div><p class="app-section-label">Recent progress</p><h2 class="section-title">最近成长记录</h2><p class="section-subtitle">让完成过的事情被看见，也方便下一次复盘。</p></div><button type="button" class="app-btn-secondary !px-3 !py-2" @click="go('/Records')">查看全部</button></div>
          <div v-if="recentRecords.length" class="record-grid"><button v-for="record in recentRecords" :key="record.id || getGrowthDate(record)" type="button" class="record-row" @click="go('/Records')"><span class="record-date">{{ formatDate(getGrowthDate(record)) }}</span><span class="min-w-0 flex-1"><strong>{{ record.title || record.activityName || record.examName || '成长记录' }}</strong><small>{{ record.achievementThisPeriod || record.summary || record.description || '记录一段真实的成长。' }}</small></span><ArrowRight class="h-4 w-4 shrink-0" /></button></div>
          <div v-else class="empty-panel"><EditPen class="mb-2 h-7 w-7" /><strong>还没有最近记录</strong><span>写下一条成长记录，未来回看时会更有意义。</span><button type="button" class="app-btn-primary mt-2" @click="go('/Records')">去记录</button></div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.today-hero { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 18px; }
.loading-panel { display: grid; min-height: 180px; place-items: center; align-content: center; gap: 8px; color: var(--theme-primary); }
.loading-panel strong { color: var(--app-text); font-size: 14px; }
.loading-panel span { color: var(--app-text-muted); font-size: 12px; }
.today-stat { min-height: 118px; padding: 18px; }
.today-stat > span { color: var(--app-text-muted); font-size: 12px; }
.today-stat > strong { display: block; margin-top: 8px; color: var(--app-text); font-size: 30px; font-weight: 850; }
.today-stat > strong em { margin-left: 3px; color: var(--app-text-muted); font-size: 12px; font-style: normal; font-weight: 600; }
.today-stat > small { display: block; margin-top: 5px; color: var(--app-text-muted); font-size: 11px; }
.today-layout { display: grid; gap: 20px; grid-template-columns: minmax(0, 1.4fr) minmax(280px, .6fr); }
.section-title { color: var(--app-text); font-size: 18px; font-weight: 800; }
.section-subtitle { margin-top: 3px; color: var(--app-text-muted); font-size: 12px; }
.daily-task-row, .action-row, .reminder-row, .schedule-row, .goal-row, .record-row { display: flex; width: 100%; align-items: center; gap: 10px; border: 1px solid var(--app-border); border-radius: 8px; background: transparent; padding: 11px 12px; text-align: left; transition: 180ms ease; }
.daily-task-row:hover, .action-row:hover, .reminder-row:hover, .schedule-row:hover, .goal-row:hover, .record-row:hover { border-color: rgb(var(--theme-primary-rgb) / .35); background: rgb(var(--theme-primary-rgb) / .06); }
.daily-task-row strong, .daily-task-row small, .action-row strong, .action-row small, .reminder-row strong, .reminder-row small, .schedule-row strong, .schedule-row small, .goal-row strong, .goal-row small, .record-row strong, .record-row small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.daily-task-row strong, .action-row strong, .reminder-row strong, .schedule-row strong, .goal-row strong, .record-row strong { color: var(--app-text); font-size: 13px; }
.daily-task-row small, .action-row small, .reminder-row small, .schedule-row small, .goal-row small, .record-row small { margin-top: 3px; color: var(--app-text-muted); font-size: 11px; }
.task-check { display: grid; width: 24px; height: 24px; flex: 0 0 24px; place-items: center; border: 1px solid var(--app-border); border-radius: 50%; color: white; }
.task-check.done { border-color: var(--theme-primary); background: var(--theme-primary); }
.task-exp { flex-shrink: 0; color: var(--theme-primary); font-size: 11px; font-weight: 800; }
.action-row { min-height: 58px; }
.status-pill { flex-shrink: 0; border-radius: 999px; background: rgb(var(--theme-primary-rgb) / .1); padding: 3px 7px; color: var(--theme-primary); font-size: 10px; }
.mini-action { flex-shrink: 0; border: 1px solid rgb(var(--theme-primary-rgb) / .35); border-radius: 6px; background: rgb(var(--theme-primary-rgb) / .1); padding: 6px 9px; color: var(--theme-primary); font-size: 11px; font-weight: 700; }
.empty-panel { display: grid; justify-items: center; gap: 3px; padding: 28px 12px; color: var(--app-text-muted); text-align: center; }
.empty-panel strong { color: var(--app-text); font-size: 13px; }
.empty-panel span { font-size: 11px; }
.reminder-row { align-items: flex-start; padding: 10px; color: var(--theme-primary); }
.reminder-row.tone-danger { color: rgb(225 29 72); }
.reminder-row.tone-warning { color: rgb(217 119 6); }
.reminder-row.tone-info { color: rgb(37 99 235); }
.reminder-row.tone-success { color: rgb(5 150 105); }
.reminder-row strong, .reminder-row small { color: var(--app-text); }
.reminder-row small { color: var(--app-text-muted); }
.schedule-date { width: 48px; flex-shrink: 0; color: var(--theme-primary); font-size: 11px; font-weight: 800; }
.goal-row { align-items: flex-start; position: relative; padding-left: 12px; }
.goal-progress { width: 3px; min-height: 34px; overflow: hidden; border-radius: 2px; background: var(--app-border); }
.goal-progress i { display: block; width: 0; height: 100%; border-radius: inherit; background: var(--theme-primary); }
.record-grid { display: grid; gap: 8px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.record-date { width: 74px; flex-shrink: 0; color: var(--theme-primary); font-size: 11px; font-weight: 700; }
.empty-text { color: var(--app-text-muted); font-size: 13px; text-align: center; }
@media (max-width: 900px) { .today-layout { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .record-grid { grid-template-columns: 1fr; } .today-stat { min-height: 104px; } }
</style>

