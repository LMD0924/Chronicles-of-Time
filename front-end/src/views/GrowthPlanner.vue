<script setup>
defineOptions({ name: 'GrowthPlanner' })

import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Bell, Calendar, Check, Download, EditPen, Refresh, Share, Trophy } from '@element-plus/icons-vue'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { getStoredTheme, ThemeType } from '@/utils/theme.js'

const router = useRouter()
const route = useRoute()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const loading = ref(true)
const refreshing = ref(false)
const activeView = ref('overview')
const calendarMode = ref('month')
const userId = ref(null)
const currentUserName = ref('')
const today = new Date()
const weekStart = ref(startOfWeek(today))
const currentMonth = ref(new Date(today.getFullYear(), today.getMonth(), 1))
const reportDraft = ref({ reflection: '', nextWeekFocus: '' })
const reportSaving = ref(false)
const planAdded = ref([])
const notifications = ref([])
const unreadCount = ref(0)
const notificationPreference = ref({ enabled: 1, quietHoursEnabled: 0, quietStart: '22:00', quietEnd: '08:00', preferredStage: 'all' })
const preferredStage = ref(localStorage.getItem('preferred_stage') || 'all')
const shareCanvas = ref(null)
const shareIncludeName = ref(false)
const shareIncludeScores = ref(true)
const shareIncludeStreak = ref(true)

const summary = ref({ continuousLoginDays: 0, totalLoginDays: 0, growthExperience: 0, checkedInToday: false })
const tasks = ref([])
const goals = ref([])
const interviews = ref([])
const mistakes = ref([])
const scores = ref([])
const growthRecords = ref([])
const articles = ref([])

