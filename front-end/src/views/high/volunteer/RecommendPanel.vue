<template>
  <div class="volunteer-panel space-y-6">
    <section class="panel-shell panel-shell-hero" :class="isDark ? 'panel-shell-dark' : 'panel-shell-light'">
      <div class="panel-head">
        <div class="panel-brand panel-brand-primary">
          <span>🎯</span>
        </div>
        <div>
          <h2 class="panel-title" :class="isDark ? 'text-white' : 'text-slate-900'">综合智能推荐</h2>
          <p class="panel-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">根据分数、位次和选科，生成更贴近实际录取区间的推荐结果。</p>
        </div>
      </div>

      <div class="control-grid control-grid-4">
        <label class="field">
          <span class="field-label">年份</span>
          <div class="select-wrap">
            <select v-model="recommendParams.year" class="select-input">
              <option v-for="y in [2025, 2024, 2023, 2022]" :key="y" :value="y">{{ y }}年</option>
            </select>
            <svg class="select-arrow" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </label>

        <label class="field">
          <span class="field-label">省份</span>
          <div class="select-wrap">
            <select v-model="recommendParams.province" class="select-input">
              <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
            </select>
            <svg class="select-arrow" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </label>

        <label class="field">
          <span class="field-label">高考分数</span>
          <input v-model.number="recommendParams.score" type="number" min="0" max="750" class="input-base" placeholder="0 - 750" />
        </label>

        <label class="field">
          <span class="field-label">全省排名</span>
          <input v-model.number="recommendParams.rank" type="number" min="0" class="input-base" placeholder="可选" />
        </label>
      </div>

      <div class="field mt-6">
        <span class="field-label">选科</span>
        <div class="chip-group">
          <button
            v-for="subject in subjects"
            :key="subject"
            type="button"
            @click="toggleSubject(subject)"
            :class="recommendParams.subjects.includes(subject) ? 'chip chip-active' : 'chip chip-inactive'"
          >
            {{ subject }}
          </button>
        </div>
      </div>

      <button type="button" class="action-btn action-btn-primary mt-6" @click="getRecommendations" :disabled="loading">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M10 3l1.9 4.4L16 9.1l-4.1 1.4L10 15l-1.9-4.5L4 9.1l4.1-1.7L10 3Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
        </svg>
        <span>{{ loading ? '分析中...' : '开始智能推荐' }}</span>
      </button>
    </section>

    <section class="panel-shell" :class="isDark ? 'panel-shell-dark' : 'panel-shell-light'">
      <div class="panel-head">
        <div class="panel-brand panel-brand-secondary">
          <span>📘</span>
        </div>
        <div>
          <h2 class="panel-title" :class="isDark ? 'text-white' : 'text-slate-900'">按专业推荐</h2>
          <p class="panel-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">输入专业代码，快速找出更合适的院校组合。</p>
        </div>
      </div>

      <div class="control-grid control-grid-3">
        <label class="field">
          <span class="field-label">专业代码</span>
          <input v-model="recommendByMajorParams.majorCode" type="text" class="input-base" placeholder="如 080901" />
        </label>

        <label class="field">
          <span class="field-label">高考分数</span>
          <input v-model.number="recommendByMajorParams.score" type="number" min="0" max="750" class="input-base" placeholder="0 - 750" />
        </label>

        <label class="field">
          <span class="field-label">省份</span>
          <div class="select-wrap">
            <select v-model="recommendByMajorParams.province" class="select-input">
              <option value="北京">北京</option>
              <option value="上海">上海</option>
              <option value="浙江">浙江</option>
              <option value="广东">广东</option>
            </select>
            <svg class="select-arrow" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </label>
      </div>

      <button type="button" class="action-btn action-btn-secondary mt-6" @click="getRecommendByMajor">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M4 4h12v12H4z" stroke="currentColor" stroke-width="1.6" />
          <path d="M7 10h6M10 7v6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
        </svg>
        <span>开始按专业推荐</span>
      </button>

      <div v-if="recommendByMajorResults.length > 0" class="results-block mt-6">
        <div class="results-meta" :class="isDark ? 'text-slate-400' : 'text-slate-500'">共找到 {{ recommendByMajorResults.length }} 所院校</div>
        <div class="stack-list">
          <article v-for="item in recommendByMajorResults" :key="item.universityId" class="result-card" :class="isDark ? 'result-card-dark' : 'result-card-light'">
            <div class="result-card-main">
              <div>
                <h4 class="result-title" :class="isDark ? 'text-white' : 'text-slate-900'">{{ item.universityName }}</h4>
                <p class="result-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">{{ item.majorName }} · 录取线 {{ item.minScore }} 分</p>
              </div>
              <div class="result-aside">
                <span class="status-pill" :class="strategyClass(item.strategy)">{{ item.strategy }}</span>
                <div class="result-percent" :class="getProbabilityColor(item.probability)">{{ (item.probability * 100).toFixed(0) }}%</div>
                <button type="button" class="text-action" @click="getChance(item.universityId, item.majorId)">机会分析</button>
              </div>
            </div>
          </article>
        </div>
      </div>

      <AiInsightPanel
        v-if="recommendByMajorResults.length > 0"
        class="mt-6"
        scenario="volunteer"
        title="AI 按专业志愿分析"
        description="围绕目标专业，分析候选院校的梯度、风险和保底配置。"
        button-text="分析专业志愿"
        :is-dark="isDark"
        :payload="majorAiPayload"
      />
    </section>

    <section v-if="recommendations.length > 0" class="space-y-6">
      <AiInsightPanel
        scenario="volunteer"
        title="AI 志愿推荐分析"
        description="根据分数、位次、选科和推荐结果，检查冲稳保结构并给出调整建议。"
        button-text="分析推荐结果"
        :is-dark="isDark"
        :payload="volunteerAiPayload"
      />

      <div v-for="section in visibleSections" :key="section.key" class="result-section" :class="section.shellClass">
        <div class="section-head">
          <h3 class="section-title" :class="section.titleClass">
            <span>{{ section.icon }}</span>
            {{ section.label }}
          </h3>
          <span class="section-count" :class="isDark ? 'text-slate-400' : 'text-slate-500'">{{ groupedRecommendations[section.key].length }} 条</span>
        </div>

        <div class="grid-cards">
          <article v-for="item in groupedRecommendations[section.key]" :key="item.universityId + '-' + item.majorId" class="mini-card" :class="isDark ? 'mini-card-dark' : 'mini-card-light'">
            <div>
              <h4 class="result-title" :class="isDark ? 'text-white' : 'text-slate-900'">{{ item.universityName }}</h4>
              <p class="result-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">{{ item.majorName }}</p>
            </div>
            <div class="mini-card-foot">
              <div>
                <div class="mini-meta" :class="isDark ? 'text-slate-400' : 'text-slate-500'">录取线 {{ item.minScore }} 分</div>
                <div class="mini-meta" :class="isDark ? 'text-slate-400' : 'text-slate-500'">分差 {{ item.scoreDiff }} 分</div>
              </div>
              <div class="text-right">
                <div class="result-percent" :class="section.percentClass">{{ (item.probability * 100).toFixed(0) }}%</div>
                <button type="button" class="text-action mt-2" @click="addToPlan(item)">加入志愿</button>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <transition name="modal-fade">
      <div v-if="chanceVisible" class="modal-overlay" @click.self="closeChanceModal">
        <div class="modal-shell modal-shell-md" :class="isDark ? 'modal-shell-dark' : 'modal-shell-light'">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">机会分析</p>
              <h3 class="modal-title">{{ chanceInfo?.universityName || '录取机会' }}</h3>
            </div>
            <button type="button" class="modal-close" aria-label="关闭" @click="closeChanceModal">×</button>
          </div>
          <div class="modal-body">
            <div class="chance-grid">
              <div class="chance-stat">
                <span class="chance-label">录取概率</span>
                <strong class="chance-value text-sky-500">{{ chanceDisplayProbability }}</strong>
              </div>
              <div class="chance-stat">
                <span class="chance-label">等级</span>
                <strong class="chance-value">{{ chanceInfo?.level || '未知' }}</strong>
              </div>
              <div class="chance-stat">
                <span class="chance-label">你的分数</span>
                <strong class="chance-value">{{ chanceInfo?.yourScore ?? '--' }}</strong>
              </div>
              <div class="chance-stat">
                <span class="chance-label">目标分数</span>
                <strong class="chance-value">{{ chanceInfo?.targetScore ?? '--' }}</strong>
              </div>
            </div>
            <div class="chance-note">分差 {{ chanceInfo?.scoreDiff ?? '--' }} 分</div>
            <p class="chance-description">{{ chanceInfo?.suggestion || '暂无建议' }}</p>
          </div>
          <div class="dialog-footer">
            <button type="button" class="dialog-btn confirm" @click="closeChanceModal">知道了</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import messageApi from '@/utils/messageApi'
