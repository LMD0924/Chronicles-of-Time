<!--
  文件说明：在线练习、正式考试和错题练习页面。
-->
<template>
  <div class="space-y-6">
    <section class="app-card-surface p-6 border border-white/20 dark:border-gray-700/30">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">在线练习</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">按分类、知识点和难度随机组卷，考试完成后自动生成历史记录和错题本。</p>
        </div>
        <div class="flex items-center gap-3">
          <span v-if="activeSession.sessionId" class="px-3 py-1 rounded-lg bg-gray-100 dark:bg-gray-800 text-sm text-gray-600 dark:text-gray-300">
            用时 {{ formatTime(elapsedSeconds) }}
          </span>
          <span v-if="activeSession.antiCheatEnabled" class="px-3 py-1 rounded-lg bg-red-50 text-red-600 text-sm">
            防作弊 {{ suspiciousCount }} 次
          </span>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 xl:grid-cols-[360px_minmax(0,1fr)] gap-6">
      <aside class="space-y-6">
        <section class="bg-white dark:bg-dark-surface rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50">
          <h3 class="font-semibold text-gray-900 dark:text-gray-100 mb-4">组卷条件</h3>
          <div class="space-y-4">
            <div>
              <label class="form-label">模式</label>
              <div class="grid grid-cols-3 gap-2">
                <button v-for="item in modes" :key="item.value" @click="form.mode = item.value" class="seg-btn" :class="form.mode === item.value ? 'seg-btn-active' : ''">
                  {{ item.label }}
                </button>
              </div>
            </div>
            <div>
              <label class="form-label">第一层分类</label>
              <select v-model="form.categoryLevel" class="form-control">
                <option value="">全部分类</option>
                <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">科目 / 专业方向</label>
              <input v-model="form.subjectName" class="form-control" placeholder="如 数学、公务员行测、Java 面试">
            </div>
            <div>
              <label class="form-label">知识点</label>
              <div class="flex flex-wrap gap-2 mb-2">
                <button
                  v-for="point in filterOptions.knowledgePoints"
                  :key="point"
                  @click="toggleKnowledge(point)"
                  class="tag-btn"
                  :class="form.knowledgePoints.includes(point) ? 'tag-btn-active' : ''"
                >
                  {{ point }}
                </button>
              </div>
              <input v-model="manualKnowledge" class="form-control" placeholder="自定义知识点，多个用逗号分隔">
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="form-label">题型</label>
                <select v-model="form.questionType" class="form-control">
                  <option value="">全部题型</option>
                  <option v-for="item in questionTypes" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>
              <div>
                <label class="form-label">难度</label>
                <select v-model="form.difficultyLevel" class="form-control">
                  <option value="">全部难度</option>
                  <option value="简单">简单</option>
                  <option value="中等">中等</option>
                  <option value="困难">困难</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="form-label">题量</label>
                <input v-model.number="form.questionCount" type="number" min="1" max="100" class="form-control">
              </div>
              <div>
                <label class="form-label">限时(分钟)</label>
                <input v-model.number="durationMinutes" type="number" min="0" max="300" class="form-control">
              </div>
            </div>
            <button @click="startExam" :disabled="loading || !userId" class="w-full py-3 rounded-xl bg-brand-500 text-white font-medium disabled:opacity-50">
              {{ loading ? '组卷中...' : form.mode === 'mistake' ? '开始错题练习' : '开始考试' }}
            </button>
          </div>
        </section>

        <section class="bg-white dark:bg-dark-surface rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
          <div class="px-5 py-4 border-b border-gray-200/50 dark:border-gray-700/50 flex items-center justify-between">
            <h3 class="font-semibold text-gray-900 dark:text-gray-100">历史考试</h3>
            <button @click="fetchHistory" class="text-sm text-brand-600">刷新</button>
          </div>
          <div class="max-h-[460px] overflow-y-auto divide-y divide-gray-100 dark:divide-gray-800">
            <button
              v-for="item in historyList"
              :key="item.sessionId"
              @click="loadHistoryDetail(item.sessionId)"
              class="w-full text-left px-5 py-4 hover:bg-gray-50 dark:hover:bg-gray-800/50"
            >
              <div class="flex items-center justify-between gap-3">
                <span class="font-medium text-gray-800 dark:text-gray-200">{{ item.title }}</span>
                <span class="text-sm" :class="item.scorePercent >= 60 ? 'text-emerald-600' : 'text-red-600'">{{ item.scorePercent }}分</span>
              </div>
              <p class="text-xs text-gray-500 mt-1">
                {{ modeText(item.mode) }} · {{ item.totalQuestions }}题 · {{ formatDateTime(item.finishedAt) }}
              </p>
            </button>
            <div v-if="historyList.length === 0" class="px-5 py-8 text-center text-sm text-gray-500">暂无考试历史</div>
          </div>
        </section>
      </aside>

      <main class="space-y-6">
        <section v-if="questions.length > 0 && !resultData" class="bg-white dark:bg-dark-surface rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200/50 dark:border-gray-700/50 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h3 class="font-semibold text-gray-900 dark:text-gray-100">{{ activeSession.title }}</h3>
              <p class="text-sm text-gray-500 mt-1">已答 {{ answeredCount }}/{{ questions.length }}</p>
            </div>
            <button @click="submitExam" :disabled="submitting" class="px-5 py-2 rounded-xl bg-emerald-500 text-white disabled:opacity-50">
              {{ submitting ? '提交中...' : '提交试卷' }}
            </button>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-[220px_minmax(0,1fr)]">
            <div class="p-5 border-r border-gray-200/50 dark:border-gray-700/50">
              <div class="grid grid-cols-5 gap-2">
                <button
                  v-for="(question, index) in questions"
                  :key="question.id"
                  @click="currentIndex = index"
                  class="question-nav"
                  :class="[
                    currentIndex === index ? 'question-nav-current' : '',
                    isAnswered(question.id) ? 'question-nav-done' : ''
                  ]"
                >
                  {{ index + 1 }}
                </button>
              </div>
            </div>

            <div class="p-6">
              <div class="flex flex-wrap items-center gap-2 mb-4">
                <span class="badge">{{ currentQuestion.questionType }}</span>
                <span class="badge">{{ currentQuestion.categoryLevel || '未分类' }}</span>
                <span class="badge">{{ currentQuestion.difficultyLevel || '未标难度' }}</span>
                <span v-if="currentQuestion.knowledgePoint" class="badge">{{ currentQuestion.knowledgePoint }}</span>
                <span class="text-sm text-gray-500">{{ scoreOf(currentQuestion) }} 分</span>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 leading-relaxed mb-6">{{ currentQuestion.questionTitle }}</h3>

              <div v-if="isChoiceQuestion(currentQuestion)" class="space-y-3">
                <label
                  v-for="option in parseOptions(currentQuestion.options)"
                  :key="option"
                  class="option-item"
                  :class="isSelected(currentQuestion, option) ? 'option-item-active' : ''"
                >
                  <input class="sr-only" :type="currentQuestion.questionType === '多选' ? 'checkbox' : 'radio'" :checked="isSelected(currentQuestion, option)" @change="selectAnswer(currentQuestion, option)">
                  <span>{{ option }}</span>
                </label>
              </div>

              <textarea
                v-else
                v-model="answers[currentQuestion.id]"
                rows="8"
                class="form-control"
                placeholder="请输入答案"
              ></textarea>

              <div class="flex justify-between mt-8">
                <button @click="currentIndex--" :disabled="currentIndex === 0" class="px-4 py-2 rounded-xl border border-gray-200 text-gray-600 disabled:opacity-50">上一题</button>
                <button @click="currentIndex++" :disabled="currentIndex === questions.length - 1" class="px-4 py-2 rounded-xl border border-gray-200 text-gray-600 disabled:opacity-50">下一题</button>
              </div>
            </div>
          </div>
        </section>

        <section v-else-if="resultData" class="bg-white dark:bg-dark-surface rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
          <div class="p-6 border-b border-gray-200/50 dark:border-gray-700/50">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100">考试结果</h3>
                <p class="text-sm text-gray-500 mt-1">得分 {{ resultData.scoreObtained }}/{{ resultData.scoreTotal }}，正确 {{ resultData.correctCount }} 题，错误 {{ resultData.wrongCount }} 题，未答 {{ resultData.unansweredCount }} 题</p>
              </div>
              <div class="text-4xl font-bold" :class="resultData.scorePercent >= 60 ? 'text-emerald-600' : 'text-red-600'">{{ resultData.scorePercent }}</div>
            </div>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-800">
            <article v-for="(item, index) in resultData.details" :key="item.questionId" class="p-6">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <div class="flex flex-wrap items-center gap-2 mb-2">
                    <span class="badge">第 {{ index + 1 }} 题</span>
                    <span class="badge">{{ item.questionType }}</span>
                    <span :class="item.correct ? 'text-emerald-600' : 'text-red-600'" class="text-sm font-medium">{{ item.correct ? '正确' : '错误' }}</span>
                  </div>
                  <p class="font-medium text-gray-900 dark:text-gray-100">{{ item.questionTitle }}</p>
                </div>
                <span class="text-sm text-gray-500">{{ item.score }} 分</span>
              </div>
              <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                <div class="p-3 rounded-xl bg-red-50 dark:bg-red-950/20 text-red-700 dark:text-red-300">你的答案：{{ item.userAnswer || '未作答' }}</div>
                <div class="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-300">正确答案：{{ item.correctAnswer }}</div>
              </div>
              <p v-if="item.answerAnalysis" class="mt-3 text-sm text-gray-600 dark:text-gray-300">解析：{{ item.answerAnalysis }}</p>
            </article>
          </div>
        </section>

        <section v-else class="bg-white dark:bg-dark-surface rounded-2xl p-12 border border-gray-200/50 dark:border-gray-700/50 text-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">请选择条件开始练习</h3>
          <p class="text-sm text-gray-500 mt-2">新增题目审核通过后，会进入你的私有题库参与抽题。</p>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const modes = [
  { label: '考试', value: 'exam' },
  { label: '练习', value: 'practice' },
  { label: '错题', value: 'mistake' }
]