const menuItems = [
  { key: 'planner', label: '成长规划', icon: '🧭', path: '/GrowthPlanner' },
  { key: 'work', label: '职场工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'records', label: '成长记录', icon: '📝', path: '/Records' },
]
const stageOptions = [
  { value: 'all', label: '全阶段', description: '展示全部学习、生活和职场数据' },
  { value: 'high_school', label: '高中', description: '聚焦考试、错题和志愿规划' },
  { value: 'university', label: '大学', description: '聚焦课程、论文和能力积累' },
  { value: 'workplace', label: '职场', description: '聚焦任务、目标和面试准备' },
]

function startOfWeek(value) {
  const date = new Date(value)
  const day = date.getDay() || 7
  date.setDate(date.getDate() - day + 1)
  date.setHours(0, 0, 0, 0)
  return date
}
function addDays(value, amount) {
  const date = new Date(value)
  date.setDate(date.getDate() + amount)
  return date
}
function dateKey(value) {
  if (!value) return ''
  const date = value instanceof Date ? value : new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return String(value).slice(0, 10)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function dateOnly(value) { return value ? String(value).replace('T', ' ').slice(0, 10) : '' }
function formatDate(value) { return dateOnly(value) || '未定日期' }
function isBetween(value, start, end) {
  const date = dateOnly(value)
  return date >= dateKey(start) && date <= dateKey(end)
}
function unwrapList(result) {
  const data = result?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  return []
}

const weekEnd = computed(() => addDays(weekStart.value, 6))
const weekLabel = computed(() => `${dateKey(weekStart.value)} 至 ${dateKey(weekEnd.value)}`)
const activeStageLabel = computed(() => stageOptions.find((item) => item.value === preferredStage.value)?.label || '全阶段')

const weeklyRecords = computed(() => growthRecords.value.filter((item) => isBetween(item.recordDate || item.record_date, weekStart.value, weekEnd.value)))
const weeklyTasks = computed(() => tasks.value.filter((item) => isBetween(item.completedAt || item.updatedAt || item.updateTime, weekStart.value, weekEnd.value)))
const weeklyScores = computed(() => scores.value.filter((item) => isBetween(item.examDate, weekStart.value, weekEnd.value)))
const weeklyMistakes = computed(() => mistakes.value.filter((item) => isBetween(item.lastReviewDate || item.last_review_date, weekStart.value, weekEnd.value)))
const weeklyArticles = computed(() => articles.value.filter((item) => isBetween(item.publishTime || item.publish_at || item.createdAt, weekStart.value, weekEnd.value)))
const weeklyStudyHours = computed(() => weeklyRecords.value.reduce((sum, item) => sum + Number(item.studyHours || 0), 0))
const weeklyCompletedTasks = computed(() => weeklyTasks.value.filter((item) => item.status === 'DONE').length)
const weeklyAverageScore = computed(() => {
  if (!weeklyScores.value.length) return 0
  return Math.round(weeklyScores.value.reduce((sum, item) => sum + Number(item.score || 0), 0) / weeklyScores.value.length)
})
const quadrantStats = computed(() => [
  { key: 'IMPORTANT_URGENT', label: '重要 · 紧急', tone: 'rose' },
  { key: 'IMPORTANT_NOT_URGENT', label: '重要 · 不紧急', tone: 'green' },
  { key: 'URGENT_NOT_IMPORTANT', label: '紧急 · 不重要', tone: 'cyan' },
  { key: 'NOT_IMPORTANT_NOT_URGENT', label: '不重要 · 不紧急', tone: 'slate' },
].map((quadrant) => {
  const all = weeklyTasks.value.filter((item) => (item.quadrant || 'NOT_IMPORTANT_NOT_URGENT') === quadrant.key)
  return { ...quadrant, total: all.length, done: all.filter((item) => item.status === 'DONE').length }
}))
const weeklyStats = computed(() => ({
  records: weeklyRecords.value.length,
  completedTasks: weeklyCompletedTasks.value,
  studyHours: Math.round(weeklyStudyHours.value * 10) / 10,
  scores: weeklyScores.value.length,
  averageScore: weeklyAverageScore.value,
  reviewedMistakes: weeklyMistakes.value.length,
  articles: weeklyArticles.value.length,
  streak: summary.value.continuousLoginDays || 0,
}))

const dueTasks = computed(() => tasks.value
  .filter((item) => item.status !== 'DONE')
  .sort((left, right) => dateOnly(left.dueDate || left.due_date).localeCompare(dateOnly(right.dueDate || right.due_date))))
const dueMistakes = computed(() => mistakes.value
  .filter((item) => !item.mastered && dateOnly(item.nextReviewDate || item.next_review_date) <= dateKey(new Date()))
  .slice(0, 12))
const upcomingInterviews = computed(() => interviews.value
  .filter((item) => item.status !== 'PASSED' && item.status !== 'CANCELLED')
  .sort((left, right) => dateOnly(left.interviewDate || left.interview_date).localeCompare(dateOnly(right.interviewDate || right.interview_date))))
const smartPlan = computed(() => {
  const candidates = []
  dueTasks.value.slice(0, 2).forEach((item) => candidates.push({ key: `task-${item.id}`, source: 'task', title: item.taskName || '完成一项待办任务', description: item.notes || '优先处理有明确截止日期的工作。', dueDate: item.dueDate || item.due_date, item }))
  dueMistakes.value.slice(0, 2).forEach((item) => candidates.push({ key: `mistake-${item.id}`, source: 'mistake', title: `复习错题：${item.subjectName || '学习'}`, description: item.knowledgePoint || item.mistakeName || '巩固一个薄弱知识点。', dueDate: dateKey(new Date()), item }))
  upcomingInterviews.value.slice(0, 2).forEach((item) => candidates.push({ key: `interview-${item.id}`, source: 'interview', title: `准备面试：${item.companyName || '目标公司'}`, description: item.positionName || item.interviewRound || '整理面试材料和自我介绍。', dueDate: item.interviewDate || item.interview_date, item }))
  goals.value.filter((item) => item.status !== 'DONE' && item.status !== 'ARCHIVED').slice(0, 2).forEach((item) => candidates.push({ key: `goal-${item.id}`, source: 'goal', title: `推进目标：${item.goalName || '本周目标'}`, description: item.metric || item.notes || '把目标拆成一个可执行动作。', dueDate: item.targetDate, item }))
  return candidates.filter((item, index, list) => list.findIndex((candidate) => candidate.key === item.key) === index).slice(0, 3)
})

const calendarEvents = computed(() => {
  const events = []
  tasks.value.filter((item) => item.dueDate || item.due_date).forEach((item) => events.push({ key: `task-${item.id}`, date: dateOnly(item.dueDate || item.due_date), type: 'task', title: item.taskName || '职场任务', item, editable: true }))
  interviews.value.filter((item) => item.interviewDate || item.interview_date).forEach((item) => events.push({ key: `interview-${item.id}`, date: dateOnly(item.interviewDate || item.interview_date), type: 'interview', title: `${item.companyName || '目标公司'} · 面试`, item, editable: true }))
  goals.value.filter((item) => item.targetDate).forEach((item) => events.push({ key: `goal-${item.id}`, date: dateOnly(item.targetDate), type: 'goal', title: item.goalName || '目标截止', item, editable: true }))
  mistakes.value.filter((item) => item.nextReviewDate || item.next_review_date).forEach((item) => events.push({ key: `mistake-${item.id}`, date: dateOnly(item.nextReviewDate || item.next_review_date), type: 'mistake', title: `复习 · ${item.subjectName || '错题'}`, item, editable: false }))
  scores.value.filter((item) => item.examDate).forEach((item) => events.push({ key: `score-${item.id}`, date: dateOnly(item.examDate), type: 'score', title: item.examName || '考试', item, editable: false }))
  growthRecords.value.filter((item) => item.recordDate).forEach((item) => events.push({ key: `growth-${item.id}`, date: dateOnly(item.recordDate), type: 'growth', title: item.activityName || item.examName || '成长记录', item, editable: false }))
  return events
})
const calendarDays = computed(() => {
  const first = calendarMode.value === 'month' ? new Date(currentMonth.value) : new Date(weekStart.value)
  const start = calendarMode.value === 'month' ? startOfWeek(new Date(first.getFullYear(), first.getMonth(), 1)) : startOfWeek(first)
  const count = calendarMode.value === 'month' ? 42 : 7
  return Array.from({ length: count }, (_, index) => {
    const date = addDays(start, index)
    return { date, key: dateKey(date), inMonth: date.getMonth() === currentMonth.value.getMonth(), events: calendarEvents.value.filter((event) => event.date === dateKey(date)) }
  })
})

const loadData = async () => {
  loading.value = true
  try {
    const userResult = await request.get('/user/getUserById').catch(() => null)
    userId.value = userResult?.data?.id || null
    currentUserName.value = userResult?.data?.name || userResult?.data?.username || ''
    const results = await Promise.allSettled([
      request.get('/activity/summary'),
      request.get('/workplace/tasks'),
      request.get('/workplace/goals'),
      request.get('/workplace/interviews'),
      request.post('/growth/list', { pageNum: 1, pageSize: 100 }),
      userId.value ? request.get(`/score/list/${userId.value}`) : Promise.resolve({ data: [] }),
      request.get('/content/my/list', { pageNum: 1, pageSize: 100 }),
      userId.value ? request.get(`/mistake/list/${userId.value}`) : Promise.resolve({ data: [] }),
    ])
    if (results[0].status === 'fulfilled') summary.value = { ...summary.value, ...(results[0].value.data || {}) }
    tasks.value = results[1].status === 'fulfilled' ? unwrapList(results[1].value) : []
    goals.value = results[2].status === 'fulfilled' ? unwrapList(results[2].value) : []
    interviews.value = results[3].status === 'fulfilled' ? unwrapList(results[3].value) : []
    growthRecords.value = results[4].status === 'fulfilled' ? unwrapList(results[4].value) : []
    scores.value = results[5].status === 'fulfilled' ? unwrapList(results[5].value) : []
    articles.value = results[6].status === 'fulfilled' ? unwrapList(results[6].value) : []
    mistakes.value = results[7].status === 'fulfilled' ? unwrapList(results[7].value) : []
    await syncNotifications()
    await loadWeeklyReport()
  } catch (error) {
    console.error('加载成长规划中心失败', error)
    ElMessage.error('成长规划数据加载失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const loadWeeklyReport = async () => {
  try {
    const response = await request.get('/productivity/weekly-report', { weekStart: dateKey(weekStart.value) })
    if (response.data) {
      reportDraft.value = { reflection: response.data.reflection || '', nextWeekFocus: response.data.nextWeekFocus || '' }
    } else {
      reportDraft.value = { reflection: '', nextWeekFocus: '' }
    }
  } catch (_) {}
}
const saveWeeklyReport = async () => {
  reportSaving.value = true
  try {
    await request.put('/productivity/weekly-report', {
      weekStart: dateKey(weekStart.value),
      weekEnd: dateKey(weekEnd.value),
      reportJson: JSON.stringify(weeklyStats.value),
      reflection: reportDraft.value.reflection,
      nextWeekFocus: reportDraft.value.nextWeekFocus,
    })
    ElMessage.success('本周复盘已保存')
  } catch (error) {
    ElMessage.error(error.message || '周报保存失败')
  } finally {
    reportSaving.value = false
  }
}
const syncNotifications = async () => {
  const reminders = []
  dueTasks.value.filter((item) => dateOnly(item.dueDate || item.due_date) < dateKey(new Date())).slice(0, 8).forEach((item) => reminders.push({ noticeType: 'task_overdue', title: `任务已逾期：${item.taskName || '未命名任务'}`, content: `截止日期 ${formatDate(item.dueDate || item.due_date)}`, bizId: item.id, dedupeKey: `task-overdue-${item.id}`, actionPath: '/WorkRecords', dueAt: formatDate(item.dueDate || item.due_date) }))
  tasks.value.filter((item) => item.status !== 'DONE' && item.reminderEnabled && item.reminderAt).slice(0, 20).forEach((item) => reminders.push({ noticeType: 'task_reminder', title: '任务提醒：' + (item.taskName || '未命名任务'), content: item.notes || ('计划在 ' + String(item.reminderAt).replace('T', ' ') + ' 开始'), bizId: item.id, dedupeKey: 'task-reminder-' + item.id + '-' + item.reminderAt, actionPath: '/TaskMatrix?view=today', dueAt: String(item.reminderAt).replace('T', ' ') }))
  dueMistakes.value.slice(0, 8).forEach((item) => reminders.push({ noticeType: 'mistake_review', title: `有待复习错题：${item.subjectName || '学习'}`, content: item.knowledgePoint || item.mistakeName || '今天完成一次复习', bizId: item.id, dedupeKey: `mistake-review-${item.id}`, actionPath: '/StudyDashboard?tab=mistake', dueAt: formatDate(item.nextReviewDate || item.next_review_date) }))
  upcomingInterviews.value.filter((item) => isBetween(item.interviewDate || item.interview_date, new Date(), addDays(new Date(), 7))).slice(0, 8).forEach((item) => reminders.push({ noticeType: 'interview_upcoming', title: `面试安排：${item.companyName || '目标公司'}`, content: `${item.positionName || '面试准备'} · ${formatDate(item.interviewDate || item.interview_date)}`, bizId: item.id, dedupeKey: `interview-upcoming-${item.id}`, actionPath: '/InterviewLab', dueAt: formatDate(item.interviewDate || item.interview_date) }))
  if (!summary.value.checkedInToday) reminders.push({ noticeType: 'checkin', title: '今天还没有留下成长足迹', content: '完成每日打卡，开启今天的计划', dedupeKey: `checkin-${dateKey(new Date())}`, actionPath: '/DailyCheckin', dueAt: dateKey(new Date()) })
  try { await request.post('/notifications/sync', reminders) } catch (_) {}
  await loadNotifications()
}
const loadNotifications = async () => {
  try {
    const response = await request.get('/notifications', { limit: 50 })
    notifications.value = response.data?.records || []
    unreadCount.value = Number(response.data?.unreadCount || 0)
    if (response.data?.preference) notificationPreference.value = { ...notificationPreference.value, ...response.data.preference }
  } catch (_) {}
}
const markNotification = async (item) => {
  if (!item.readStatus) await request.put(`/notifications/${item.id}/read`)
  if (item.actionPath) router.push(item.actionPath)
  await loadNotifications()
}
const markAllNotifications = async () => { await request.put('/notifications/read-all'); await loadNotifications() }
const dismissNotification = async (item) => { await request.delete(`/notifications/${item.id}`); await loadNotifications() }
const savePreference = async () => {
  localStorage.setItem('preferred_stage', preferredStage.value)
  try {
    await request.put('/notifications/preference', { ...notificationPreference.value, preferredStage: preferredStage.value })
    ElMessage.success('成长阶段与提醒偏好已保存')
  } catch (_) { ElMessage.warning('已保存到本机，后端偏好暂时不可用') }
}
const addPlanItem = async (plan) => {
  if (planAdded.value.includes(plan.key)) return
  const task = { taskName: plan.title, taskType: 'GROWTH_PLAN', status: 'TODO', priority: 'HIGH', dueDate: formatDate(plan.dueDate) === '未定日期' ? dateKey(new Date()) : formatDate(plan.dueDate), estimatedMinutes: 30, notes: plan.description }
  try {
    await request.post('/workplace/tasks', task)
    planAdded.value.push(plan.key)
    ElMessage.success('已加入今日行动')
  } catch (error) { ElMessage.error(error.message || '加入计划失败') }
}
const shiftWeek = async (amount) => { weekStart.value = addDays(weekStart.value, amount * 7); await loadWeeklyReport() }
const shiftMonth = (amount) => { currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + amount, 1) }
const goToday = () => { weekStart.value = startOfWeek(new Date()); currentMonth.value = new Date(new Date().getFullYear(), new Date().getMonth(), 1) }
const startDrag = (event, item) => { if (item.editable) event.dataTransfer.setData('application/json', JSON.stringify({ type: item.type, id: item.item.id })) }
const dropEvent = async (event, day) => {
  const raw = event.dataTransfer.getData('application/json')
  if (!raw) return
  const payload = JSON.parse(raw)
  const source = calendarEvents.value.find((item) => item.type === payload.type && String(item.item.id) === String(payload.id))
  if (!source) return
  try {
    if (source.type === 'task') await request.post('/workplace/tasks', { ...source.item, dueDate: day.key })
    if (source.type === 'goal') await request.post('/workplace/goals', { ...source.item, targetDate: day.key })
    if (source.type === 'interview') await request.post('/workplace/interviews', { ...source.item, interviewDate: day.key })
    ElMessage.success('日期已更新')
    await loadData()
  } catch (error) { ElMessage.error(error.message || '日期更新失败') }
}
const downloadBlob = (content, filename, type) => { const url = URL.createObjectURL(new Blob([content], { type })); const link = document.createElement('a'); link.href = url; link.download = filename; link.click(); URL.revokeObjectURL(url) }
const exportJson = () => downloadBlob(JSON.stringify({ exportedAt: new Date().toISOString(), summary: summary.value, tasks: tasks.value, goals: goals.value, interviews: interviews.value, mistakes: mistakes.value, scores: scores.value, growthRecords: growthRecords.value, articles: articles.value }, null, 2), `拾光记备份-${dateKey(new Date())}.json`, 'application/json;charset=utf-8')
const exportExcel = () => {
  const rows = calendarEvents.value.map((item) => `<tr><td>${item.date}</td><td>${item.type}</td><td>${escapeHtml(item.title)}</td></tr>`).join('')
  downloadBlob(`<html><meta charset="utf-8"><table><tr><th>日期</th><th>类型</th><th>内容</th></tr>${rows}</table></html>`, `拾光记日历-${dateKey(new Date())}.xls`, 'application/vnd.ms-excel;charset=utf-8')
}
const exportPdf = () => {
  const printWindow = window.open('', '_blank', 'width=920,height=720')
  if (!printWindow) { ElMessage.warning('浏览器阻止了打印窗口，请允许弹窗后重试'); return }
  printWindow.document.write(`<html><head><title>拾光记成长周报</title><style>body{font-family:Arial,"Microsoft Yahei";padding:40px;color:#25162f}h1{margin-bottom:6px}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.item{padding:16px;background:#faf5ff;border:1px solid #eadcff;border-radius:8px}small{color:#75677e}</style></head><body><h1>拾光记 · 成长周报</h1><small>${weekLabel.value}</small><div class="grid">${Object.entries(weeklyStats.value).map(([key, value]) => `<div class="item"><strong>${value}</strong><br><small>${key}</small></div>`).join('')}</div><h2>本周复盘</h2><p>${escapeHtml(reportDraft.value.reflection || '还没有填写复盘。')}</p><h2>下周重点</h2><p>${escapeHtml(reportDraft.value.nextWeekFocus || '还没有填写计划。')}</p></body></html>`)
  printWindow.document.close(); printWindow.focus(); printWindow.print()
}
const escapeHtml = (value) => String(value || '').replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]))
const drawShareCard = async () => {
  await nextTick()
  const canvas = shareCanvas.value
  if (!canvas) return
  const context = canvas.getContext('2d')
  canvas.width = 1200; canvas.height = 630
  context.fillStyle = '#25162f'; context.fillRect(0, 0, 1200, 630)
  context.fillStyle = '#f4e9ff'; context.fillRect(42, 42, 1116, 546)
  context.fillStyle = '#8b42bd'; context.font = '700 28px Microsoft Yahei'; context.fillText('拾光记 · 成长周报', 90, 120)
  context.fillStyle = '#25162f'; context.font = '700 54px Microsoft Yahei'; context.fillText(`${weeklyStats.value.completedTasks} 项行动完成`, 90, 220)
  context.font = '26px Microsoft Yahei'; context.fillStyle = '#6b5a76'; context.fillText(`${weekLabel.value} · ${activeStageLabel.value}`, 90, 270)
  const metrics = [`学习 ${weeklyStats.value.studyHours} 小时`, `复习 ${weeklyStats.value.reviewedMistakes} 道错题`, `发布 ${weeklyStats.value.articles} 条内容`]
  if (shareIncludeScores.value) metrics.push(`平均成绩 ${weeklyStats.value.averageScore || '-'} 分`)
  if (shareIncludeStreak.value) metrics.push(`连续打卡 ${weeklyStats.value.streak} 天`)
  context.font = '24px Microsoft Yahei'; context.fillStyle = '#3d2a48'; metrics.forEach((metric, index) => context.fillText(metric, 90, 350 + index * 42))
  if (shareIncludeName.value && currentUserName.value) { context.font = '22px Microsoft Yahei'; context.fillStyle = '#8b42bd'; context.fillText(`${currentUserName.value} 的成长记录`, 90, 530) }
  context.font = '20px Microsoft Yahei'; context.fillStyle = '#8c7b95'; context.fillText('把完成过的事情留下来，下一次回看就有答案。', 690, 535)
}
const downloadShareCard = async () => { await drawShareCard(); const link = document.createElement('a'); link.download = `拾光记周报-${dateKey(new Date())}.png`; link.href = shareCanvas.value.toDataURL('image/png'); link.click() }
const refresh = async () => { refreshing.value = true; await loadData() }
const handleStageChange = async () => { await savePreference() }
watch(() => route.query.view, (value) => {
  if (['overview', 'plan', 'calendar', 'notifications', 'tools'].includes(String(value || ''))) activeView.value = String(value)
})
watch([activeView, shareIncludeName, shareIncludeScores, shareIncludeStreak], () => { if (activeView.value === 'tools') drawShareCard() })
onMounted(() => {
  const requestedView = String(route.query.view || route.query.tab || '')
  if (['overview', 'plan', 'calendar', 'notifications', 'tools'].includes(requestedView)) activeView.value = requestedView
  loadData()
})
</script>

