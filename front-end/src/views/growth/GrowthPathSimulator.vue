<script setup>
import { computed, onMounted, ref, watch } from 'vue'

defineProps({
  isDark: Boolean,
})

const STORAGE_KEY = 'cot-growth-path-simulator'

const stages = [
  {
    key: 'high',
    label: '高中阶段',
    shortLabel: '高中',
    description: '从选科、提分到志愿，先把大学入口想清楚。',
    goals: [
      { value: 'tech', label: '计算机与智能方向', subtitle: '数学、编程、工程实践' },
      { value: 'health', label: '医学与生命科学', subtitle: '稳定积累、长期深造' },
      { value: 'humanities', label: '人文与综合方向', subtitle: '表达、研究、公共议题' },
    ],
    paths: [
      {
        id: 'steady',
        name: '稳健升学',
        tagline: '优先保证选择面和录取确定性',
        preference: 'certainty',
        baseScore: 82,
        minHours: 6,
        duration: '18 - 24 个月',
        risk: '低风险',
        riskTone: 'green',
        returnText: '确定性高',
        summary: '先锁定匹配度高的选科与院校梯度，再用稳定的复习节奏换取更多选择。',
        checkpoints: [
          ['本月', '完成目标专业反向筛选', '整理 3 个目标专业，核对选科要求与近三年位次。'],
          ['3 个月内', '建立成绩提升闭环', '用错题和周测结果调整复习权重，每周复盘一次。'],
          ['报考前', '完成冲稳保方案', '至少准备两套分数波动下仍然可执行的志愿方案。'],
        ],
        strengths: ['路径清晰', '容错空间大', '适合稳步提升'],
      },
      {
        id: 'explore',
        name: '专业探索',
        tagline: '先验证兴趣，再决定投入方向',
        preference: 'exploration',
        baseScore: 78,
        minHours: 5,
        duration: '12 - 20 个月',
        risk: '中风险',
        riskTone: 'amber',
        returnText: '匹配度高',
        summary: '通过微项目、学科体验和真实信息访谈，减少只因热门或想象做决定的风险。',
        checkpoints: [
          ['本月', '完成 2 次方向体验', '分别体验目标方向的公开课、题目或小项目，留下真实感受。'],
          ['6 个月内', '形成个人证据', '把成绩、作品和反馈整理成自己的专业适配证据。'],
          ['报考前', '确定主方向与备选', '用兴趣、能力和录取概率三项指标共同决策。'],
        ],
        strengths: ['减少盲选', '更关注适配', '方便后续转向'],
      },
      {
        id: 'sprint',
        name: '目标冲刺',
        tagline: '围绕一个高目标集中配置时间',
        preference: 'growth',
        baseScore: 75,
        minHours: 12,
        duration: '10 - 18 个月',
        risk: '较高风险',
        riskTone: 'red',
        returnText: '上限更高',
        summary: '把有限时间集中到关键学科和目标院校，但必须保留清晰的退路与动态监测。',
        checkpoints: [
          ['本月', '确定单一主目标', '明确目标位次、差距和每周可投入的真实时间。'],
          ['3 个月内', '验证提升速度', '用连续三次考试判断目标是否仍然值得继续冲刺。'],
          ['报考前', '设置止损线', '提前写下转向条件，避免在信息变化后被沉没成本绑住。'],
        ],
        strengths: ['目标集中', '提升上限高', '反馈速度快'],
      },
    ],
  },
  {
    key: 'university',
    label: '大学阶段',
    shortLabel: '大学',
    description: '把课程、项目和毕业选择连接成一条可执行的路线。',
    goals: [
      { value: 'employment', label: '毕业直接就业', subtitle: '实习、作品集、岗位匹配' },
      { value: 'graduate', label: '考研或继续深造', subtitle: '成绩、科研、考试准备' },
      { value: 'create', label: '创业或自由职业', subtitle: '验证需求、产品与收入' },
    ],
    paths: [
      {
        id: 'portfolio',
        name: '作品集优先',
        tagline: '用连续项目证明真实能力',
        preference: 'growth',
        baseScore: 84,
        minHours: 8,
        duration: '2 - 4 个学期',
        risk: '中风险',
        riskTone: 'amber',
        returnText: '就业信号强',
        summary: '围绕目标岗位选择课程和项目，让每个学期都留下可展示、可复盘的成果。',
        checkpoints: [
          ['本学期', '确定能力主线', '从课程树中挑出 2 个核心能力，绑定一个可展示项目。'],
          ['下学期', '获得真实反馈', '参加竞赛、实习或开源协作，补充外部评价。'],
          ['毕业前', '完成成果包装', '把项目、论文和课程成绩整理为简历和作品集版本。'],
        ],
        strengths: ['成果可见', '利于求职', '反馈及时'],
      },
      {
        id: 'research',
        name: '深造积累',
        tagline: '围绕学术目标提高长期竞争力',
        preference: 'certainty',
        baseScore: 80,
        minHours: 10,
        duration: '3 - 6 个学期',
        risk: '中风险',
        riskTone: 'amber',
        returnText: '长期复利高',
        summary: '优先守住 GPA、课程先修关系和科研经历，再逐步收敛到研究方向。',
        checkpoints: [
          ['本学期', '补齐基础课程', '检查毕业缺口与先修关系，避免后期被动补课。'],
          ['1 年内', '进入研究场景', '完成一次导师交流或研究助理经历，验证方向适配度。'],
          ['申请前', '整理学术证据', '统一管理论文、课程、推荐信和考试准备的截止时间。'],
        ],
        strengths: ['基础扎实', '节奏可控', '适合长期积累'],
      },
      {
        id: 'hybrid',
        name: '双轨探索',
        tagline: '同时保留就业和深造的转向空间',
        preference: 'exploration',
        baseScore: 76,
        minHours: 7,
        duration: '2 - 5 个学期',
        risk: '较高风险',
        riskTone: 'red',
        returnText: '选择弹性大',
        summary: '用一条核心能力线同时服务于实习和深造，但要求更强的时间管理和取舍能力。',
        checkpoints: [
          ['本学期', '确定共同能力', '找到既能用于作品集，也能用于科研的核心主题。'],
          ['1 年内', '完成一次双向验证', '分别获得岗位和导师的反馈，比较真实偏好。'],
          ['大三后', '关闭一条支线', '根据证据而不是焦虑，正式选择就业或继续深造。'],
        ],
        strengths: ['保留选择权', '降低误判', '适合尚在探索'],
      },
    ],
  },
  {
    key: 'career',
    label: '职场阶段',
    shortLabel: '职场',
    description: '把工作复盘转化为能力证据，再决定下一次跃迁。',
    goals: [
      { value: 'expert', label: '专业专家路线', subtitle: '深度、影响力、行业积累' },
      { value: 'manager', label: '管理与带团队', subtitle: '协作、决策、组织能力' },
      { value: 'switch', label: '跨行业或跨岗位', subtitle: '迁移能力、作品和网络' },
    ],
    paths: [
      {
        id: 'depth',
        name: '深度增长',
        tagline: '在一个方向持续积累稀缺能力',
        preference: 'certainty',
        baseScore: 83,
        minHours: 5,
        duration: '12 - 24 个月',
        risk: '低风险',
        riskTone: 'green',
        returnText: '稳定复利',
        summary: '围绕岗位核心能力持续交付，并将成果沉淀为可验证的专业影响力。',
        checkpoints: [
          ['本季度', '明确核心指标', '找出岗位中最能体现价值的 1 - 2 个业务指标。'],
          ['半年内', '形成标志性成果', '完成一个能被复用、传播或量化的代表项目。'],
          ['年度复盘', '争取角色升级', '用成果、反馈和能力证据支持晋升或薪酬谈判。'],
        ],
        strengths: ['方向稳定', '成果容易累积', '适合专业型人才'],
      },
      {
        id: 'leadership',
        name: '影响力增长',
        tagline: '从个人交付走向协作和决策',
        preference: 'growth',
        baseScore: 79,
        minHours: 6,
        duration: '12 - 30 个月',
        risk: '中风险',
        riskTone: 'amber',
        returnText: '影响范围大',
        summary: '主动承担跨团队问题，把沟通、判断和带人能力转化为新的职业筹码。',
        checkpoints: [
          ['本季度', '承担协作任务', '选择一个需要推动他人共同完成的目标，而不是只做个人交付。'],
          ['半年内', '建立反馈机制', '固定收集上下游反馈，识别管理能力中的真实短板。'],
          ['年度复盘', '验证管理意愿', '比较专家和管理路线的收益、压力与长期匹配度。'],
        ],
        strengths: ['影响面更广', '利于晋升', '能锻炼综合能力'],
      },
      {
        id: 'transition',
        name: '转轨试验',
        tagline: '先用小成本验证，再决定是否切换',
        preference: 'exploration',
        baseScore: 74,
        minHours: 7,
        duration: '6 - 18 个月',
        risk: '较高风险',
        riskTone: 'red',
        returnText: '上升空间大',
        summary: '利用副项目、内部协作和行业交流验证新方向，避免只凭想象裸辞转行。',
        checkpoints: [
          ['本月', '盘点可迁移能力', '把已有成果拆成方法、工具和业务理解三类证据。'],
          ['3 个月内', '完成低成本试验', '通过副项目、公开作品或内部任务获得新方向反馈。'],
          ['决定切换前', '计算真实代价', '对比收入、学习成本、城市和家庭因素后再做选择。'],
        ],
        strengths: ['降低转行成本', '更重视证据', '适合探索期'],
      },
    ],
  },
]

