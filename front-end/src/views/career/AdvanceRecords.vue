<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const { isDark } = useTheme()
const loading = ref(false)
const dashboard = ref({})
const roadmaps = ref([])
const milestones = ref([])
const skills = ref([])
const sessions = ref([])
const today = new Date().toISOString().slice(0, 10)

const emptyRoadmap = () => ({ id: null, roadmapName: '', stage: '职业进阶', targetRole: '', status: 'ACTIVE', progress: 0, startDate: today, targetDate: '', coreSkills: '', successMetrics: '', riskNotes: '' })
const emptyMilestone = () => ({ id: null, roadmapId: null, milestoneName: '', milestoneType: 'DELIVERABLE', status: 'TODO', weight: 1, dueDate: '', completedDate: '', evidenceUrl: '', notes: '' })
const emptySkill = () => ({ id: null, roadmapId: null, skillName: '', skillCategory: '专业能力', currentLevel: '入门', targetLevel: '熟练', progress: 0, practiceHours: 0, lastPracticedAt: today, evidence: '', nextPractice: '' })
const emptySession = () => ({ id: null, roadmapId: null, mentorName: '', sessionType: 'REVIEW', sessionDate: today, topic: '', advice: '', actionItems: '', valueScore: 4 })

const roadmapForm = ref(emptyRoadmap())
const milestoneForm = ref(emptyMilestone())
const skillForm = ref(emptySkill())
const sessionForm = ref(emptySession())

const stats = computed(() => [
  { label: '进阶路线', value: dashboard.value.roadmapCount ?? roadmaps.value.length },
  { label: '完成里程碑', value: `${dashboard.value.doneMilestoneCount ?? milestones.value.filter((item) => item.status === 'DONE').length}/${dashboard.value.milestoneCount ?? milestones.value.length}` },
  { label: '技能项', value: dashboard.value.skillCount ?? skills.value.length },
  { label: '技能均值', value: `${Math.round(dashboard.value.avgSkillProgress || 0)}%` },
])
const activeRoadmaps = computed(() => roadmaps.value.filter((item) => item.status !== 'DONE' && item.status !== 'ARCHIVED'))
const overdueMilestones = computed(() => milestones.value.filter((item) => item.dueDate && item.dueDate < today && item.status !== 'DONE'))

const statusText = { ACTIVE: '进行中', DONE: '已完成', ARCHIVED: '已归档', TODO: '待处理', DOING: '推进中' }
const getData = (res, fallback) => (res?.code === 200 ? (res.data ?? fallback) : fallback)
const roadmapName = (id) => roadmaps.value.find((item) => String(item.id) === String(id))?.roadmapName || '未关联路线'

const syncDefaultRoadmap = () => {
  const id = activeRoadmaps.value[0]?.id || null
  if (!milestoneForm.value.roadmapId) milestoneForm.value.roadmapId = id
  if (!skillForm.value.roadmapId) skillForm.value.roadmapId = id
  if (!sessionForm.value.roadmapId) sessionForm.value.roadmapId = id
}

const loadAll = async () => {
  loading.value = true
  try {
    const [dashRes, roadmapRes, milestoneRes, skillRes, sessionRes] = await Promise.allSettled([
      request.get('/advanced/dashboard'),
      request.get('/advanced/roadmaps'),
      request.get('/advanced/milestones'),
      request.get('/advanced/skills'),
      request.get('/advanced/mentor-sessions'),
    ])
    dashboard.value = dashRes.status === 'fulfilled' ? getData(dashRes.value, {}) : {}
    roadmaps.value = roadmapRes.status === 'fulfilled' ? getData(roadmapRes.value, []) : []
    milestones.value = milestoneRes.status === 'fulfilled' ? getData(milestoneRes.value, []) : []
    skills.value = skillRes.status === 'fulfilled' ? getData(skillRes.value, []) : []
    sessions.value = sessionRes.status === 'fulfilled' ? getData(sessionRes.value, []) : []
    syncDefaultRoadmap()
  } finally {
    loading.value = false
  }
}

