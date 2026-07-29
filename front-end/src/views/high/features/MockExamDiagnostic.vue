<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const userId = ref(null)
const records = ref([])
const volunteerPlans = ref([])

const number = value => Number(value || 0)
const scoreRate = record => {
  const fullScore = number(record.fullScore) || 100
  return Math.min(100, Math.round(number(record.score) / fullScore * 100))
}

const examGroups = computed(() => {
  const grouped = new Map()
  records.value.forEach((record) => {
    const key = `${record.examName || '未命名考试'}|${record.examDate || ''}`
    if (!grouped.has(key)) {
      grouped.set(key, { key, name: record.examName || '未命名考试', date: record.examDate, total: 0, full: 0, count: 0 })
    }
    const item = grouped.get(key)
    item.total += number(record.score)
    item.full += number(record.fullScore) || 100
    item.count += 1
  })
  return [...grouped.values()]
    .sort((a, b) => String(a.date || '').localeCompare(String(b.date || '')))
    .map(item => ({ ...item, rate: item.full ? Math.round(item.total / item.full * 100) : 0 }))
})

const latestExam = computed(() => examGroups.value.at(-1) || null)
const previousExam = computed(() => examGroups.value.at(-2) || null)
const latestChange = computed(() => latestExam.value && previousExam.value
  ? latestExam.value.total - previousExam.value.total : null)

const subjectStats = computed(() => {
  const stats = new Map()
  records.value.forEach((record) => {
    const name = record.subjectName || '未分类科目'
    const current = stats.get(name) || { name, totalRate: 0, count: 0, latest: null }
    current.totalRate += scoreRate(record)
    current.count += 1
    if (!current.latest || String(record.examDate || '') >= String(current.latest.examDate || '')) current.latest = record
    stats.set(name, current)
  })
  return [...stats.values()]
    .map(item => ({ ...item, averageRate: Math.round(item.totalRate / item.count), latestRate: scoreRate(item.latest) }))
    .sort((a, b) => a.averageRate - b.averageRate)
})

const targetScore = computed(() => {
  const value = volunteerPlans.value[0]?.targetScore ?? volunteerPlans.value[0]?.minScore
  return value == null ? null : number(value)
})
const targetGap = computed(() => latestExam.value && targetScore.value != null
  ? targetScore.value - latestExam.value.total : null)