const priorities = [
  { value: 'certainty', label: '稳妥确定', description: '优先考虑成功概率和可控性' },
  { value: 'growth', label: '成长上限', description: '愿意投入更多时间换取更高上限' },
  { value: 'exploration', label: '探索弹性', description: '保留多个选项，先用行动验证' },
]

const stageKey = ref('high')
const goalKey = ref('tech')
const priorityKey = ref('certainty')
const weeklyHours = ref(8)
const selectedPathId = ref('steady')
const savedPlan = ref(null)
const saveState = ref('')

const currentStage = computed(() => stages.find((item) => item.key === stageKey.value) || stages[0])
const currentGoal = computed(() => currentStage.value.goals.find((item) => item.value === goalKey.value) || currentStage.value.goals[0])
const currentPriority = computed(() => priorities.find((item) => item.value === priorityKey.value) || priorities[0])

const clamp = (value, min, max) => Math.min(max, Math.max(min, value))

const pathOptions = computed(() => currentStage.value.paths.map((path) => {
  const preferenceBoost = path.preference === priorityKey.value ? 8 : 0
  const capacityDelta = weeklyHours.value - path.minHours
  const capacityScore = clamp(capacityDelta, -8, 8)
  const score = clamp(path.baseScore + preferenceBoost + capacityScore, 58, 98)
  return {
    ...path,
    score,
    matchLabel: score >= 88 ? '高度匹配' : score >= 78 ? '较为匹配' : '需要权衡',
  }
}))

