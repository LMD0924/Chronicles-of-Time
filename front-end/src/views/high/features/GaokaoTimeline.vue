<script setup>
import { computed, onMounted, ref, watch } from 'vue'

const STORAGE_KEY = 'hs_gaokao_timeline'
const currentYear = new Date().getFullYear()
const defaultExamDate = new Date() > new Date(`${currentYear}-06-07`) ? `${currentYear + 1}-06-07` : `${currentYear}-06-07`
const examDate = ref(defaultExamDate)
const milestones = ref([
  { id: 'registration', title: '高考报名', note: '确认报名信息、学籍和身份证材料', due: '', done: false },
  { id: 'selection', title: '选科确认', note: '核对组合、目标专业限制和调整窗口', due: '', done: false },
  { id: 'health', title: '高考体检', note: '关注专业受限项目并保留体检结论', due: '', done: false },
  { id: 'strong-base', title: '强基 / 综评材料', note: '准备成绩单、获奖证明、自述和学校要求材料', due: '', done: false },
  { id: 'mock', title: '一模二模复盘', note: '完成分科复盘，校准志愿目标分和位次', due: '', done: false },
  { id: 'volunteer', title: '志愿填报', note: '按本省考试院通知完成冲稳保方案确认', due: '', done: false },
])

const daysLeft = computed(() => Math.max(0, Math.ceil((new Date(examDate.value) - new Date()) / 86400000)))
const completedCount = computed(() => milestones.value.filter(item => item.done).length)
const progress = computed(() => milestones.value.length ? Math.round(completedCount.value / milestones.value.length * 100) : 0)

const save = () => localStorage.setItem(STORAGE_KEY, JSON.stringify({ examDate: examDate.value, milestones: milestones.value }))
const load = () => {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
    if (saved.examDate) examDate.value = saved.examDate
    if (Array.isArray(saved.milestones)) milestones.value = milestones.value.map(item => ({ ...item, ...(saved.milestones.find(savedItem => savedItem.id === item.id) || {}) }))
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
}

watch([examDate, milestones], save, { deep: true })
onMounted(load)
</script>

<template>
  <section class="timeline space-y-4">
    <header class="timeline-head app-card-surface p-5">
      <div><p class="app-section-label">Milestones</p><h2>高考时间线</h2><p>具体报名和填报日期以本省教育考试院通知为准。</p></div>
      <div class="countdown"><span>距离高考</span><strong>{{ daysLeft }}</strong><small>天</small><input v-model="examDate" type="date" aria-label="高考日期"></div>
    </header>

    <div class="timeline-progress"><span>已完成 {{ completedCount }}/{{ milestones.length }}</span><div><i :style="{ width: `${progress}%` }"></i></div><b>{{ progress }}%</b></div>

    <div class="milestone-list">
      <article v-for="(item, index) in milestones" :key="item.id" class="milestone" :class="{ done: item.done }">
        <div class="step"><span>{{ index + 1 }}</span></div>
        <label class="milestone-check"><input v-model="item.done" type="checkbox"><i></i></label>
        <div class="milestone-copy"><h3>{{ item.title }}</h3><p>{{ item.note }}</p></div>
        <label class="due-field"><span>提醒日期</span><input v-model="item.due" type="date"></label>
      </article>
    </div>
  </section>
</template>

<style scoped>
.timeline-head, .countdown, .timeline-progress, .milestone { display: flex; align-items: center; }.timeline-head { justify-content: space-between; gap: 18px; }.timeline-head h2 { margin-top: 3px; color: var(--app-text); font-size: 21px; font-weight: 800; }.timeline-head p { margin-top: 5px; color: var(--app-text-muted); font-size: 13px; }.countdown { gap: 6px; border-left: 1px solid var(--app-border); padding-left: 20px; }.countdown span, .countdown small { color: var(--app-text-muted); font-size: 12px; }.countdown strong { color: var(--theme-primary); font-size: 30px; }.countdown input { margin-left: 8px; border: 1px solid var(--app-border); border-radius: 6px; background: var(--app-card-solid); padding: 6px; color: var(--app-text); font-size: 12px; }.timeline-progress { gap: 10px; color: var(--app-text-muted); font-size: 12px; }.timeline-progress > div { height: 8px; flex: 1; overflow: hidden; border-radius: 99px; background: rgb(var(--theme-primary-rgb) / .1); }.timeline-progress i { display: block; height: 100%; border-radius: inherit; background: var(--theme-primary); }.timeline-progress b { color: var(--theme-primary); }.milestone-list { display: grid; gap: 10px; }.milestone { gap: 13px; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-card-solid); padding: 14px; }.step span { display: grid; width: 30px; height: 30px; place-items: center; border-radius: 50%; background: rgb(var(--theme-primary-rgb) / .12); color: var(--theme-primary); font-size: 12px; font-weight: 800; }.milestone-check input { position: absolute; opacity: 0; }.milestone-check i { display: block; width: 17px; height: 17px; border: 1px solid var(--app-border); border-radius: 5px; }.milestone-check input:checked + i { border-color: var(--theme-primary); background: var(--theme-primary); box-shadow: inset 0 0 0 3px var(--app-card-solid); }.milestone-copy { min-width: 0; flex: 1; }.milestone-copy h3 { color: var(--app-text); font-size: 14px; font-weight: 800; }.milestone-copy p { margin-top: 3px; color: var(--app-text-muted); font-size: 12px; }.due-field { display: grid; gap: 4px; color: var(--app-text-muted); font-size: 10px; }.due-field input { border: 1px solid var(--app-border); border-radius: 6px; background: var(--app-card-solid); padding: 6px; color: var(--app-text); font-size: 12px; }.milestone.done { opacity: .72; }.milestone.done h3 { text-decoration: line-through; } @media (max-width: 720px) { .timeline-head { align-items: flex-start; flex-direction: column; }.countdown { border-left: 0; border-top: 1px solid var(--app-border); padding: 12px 0 0; }.milestone { align-items: flex-start; flex-wrap: wrap; }.due-field { width: 100%; margin-left: 60px; }.due-field input { width: 100%; } }
</style>