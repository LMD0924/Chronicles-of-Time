<script setup>
import messageApi from '@/utils/messageApi'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import Nav from '@/components/Nav.vue'

const { isDark } = useTheme()
const router = useRouter()
const storageKey = 'chronicles-career-toolkit-v1'
const activeTab = ref('plan')
const selectedTemplate = ref('日报')
const templateText = ref('')
const focusMinutes = ref(25)
const remainingSeconds = ref(25 * 60)
const timerRunning = ref(false)
let timer = null

const menuItems = [
  { key: 'work', label: '工作台', icon: '💼', path: '/WorkRecords' },
  { key: 'timeline', label: '成长时间线', icon: '🗓️', path: '/CareerTimeline' },
  { key: 'toolkit', label: '入职工具箱', icon: '🧰', path: '/CareerToolkit' },
  { key: 'interview', label: '模拟面试', icon: '🎙️', path: '/InterviewLab' },
]

const phases = ref([
  { key: 'first', name: '0-30 天 · 站稳脚跟', color: 'blue', items: ['熟悉团队协作方式和业务流程', '完成一次独立的小任务交付', '整理岗位常用系统、文档和联系人'].map((label) => ({ label, done: false })) },
  { key: 'second', name: '31-60 天 · 开始交付', color: 'violet', items: ['承担一个清晰的子项目或模块', '主动同步进度、风险和需要的支持', '沉淀一份可复用的工作方法或文档'].map((label) => ({ label, done: false })) },
  { key: 'third', name: '61-90 天 · 建立影响力', color: 'green', items: ['复盘阶段成果并形成量化证据', '提出一个经过验证的流程优化建议', '与直属负责人确认下一阶段成长目标'].map((label) => ({ label, done: false })) },
])

const templates = [
  { name: '日报', icon: '☀️', description: '快速同步今天的产出、阻塞和明日计划', text: '【今日完成】\n- \n\n【遇到的问题】\n- \n\n【明日计划】\n- ' },
  { name: '周报', icon: '📊', description: '把一周工作从流水账整理成结果和影响', text: '【本周完成】\n- 事项：\n- 结果：\n\n【风险与协作】\n- \n\n【下周计划】\n- 目标：\n- 验收标准：\n\n【需要支持】\n- 无' },
  { name: '会议纪要', icon: '🗒️', description: '记录结论、负责人和截止时间', text: '【会议主题】\n\n【关键结论】\n1. \n2. \n\n【行动项】\n- 负责人：\n  事项：\n  截止时间：\n\n【待确认问题】\n- ' },
  { name: '需求澄清', icon: '🔍', description: '和同事确认目标、边界、验收和优先级', text: '【需求目标】\n希望解决什么问题：\n\n【使用场景】\n谁在什么情况下使用：\n\n【验收标准】\n- \n\n【暂不处理】\n- \n\n【待确认】\n- ' },
  { name: '反馈请求', icon: '💬', description: '更具体地向同事或负责人请求反馈', text: '你好，我想针对【事项/交付物】请你给一些反馈。\n\n我最想了解：\n1. 哪个部分做得比较好？\n2. 哪个地方可以更清晰或更高效？\n3. 下一次你建议我优先改什么？\n\n如果方便，希望能在【时间】前得到建议，谢谢。' },
]

const quickNote = ref({ title: '', content: '' })
const planProgress = computed(() => {
  const all = phases.value.flatMap((phase) => phase.items)
  const done = all.filter((item) => item.done).length
  return all.length ? Math.round(done / all.length * 100) : 0
})
const timerLabel = computed(() => `${String(Math.floor(remainingSeconds.value / 60)).padStart(2, '0')}:${String(remainingSeconds.value % 60).padStart(2, '0')}`)

const save = () => {
  localStorage.setItem(storageKey, JSON.stringify({ phases: phases.value, quickNote: quickNote.value }))
}

const load = () => {
  try {
    const stored = JSON.parse(localStorage.getItem(storageKey) || '{}')
    if (Array.isArray(stored.phases)) {
      phases.value = phases.value.map((phase) => ({ ...phase, items: phase.items.map((currentItem) => {
        const storedPhase = stored.phases.find((item) => item.key === phase.key)
        const storedItem = storedPhase?.items?.find((item) => (typeof item === 'string' ? item : item.label) === currentItem.label)
        return typeof storedItem === 'object' ? storedItem : currentItem
      }) }))
    }
    if (stored.quickNote) quickNote.value = { ...quickNote.value, ...stored.quickNote }
  } catch {
    localStorage.removeItem(storageKey)
  }
  selectTemplate(templates[0])
}

const selectTemplate = (template) => {
  selectedTemplate.value = template.name
  templateText.value = template.text
}