const selectedPath = computed(() => pathOptions.value.find((path) => path.id === selectedPathId.value) || pathOptions.value[0])
const savedAtText = computed(() => savedPlan.value?.savedAt ? new Date(savedPlan.value.savedAt).toLocaleString('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }) : '')

const selectStage = (key) => {
  const nextStage = stages.find((item) => item.key === key)
  if (!nextStage) return
  stageKey.value = key
  goalKey.value = nextStage.goals[0].value
  selectedPathId.value = nextStage.paths[0].id
}

const savePlan = () => {
  const payload = {
    stageKey: stageKey.value,
    goalKey: goalKey.value,
    priorityKey: priorityKey.value,
    weeklyHours: weeklyHours.value,
    selectedPathId: selectedPath.value.id,
    savedAt: new Date().toISOString(),
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  savedPlan.value = payload
  saveState.value = '方案已保存到本机'
  window.setTimeout(() => { saveState.value = '' }, 2600)
}

const resetPlan = () => {
  stageKey.value = 'high'
  goalKey.value = 'tech'
  priorityKey.value = 'certainty'
  weeklyHours.value = 8
  selectedPathId.value = 'steady'
  savedPlan.value = null
  localStorage.removeItem(STORAGE_KEY)
  saveState.value = '已恢复默认方案'
  window.setTimeout(() => { saveState.value = '' }, 2600)
}

watch(stageKey, () => {
  if (!currentStage.value.goals.some((goal) => goal.value === goalKey.value)) goalKey.value = currentStage.value.goals[0].value
  if (!currentStage.value.paths.some((path) => path.id === selectedPathId.value)) selectedPathId.value = currentStage.value.paths[0].id
})

onMounted(() => {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
    if (!saved) return
    if (stages.some((stage) => stage.key === saved.stageKey)) stageKey.value = saved.stageKey
    if (currentStage.value.goals.some((goal) => goal.value === saved.goalKey)) goalKey.value = saved.goalKey
    if (priorities.some((priority) => priority.value === saved.priorityKey)) priorityKey.value = saved.priorityKey
    if (Number.isFinite(Number(saved.weeklyHours))) weeklyHours.value = clamp(Number(saved.weeklyHours), 2, 30)
    if (currentStage.value.paths.some((path) => path.id === saved.selectedPathId)) selectedPathId.value = saved.selectedPathId
    savedPlan.value = saved
  } catch (_) {
    localStorage.removeItem(STORAGE_KEY)
  }
})
</script>

<template>
  <div class="space-y-5">
    <section class="app-card-surface simulator-hero p-5 md:p-7">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
        <div class="max-w-2xl">
          <p class="app-section-label mb-2">Decision Lab</p>
          <h2 class="text-2xl font-black md:text-3xl">先看清路径，再决定下一步</h2>
          <p class="mt-2 text-sm leading-6 text-zinc-600 dark:text-zinc-300">选择你所在的阶段、目标和可投入时间，系统会生成三条不同取舍的成长路线。它不是预测结果，而是帮助你比较代价、风险和下一步行动。</p>
        </div>
        <div class="flex shrink-0 items-center gap-2 text-sm text-zinc-500">
          <span class="status-dot" />
          <span>当前方案：{{ selectedPath.name }}</span>
        </div>
      </div>
    </section>

    <section class="grid gap-5 xl:grid-cols-[0.78fr_1.22fr]">
      <div class="app-card-surface p-5 md:p-6">
        <div class="mb-5">
          <p class="text-xs font-bold uppercase tracking-wider text-brand-600">01 / 设定起点</p>
          <h3 class="mt-1 text-lg font-bold">你现在处在哪个阶段？</h3>
        </div>

        <div class="grid grid-cols-3 gap-2">
          <button
            v-for="stage in stages"
            :key="stage.key"
            type="button"
            class="stage-button"
            :class="stageKey === stage.key ? 'stage-button-active' : ''"
            @click="selectStage(stage.key)"
          >
            <span class="text-xs text-zinc-500">{{ stage.shortLabel }}</span>
            <strong>{{ stage.label }}</strong>
          </button>
        </div>
        <p class="mt-3 text-sm leading-6 text-zinc-500">{{ currentStage.description }}</p>

        <label class="mt-6 block text-sm font-semibold" for="path-goal">你的目标</label>
        <select id="path-goal" v-model="goalKey" class="simulator-field mt-2">
          <option v-for="goal in currentStage.goals" :key="goal.value" :value="goal.value">{{ goal.label }} · {{ goal.subtitle }}</option>
        </select>

        <label class="mt-5 block text-sm font-semibold">你的决策偏好</label>
        <div class="mt-2 space-y-2">
          <label v-for="priority in priorities" :key="priority.value" class="priority-option" :class="priorityKey === priority.value ? 'priority-option-active' : ''">
            <input v-model="priorityKey" type="radio" :value="priority.value" class="sr-only" />
            <span>
              <strong class="block text-sm">{{ priority.label }}</strong>
              <span class="mt-0.5 block text-xs text-zinc-500">{{ priority.description }}</span>
            </span>
            <span v-if="priorityKey === priority.value" class="text-brand-600">✓</span>
          </label>
        </div>

        <div class="mt-5 border-t border-zinc-200/70 pt-5 dark:border-white/10">
          <div class="flex items-center justify-between gap-3">
            <label class="text-sm font-semibold" for="weekly-hours">每周可投入时间</label>
            <span class="text-lg font-black text-brand-600">{{ weeklyHours }} 小时</span>
          </div>
          <input id="weekly-hours" v-model.number="weeklyHours" type="range" min="2" max="30" step="1" class="mt-3 w-full accent-brand-600" />
          <div class="mt-1 flex justify-between text-xs text-zinc-400"><span>2 小时</span><span>30 小时</span></div>
        </div>
      </div>

      <div class="app-card-surface p-5 md:p-6">
        <div class="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p class="text-xs font-bold uppercase tracking-wider text-brand-600">02 / 比较分支</p>
            <h3 class="mt-1 text-lg font-bold">为「{{ currentGoal.label }}」选择一条路线</h3>
          </div>
          <span class="app-pill-tag w-fit">偏好：{{ currentPriority.label }}</span>
        </div>

        <div class="grid gap-3 lg:grid-cols-3">
          <button
            v-for="path in pathOptions"
            :key="path.id"
            type="button"
            class="path-option text-left"
            :class="selectedPath.id === path.id ? 'path-option-active' : ''"
            @click="selectedPathId = path.id"
          >
            <div class="flex items-start justify-between gap-2">
              <span class="path-score">{{ path.score }}</span>
              <span class="path-risk" :class="`path-risk-${path.riskTone}`">{{ path.risk }}</span>
            </div>
            <strong class="mt-3 block text-base">{{ path.name }}</strong>
            <span class="mt-1 block text-xs leading-5 text-zinc-500">{{ path.tagline }}</span>
            <div class="mt-4 h-1.5 rounded-full bg-zinc-100 dark:bg-zinc-800">
              <div class="h-full rounded-full bg-brand-500 transition-all duration-300" :style="{ width: `${path.score}%` }" />
            </div>
            <span class="mt-2 block text-xs font-semibold text-brand-600">{{ path.matchLabel }}</span>
          </button>
        </div>

        <div class="mt-5 grid gap-3 sm:grid-cols-3">
          <div class="metric-box"><span>预计周期</span><strong>{{ selectedPath.duration }}</strong></div>
          <div class="metric-box"><span>发展回报</span><strong>{{ selectedPath.returnText }}</strong></div>
          <div class="metric-box"><span>匹配得分</span><strong>{{ selectedPath.score }} / 100</strong></div>
        </div>
        <p class="mt-5 rounded-lg bg-brand-50 px-4 py-3 text-sm leading-6 text-brand-900 dark:bg-brand-500/10 dark:text-brand-100">{{ selectedPath.summary }}</p>
      </div>
    </section>

    <section class="grid gap-5 xl:grid-cols-[1.2fr_0.8fr]">
      <div class="app-card-surface p-5 md:p-6">
        <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p class="text-xs font-bold uppercase tracking-wider text-brand-600">03 / 行动路线</p>
            <h3 class="mt-1 text-lg font-bold">{{ selectedPath.name }} · 三个验证节点</h3>
          </div>
          <span class="text-xs text-zinc-500">目标：{{ currentGoal.label }}</span>
        </div>
        <div class="mt-6 space-y-5">
          <div v-for="(checkpoint, index) in selectedPath.checkpoints" :key="checkpoint[0]" class="checkpoint-row">
            <div class="checkpoint-number">{{ index + 1 }}</div>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
                <span class="text-xs font-bold text-brand-600">{{ checkpoint[0] }}</span>
                <strong class="text-sm">{{ checkpoint[1] }}</strong>
              </div>
              <p class="mt-1 text-sm leading-6 text-zinc-500">{{ checkpoint[2] }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="app-card-surface p-5 md:p-6">
        <p class="text-xs font-bold uppercase tracking-wider text-brand-600">04 / 复盘提示</p>
        <h3 class="mt-1 text-lg font-bold">这条路线适合什么样的你？</h3>
        <ul class="mt-5 space-y-3">
          <li v-for="strength in selectedPath.strengths" :key="strength" class="flex items-center gap-2 text-sm">
            <span class="check-mark">✓</span>
            <span>{{ strength }}</span>
          </li>
        </ul>
        <div class="mt-6 border-t border-zinc-200/70 pt-5 dark:border-white/10">
          <p class="text-xs text-zinc-500">下次复盘时，优先回答：</p>
          <p class="mt-2 text-sm font-semibold leading-6">“我是否获得了新的证据，证明这条路线仍然值得继续？”</p>
        </div>
        <div class="mt-6 flex flex-wrap gap-2">
          <button type="button" class="app-btn-primary" @click="savePlan">保存当前方案</button>
          <button type="button" class="app-btn-secondary" @click="resetPlan">恢复默认</button>
        </div>
        <p v-if="saveState" class="mt-3 text-xs font-semibold text-emerald-600">{{ saveState }}</p>
        <p v-else-if="savedPlan" class="mt-3 text-xs text-zinc-500">上次保存：{{ savedAtText }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.simulator-hero {
  background:
    linear-gradient(135deg, rgba(var(--theme-primary-rgb), 0.13), transparent 52%),
    var(--app-card);
}

.status-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 999px;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12);
}

.stage-button,
.priority-option,
.path-option {
  border: 1px solid var(--app-card-border);
  background: rgba(255, 255, 255, 0.42);
  transition: 160ms ease;
}

html.dark .stage-button,
html.dark .priority-option,
html.dark .path-option {
  background: rgba(255, 255, 255, 0.04);
}

.stage-button {
  display: flex;
  min-height: 4.3rem;
  flex-direction: column;
  justify-content: center;
  gap: 0.25rem;
  border-radius: 0.75rem;
  padding: 0.7rem;
  text-align: left;
}

.stage-button:hover,
.priority-option:hover,
.path-option:hover {
  border-color: rgba(var(--theme-primary-rgb), 0.45);
  transform: translateY(-1px);
}

.stage-button-active,
.priority-option-active,
.path-option-active {
  border-color: rgba(var(--theme-primary-rgb), 0.7);
  background: rgba(var(--theme-primary-rgb), 0.1);
  box-shadow: 0 10px 24px -18px rgba(var(--theme-primary-rgb), 0.9);
}

.stage-button strong {
  font-size: 0.85rem;
}

.priority-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  border-radius: 0.75rem;
  padding: 0.75rem 0.85rem;
  cursor: pointer;
}

.simulator-field {
  width: 100%;
  border: 1px solid var(--app-card-border);
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.6);
  padding: 0.7rem 0.8rem;
  color: var(--app-text);
  font-size: 0.875rem;
  outline: none;
}