const saveRoadmap = async () => {
  if (!roadmapForm.value.roadmapName.trim()) return ElMessage.warning('请填写路线名称')
  await request.post('/advanced/roadmaps', roadmapForm.value)
  ElMessage.success('路线已保存')
  roadmapForm.value = emptyRoadmap()
  await loadAll()
}
const editRoadmap = (item) => { roadmapForm.value = { ...emptyRoadmap(), ...item } }
const deleteRoadmap = async (item) => {
  await ElMessageBox.confirm(`确认删除路线「${item.roadmapName}」及其关联记录？`, '删除路线', { type: 'warning' })
  await request.delete(`/advanced/roadmaps/${item.id}`)
  ElMessage.success('路线已删除')
  await loadAll()
}

const saveMilestone = async () => {
  if (!milestoneForm.value.milestoneName.trim()) return ElMessage.warning('请填写里程碑名称')
  await request.post('/advanced/milestones', milestoneForm.value)
  ElMessage.success('里程碑已保存')
  milestoneForm.value = { ...emptyMilestone(), roadmapId: activeRoadmaps.value[0]?.id || null }
  await loadAll()
}
const editMilestone = (item) => { milestoneForm.value = { ...emptyMilestone(), ...item } }
const doneMilestone = async (item) => {
  await request.post('/advanced/milestones', { ...item, status: 'DONE', completedDate: item.completedDate || today })
  await loadAll()
}

const saveSkill = async () => {
  if (!skillForm.value.skillName.trim()) return ElMessage.warning('请填写技能名称')
  await request.post('/advanced/skills', skillForm.value)
  ElMessage.success('技能进度已保存')
  skillForm.value = { ...emptySkill(), roadmapId: activeRoadmaps.value[0]?.id || null }
  await loadAll()
}
const editSkill = (item) => { skillForm.value = { ...emptySkill(), ...item } }

const saveSession = async () => {
  if (!sessionForm.value.mentorName.trim() || !sessionForm.value.topic.trim()) return ElMessage.warning('请填写导师和主题')
  await request.post('/advanced/mentor-sessions', sessionForm.value)
  ElMessage.success('导师会话已保存')
  sessionForm.value = { ...emptySession(), roadmapId: activeRoadmaps.value[0]?.id || null }
  await loadAll()
}
const editSession = (item) => { sessionForm.value = { ...emptySession(), ...item } }