const categoryOptions = ['高中', '大学', '考公', '考研', '考证', '专业面试']
const questionTypes = ['单选', '多选', '判断', '填空', '解答']

const userId = ref('')
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const durationMinutes = ref(30)
const manualKnowledge = ref('')
const questions = ref([])
const answers = ref({})
const currentIndex = ref(0)
const historyList = ref([])
const resultData = ref(null)
const elapsedSeconds = ref(0)
const suspiciousCount = ref(0)
let timer = null

const activeSession = reactive({
  sessionId: '',
  title: '',
  mode: '',
  antiCheatEnabled: false,
  startedAt: 0
})

const form = reactive({
  mode: 'exam',
  categoryLevel: '高中',
  subjectName: '',
  questionType: '',
  knowledgePoints: [],
  difficultyLevel: '',
  questionCount: 10
})

const filterOptions = reactive({
  subjects: [],
  questionTypes: [],
  knowledgePoints: []
})

const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
const answeredCount = computed(() => questions.value.filter(q => isAnswered(q.id)).length)

const getUserInfo = async () => {
  try {
    const res = await request.get('/user/getUserById')
    const data = res.data || res
    userId.value = data?.id || props.studentId || 1
    await Promise.all([fetchFilters(), fetchHistory()])
  } catch {
    userId.value = props.studentId || 1
    await Promise.all([fetchFilters(), fetchHistory()])
  }
}