html.dark .simulator-field {
  background: rgba(255, 255, 255, 0.06);
}

.simulator-field:focus {
  border-color: rgba(var(--theme-primary-rgb), 0.65);
  box-shadow: 0 0 0 3px rgba(var(--theme-primary-rgb), 0.12);
}

.path-option {
  min-height: 11.5rem;
  border-radius: 0.8rem;
  padding: 1rem;
}

.path-score {
  color: rgb(var(--color-brand-600));
  font-size: 1.7rem;
  font-weight: 900;
  line-height: 1;
}

.path-risk {
  border-radius: 999px;
  padding: 0.25rem 0.5rem;
  font-size: 0.68rem;
  font-weight: 700;
}

.path-risk-green { color: #15803d; background: #dcfce7; }
.path-risk-amber { color: #b45309; background: #fef3c7; }
.path-risk-red { color: #b91c1c; background: #fee2e2; }

html.dark .path-risk-green { color: #86efac; background: rgba(34, 197, 94, 0.15); }
html.dark .path-risk-amber { color: #fcd34d; background: rgba(245, 158, 11, 0.15); }
html.dark .path-risk-red { color: #fca5a5; background: rgba(239, 68, 68, 0.15); }

.metric-box {
  display: flex;
  min-height: 4.5rem;
  flex-direction: column;
  justify-content: center;
  gap: 0.35rem;
  border-radius: 0.75rem;
  background: rgba(var(--theme-primary-rgb), 0.06);
  padding: 0.75rem 0.9rem;
}

.metric-box span {
  color: var(--app-text-muted);
  font-size: 0.72rem;
}

.metric-box strong {
  font-size: 0.92rem;
}

.checkpoint-row {
  display: flex;
  align-items: flex-start;
  gap: 0.85rem;
}

.checkpoint-number,
.check-mark {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 999px;
  color: rgb(var(--color-brand-600));
  background: rgba(var(--theme-primary-rgb), 0.1);
  font-size: 0.8rem;
  font-weight: 800;
}

.check-mark {
  width: 1.35rem;
  height: 1.35rem;
  color: #15803d;
  background: #dcfce7;
  font-size: 0.7rem;
}

html.dark .check-mark {
  color: #86efac;
  background: rgba(34, 197, 94, 0.15);
}
</style>