onMounted(loadAll)
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="app-section-label mb-2">Advanced Growth</p>
            <h1 class="app-page-title">进阶成长路线</h1>
            <p class="app-page-desc">接入进阶成长服务，把长期目标拆成路线图、里程碑、技能训练和导师反馈。</p>
          </div>
          <button type="button" class="app-btn-secondary" :disabled="loading" @click="loadAll">刷新数据</button>
        </header>

        <section class="grid gap-4 md:grid-cols-4">
          <div v-for="item in stats" :key="item.label" class="app-card-surface-flat p-5"><p class="text-sm text-zinc-500">{{ item.label }}</p><p class="mt-2 text-3xl font-black">{{ item.value }}</p></div>
        </section>

        <section v-if="dashboard.suggestions?.length || overdueMilestones.length" class="app-card-surface p-5">
          <h2 class="mb-3 text-lg font-bold">路线建议</h2>
          <div class="grid gap-3 md:grid-cols-2">
            <p v-for="item in dashboard.suggestions || []" :key="item" class="rounded-lg border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-800">{{ item }}</p>
            <p v-for="item in overdueMilestones" :key="item.id" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ item.milestoneName }} 已逾期，截止日 {{ item.dueDate }}</p>
          </div>
        </section>

        <section class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <div class="app-card-surface p-5">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">路线图</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="roadmapForm = emptyRoadmap()">新路线</button></div>
            <div class="grid gap-3 md:grid-cols-2">
              <input v-model="roadmapForm.roadmapName" class="field md:col-span-2" placeholder="路线名称" />
              <input v-model="roadmapForm.stage" class="field" placeholder="阶段" />
              <input v-model="roadmapForm.targetRole" class="field" placeholder="目标角色" />
              <input v-model="roadmapForm.startDate" class="field" type="date" />
              <input v-model="roadmapForm.targetDate" class="field" type="date" />
              <textarea v-model="roadmapForm.coreSkills" class="field min-h-16 md:col-span-2" placeholder="核心能力" />
              <textarea v-model="roadmapForm.successMetrics" class="field min-h-16 md:col-span-2" placeholder="成功指标" />
              <textarea v-model="roadmapForm.riskNotes" class="field min-h-16 md:col-span-2" placeholder="风险和约束" />
            </div>
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveRoadmap">保存路线</button>
          </div>

          <div class="app-card-surface p-5">
            <h2 class="mb-4 text-lg font-bold">路线列表</h2>
            <div class="space-y-3">
              <article v-for="item in roadmaps" :key="item.id" class="list-row">
                <div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><h3 class="font-semibold">{{ item.roadmapName }}</h3><span class="app-pill-tag">{{ statusText[item.status] || item.status }}</span></div><p class="mt-1 text-sm text-zinc-500">{{ item.stage }} · 目标 {{ item.targetRole || '-' }} · {{ item.targetDate || '-' }}</p><div class="mt-3 h-2 rounded-full bg-zinc-100 dark:bg-zinc-800"><div class="h-full rounded-full app-gradient-bar" :style="{ width: `${item.progress || 0}%` }" /></div></div>
                <div class="flex shrink-0 gap-2"><button type="button" class="mini-btn" @click="editRoadmap(item)">编辑</button><button type="button" class="mini-btn danger" @click="deleteRoadmap(item)">删除</button></div>
              </article>
              <p v-if="!roadmaps.length" class="empty-text">还没有进阶路线。</p>
            </div>
          </div>
        </section>

        <section class="grid gap-6 xl:grid-cols-2">
          <div class="app-card-surface p-5">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">里程碑</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="milestoneForm = { ...emptyMilestone(), roadmapId: activeRoadmaps[0]?.id || null }">新里程碑</button></div>
            <div class="grid gap-3 md:grid-cols-2">
              <input v-model="milestoneForm.milestoneName" class="field md:col-span-2" placeholder="里程碑名称" />
              <select v-model="milestoneForm.roadmapId" class="field"><option :value="null">不关联路线</option><option v-for="item in roadmaps" :key="item.id" :value="item.id">{{ item.roadmapName }}</option></select>
              <select v-model="milestoneForm.status" class="field"><option value="TODO">待处理</option><option value="DOING">推进中</option><option value="DONE">已完成</option></select>
              <input v-model="milestoneForm.milestoneType" class="field" placeholder="类型" />
              <input v-model.number="milestoneForm.weight" class="field" min="1" type="number" placeholder="权重" />
              <input v-model="milestoneForm.dueDate" class="field" type="date" />
              <input v-model="milestoneForm.evidenceUrl" class="field" placeholder="证据链接" />
              <textarea v-model="milestoneForm.notes" class="field min-h-16 md:col-span-2" placeholder="备注" />
            </div>
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveMilestone">保存里程碑</button>
            <div class="mt-5 space-y-3"><article v-for="item in milestones" :key="item.id" class="list-row"><div><h3 class="font-semibold">{{ item.milestoneName }}</h3><p class="mt-1 text-sm text-zinc-500">{{ roadmapName(item.roadmapId) }} · {{ statusText[item.status] || item.status }} · {{ item.dueDate || '-' }}</p></div><div class="flex gap-2"><button v-if="item.status !== 'DONE'" type="button" class="mini-btn" @click="doneMilestone(item)">完成</button><button type="button" class="mini-btn" @click="editMilestone(item)">编辑</button></div></article><p v-if="!milestones.length" class="empty-text">还没有里程碑。</p></div>
          </div>

          <div class="app-card-surface p-5">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">技能训练</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="skillForm = { ...emptySkill(), roadmapId: activeRoadmaps[0]?.id || null }">新技能</button></div>
            <div class="grid gap-3 md:grid-cols-2">
              <input v-model="skillForm.skillName" class="field" placeholder="技能名称" />
              <input v-model="skillForm.skillCategory" class="field" placeholder="分类" />
              <select v-model="skillForm.roadmapId" class="field"><option :value="null">不关联路线</option><option v-for="item in roadmaps" :key="item.id" :value="item.id">{{ item.roadmapName }}</option></select>
              <input v-model.number="skillForm.practiceHours" class="field" min="0" type="number" placeholder="练习小时" />
              <input v-model="skillForm.currentLevel" class="field" placeholder="当前水平" />
              <input v-model="skillForm.targetLevel" class="field" placeholder="目标水平" />
              <input v-model.number="skillForm.progress" class="field" min="0" max="100" type="number" placeholder="进度" />
              <input v-model="skillForm.lastPracticedAt" class="field" type="date" />
              <textarea v-model="skillForm.nextPractice" class="field min-h-16 md:col-span-2" placeholder="下一次练习" />
              <textarea v-model="skillForm.evidence" class="field min-h-16 md:col-span-2" placeholder="成果证据" />
            </div>
            <button type="button" class="app-btn-primary mt-4 w-full" @click="saveSkill">保存技能</button>
            <div class="mt-5 grid gap-3 md:grid-cols-2"><article v-for="item in skills" :key="item.id" class="rounded-xl border p-4" :class="isDark ? 'border-white/10 bg-white/5' : 'border-zinc-100 bg-white/60'"><div class="mb-2 flex items-center justify-between gap-3"><h3 class="font-semibold">{{ item.skillName }}</h3><button type="button" class="mini-btn" @click="editSkill(item)">编辑</button></div><p class="text-sm text-zinc-500">{{ item.skillCategory }} · {{ item.currentLevel }} → {{ item.targetLevel }}</p><div class="mt-3 h-2 rounded-full bg-zinc-100 dark:bg-zinc-800"><div class="h-full rounded-full app-gradient-bar" :style="{ width: `${item.progress || 0}%` }" /></div></article><p v-if="!skills.length" class="empty-text md:col-span-2">还没有技能记录。</p></div>
          </div>
        </section>

        <section class="app-card-surface p-5">
          <div class="mb-4 flex items-center justify-between"><h2 class="text-lg font-bold">导师会话</h2><button type="button" class="app-btn-secondary !px-3 !py-2" @click="sessionForm = { ...emptySession(), roadmapId: activeRoadmaps[0]?.id || null }">新会话</button></div>
          <div class="grid gap-3 md:grid-cols-4">
            <input v-model="sessionForm.mentorName" class="field" placeholder="导师/反馈人" />
            <input v-model="sessionForm.sessionType" class="field" placeholder="会话类型" />
            <select v-model="sessionForm.roadmapId" class="field"><option :value="null">不关联路线</option><option v-for="item in roadmaps" :key="item.id" :value="item.id">{{ item.roadmapName }}</option></select>
            <input v-model="sessionForm.sessionDate" class="field" type="date" />
            <input v-model="sessionForm.topic" class="field md:col-span-2" placeholder="主题" />
            <input v-model.number="sessionForm.valueScore" class="field" min="1" max="5" type="number" placeholder="价值评分" />
            <button type="button" class="app-btn-primary" @click="saveSession">保存会话</button>
            <textarea v-model="sessionForm.advice" class="field min-h-16 md:col-span-2" placeholder="建议" />
            <textarea v-model="sessionForm.actionItems" class="field min-h-16 md:col-span-2" placeholder="行动项" />
          </div>
          <div class="mt-5 grid gap-3 lg:grid-cols-3"><article v-for="item in sessions" :key="item.id" class="rounded-xl border p-4" :class="isDark ? 'border-white/10 bg-white/5' : 'border-zinc-100 bg-white/60'"><div class="mb-2 flex items-center justify-between gap-3"><h3 class="font-semibold">{{ item.mentorName }}</h3><button type="button" class="mini-btn" @click="editSession(item)">编辑</button></div><p class="text-sm text-zinc-500">{{ item.sessionDate }} · {{ item.topic }}</p><p class="mt-2 line-clamp-3 text-sm">{{ item.advice || item.actionItems || '暂无纪要' }}</p></article><p v-if="!sessions.length" class="empty-text lg:col-span-3">还没有导师会话。</p></div>
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
