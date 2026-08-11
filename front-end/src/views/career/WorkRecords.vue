<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const { isDark } = useTheme()
const router = useRouter()

const menuItems = [
  { key: 'work', label: '工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'timeline', label: '成长时间线', icon: '🗓️', path: '/CareerTimeline' },
  { key: 'toolkit', label: '入职工具箱', icon: '🧰', path: '/CareerToolkit' },
  { key: 'interview', label: '模拟面试', icon: '🎙️', path: '/InterviewLab' },
]

const loading = ref(false)
const dashboard = ref({})
const profile = ref(null)
const goals = ref([])
const tasks = ref([])
const interviews = ref([])
const reviews = ref([])
const today = new Date().toISOString().slice(0, 10)

const emptyProfile = () => ({
  id: null,
  currentTitle: '',
  targetTitle: '',
  industry: '',
  city: '',
  yearsOfExperience: 0,
  strengths: '',
  weakness: '',
  careerValues: '',
  salaryExpectation: '',
  visibility: 'PRIVATE',
})

const emptyGoal = () => ({
  id: null,
  goalName: '',
  goalType: 'GROWTH',
  priority: 'MEDIUM',
  status: 'ACTIVE',
  progress: 0,
  startDate: today,
  targetDate: '',
  metric: '',
  notes: '',
})

const emptyTask = () => ({
  id: null,
  goalId: null,
  taskName: '',
  taskType: 'DELIVERY',
  status: 'TODO',
  priority: 'MEDIUM',
  dueDate: '',
  estimatedMinutes: 60,
  actualMinutes: 0,
  outcome: '',
  notes: '',
})

const emptyInterview = () => ({
  id: null,
  companyName: '',
  positionName: '',
  interviewRound: '一面',
  interviewDate: '',
  status: 'PREPARING',
  questionBank: '',
  preparationNotes: '',
  feedback: '',
  result: '',
  confidenceScore: 60,
})

const emptyReview = () => ({
  id: null,
  reviewDate: today,
  reviewType: 'WEEKLY',
  wins: '',
  problems: '',
  learnings: '',
  nextActions: '',
  energyLevel: 3,
  communicationScore: 3,
  deliveryScore: 3,
})

const profileForm = ref(emptyProfile())
const goalForm = ref(emptyGoal())
const taskForm = ref(emptyTask())
const interviewForm = ref(emptyInterview())
const reviewForm = ref(emptyReview())

const stats = computed(() => [
  { label: '职业目标', value: dashboard.value.goalCount ?? goals.value.length },
  { label: '待办任务', value: dashboard.value.todoTaskCount ?? tasks.value.filter((item) => item.status !== 'DONE').length },
  { label: '近期面试', value: dashboard.value.upcomingInterviewCount ?? interviews.value.length },
  { label: '交付均分', value: Math.round(dashboard.value.avgDeliveryScore || 0) },
])

const activeGoals = computed(() => goals.value.filter((item) => item.status !== 'DONE' && item.status !== 'ARCHIVED'))
const overdueTasks = computed(() => tasks.value.filter((item) => item.dueDate && item.dueDate < today && item.status !== 'DONE'))

const statusText = {
  ACTIVE: '进行中', DONE: '已完成', ARCHIVED: '已归档', TODO: '待处理', DOING: '推进中',
  PREPARING: '准备中', WAITING: '等待反馈', PASSED: '通过', FAILED: '未通过',
}
const priorityText = { HIGH: '高', MEDIUM: '中', LOW: '低' }
const getData = (res, fallback) => (res?.code === 200 ? (res.data ?? fallback) : fallback)

const loadAll = async () => {
  loading.value = true
  try {
    const [dashRes, profileRes, goalRes, taskRes, interviewRes, reviewRes] = await Promise.allSettled([
      request.get('/workplace/dashboard'),
      request.get('/workplace/profile'),
      request.get('/workplace/goals'),
      request.get('/workplace/tasks'),
      request.get('/workplace/interviews'),
      request.get('/workplace/reviews'),
    ])
    dashboard.value = dashRes.status === 'fulfilled' ? getData(dashRes.value, {}) : {}
    profile.value = profileRes.status === 'fulfilled' ? getData(profileRes.value, null) : null
    goals.value = goalRes.status === 'fulfilled' ? getData(goalRes.value, []) : []
    tasks.value = taskRes.status === 'fulfilled' ? getData(taskRes.value, []) : []
    interviews.value = interviewRes.status === 'fulfilled' ? getData(interviewRes.value, []) : []
    reviews.value = reviewRes.status === 'fulfilled' ? getData(reviewRes.value, []) : []
    profileForm.value = profile.value ? { ...emptyProfile(), ...profile.value } : emptyProfile()
    if (!taskForm.value.goalId && activeGoals.value.length) taskForm.value.goalId = activeGoals.value[0].id
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  const res = await request.post('/workplace/profile', profileForm.value)
  profile.value = res.data
  profileForm.value = { ...emptyProfile(), ...res.data }
  ElMessage.success('职业档案已保存')
  await loadAll()
}

const saveGoal = async () => {
  if (!goalForm.value.goalName.trim()) return ElMessage.warning('请填写目标名称')
  await request.post('/workplace/goals', goalForm.value)
  ElMessage.success('目标已保存')
  goalForm.value = emptyGoal()
  await loadAll()
}

const editGoal = (item) => { goalForm.value = { ...emptyGoal(), ...item } }
const deleteGoal = async (item) => {
  await ElMessageBox.confirm(`确认删除目标「${item.goalName}」及其任务？`, '删除目标', { type: 'warning' })
  await request.delete(`/workplace/goals/${item.id}`)
  ElMessage.success('目标已删除')
  await loadAll()
}

const saveTask = async () => {
  if (!taskForm.value.taskName.trim()) return ElMessage.warning('请填写任务名称')
  await request.post('/workplace/tasks', taskForm.value)
  ElMessage.success('任务已保存')
  taskForm.value = { ...emptyTask(), goalId: activeGoals.value[0]?.id || null }
  await loadAll()
}

const editTask = (item) => { taskForm.value = { ...emptyTask(), ...item } }
const updateTaskStatus = async (item, status) => {
  await request.post('/workplace/tasks', { ...item, status })
  await loadAll()
}
const deleteTask = async (item) => {
  await ElMessageBox.confirm(`确认删除任务「${item.taskName}」？`, '删除任务', { type: 'warning' })
  await request.delete(`/workplace/tasks/${item.id}`)
  ElMessage.success('任务已删除')
  await loadAll()
}

const saveInterview = async () => {
  if (!interviewForm.value.companyName.trim()) return ElMessage.warning('请填写公司名称')
  await request.post('/workplace/interviews', interviewForm.value)
  ElMessage.success('面试记录已保存')
  interviewForm.value = emptyInterview()
  await loadAll()
}
const editInterview = (item) => { interviewForm.value = { ...emptyInterview(), ...item } }

const saveReview = async () => {
  if (!reviewForm.value.wins.trim() && !reviewForm.value.problems.trim()) return ElMessage.warning('至少填写一个复盘项')
  await request.post('/workplace/reviews', reviewForm.value)
  ElMessage.success('工作复盘已保存')
  reviewForm.value = emptyReview()
  await loadAll()
}
const editReview = (item) => { reviewForm.value = { ...emptyReview(), ...item } }
const goalName = (id) => goals.value.find((item) => String(item.id) === String(id))?.goalName || '未关联目标'

onMounted(loadAll)
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="app-section-label mb-2">Workplace</p>
            <h1 class="app-page-title">职场工作台</h1>
            <p class="app-page-desc">同步后端职场服务，管理职业档案、目标任务、面试准备和周期复盘。</p>
          </div>
          <div class="flex flex-wrap gap-2"><button type="button" class="app-btn-secondary" @click="router.push('/TaskMatrix?view=matrix')">四象限任务</button><button type="button" class="app-btn-secondary" :disabled="loading" @click="loadAll">刷新数据</button></div>
        </header>

        <section class="grid gap-4 md:grid-cols-4">
          <div v-for="item in stats" :key="item.label" class="app-card-surface-flat p-5">
            <p class="text-sm text-zinc-500">{{ item.label }}</p>
            <p class="mt-2 text-3xl font-black">{{ item.value }}</p>
          </div>
        </section>

        <section v-if="dashboard.suggestions?.length || overdueTasks.length" class="app-card-surface p-5">
          <h2 class="mb-3 text-lg font-bold">今日提醒</h2>
          <div class="grid gap-3 md:grid-cols-2">
            <p v-for="item in dashboard.suggestions || []" :key="item" class="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">{{ item }}</p>
            <p v-for="item in overdueTasks" :key="item.id" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ item.taskName }} 已逾期，截止日 {{ item.dueDate }}</p>
          </div>
        </section>

        <section class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <div class="app-card-surface p-5">
            <h2 class="mb-4 text-lg font-bold">职业档案</h2>
            <div class="grid gap-3 sm:grid-cols-2">
              <input v-model="profileForm.currentTitle" class="field" placeholder="当前岗位" />
              <input v-model="profileForm.targetTitle" class="field" placeholder="目标岗位" />
              <input v-model="profileForm.industry" class="field" placeholder="行业方向" />
              <input v-model="profileForm.city" class="field" placeholder="城市" />
              <input v-model.number="profileForm.yearsOfExperience" class="field" min="0" type="number" placeholder="工作年限" />
              <input v-model="profileForm.salaryExpectation" class="field" placeholder="薪资期望" />
            </div>
            <textarea v-model="profileForm.strengths" class="field mt-3 min-h-20" placeholder="优势能力" />
            <textarea v-model="profileForm.weakness" class="field mt-3 min-h-20" placeholder="待提升短板" />
            <textarea v-model="profileForm.careerValues" class="field mt-3 min-h-20" placeholder="职业价值观" />
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveProfile">保存档案</button>
          </div>

          <div class="app-card-surface p-5">
            <div class="mb-4 flex items-center justify-between">
              <h2 class="text-lg font-bold">职业目标</h2>
              <button type="button" class="app-btn-secondary !px-3 !py-2" @click="goalForm = emptyGoal()">新目标</button>
            </div>
            <div class="grid gap-3 md:grid-cols-2">
              <input v-model="goalForm.goalName" class="field md:col-span-2" placeholder="目标名称" />
              <select v-model="goalForm.goalType" class="field"><option value="GROWTH">能力成长</option><option value="PROMOTION">晋升</option><option value="PROJECT">项目交付</option><option value="JOB_CHANGE">机会转换</option></select>
              <select v-model="goalForm.priority" class="field"><option value="HIGH">高优先级</option><option value="MEDIUM">中优先级</option><option value="LOW">低优先级</option></select>
              <input v-model="goalForm.startDate" class="field" type="date" />
              <input v-model="goalForm.targetDate" class="field" type="date" />
              <input v-model="goalForm.metric" class="field md:col-span-2" placeholder="衡量指标" />
              <textarea v-model="goalForm.notes" class="field min-h-16 md:col-span-2" placeholder="备注" />
            </div>
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveGoal">保存目标</button>
            <div class="mt-5 space-y-3">
              <article v-for="item in goals" :key="item.id" class="list-row">
                <div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><h3 class="font-semibold">{{ item.goalName }}</h3><span class="app-pill-tag">{{ statusText[item.status] || item.status }}</span><span class="text-xs text-zinc-500">{{ priorityText[item.priority] || item.priority }}</span></div><p class="mt-1 text-sm text-zinc-500">{{ item.metric || '暂无指标' }} · 截止 {{ item.targetDate || '-' }}</p><div class="mt-3 h-2 rounded-full bg-zinc-100 dark:bg-zinc-800"><div class="h-full rounded-full app-gradient-bar" :style="{ width: `${item.progress || 0}%` }" /></div></div>
                <div class="flex shrink-0 gap-2"><button type="button" class="mini-btn" @click="editGoal(item)">编辑</button><button type="button" class="mini-btn danger" @click="deleteGoal(item)">删除</button></div>
              </article>
              <p v-if="!goals.length" class="empty-text">还没有职业目标。</p>
            </div>
          </div>
        </section>

        <section class="grid gap-6 xl:grid-cols-2">
          <div class="app-card-surface p-5">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">任务推进</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="taskForm = { ...emptyTask(), goalId: activeGoals[0]?.id || null }">新任务</button></div>
            <div class="grid gap-3 md:grid-cols-2">
              <input v-model="taskForm.taskName" class="field md:col-span-2" placeholder="任务名称" />
              <select v-model="taskForm.goalId" class="field"><option :value="null">不关联目标</option><option v-for="item in goals" :key="item.id" :value="item.id">{{ item.goalName }}</option></select>
              <select v-model="taskForm.status" class="field"><option value="TODO">待处理</option><option value="DOING">推进中</option><option value="DONE">已完成</option></select>
              <select v-model="taskForm.priority" class="field"><option value="HIGH">高优先级</option><option value="MEDIUM">中优先级</option><option value="LOW">低优先级</option></select>
              <input v-model="taskForm.dueDate" class="field" type="date" />
              <input v-model.number="taskForm.estimatedMinutes" class="field" min="0" type="number" placeholder="预计分钟" />
              <input v-model.number="taskForm.actualMinutes" class="field" min="0" type="number" placeholder="实际分钟" />
              <textarea v-model="taskForm.notes" class="field min-h-16 md:col-span-2" placeholder="任务说明" />
            </div>
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveTask">保存任务</button>
            <div class="mt-5 space-y-3">
              <article v-for="item in tasks" :key="item.id" class="list-row"><div class="min-w-0 flex-1"><h3 class="font-semibold">{{ item.taskName }}</h3><p class="mt-1 text-sm text-zinc-500">{{ goalName(item.goalId) }} · {{ statusText[item.status] || item.status }} · {{ item.dueDate || '-' }}</p></div><div class="flex shrink-0 flex-wrap justify-end gap-2"><button v-if="item.status !== 'DONE'" type="button" class="mini-btn" @click="updateTaskStatus(item, 'DONE')">完成</button><button type="button" class="mini-btn" @click="editTask(item)">编辑</button><button type="button" class="mini-btn danger" @click="deleteTask(item)">删除</button></div></article>
              <p v-if="!tasks.length" class="empty-text">还没有任务。</p>
            </div>
          </div>

          <div class="space-y-6">
            <div class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">面试准备</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="interviewForm = emptyInterview()">新面试</button><button type="button" class="app-btn-primary !px-3 !py-2" @click="router.push('/InterviewLab')">AI 模拟面试</button></div>
              <div class="grid gap-3 md:grid-cols-2"><input v-model="interviewForm.companyName" class="field" placeholder="公司" /><input v-model="interviewForm.positionName" class="field" placeholder="岗位" /><input v-model="interviewForm.interviewRound" class="field" placeholder="轮次" /><input v-model="interviewForm.interviewDate" class="field" type="date" /><select v-model="interviewForm.status" class="field"><option value="PREPARING">准备中</option><option value="WAITING">等待反馈</option><option value="PASSED">通过</option><option value="FAILED">未通过</option></select><input v-model.number="interviewForm.confidenceScore" class="field" min="0" max="100" type="number" placeholder="信心分" /><textarea v-model="interviewForm.preparationNotes" class="field min-h-16 md:col-span-2" placeholder="准备笔记" /></div>
              <button type="button" class="app-btn-primary mt-4 w-full" @click="saveInterview">保存面试</button>
              <div class="mt-5 space-y-3"><article v-for="item in interviews.slice(0, 5)" :key="item.id" class="list-row"><div><h3 class="font-semibold">{{ item.companyName }} · {{ item.positionName }}</h3><p class="mt-1 text-sm text-zinc-500">{{ item.interviewRound }} · {{ item.interviewDate || '-' }} · {{ statusText[item.status] || item.status }}</p></div><button type="button" class="mini-btn" @click="editInterview(item)">编辑</button></article><p v-if="!interviews.length" class="empty-text">还没有面试记录。</p></div>
            </div>

            <div class="app-card-surface p-5">
              <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">工作复盘</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="reviewForm = emptyReview()">新复盘</button></div>
              <div class="grid gap-3 md:grid-cols-2"><input v-model="reviewForm.reviewDate" class="field" type="date" /><select v-model="reviewForm.reviewType" class="field"><option value="DAILY">日复盘</option><option value="WEEKLY">周复盘</option><option value="MONTHLY">月复盘</option><option value="PROJECT">项目复盘</option></select><textarea v-model="reviewForm.wins" class="field min-h-16 md:col-span-2" placeholder="本期成果" /><textarea v-model="reviewForm.problems" class="field min-h-16 md:col-span-2" placeholder="问题阻塞" /><textarea v-model="reviewForm.learnings" class="field min-h-16 md:col-span-2" placeholder="收获沉淀" /><textarea v-model="reviewForm.nextActions" class="field min-h-16 md:col-span-2" placeholder="下一步行动" /></div>
              <button type="button" class="app-btn-primary mt-4 w-full" @click="saveReview">保存复盘</button>
              <div class="mt-5 space-y-3"><article v-for="item in reviews.slice(0, 4)" :key="item.id" class="list-row"><div><h3 class="font-semibold">{{ item.reviewDate }} · {{ item.reviewType }}</h3><p class="mt-1 line-clamp-2 text-sm text-zinc-500">{{ item.wins || item.learnings || item.problems || '暂无摘要' }}</p></div><button type="button" class="mini-btn" @click="editReview(item)">编辑</button></article><p v-if="!reviews.length" class="empty-text">还没有复盘记录。</p></div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.field { width: 100%; border-radius: 12px; border: 1px solid var(--app-card-border); background: rgba(255, 255, 255, 0.72); padding: 0.65rem 0.8rem; color: var(--app-text); font-size: 0.92rem; outline: none; }
html.dark .field { background: rgba(255, 255, 255, 0.06); }
.field:focus { border-color: rgba(var(--theme-primary-rgb), 0.45); }
.list-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; border: 1px solid var(--app-card-border); border-radius: 12px; padding: 0.85rem; background: rgba(255, 255, 255, 0.42); }
html.dark .list-row { background: rgba(255, 255, 255, 0.04); }
.mini-btn { border-radius: 10px; border: 1px solid var(--app-card-border); padding: 0.35rem 0.65rem; color: var(--app-text-secondary); font-size: 0.8rem; transition: all 160ms ease; }
.mini-btn:hover { color: rgb(var(--color-brand-600)); border-color: rgba(var(--theme-primary-rgb), 0.4); }
.mini-btn.danger:hover { color: #dc2626; border-color: rgba(220, 38, 38, 0.35); }
.empty-text { padding: 1rem; color: var(--app-text-muted); font-size: 0.9rem; text-align: center; }
</style>