import { ref, computed, inject, onMounted } from 'vue'
import request from '@/utils/request'
import AiInsightPanel from '@/views/high/components/AiInsightPanel.vue'

const isDark = inject('isDark', ref(false))

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  }
})

const subjects = ['物理', '化学', '生物', '历史', '政治', '地理']
const provinces = ref([])

const loading = ref(false)
const recommendations = ref([])
const recommendParams = ref({ userId: props.userId, year: 2025, province: '浙江', score: null, rank: null, subjects: [] })
const recommendByMajorParams = ref({ userId: props.userId, majorCode: '', score: null, province: '浙江' })
const recommendByMajorResults = ref([])
const chanceVisible = ref(false)
const chanceInfo = ref(null)

const strategySections = [
  {
    key: '冲刺',
    label: '冲刺志愿',
    icon: '🚀',
    shellClass: 'section-shell section-shell-amber',
    titleClass: 'text-amber-500',
    percentClass: 'text-amber-500'
  },
  {
    key: '稳妥',
    label: '稳妥志愿',
    icon: '🟢',
    shellClass: 'section-shell section-shell-emerald',
    titleClass: 'text-emerald-500',
    percentClass: 'text-emerald-500'
  },
  {
    key: '保底',
    label: '保底志愿',
    icon: '🛟',
    shellClass: 'section-shell section-shell-sky',
    titleClass: 'text-sky-500',
    percentClass: 'text-sky-500'
  },
  {
    key: '梦想',
    label: '梦想志愿',
    icon: '✨',
    shellClass: 'section-shell section-shell-violet',
    titleClass: 'text-violet-500',
    percentClass: 'text-violet-500'
  }
]

