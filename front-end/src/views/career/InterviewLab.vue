<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'
import { getStoredTheme, ThemeType } from '@/utils/theme'

const router = useRouter()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const positionName = ref('')
const industry = ref('互联网 / 软件')
const interviewType = ref('综合面试')
const round = ref(1)
const currentQuestion = ref('')
const answer = ref('')
const history = ref([])
const feedback = ref(null)
const submitting = ref(false)
const aiEnabled = ref(null)

const menuItems = [
  { key: 'work', label: '工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'timeline', label: '成长时间线', icon: '🗓️', path: '/CareerTimeline' },
  { key: 'toolkit', label: '入职工具箱', icon: '🧰', path: '/CareerToolkit' },
  { key: 'interview', label: '模拟面试', icon: '🎙️', path: '/InterviewLab' },
]

const ready = computed(() => Boolean(currentQuestion.value))
const scoreClass = computed(() => !feedback.value ? '' : feedback.value.score >= 80 ? 'good' : feedback.value.score >= 60 ? 'normal' : 'needs-work')

const payload = (answerText = '') => ({
  positionName: positionName.value.trim(),
  industry: industry.value.trim(),
  interviewType: interviewType.value,
  round: round.value,
  previousQuestion: currentQuestion.value,
  answer: answerText,
})

const start = async () => {
  submitting.value = true
  try {
    const res = await request.post('/workplace/ai-interview/turn', payload())
    currentQuestion.value = res.data.question
    aiEnabled.value = res.data.aiEnabled
    feedback.value = null
    answer.value = ''
    history.value = [{ role: 'interviewer', content: res.data.question }]
    round.value = 1
  } finally {
    submitting.value = false
  }
}

const submitAnswer = async () => {
  const content = answer.value.trim()
  if (!content) return ElMessage.warning('请先输入你的回答')
  submitting.value = true
  try {
    history.value.push({ role: 'candidate', content })
    const res = await request.post('/workplace/ai-interview/turn', payload(content))
    feedback.value = res.data
    aiEnabled.value = res.data.aiEnabled
    currentQuestion.value = res.data.question
    history.value.push({ role: 'interviewer', content: res.data.question })
    answer.value = ''
    round.value += 1
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="app-shell app-page-bg min-h-screen" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container interview-layout">
        <section class="interview-intro">
          <div><p class="app-section-label">AI Interview Lab</p><h1>在线模拟面试</h1><p>由虚拟面试官连续提问。每次作答后获得结构、相关性、证据和表达建议。</p></div>
          <button type="button" class="app-btn-secondary" @click="router.push('/WorkRecords')">返回工作台</button>
        </section>

        <section class="interview-stage">
          <aside class="interviewer-panel">
            <div class="avatar-scene"><div class="halo"></div><div class="avatar-person"><i></i><b></b><span></span></div></div>
            <p class="interviewer-role">AI 虚拟面试官</p>
            <strong>{{ aiEnabled === false ? '规则模拟模式' : '职业面试官' }}</strong>
            <small>{{ aiEnabled === false ? '配置 API Key 后将启用 AI 动态追问与评分。' : '将根据你的回答持续追问。' }}</small>
          </aside>

          <section class="conversation-panel">
            <div v-if="!ready" class="setup-form">
              <h2>设置本次面试</h2>
              <label>目标岗位<input v-model="positionName" maxlength="60" placeholder="例如：Java 后端开发工程师"></label>
              <label>行业方向<input v-model="industry" maxlength="60"></label>
              <label>面试类型<select v-model="interviewType"><option>综合面试</option><option>技术面试</option><option>产品面试</option><option>行为面试</option></select></label>
              <button type="button" class="app-btn-primary" :disabled="submitting" @click="start">开始模拟面试</button>
            </div>

            <template v-else>
              <header class="conversation-head"><span>第 {{ round }} 题</span><small>{{ positionName || '通用岗位' }} · {{ interviewType }}</small></header>
              <div class="conversation-list">
                <article v-for="(item, index) in history" :key="index" class="dialogue" :class="item.role">
                  <span>{{ item.role === 'interviewer' ? '面试官' : '我' }}</span><p>{{ item.content }}</p>
                </article>
              </div>
              <div class="answer-box"><textarea v-model="answer" rows="5" :disabled="submitting" placeholder="建议按 STAR 结构作答：背景、任务、行动、结果。"></textarea><button type="button" class="app-btn-primary" :disabled="submitting" @click="submitAnswer">{{ submitting ? '正在分析...' : '提交回答并继续' }}</button></div>
            </template>
          </section>
        </section>

        <section v-if="feedback" class="feedback-panel" :class="scoreClass">
          <div class="score"><span>本题评分</span><strong>{{ feedback.score }}</strong><small>/100</small></div>
          <div class="feedback-copy"><h2>{{ feedback.summary }}</h2><p>下一题重点：{{ feedback.nextFocus }}</p><div class="feedback-columns"><div><h3>做得不错</h3><p v-for="item in feedback.strengths" :key="item">{{ item }}</p></div><div><h3>继续优化</h3><p v-for="item in feedback.improvements" :key="item">{{ item }}</p></div></div></div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.interview-layout { display: grid; gap: 18px; }.interview-intro, .conversation-head, .feedback-panel { display: flex; align-items: center; }.interview-intro { justify-content: space-between; gap: 16px; }.interview-intro h1 { color: var(--app-text); font-size: 28px; font-weight: 800; }.interview-intro p { margin-top: 5px; color: var(--app-text-muted); font-size: 13px; }.interview-stage { display: grid; min-height: 600px; overflow: hidden; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); grid-template-columns: 270px minmax(0, 1fr); }.interviewer-panel { display: grid; align-content: center; justify-items: center; gap: 8px; padding: 26px; background: color-mix(in srgb, var(--app-card-solid) 90%, var(--theme-primary) 10%); text-align: center; }.avatar-scene { position: relative; display: grid; width: 150px; height: 150px; place-items: center; overflow: hidden; border-radius: 50%; background: rgb(var(--theme-primary-rgb) / .16); }.halo { position: absolute; width: 118px; height: 118px; border: 1px solid rgb(var(--theme-primary-rgb) / .35); border-radius: 50%; animation: pulse 2.2s ease-in-out infinite; }.avatar-person { position: relative; width: 80px; height: 100px; }.avatar-person i { position: absolute; top: 4px; left: 22px; width: 37px; height: 37px; border-radius: 50%; background: var(--theme-primary); }.avatar-person b { position: absolute; bottom: 3px; left: 5px; width: 72px; height: 58px; border-radius: 38px 38px 12px 12px; background: var(--theme-secondary); }.avatar-person span { position: absolute; top: 18px; left: 28px; width: 24px; height: 9px; border-bottom: 2px solid white; border-radius: 50%; }.interviewer-role, .interviewer-panel small { color: var(--app-text-muted); font-size: 12px; }.interviewer-panel strong { color: var(--app-text); font-size: 16px; }.conversation-panel { display: grid; min-height: 0; grid-template-rows: auto minmax(0, 1fr) auto; }.setup-form { display: grid; max-width: 540px; gap: 14px; align-self: center; justify-self: center; width: min(100%, 540px); padding: 28px; }.setup-form h2 { color: var(--app-text); font-size: 19px; font-weight: 800; }.setup-form label { display: grid; gap: 6px; color: var(--app-text-secondary); font-size: 13px; }.setup-form input, .setup-form select, .answer-box textarea { width: 100%; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); padding: 10px; color: var(--app-text); outline: none; }.conversation-head { justify-content: space-between; border-bottom: 1px solid var(--app-border); padding: 16px 20px; }.conversation-head span { color: var(--theme-primary); font-size: 14px; font-weight: 800; }.conversation-head small { color: var(--app-text-muted); }.conversation-list { display: grid; align-content: start; gap: 14px; overflow-y: auto; padding: 20px; }.dialogue { display: grid; max-width: 82%; gap: 5px; }.dialogue > span { color: var(--app-text-muted); font-size: 11px; }.dialogue p { border: 1px solid var(--app-border); border-radius: 8px 8px 8px 3px; background: rgb(var(--theme-primary-rgb) / .08); padding: 11px 13px; color: var(--app-text); line-height: 1.65; }.dialogue.candidate { justify-self: end; }.dialogue.candidate > span { text-align: right; }.dialogue.candidate p { border-color: transparent; border-radius: 8px 8px 3px 8px; background: var(--theme-primary); color: white; }.answer-box { display: grid; gap: 9px; border-top: 1px solid var(--app-border); padding: 15px; }.answer-box textarea { resize: vertical; min-height: 100px; }.answer-box button { justify-self: end; }.feedback-panel { gap: 20px; border: 1px solid var(--app-border); border-left: 4px solid var(--theme-primary); border-radius: 8px; background: var(--app-card-solid); padding: 18px; }.score { min-width: 104px; text-align: center; }.score span, .score small { color: var(--app-text-muted); font-size: 11px; }.score strong { margin: 2px; color: var(--theme-primary); font-size: 42px; }.feedback-copy { flex: 1; }.feedback-copy h2 { color: var(--app-text); font-size: 16px; font-weight: 800; }.feedback-copy > p { margin-top: 5px; color: var(--app-text-muted); font-size: 13px; }.feedback-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-top: 13px; }.feedback-columns h3 { color: var(--app-text-secondary); font-size: 12px; font-weight: 800; }.feedback-columns p { margin-top: 4px; color: var(--app-text-muted); font-size: 12px; }.good { border-left-color: #059669; }.good .score strong { color: #059669; }.needs-work { border-left-color: #dc2626; }.needs-work .score strong { color: #dc2626; }@keyframes pulse { 50% { transform: scale(1.1); opacity: .45; } }@media (max-width: 800px) { .interview-stage { grid-template-columns: 1fr; }.interviewer-panel { grid-template-columns: auto 1fr; justify-items: start; text-align: left; }.avatar-scene { width: 70px; height: 70px; grid-row: span 3; }.avatar-person { transform: scale(.55); }.halo { width: 55px; height: 55px; }.feedback-panel { align-items: flex-start; flex-direction: column; }.feedback-columns { grid-template-columns: 1fr; } }@media (max-width: 560px) { .interview-intro { align-items: flex-start; flex-direction: column; }.interview-stage { min-height: 0; }.dialogue { max-width: 94%; } }
</style>