<template>
  <div class="app-shell app-page-bg min-h-screen" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="planner-hero"><div><p class="app-section-label mb-2">Growth OS · Review Loop</p><h1 class="app-page-title">成长规划中心</h1><p class="app-page-desc">把一周的行动、复习、考试和记录整理成下一步。</p></div><div class="flex flex-wrap gap-2"><button class="app-btn-secondary" type="button" @click="router.push('/Today')"><Calendar class="mr-1 inline-block h-4 w-4" />今日工作台</button><button class="app-btn-secondary" type="button" :disabled="refreshing" @click="refresh"><Refresh class="mr-1 inline-block h-4 w-4" />同步数据</button></div></header>
        <div class="planner-tabs" role="tablist"><button v-for="tab in [{key:'overview',label:'成长周报',icon:'📊'},{key:'plan',label:'智能计划',icon:'🎯'},{key:'calendar',label:'周/月日历',icon:'🗓️'},{key:'notifications',label:'通知中心',icon:'🔔'},{key:'tools',label:'导出与分享',icon:'📤'}]" :key="tab.key" type="button" role="tab" :aria-selected="activeView === tab.key" :class="{ active: activeView === tab.key }" @click="activeView = tab.key"><span>{{ tab.icon }}</span>{{ tab.label }}<b v-if="tab.key === 'notifications' && unreadCount">{{ unreadCount }}</b></button></div>
        <div v-if="loading" class="app-card-surface planner-loading"><Refresh class="h-7 w-7 animate-spin" /><strong>正在整理你的成长数据...</strong><span>不同模块会陆续汇总到同一份规划中。</span></div>
        <template v-else>
          <section v-if="activeView === 'overview'" class="space-y-5">
            <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><article class="planner-stat app-card-surface"><span>行动完成</span><strong>{{ weeklyStats.completedTasks }}</strong><small>{{ weekLabel }}</small></article><article class="planner-stat app-card-surface"><span>学习时长</span><strong>{{ weeklyStats.studyHours }}<em>h</em></strong><small>来自成长记录</small></article><article class="planner-stat app-card-surface"><span>错题复习</span><strong>{{ weeklyStats.reviewedMistakes }}</strong><small>本周复习记录</small></article><article class="planner-stat app-card-surface"><span>连续打卡</span><strong>{{ weeklyStats.streak }}<em>天</em></strong><small>继续保持节奏</small></article></div><div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4"><article v-for="quadrant in quadrantStats" :key="quadrant.key" class="app-card-surface p-4"><div class="mb-2 flex items-center justify-between gap-2"><span class="text-xs text-zinc-500">{{ quadrant.label }}</span><strong class="text-sm">{{ quadrant.done }}/{{ quadrant.total }}</strong></div><div class="h-1.5 overflow-hidden rounded-full bg-zinc-200 dark:bg-zinc-800"><i class="block h-full rounded-full" :class="'bg-' + quadrant.tone + '-500'" :style="{ width: (quadrant.total ? quadrant.done / quadrant.total * 100 : 0) + '%' }"></i></div></article></div>
            <div class="planner-two-col"><section class="app-card-surface p-5 sm:p-6"><div class="flex flex-wrap items-end justify-between gap-3 mb-5"><div><p class="app-section-label">Weekly review</p><h2 class="planner-title">本周发生了什么</h2><p class="planner-muted">系统根据已有记录生成摘要，你只需要补充真实感受。</p></div><div class="flex gap-2"><button class="mini-action" type="button" @click="shiftWeek(-1)">上一周</button><button class="mini-action" type="button" @click="goToday">本周</button><button class="mini-action" type="button" @click="shiftWeek(1)">下一周</button></div></div><div class="review-summary"><p>这周完成了 <strong>{{ weeklyStats.completedTasks }}</strong> 项行动，记录了 <strong>{{ weeklyStats.records }}</strong> 段成长，复习了 <strong>{{ weeklyStats.reviewedMistakes }}</strong> 道错题<span v-if="weeklyStats.averageScore">，平均成绩 <strong>{{ weeklyStats.averageScore }}</strong> 分</span>。</p><p class="planner-muted">{{ weeklyStats.records || weeklyStats.completedTasks ? '你已经留下了足够多的证据，继续把下一步写清楚。' : '这周还没有足够的记录，从完成一件小事开始。' }}</p></div><label class="planner-label">本周复盘<textarea v-model="reportDraft.reflection" rows="4" placeholder="这周最值得记住的进步、困难或发现是什么？"></textarea></label><label class="planner-label">下周重点<textarea v-model="reportDraft.nextWeekFocus" rows="3" placeholder="把下周最重要的 1-3 件事写下来..."></textarea></label><button class="app-btn-primary" type="button" :disabled="reportSaving" @click="saveWeeklyReport"><Check class="mr-1 inline-block h-4 w-4" />{{ reportSaving ? '保存中...' : '保存本周复盘' }}</button></section><aside class="space-y-5"><section class="app-card-surface p-5"><div class="flex items-center justify-between mb-4"><div><p class="app-section-label">Next move</p><h2 class="planner-title">明天先做什么</h2></div><Trophy class="h-6 w-6 text-brand-500" /></div><div v-if="smartPlan.length" class="space-y-2"><article v-for="item in smartPlan" :key="item.key" class="planner-plan-row"><span class="plan-dot"></span><span class="min-w-0 flex-1"><strong>{{ item.title }}</strong><small>{{ item.description }} · {{ formatDate(item.dueDate) }}</small></span><button class="mini-action" type="button" :disabled="planAdded.includes(item.key)" @click="addPlanItem(item)">{{ planAdded.includes(item.key) ? '已加入' : '加入' }}</button></article></div><p v-else class="planner-empty">当前没有明确的下一步，去任务或目标页面添加一项。</p></section><section class="app-card-surface p-5"><div class="flex items-center justify-between mb-4"><div><p class="app-section-label">Personalize</p><h2 class="planner-title">我的成长阶段</h2></div><EditPen class="h-5 w-5 text-brand-500" /></div><div class="stage-options"><button v-for="stage in stageOptions" :key="stage.value" type="button" :class="{ active: preferredStage === stage.value }" @click="preferredStage = stage.value; handleStageChange()"><strong>{{ stage.label }}</strong><small>{{ stage.description }}</small></button></div></section></aside></div>
          </section>
          <section v-else-if="activeView === 'plan'" class="planner-two-col"><section class="app-card-surface p-5 sm:p-6"><div class="mb-5"><p class="app-section-label">Daily priorities</p><h2 class="planner-title">智能今日计划</h2><p class="planner-muted">按截止日期、复习时间和面试安排，选出最值得今天行动的三件事。</p></div><div v-if="smartPlan.length" class="space-y-3"><article v-for="item in smartPlan" :key="item.key" class="plan-card"><div class="plan-card-index">{{ smartPlan.indexOf(item) + 1 }}</div><div class="min-w-0 flex-1"><strong>{{ item.title }}</strong><p>{{ item.description }}</p><small>{{ item.source }} · {{ formatDate(item.dueDate) }}</small></div><button class="app-btn-primary" type="button" :disabled="planAdded.includes(item.key)" @click="addPlanItem(item)">{{ planAdded.includes(item.key) ? '已加入行动' : '加入今日行动' }}</button></article></div><div v-else class="planner-empty">暂无推荐计划。</div></section><aside class="app-card-surface p-5 sm:p-6"><p class="app-section-label">Why these</p><h2 class="planner-title mb-4">推荐依据</h2><ul class="reason-list"><li>优先处理已经逾期或临近截止的任务</li><li>把到期错题转成可执行的复习动作</li><li>面试前 7 天自动提高准备事项优先级</li><li>目标没有下一步时，建议先拆出一个小行动</li></ul></aside></section>
          <section v-else-if="activeView === 'calendar'" class="app-card-surface p-5 sm:p-6"><div class="calendar-toolbar"><div><p class="app-section-label">Schedule</p><h2 class="planner-title">行动日历</h2><p class="planner-muted">任务、目标和面试可以拖动调整日期；错题和成绩为只读记录。</p></div><div class="flex flex-wrap gap-2"><button class="mini-action" type="button" @click="calendarMode = 'week'" :class="{ active: calendarMode === 'week' }">周</button><button class="mini-action" type="button" @click="calendarMode = 'month'" :class="{ active: calendarMode === 'month' }">月</button><button class="mini-action" type="button" @click="calendarMode === 'month' ? shiftMonth(-1) : shiftWeek(-1)">上一页</button><button class="mini-action" type="button" @click="goToday">今天</button><button class="mini-action" type="button" @click="calendarMode === 'month' ? shiftMonth(1) : shiftWeek(1)">下一页</button></div></div><div class="calendar-heading"><strong>{{ calendarMode === 'month' ? `${currentMonth.getFullYear()} 年 ${currentMonth.getMonth() + 1} 月` : weekLabel }}</strong></div><div class="calendar-grid"><div v-for="label in ['一','二','三','四','五','六','日']" :key="label" class="calendar-weekday">周{{ label }}</div><div v-for="day in calendarDays" :key="day.key" class="calendar-day" :class="{ muted: !day.inMonth && calendarMode === 'month', today: day.key === dateKey(new Date()) }" @dragover.prevent @drop.prevent="dropEvent($event, day)"><time>{{ day.date.getDate() }}</time><button v-for="event in day.events.slice(0, 4)" :key="event.key" type="button" class="calendar-event" :class="`event-${event.type}`" :draggable="event.editable" @dragstart="startDrag($event, event)" @click="event.type === 'task' ? router.push('/WorkRecords') : event.type === 'interview' ? router.push('/InterviewLab') : null">{{ event.title }}</button><span v-if="day.events.length > 4" class="calendar-more">+{{ day.events.length - 4 }} 项</span></div></div></section>
          <section v-else-if="activeView === 'notifications'" class="planner-two-col"><section class="app-card-surface p-5 sm:p-6"><div class="flex flex-wrap items-end justify-between gap-3 mb-5"><div><p class="app-section-label">Inbox</p><h2 class="planner-title">通知中心 <span v-if="unreadCount" class="notification-count">{{ unreadCount }} 未读</span></h2><p class="planner-muted">逾期任务、错题复习、面试和打卡提醒会保存在这里。</p></div><button class="app-btn-secondary" type="button" @click="markAllNotifications">全部已读</button></div><div v-if="notifications.length" class="space-y-2"><div v-for="item in notifications" :key="item.id" class="notification-row" :class="{ unread: !item.readStatus }"><button type="button" class="notification-main" @click="markNotification(item)"><span class="notification-dot" :class="`dot-${item.noticeType}`"></span><span class="min-w-0 flex-1"><strong>{{ item.title }}</strong><small>{{ item.content || '打开相关模块继续行动' }} · {{ item.dueAt || '' }}</small></span><ArrowRight class="h-4 w-4 shrink-0" /></button><button type="button" class="notification-dismiss" aria-label="移除通知" title="移除" @click.stop="dismissNotification(item)">×</button></div></div><div v-else class="planner-empty"><Bell class="mb-2 h-8 w-8" /><strong>暂时没有通知</strong><span>完成一次打卡或创建任务后，提醒会自动出现。</span></div></section><aside class="app-card-surface p-5 sm:p-6"><p class="app-section-label">Quiet hours</p><h2 class="planner-title mb-4">提醒设置</h2><label class="switch-row"><span>启用提醒</span><input v-model="notificationPreference.enabled" type="checkbox" :true-value="1" :false-value="0" @change="savePreference" /></label><label class="switch-row"><span>免打扰时段</span><input v-model="notificationPreference.quietHoursEnabled" type="checkbox" :true-value="1" :false-value="0" @change="savePreference" /></label><div class="grid grid-cols-2 gap-2"><label class="planner-label">开始<input v-model="notificationPreference.quietStart" type="time" @change="savePreference" /></label><label class="planner-label">结束<input v-model="notificationPreference.quietEnd" type="time" @change="savePreference" /></label></div></aside></section>
          <section v-else class="space-y-5"><div class="app-card-surface p-5 sm:p-6"><div class="mb-5"><p class="app-section-label">Own your data</p><h2 class="planner-title">导出与分享</h2><p class="planner-muted">把成长数据带走，或把一周的进步生成一张可分享的成就卡片。</p></div><div class="tool-grid"><button class="tool-button" type="button" @click="exportJson"><Download /><strong>JSON 备份</strong><small>完整导出任务、成绩、错题、文章和成长记录</small></button><button class="tool-button" type="button" @click="exportExcel"><Download /><strong>Excel 日历</strong><small>导出所有带日期的行动和里程碑</small></button><button class="tool-button" type="button" @click="exportPdf"><Download /><strong>打印为 PDF</strong><small>生成周报打印页，可在浏览器中保存为 PDF</small></button></div></div><div class="app-card-surface p-5 sm:p-6"><div class="flex flex-wrap items-center justify-between gap-3 mb-4"><div><p class="app-section-label">Shareable proof</p><h2 class="planner-title">成就分享卡片</h2></div><button class="app-btn-primary" type="button" @click="downloadShareCard"><Share class="mr-1 inline-block h-4 w-4" />下载 PNG</button></div><div class="flex flex-wrap gap-4 mb-5"><label class="switch-row"><span>显示姓名</span><input v-model="shareIncludeName" type="checkbox" /></label><label class="switch-row"><span>显示成绩</span><input v-model="shareIncludeScores" type="checkbox" /></label><label class="switch-row"><span>显示连续打卡</span><input v-model="shareIncludeStreak" type="checkbox" /></label></div><canvas ref="shareCanvas" class="share-canvas" width="1200" height="630"></canvas></div></section>
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.planner-hero { display:flex; flex-wrap:wrap; align-items:flex-end; justify-content:space-between; gap:18px; }
.planner-tabs { display:flex; gap:6px; overflow-x:auto; border-bottom:1px solid var(--app-border); padding-bottom:5px; scrollbar-width:none; }
.planner-tabs::-webkit-scrollbar { display:none; }
.planner-tabs button { display:inline-flex; flex:0 0 auto; align-items:center; gap:6px; border-bottom:2px solid transparent; padding:10px 12px; color:var(--app-text-muted); font-size:13px; font-weight:700; }
.planner-tabs button.active { border-color:var(--theme-primary); color:var(--theme-primary); }
.planner-tabs b,.notification-count { border-radius:999px; background:rgb(var(--theme-primary-rgb) / .12); padding:2px 6px; color:var(--theme-primary); font-size:10px; }
.planner-loading { display:grid; min-height:220px; place-items:center; align-content:center; gap:8px; color:var(--theme-primary); }
.planner-loading strong { color:var(--app-text); font-size:14px; }
.planner-loading span,.planner-muted { color:var(--app-text-muted); font-size:12px; }
.planner-two-col { display:grid; gap:20px; grid-template-columns:minmax(0,1.35fr) minmax(280px,.65fr); }
.planner-stat { min-height:120px; padding:18px; }
.planner-stat span { color:var(--app-text-muted); font-size:12px; }
.planner-stat strong { display:block; margin-top:8px; color:var(--app-text); font-size:30px; font-weight:850; }
.planner-stat em { margin-left:3px; color:var(--app-text-muted); font-size:12px; font-style:normal; }
.planner-stat small { display:block; margin-top:5px; color:var(--app-text-muted); font-size:11px; }
.planner-title { color:var(--app-text); font-size:18px; font-weight:850; }
.review-summary { margin-bottom:18px; border-left:3px solid var(--theme-primary); background:rgb(var(--theme-primary-rgb) / .06); padding:14px 16px; color:var(--app-text); font-size:14px; line-height:1.8; }
.review-summary strong { color:var(--theme-primary); }
.planner-label { display:grid; gap:6px; margin-top:14px; color:var(--app-text); font-size:12px; font-weight:700; }
.planner-label textarea,.planner-label input { width:100%; border:1px solid var(--app-border); border-radius:8px; background:transparent; padding:10px; color:var(--app-text); outline:none; resize:vertical; }
.planner-label textarea:focus,.planner-label input:focus { border-color:rgb(var(--theme-primary-rgb) / .55); }
.planner-plan-row,.notification-row { display:flex; width:100%; align-items:center; gap:10px; border:1px solid var(--app-border); border-radius:8px; background:transparent; padding:7px 9px; text-align:left; }
.notification-main { display:flex; min-width:0; flex:1; align-items:center; gap:10px; text-align:left; }.notification-dismiss { width:24px; height:24px; border-radius:5px; color:var(--app-text-muted); font-size:18px; line-height:20px; }.notification-dismiss:hover { background:rgb(225 29 72 / .1); color:rgb(225 29 72); }.planner-plan-row strong,.notification-row strong { display:block; overflow:hidden; color:var(--app-text); font-size:13px; text-overflow:ellipsis; white-space:nowrap; }
.planner-plan-row small,.notification-row small { display:block; margin-top:3px; overflow:hidden; color:var(--app-text-muted); font-size:11px; text-overflow:ellipsis; white-space:nowrap; }
.plan-dot,.notification-dot { width:9px; height:9px; flex:0 0 9px; border-radius:50%; background:var(--theme-primary); }
.notification-dot { background:rgb(37 99 235); }
.dot-task_overdue { background:rgb(225 29 72); }.dot-mistake_review { background:rgb(217 119 6); }.dot-interview_upcoming { background:rgb(37 99 235); }.dot-checkin { background:rgb(5 150 105); }
.notification-row.unread { border-color:rgb(var(--theme-primary-rgb) / .32); background:rgb(var(--theme-primary-rgb) / .05); }
.planner-empty { display:grid; justify-items:center; gap:5px; padding:34px 12px; color:var(--app-text-muted); text-align:center; font-size:12px; }
.planner-empty strong { color:var(--app-text); font-size:14px; }
.stage-options { display:grid; gap:7px; }
.stage-options button { display:grid; gap:3px; border:1px solid var(--app-border); border-radius:8px; padding:10px; text-align:left; }
.stage-options button.active { border-color:rgb(var(--theme-primary-rgb) / .5); background:rgb(var(--theme-primary-rgb) / .08); }
.stage-options strong { color:var(--app-text); font-size:13px; }.stage-options small { color:var(--app-text-muted); font-size:11px; }
.plan-card { display:flex; align-items:center; gap:12px; border:1px solid var(--app-border); border-radius:8px; padding:15px; }
.plan-card-index { display:grid; width:30px; height:30px; flex:0 0 30px; place-items:center; border-radius:50%; background:var(--theme-primary); color:white; font-weight:800; }
.plan-card strong { color:var(--app-text); font-size:14px; }.plan-card p { margin-top:3px; color:var(--app-text-muted); font-size:12px; }.plan-card small { display:block; margin-top:6px; color:var(--theme-primary); font-size:11px; }
.reason-list { display:grid; gap:12px; color:var(--app-text-muted); font-size:13px; line-height:1.5; }.reason-list li { padding-left:16px; position:relative; }.reason-list li::before { content:'+'; position:absolute; left:0; color:var(--theme-primary); font-weight:800; }
.calendar-toolbar { display:flex; flex-wrap:wrap; align-items:flex-end; justify-content:space-between; gap:14px; }.calendar-heading { margin:18px 0 10px; color:var(--app-text); font-size:15px; }.calendar-grid { display:grid; grid-template-columns:repeat(7,minmax(0,1fr)); border-top:1px solid var(--app-border); border-left:1px solid var(--app-border); }.calendar-weekday { padding:8px; border-right:1px solid var(--app-border); border-bottom:1px solid var(--app-border); color:var(--app-text-muted); font-size:11px; font-weight:700; text-align:center; }.calendar-day { min-height:116px; border-right:1px solid var(--app-border); border-bottom:1px solid var(--app-border); padding:7px; }.calendar-day.muted { opacity:.45; }.calendar-day.today { background:rgb(var(--theme-primary-rgb) / .06); }.calendar-day time { display:block; margin-bottom:5px; color:var(--app-text); font-size:12px; font-weight:800; }.calendar-event { display:block; width:100%; overflow:hidden; border-left:3px solid var(--theme-primary); margin-top:4px; background:rgb(var(--theme-primary-rgb) / .08); padding:3px 5px; color:var(--app-text); font-size:10px; text-align:left; text-overflow:ellipsis; white-space:nowrap; }.calendar-event.event-interview { border-color:rgb(37 99 235); }.calendar-event.event-mistake { border-color:rgb(217 119 6); }.calendar-event.event-score { border-color:rgb(5 150 105); }.calendar-event.event-growth { border-color:rgb(139 92 246); }.calendar-more { display:block; margin-top:4px; color:var(--theme-primary); font-size:10px; }
.mini-action { border:1px solid rgb(var(--theme-primary-rgb) / .3); border-radius:6px; background:transparent; padding:6px 9px; color:var(--theme-primary); font-size:11px; font-weight:700; }.mini-action.active { background:rgb(var(--theme-primary-rgb) / .1); }
.tool-grid { display:grid; gap:12px; grid-template-columns:repeat(3,minmax(0,1fr)); }.tool-button { display:grid; justify-items:start; gap:6px; border:1px solid var(--app-border); border-radius:8px; padding:16px; text-align:left; }.tool-button svg { color:var(--theme-primary); }.tool-button strong { color:var(--app-text); font-size:14px; }.tool-button small { color:var(--app-text-muted); font-size:11px; line-height:1.5; }.switch-row { display:flex; align-items:center; justify-content:space-between; gap:12px; color:var(--app-text); font-size:12px; }.share-canvas { display:block; width:100%; max-width:900px; height:auto; border:1px solid var(--app-border); border-radius:8px; }
@media (max-width:900px) { .planner-two-col { grid-template-columns:1fr; }.tool-grid { grid-template-columns:1fr; }.calendar-day { min-height:92px; } }
@media (max-width:640px) { .calendar-day { min-height:76px; padding:4px; }.calendar-event { font-size:9px; }.planner-tabs button { padding-inline:8px; }.plan-card { align-items:flex-start; flex-wrap:wrap; }.plan-card .app-btn-primary { margin-left:42px; } }
</style>