const volunteerAiPayload = computed(() => ({
  userId: props.userId,
  profile: {
    ...recommendParams.value,
    recommendationCount: recommendations.value.length,
    strategyCount: {
      冲刺: groupedRecommendations.value['冲刺'].length,
      稳妥: groupedRecommendations.value['稳妥'].length,
      保底: groupedRecommendations.value['保底'].length,
      梦想: groupedRecommendations.value['梦想'].length
    }
  },
  candidates: recommendations.value,
  question: '请分析推荐结果的冲稳保结构、录取风险、选科匹配和志愿调整建议。'
}))

const majorAiPayload = computed(() => ({
  userId: props.userId,
  profile: {
    ...recommendByMajorParams.value,
    resultCount: recommendByMajorResults.value.length
  },
  candidates: recommendByMajorResults.value,
  question: '请分析按专业推荐结果中哪些院校更适合作为冲刺、稳妥和保底选择。'
}))

const groupedRecommendations = computed(() => ({
  冲刺: recommendations.value.filter(r => r.strategy === '冲刺'),
  稳妥: recommendations.value.filter(r => r.strategy === '稳妥'),
  保底: recommendations.value.filter(r => r.strategy === '保底'),
  梦想: recommendations.value.filter(r => r.strategy === '梦想')
}))

const visibleSections = computed(() => strategySections.filter(section => groupedRecommendations.value[section.key].length > 0))

const chanceDisplayProbability = computed(() => {
  if (!chanceInfo.value || chanceInfo.value.probability == null) return '--'
  return `${(chanceInfo.value.probability * 100).toFixed(0)}%`
})