const fetchFilters = async () => {
  if (!userId.value) return
  try {
    const res = await request.get(`/question/filters/${userId.value}`)
    if (res.code === 200 && res.data) {
      filterOptions.subjects = res.data.subjects || []
      filterOptions.questionTypes = res.data.questionTypes || []
      filterOptions.knowledgePoints = res.data.knowledgePoints || []
    }
  } catch (error) {
    console.error('获取筛选条件失败', error)
  }
}

const fetchHistory = async () => {
  if (!userId.value) return
  try {
    const res = await request.get(`/question/exam/history/${userId.value}`)
    if (res.code === 200) historyList.value = res.data || []
  } catch (error) {
    console.error('获取历史失败', error)
  }
}

const startExam = async () => {
  loading.value = true
  resultData.value = null
  try {
    const knowledgePoints = [
      ...form.knowledgePoints,
      ...manualKnowledge.value.split(/[,，、;]/).map(item => item.trim()).filter(Boolean)
    ]
    const res = await request.post('/question/exam/start', {
      userId: userId.value,
      mode: form.mode,
      title: form.mode === 'mistake' ? '错题练习' : '在线考试',
      categoryLevel: form.categoryLevel,
      subjectName: form.subjectName,
      questionType: form.questionType,
      knowledgePoints: [...new Set(knowledgePoints)],
      difficultyLevel: form.difficultyLevel,
      questionCount: form.questionCount,
      durationSeconds: durationMinutes.value ? durationMinutes.value * 60 : null,
      antiCheatEnabled: form.mode === 'exam'
    })
    if (res.code === 200 && res.data) {
      questions.value = res.data.questions || []
      answers.value = {}
      currentIndex.value = 0
      suspiciousCount.value = 0
      elapsedSeconds.value = 0
      Object.assign(activeSession, {
        sessionId: res.data.sessionId,
        title: form.mode === 'mistake' ? '错题练习' : '在线考试',
        mode: res.data.mode,
        antiCheatEnabled: !!res.data.antiCheatEnabled,
        startedAt: Date.now()
      })
      startTimer()
      ElMessage.success(`已抽取 ${questions.value.length} 道题`)
    }
  } catch (error) {
    ElMessage.error(error.msg || error.message || '组卷失败')
  } finally {
    loading.value = false
  }
}