const copyTemplate = async () => {
  try {
    await navigator.clipboard.writeText(templateText.value)
    messageApi.success('模板已复制，可以直接粘贴使用')
  } catch {
    messageApi.info('请手动选中文本复制')
  }
}

const setFocusMinutes = (value) => {
  focusMinutes.value = value
  if (!timerRunning.value) remainingSeconds.value = value * 60
}

const toggleTimer = () => {
  timerRunning.value = !timerRunning.value
  if (timerRunning.value) {
    timer = window.setInterval(() => {
      if (remainingSeconds.value <= 1) {
        stopTimer()
        messageApi.success('专注完成，起来活动一下吧')
      } else {
        remainingSeconds.value -= 1
      }
    }, 1000)
  } else {
    stopTimer()
  }
}

const stopTimer = () => {
  timerRunning.value = false
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

const resetTimer = () => {
  stopTimer()
  remainingSeconds.value = focusMinutes.value * 60
}

watch([phases, quickNote], save, { deep: true })
onMounted(load)
onBeforeUnmount(stopTimer)
</script>

<template>
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :menuItems="menuItems" />
    <main class="app-main">
      <div class="app-container space-y-6">
        <header class="toolkit-hero">
          <div><p class="app-section-label mb-2">Newcomer Toolkit</p><h1 class="app-page-title">入职工具箱</h1><p class="app-page-desc">把试用期规划、沟通模板和专注时间装进一个轻量工作台，减少不知道从哪里开始的时刻。</p></div>
          <button type="button" class="app-btn-secondary" @click="router.push('/WorkRecords')">返回职场概览</button>
        </header>

        <nav class="tool-tabs" aria-label="工具箱分类">
          <button type="button" :class="{ active: activeTab === 'plan' }" @click="activeTab = 'plan'"><span>🧭</span> 90 天计划</button>
          <button type="button" :class="{ active: activeTab === 'templates' }" @click="activeTab = 'templates'"><span>🧩</span> 沟通模板</button>
          <button type="button" :class="{ active: activeTab === 'focus' }" @click="activeTab = 'focus'"><span>⏱️</span> 专注时钟</button>
          <button type="button" :class="{ active: activeTab === 'note' }" @click="activeTab = 'note'"><span>📝</span> 临时记录</button>
        </nav>

        <section v-if="activeTab === 'plan'" class="space-y-5">
          <div class="app-card-surface progress-card"><div><p class="eyebrow">ONBOARDING MOMENTUM</p><h2>你的入职节奏</h2><p>每完成一项，就为自己留下一个可以被看见的证据。</p></div><div class="progress-ring" :style="{ '--progress': `${planProgress * 3.6}deg` }"><strong>{{ planProgress }}<small>%</small></strong></div></div>
          <div class="phase-grid">
            <article v-for="phase in phases" :key="phase.key" class="phase-card" :class="`phase-${phase.color}`"><div class="phase-head"><div><span class="phase-dot"></span><p>{{ phase.name }}</p></div><span>{{ phase.items.filter((item) => item.done).length }}/{{ phase.items.length }}</span></div><div class="phase-line"><i :style="{ width: `${phase.items.length ? phase.items.filter((item) => item.done).length / phase.items.length * 100 : 0}%` }"></i></div><label v-for="item in phase.items" :key="item.label" class="check-row"><input v-model="item.done" type="checkbox"><span>{{ item.label }}</span></label></article>
          </div>
          <div class="app-card-surface p-5"><div class="section-heading"><div><h2>把计划变成证据</h2><p>完成任务后补一句结果，月底复盘时会非常有用。</p></div><span>📌</span></div><textarea v-model="quickNote.content" class="tool-textarea" rows="5" placeholder="例如：独立完成了用户列表接口，接口响应时间从 800ms 降到了 300ms。" /></div>
        </section>

        <section v-else-if="activeTab === 'templates'" class="tool-grid">
          <div class="app-card-surface template-list"><div class="section-heading"><div><h2>常用工作模板</h2><p>选一个场景，直接改成自己的表达。</p></div><span>🧩</span></div><button v-for="template in templates" :key="template.name" type="button" class="template-item" :class="{ active: selectedTemplate === template.name }" @click="selectTemplate(template)"><span>{{ template.icon }}</span><span><strong>{{ template.name }}</strong><small>{{ template.description }}</small></span><b>›</b></button></div>
          <div class="app-card-surface template-editor"><div class="section-heading"><div><p class="eyebrow">{{ selectedTemplate }}</p><h2>编辑模板</h2></div><button type="button" class="app-btn-secondary !px-3 !py-2" @click="copyTemplate">复制文本</button></div><textarea v-model="templateText" class="tool-textarea template-area" /><p class="editor-hint">先写事实，再写影响；不确定的内容用“待确认”标记，沟通会更稳。</p></div>
        </section>

        <section v-else-if="activeTab === 'focus'" class="focus-layout">
          <div class="app-card-surface focus-card"><p class="eyebrow">FOCUS SESSION</p><h2>现在只做一件事</h2><div class="timer-face" :class="{ running: timerRunning }">{{ timerLabel }}</div><div class="timer-presets"><button v-for="minute in [15, 25, 45, 60]" :key="minute" type="button" :class="{ active: focusMinutes === minute }" @click="setFocusMinutes(minute)">{{ minute }} 分钟</button></div><div class="timer-actions"><button type="button" class="app-btn-primary" @click="toggleTimer">{{ timerRunning ? '暂停专注' : '开始专注' }}</button><button type="button" class="app-btn-secondary" @click="resetTimer">重置</button></div></div>
          <div class="app-card-surface focus-guide"><div class="section-heading"><div><h2>专注前写下结果</h2><p>结束后只需要回答：我完成了什么？</p></div><span>🎯</span></div><input v-model="quickNote.title" class="tool-input" placeholder="这次专注要完成什么" /><textarea v-model="quickNote.content" class="tool-textarea" rows="7" placeholder="完成标准、需要的资料、可能的阻塞..." /><div class="focus-rules"><span>✓ 关掉不相关的标签页</span><span>✓ 先做最小可交付版本</span><span>✓ 遇到阻塞先记录问题</span></div></div>
        </section>

        <section v-else class="note-layout">
          <div class="app-card-surface p-5"><div class="section-heading"><div><p class="eyebrow">QUICK CAPTURE</p><h2>临时记录</h2><p>先把想法记下来，稍后再整理到目标或任务里。</p></div><span>📝</span></div><input v-model="quickNote.title" class="tool-input" placeholder="记录标题" /><textarea v-model="quickNote.content" class="tool-textarea note-area" placeholder="今天学到的东西、需要追问的问题、突然想到的改进..." /><div class="note-footer"><span>自动保存在当前浏览器</span><button type="button" class="app-btn-primary" @click="save">保存记录</button></div></div>
          <aside class="app-card-surface p-5 note-side"><h2>记录原则</h2><div><strong>事实</strong><p>发生了什么，避免只写“很忙”“很累”。</p></div><div><strong>影响</strong><p>对用户、团队、质量或进度带来了什么变化。</p></div><div><strong>下一步</strong><p>下一次准备做哪个具体动作。</p></div></aside>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.toolkit-hero { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 18px; }.tool-tabs { display: flex; flex-wrap: wrap; gap: 8px; border-bottom: 1px solid var(--app-border); padding-bottom: 10px; }.tool-tabs button { display: inline-flex; align-items: center; gap: 7px; border: 1px solid var(--app-border); border-radius: 999px; padding: 8px 13px; color: var(--app-text-muted); font-size: 12px; transition: 180ms ease; }.tool-tabs button.active, .tool-tabs button:hover { border-color: rgb(var(--theme-primary-rgb) / .42); background: rgb(var(--theme-primary-rgb) / .1); color: var(--theme-primary); }.tool-tabs span { font-size: 15px; }
.progress-card { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 22px; }.eyebrow { color: var(--theme-primary); font-size: 10px; font-weight: 850; letter-spacing: .14em; }.progress-card h2, .focus-card h2, .section-heading h2, .note-side h2 { margin-top: 5px; color: var(--app-text); font-size: 19px; font-weight: 850; }.progress-card p:not(.eyebrow), .section-heading p, .focus-card > h2 + p { margin-top: 5px; color: var(--app-text-muted); font-size: 12px; }.progress-ring { display: grid; width: 92px; height: 92px; flex-shrink: 0; place-items: center; border-radius: 50%; background: conic-gradient(var(--theme-primary) var(--progress), rgb(var(--theme-primary-rgb) / .1) var(--progress)); }.progress-ring::before { position: absolute; width: 72px; height: 72px; border-radius: 50%; background: var(--app-card-solid); content: ''; }.progress-ring strong { position: relative; z-index: 1; color: var(--app-text); font-size: 23px; }.progress-ring small { font-size: 12px; }
.phase-grid { display: grid; gap: 16px; grid-template-columns: repeat(3, minmax(0, 1fr)); }.phase-card { border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); padding: 18px; }.phase-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }.phase-head > div { display: flex; align-items: center; gap: 8px; }.phase-head p { color: var(--app-text); font-size: 13px; font-weight: 800; }.phase-head > span { color: var(--app-text-muted); font-size: 11px; }.phase-dot { width: 9px; height: 9px; border-radius: 50%; background: var(--theme-primary); }.phase-violet .phase-dot { background: #8b5cf6; }.phase-green .phase-dot { background: #10b981; }.phase-line { height: 4px; margin: 13px 0; overflow: hidden; border-radius: 4px; background: var(--app-border); }.phase-line i { display: block; height: 100%; border-radius: inherit; background: var(--theme-primary); transition: width 350ms ease; }.phase-violet .phase-line i { background: #8b5cf6; }.phase-green .phase-line i { background: #10b981; }.check-row { display: flex; align-items: flex-start; gap: 8px; margin-top: 12px; color: var(--app-text-secondary); font-size: 12px; line-height: 1.5; cursor: pointer; }.check-row input { accent-color: var(--theme-primary); margin-top: 2px; }.check-row input:checked + span { color: var(--app-text-muted); text-decoration: line-through; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }.section-heading > span { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 9px; background: rgb(var(--theme-primary-rgb) / .1); font-size: 16px; }.tool-textarea, .tool-input { width: 100%; margin-top: 15px; border: 1px solid var(--app-border); border-radius: 8px; background: transparent; padding: 11px 12px; color: var(--app-text); font-size: 13px; outline: none; }.tool-textarea { resize: vertical; line-height: 1.7; }.tool-textarea:focus, .tool-input:focus { border-color: rgb(var(--theme-primary-rgb) / .5); box-shadow: 0 0 0 3px rgb(var(--theme-primary-rgb) / .1); }.tool-grid { display: grid; gap: 20px; grid-template-columns: minmax(260px, .8fr) minmax(0, 1.2fr); }.template-list, .template-editor { padding: 20px; }.template-item { display: grid; width: 100%; grid-template-columns: 30px minmax(0, 1fr) 15px; align-items: center; gap: 10px; margin-top: 10px; border: 1px solid transparent; border-radius: 8px; padding: 10px; text-align: left; transition: 180ms ease; }.template-item:hover, .template-item.active { border-color: rgb(var(--theme-primary-rgb) / .3); background: rgb(var(--theme-primary-rgb) / .07); }.template-item > span:first-child { font-size: 18px; }.template-item strong, .template-item small { display: block; }.template-item strong { color: var(--app-text); font-size: 13px; }.template-item small { margin-top: 3px; color: var(--app-text-muted); font-size: 11px; line-height: 1.45; }.template-item b { color: var(--app-text-muted); font-size: 18px; font-weight: 400; }.template-area { min-height: 400px; }.editor-hint { margin-top: 10px; color: var(--app-text-muted); font-size: 11px; }
.focus-layout, .note-layout { display: grid; gap: 20px; grid-template-columns: minmax(280px, .75fr) minmax(0, 1.25fr); }.focus-card { display: grid; justify-items: center; padding: 28px; text-align: center; }.focus-card > h2 { margin-top: 7px; }.timer-face { display: grid; width: 210px; height: 210px; margin: 24px 0; place-items: center; border: 10px solid rgb(var(--theme-primary-rgb) / .12); border-top-color: var(--theme-primary); border-radius: 50%; color: var(--app-text); font-size: 42px; font-weight: 850; transition: 180ms ease; }.timer-face.running { animation: timer-pulse 2s ease-in-out infinite; border-right-color: var(--theme-primary); }.timer-presets { display: flex; flex-wrap: wrap; justify-content: center; gap: 6px; }.timer-presets button { border: 1px solid var(--app-border); border-radius: 999px; padding: 6px 9px; color: var(--app-text-muted); font-size: 11px; }.timer-presets button.active { border-color: var(--theme-primary); color: var(--theme-primary); }.timer-actions { display: flex; gap: 8px; margin-top: 18px; }.focus-guide { padding: 22px; }.focus-rules { display: grid; gap: 9px; margin-top: 16px; color: var(--app-text-muted); font-size: 12px; }.focus-rules span { border-bottom: 1px solid var(--app-border); padding-bottom: 8px; }.note-area { min-height: 360px; }.note-footer { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-top: 14px; color: var(--app-text-muted); font-size: 11px; }.note-side { display: grid; align-content: start; gap: 18px; padding: 22px; }.note-side > div { border-left: 2px solid var(--theme-primary); padding-left: 12px; }.note-side strong { color: var(--app-text); font-size: 13px; }.note-side p { margin-top: 4px; color: var(--app-text-muted); font-size: 12px; line-height: 1.65; }
@keyframes timer-pulse { 50% { transform: scale(1.02); box-shadow: 0 0 0 10px rgb(var(--theme-primary-rgb) / .05); } }
@media (max-width: 900px) { .phase-grid, .tool-grid, .focus-layout, .note-layout { grid-template-columns: 1fr; } }.app-btn-primary:disabled, .app-btn-secondary:disabled { cursor: wait; opacity: .55; }
</style>