const getProbabilityColor = (prob) => {
  if (prob >= 0.7) return 'text-emerald-500'
  if (prob >= 0.4) return 'text-amber-500'
  return 'text-rose-500'
}

const strategyClass = (strategy) => {
  if (strategy === '冲刺') return 'status-pill-amber'
  if (strategy === '稳妥') return 'status-pill-emerald'
  if (strategy === '保底') return 'status-pill-sky'
  return 'status-pill-violet'
}

const toggleSubject = (subject) => {
  const index = recommendParams.value.subjects.indexOf(subject)
  if (index > -1) {
    recommendParams.value.subjects.splice(index, 1)
  } else {
    recommendParams.value.subjects.push(subject)
  }
}

const getRecommendations = async () => {
  if (!recommendParams.value.score) {
    messageApi.warning('请先填写高考分数')
    return
  }
  loading.value = true
  try {
    const queryParams = {
      userId: recommendParams.value.userId,
      year: recommendParams.value.year,
      province: recommendParams.value.province,
      score: recommendParams.value.score,
      rank: recommendParams.value.rank || 0
    }
    const res = await request.post('/volunteer/recommend/universities', recommendParams.value.subjects, queryParams)
    if (res.code === 200) {
      recommendations.value = res.data || []
      if (recommendations.value.length === 0) messageApi.warning('未找到匹配的推荐结果')
    }
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    loading.value = false
  }
}

const getRecommendByMajor = async () => {
  if (!recommendByMajorParams.value.majorCode || !recommendByMajorParams.value.score) {
    messageApi.warning('请先填写专业代码和分数')
    return
  }
  try {
    const res = await request.get('/volunteer/recommend/byMajor', recommendByMajorParams.value)
    if (res.code === 200) {
      recommendByMajorResults.value = res.data || []
      if (recommendByMajorResults.value.length === 0) messageApi.warning('未找到相关专业推荐')
    }
  } catch (error) {
    console.error('按专业推荐失败', error)
  }
}

const openChanceModal = (chance, universityName = '', majorName = '') => {
  chanceInfo.value = {
    ...chance,
    universityName,
    majorName
  }
  chanceVisible.value = true
}

const closeChanceModal = () => {
  chanceVisible.value = false
  chanceInfo.value = null
}

const getChance = async (universityId, majorId) => {
  try {
    const res = await request.get(`/volunteer/chance/${props.userId}`, { universityId, majorId })
    if (res.code === 200) {
      openChanceModal(res.data || {})
    }
  } catch (error) {
    console.error('获取机会分析失败', error)
  }
}

const addToPlan = async (item) => {
  messageApi.info(`已将 ${item.universityName} - ${item.majorName} 加入志愿，请在“志愿方案”页面完善`)
}

const loadFilters = async () => {
  try {
    const res = await request.get('/volunteer/filter/provinces')
    if (res.code === 200) {
      provinces.value = res.data || []
    }
  } catch (error) {
    console.error('加载筛选条件失败', error)
  }
}

onMounted(() => {
  loadFilters()
})

defineExpose({
  getRecommendations
})
</script>

<style scoped>
.volunteer-panel {
  color: inherit;
}

.panel-shell {
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(22px);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}

.panel-shell-light {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.panel-shell-dark {
  background: rgba(15, 23, 42, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.panel-shell-hero {
  border-top: 3px solid rgba(15, 118, 110, 0.35);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 22px;
}

.panel-brand {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: 0 0 auto;
}

.panel-brand-primary {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.18), rgba(37, 99, 235, 0.18));
}

.panel-brand-secondary {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.18), rgba(20, 184, 166, 0.18));
}

.panel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.panel-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.6;
}

.control-grid {
  display: grid;
  gap: 16px;
}