const diagnosticSummary = computed(() => {
  if (!records.value.length) return '先录入至少一次模考的分科成绩，系统会自动找出薄弱学科和总分变化。'
  const weakest = subjectStats.value.slice(0, 2).map(item => item.name).join('、')
  if (targetGap.value != null && targetGap.value > 0) return `距离当前志愿目标还差 ${targetGap.value} 分，优先补强 ${weakest || '薄弱学科'}。`
  if (latestChange.value != null && latestChange.value < 0) return `最近一次总分较上次下降 ${Math.abs(latestChange.value)} 分，建议先复盘 ${weakest || '失分科目'}。`
  return `当前优势较稳定，下一轮重点仍是 ${weakest || '薄弱学科'} 的得分率提升。`
})

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const userRes = await request.get('/user/getUserById')
    userId.value = userRes.data?.id
    if (!userId.value) throw new Error('用户信息不可用')
    const [scoreRes, planRes] = await Promise.all([
      request.get(`/score/list/${userId.value}`),
      request.get(`/volunteer/plan/list/${userId.value}`).catch(() => ({ data: [] })),
    ])
    records.value = scoreRes.data || []
    volunteerPlans.value = planRes.data || []
  } catch (error) {
    loadError.value = '暂时无法读取成绩数据'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="diagnostic space-y-4">
    <header class="diagnostic-head">
      <div>
        <p class="app-section-label">Mock Exam</p>
        <h2>模考诊断</h2>
        <p>{{ diagnosticSummary }}</p>
      </div>
      <div class="head-actions">
        <button type="button" class="app-btn-secondary text-sm" @click="router.push('/StudyDashboard?tab=analysis')">录入与查看成绩</button>
        <button type="button" class="app-btn-primary text-sm" @click="router.push('/Volunteer')">校准志愿目标</button>
      </div>
    </header>

    <p v-if="loadError" class="state-message">{{ loadError }}</p>
    <p v-else-if="loading" class="state-message">正在汇总模考数据...</p>

    <template v-else>
      <div class="summary-grid">
        <article class="metric-card">
          <span>最近总分</span>
          <strong>{{ latestExam ? latestExam.total : '-' }}</strong>
          <small>{{ latestExam?.name || '暂无模考记录' }}</small>
        </article>
        <article class="metric-card">
          <span>总分得分率</span>
          <strong>{{ latestExam ? `${latestExam.rate}%` : '-' }}</strong>
          <small>{{ latestExam?.date || '录入分科成绩后生成' }}</small>
        </article>
        <article class="metric-card">
          <span>较上次变化</span>
          <strong :class="latestChange == null ? '' : latestChange >= 0 ? 'positive' : 'negative'">{{ latestChange == null ? '-' : `${latestChange >= 0 ? '+' : ''}${latestChange}` }}</strong>
          <small>{{ previousExam ? `对比 ${previousExam.name}` : '需要两次模考记录' }}</small>
        </article>
        <article class="metric-card">
          <span>目标分差</span>
          <strong :class="targetGap == null ? '' : targetGap <= 0 ? 'positive' : 'negative'">{{ targetGap == null ? '-' : targetGap <= 0 ? '已达标' : `差 ${targetGap}` }}</strong>
          <small>{{ targetScore == null ? '在志愿方案设置目标分' : `目标 ${targetScore} 分` }}</small>
        </article>
      </div>

      <div class="diagnostic-grid">
        <article class="app-card-surface p-5">
          <div class="panel-heading"><h3>学科薄弱排序</h3><span>按平均得分率</span></div>
          <div v-if="subjectStats.length" class="subject-list">
            <div v-for="subject in subjectStats" :key="subject.name" class="subject-row">
              <div><strong>{{ subject.name }}</strong><small>最近得分率 {{ subject.latestRate }}%</small></div>
              <div class="progress-track"><i :style="{ width: `${subject.averageRate}%` }"></i></div>
              <b>{{ subject.averageRate }}%</b>
            </div>
          </div>
          <p v-else class="empty-copy">还没有可分析的分科成绩。</p>
        </article>

        <article class="app-card-surface p-5">
          <div class="panel-heading"><h3>总分趋势</h3><span>最近 {{ examGroups.length }} 次</span></div>
          <div v-if="examGroups.length" class="trend-list">
            <div v-for="exam in examGroups" :key="exam.key" class="trend-item">
              <span>{{ exam.name }}</span><b>{{ exam.total }} 分</b><small>{{ exam.rate }}% · {{ exam.date || '未填写日期' }}</small>
            </div>
          </div>
          <p v-else class="empty-copy">先在公共学习仪表盘录入模考成绩。</p>
        </article>
      </div>
    </template>
  </section>
</template>

<style scoped>
.diagnostic-head,
.head-actions,
.panel-heading,
.subject-row,
.trend-item { display: flex; align-items: center; }
.diagnostic-head { justify-content: space-between; gap: 18px; }
.diagnostic-head h2 { color: var(--app-text); font-size: 21px; font-weight: 800; }
.diagnostic-head p { margin-top: 5px; color: var(--app-text-muted); font-size: 13px; }
.head-actions { flex-wrap: wrap; gap: 8px; }
.summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.metric-card { border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); padding: 16px; }
.metric-card span, .metric-card small { display: block; color: var(--app-text-muted); font-size: 12px; }
.metric-card strong { display: block; margin: 8px 0 4px; color: var(--app-text); font-size: 25px; }
.positive { color: #059669 !important; }.negative { color: #dc2626 !important; }
.diagnostic-grid { display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(280px, .75fr); gap: 14px; }
.panel-heading { justify-content: space-between; gap: 10px; margin-bottom: 14px; }.panel-heading h3 { color: var(--app-text); font-size: 15px; font-weight: 800; }.panel-heading span { color: var(--app-text-muted); font-size: 11px; }
.subject-list, .trend-list { display: grid; gap: 11px; }.subject-row { gap: 12px; }.subject-row > div:first-child { width: 104px; }.subject-row strong, .subject-row small { display: block; }.subject-row small, .trend-item small { margin-top: 2px; color: var(--app-text-muted); font-size: 11px; }.progress-track { height: 8px; flex: 1; overflow: hidden; border-radius: 99px; background: rgb(var(--theme-primary-rgb) / .1); }.progress-track i { display: block; height: 100%; border-radius: inherit; background: var(--theme-primary); }.subject-row b { width: 38px; color: var(--app-text-secondary); font-size: 12px; text-align: right; }.trend-item { justify-content: space-between; flex-wrap: wrap; gap: 5px 10px; border-bottom: 1px solid var(--app-border); padding-bottom: 10px; }.trend-item span { flex: 1; color: var(--app-text-secondary); font-size: 13px; }.trend-item b { color: var(--theme-primary); font-size: 13px; }.trend-item small { width: 100%; }.state-message, .empty-copy { color: var(--app-text-muted); font-size: 13px; }
@media (max-width: 900px) { .summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }.diagnostic-grid { grid-template-columns: 1fr; } }
@media (max-width: 560px) { .diagnostic-head { align-items: flex-start; flex-direction: column; }.summary-grid { grid-template-columns: 1fr; }.head-actions { width: 100%; }.head-actions button { flex: 1; } }
</style>