const submitExam = async () => {
  if (!activeSession.sessionId) return
  const confirmText = answeredCount.value < questions.value.length
    ? `还有 ${questions.value.length - answeredCount.value} 题未答，确定提交吗？`
    : '确定提交试卷吗？'
  try {
    await ElMessageBox.confirm(confirmText, '提交确认', { type: 'warning' })
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = questions.value.map(q => ({
      questionId: q.id,
      userAnswer: normalizeUserAnswer(answers.value[q.id]),
      answerTimeSeconds: null
    }))
    const res = await request.post('/question/exam/submit', {
      userId: userId.value,
      sessionId: activeSession.sessionId,
      durationSeconds: elapsedSeconds.value,
      suspiciousCount: suspiciousCount.value,
      answers: payload
    })
    if (res.code === 200) {
      resultData.value = res.data
      stopTimer()
      await fetchHistory()
      ElMessage.success('提交成功')
    }
  } catch (error) {
    ElMessage.error(error.msg || error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const loadHistoryDetail = async (sessionId) => {
  try {
    const res = await request.get(`/question/exam/detail/${userId.value}/${sessionId}`)
    if (res.code === 200 && res.data) {
      resultData.value = {
        ...res.data.session,
        sessionId: res.data.session.sessionId,
        scorePercent: res.data.session.scorePercent,
        scoreObtained: res.data.session.scoreObtained,
        scoreTotal: res.data.session.scoreTotal,
        correctCount: res.data.session.correctCount,
        wrongCount: res.data.session.wrongCount,
        unansweredCount: Math.max(0, (res.data.session.totalQuestions || 0) - (res.data.session.correctCount || 0) - (res.data.session.wrongCount || 0)),
        details: res.data.details || []
      }
      questions.value = []
      stopTimer()
    }
  } catch (error) {
    ElMessage.error('历史详情加载失败')
  }
}

const toggleKnowledge = (point) => {
  const index = form.knowledgePoints.indexOf(point)
  if (index >= 0) form.knowledgePoints.splice(index, 1)
  else form.knowledgePoints.push(point)
}

const isChoiceQuestion = (question) => ['单选', '多选', '判断', 'single', 'multiple', 'judge'].includes(question.questionType)

const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(options).split(/[,，]/).map(item => item.trim()).filter(Boolean)
  }
}

const optionValue = (option) => {
  const match = String(option).match(/^([A-Z])[\.\、\s]/)
  return match ? match[1] : String(option)
}

const selectAnswer = (question, option) => {
  const value = optionValue(option)
  if (question.questionType === '多选' || question.questionType === 'multiple') {
    const current = Array.isArray(answers.value[question.id]) ? answers.value[question.id] : []
    const index = current.indexOf(value)
    if (index >= 0) current.splice(index, 1)
    else current.push(value)
    answers.value[question.id] = [...current].sort()
  } else if (question.questionType === '判断' || question.questionType === 'judge') {
    answers.value[question.id] = value === 'A' ? '正确' : value === 'B' ? '错误' : value
  } else {
    answers.value[question.id] = value
  }
}

const isSelected = (question, option) => {
  const value = optionValue(option)
  const current = answers.value[question.id]
  if (Array.isArray(current)) return current.includes(value)
  if (question.questionType === '判断' || question.questionType === 'judge') {
    return (current === '正确' && value === 'A') || (current === '错误' && value === 'B') || current === value
  }
  return current === value
}

const isAnswered = (questionId) => {
  const answer = answers.value[questionId]
  return Array.isArray(answer) ? answer.length > 0 : !!String(answer || '').trim()
}

const normalizeUserAnswer = (answer) => Array.isArray(answer) ? answer.join(',') : String(answer || '').trim()

const scoreOf = (question) => question.scoreValue || 2

const startTimer = () => {
  stopTimer()
  timer = window.setInterval(() => {
    elapsedSeconds.value += 1
    if (durationMinutes.value && elapsedSeconds.value >= durationMinutes.value * 60) {
      stopTimer()
      submitExam()
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

const handleVisibilityChange = () => {
  if (activeSession.antiCheatEnabled && document.hidden && questions.value.length > 0 && !resultData.value) {
    suspiciousCount.value += 1
  }
}

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = Math.floor(seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '--'

const modeText = (mode) => ({ exam: '考试', practice: '练习', mistake: '错题' }[mode] || mode)

watch(() => form.mode, (mode) => {
  if (mode === 'mistake') durationMinutes.value = 0
  else if (!durationMinutes.value) durationMinutes.value = 30
})

watch(() => route.query.mode, (mode) => {
  if (mode === 'mistake') {
    form.mode = 'mistake'
    durationMinutes.value = 0
  }
}, { immediate: true })

onMounted(() => {
  getUserInfo()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  stopTimer()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.form-label {
  display: block;
  margin-bottom: 6px;
  color: rgb(75 85 99);
  font-size: 13px;
  font-weight: 600;
}

.form-control {
  width: 100%;
  border: 1px solid rgb(229 231 235);
  border-radius: 12px;
  background: white;
  padding: 10px 12px;
  color: rgb(31 41 55);
  font-size: 14px;
  outline: none;
}

.form-control:focus {
  border-color: rgb(99 102 241);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.seg-btn,
.tag-btn {
  border: 1px solid rgb(229 231 235);
  border-radius: 10px;
  background: white;
  padding: 8px 10px;
  color: rgb(75 85 99);
  font-size: 13px;
}

.seg-btn-active,
.tag-btn-active {
  border-color: rgb(99 102 241);
  background: rgb(238 242 255);
  color: rgb(79 70 229);
  font-weight: 700;
}

.question-nav {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgb(243 244 246);
  color: rgb(75 85 99);
  font-size: 13px;
}

.question-nav-current {
  outline: 2px solid rgb(99 102 241);
  outline-offset: 2px;
}

.question-nav-done {
  background: rgb(16 185 129);
  color: white;
}

.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: rgb(243 244 246);
  padding: 4px 10px;
  color: rgb(75 85 99);
  font-size: 12px;
}

.option-item {
  display: flex;
  cursor: pointer;
  align-items: center;
  border: 1px solid rgb(229 231 235);
  border-radius: 12px;
  padding: 14px 16px;
  color: rgb(55 65 81);
}

.option-item-active {
  border-color: rgb(99 102 241);
  background: rgb(238 242 255);
  color: rgb(67 56 202);
  font-weight: 700;
}

:global(.dark) .form-control,
:global(.dark) .seg-btn,
:global(.dark) .tag-btn,
:global(.dark) .option-item {
  border-color: rgb(55 65 81);
  background: rgb(31 41 55);
  color: rgb(229 231 235);
}

:global(.dark) .badge,
:global(.dark) .question-nav {
  background: rgb(31 41 55);
  color: rgb(209 213 219);
}
</style>