.control-grid-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.control-grid-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.field {
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.select-wrap {
  position: relative;
}

.select-input,
.input-base {
  width: 100%;
  height: 44px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: #0f172a;
  padding: 0 14px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.select-input {
  appearance: none;
  padding-right: 42px;
}

.select-input:focus,
.input-base:focus {
  border-color: rgba(15, 118, 110, 0.7);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  width: 18px;
  height: 18px;
  transform: translateY(-50%);
  color: #64748b;
  pointer-events: none;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip-active {
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.18);
}

.chip-inactive {
  color: #475569;
  background: rgba(148, 163, 184, 0.12);
}

.chip-inactive:hover {
  background: rgba(148, 163, 184, 0.18);
}

.action-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.action-btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  box-shadow: 0 16px 30px rgba(37, 99, 235, 0.22);
}

.action-btn-secondary {
  color: #fff;
  background: linear-gradient(135deg, #14b8a6, #0ea5e9);
  box-shadow: 0 16px 30px rgba(14, 165, 233, 0.18);
}

.results-block,
.result-section {
  border-radius: 20px;
}

.results-meta,
.section-count,
.result-subtitle,
.mini-meta,
.chance-label,
.chance-description {
  letter-spacing: 0;
}

.stack-list,
.grid-cards {
  display: grid;
  gap: 14px;
}

.result-card,
.mini-card {
  border-radius: 18px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.result-card-light,
.mini-card-light {
  background: rgba(248, 250, 252, 0.9);
}

.result-card-dark,
.mini-card-dark {
  background: rgba(30, 41, 59, 0.72);
}

.result-card-main,
.mini-card-foot {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.result-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
}

.result-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
}

.result-aside {
  display: grid;
  justify-items: end;
  gap: 6px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.status-pill-amber { background: rgba(245, 158, 11, 0.12); color: #d97706; }
.status-pill-emerald { background: rgba(16, 185, 129, 0.12); color: #059669; }
.status-pill-sky { background: rgba(14, 165, 233, 0.12); color: #0284c7; }
.status-pill-violet { background: rgba(124, 58, 237, 0.12); color: #7c3aed; }

.result-percent {
  font-size: 26px;
  line-height: 1;
  font-weight: 900;
}

.text-action {
  border: none;
  background: transparent;
  color: #0f766e;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
}

.text-action:hover {
  color: #2563eb;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 800;
}

.section-shell {
  border: 1px solid rgba(148, 163, 184, 0.18);
  padding: 18px;
  border-radius: 18px;
}

.section-shell-amber { background: rgba(245, 158, 11, 0.06); }
.section-shell-emerald { background: rgba(16, 185, 129, 0.06); }
.section-shell-sky { background: rgba(14, 165, 233, 0.06); }
.section-shell-violet { background: rgba(124, 58, 237, 0.06); }

.chance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.chance-stat {
  border-radius: 16px;
  padding: 14px;
  background: rgba(148, 163, 184, 0.08);
}

.chance-value {
  display: block;
  margin-top: 6px;
  font-size: 18px;
  font-weight: 900;
}

.chance-note {
  margin-top: 14px;
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(20, 184, 166, 0.08);
  color: #0f766e;
  font-weight: 700;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(18px);
}

.modal-shell {
  width: min(100%, 620px);
  max-height: min(86vh, 760px);
  overflow: hidden;
  border-radius: 22px;
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.28);
}

.modal-shell-light {
  background: rgba(255, 255, 255, 0.96);
  color: #111827;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.modal-shell-dark {
  background: rgba(15, 23, 42, 0.94);
  color: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.24);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.modal-body {
  padding: 22px 28px 10px;
}

.modal-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 800;
  color: #0f766e;
}

.modal-title {
  margin: 0;
  font-size: 22px;
  font-weight: 900;
}

.modal-close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.14);
  color: inherit;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 18px 28px 26px;
}

.dialog-btn {
  min-width: 92px;
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  border: none;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.dialog-btn.confirm {
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #2563eb);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.18s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

@media (max-width: 960px) {
  .control-grid-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .panel-shell,
  .modal-body,
  .modal-header,
  .dialog-footer {
    padding-left: 18px;
    padding-right: 18px;
  }

  .control-grid-4,
  .control-grid-3,
  .chance-grid {
    grid-template-columns: 1fr;
  }

  .result-card-main,
  .mini-card-foot {
    flex-direction: column;
  }

  .result-aside {
    justify-items: start;
  }

  .dialog-footer {
    flex-direction: column-reverse;
  }

  .dialog-btn {
    width: 100%;
  }
}
</style>
