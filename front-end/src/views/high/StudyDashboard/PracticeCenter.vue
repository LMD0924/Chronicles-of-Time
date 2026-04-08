<template>
  <div class="space-y-6 max-w-[1400px] mx-auto px-4 lg:px-6 mt-24">
    <!-- 练习头部 - 考试信息栏 -->
    <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-indigo-500/10">
      <div class="flex flex-wrap justify-between items-center gap-4">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/30">
            <span class="text-white text-2xl">📝</span>
          </div>
          <div>
            <h2 class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">实战练习</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">✨ 精选题目，智能组卷</p>
          </div>
        </div>

        <div class="flex gap-3 flex-wrap">
          <select v-model="examType" class="px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm font-medium shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-indigo-400/50">
            <option value="高中">🏫 高中题库</option>
            <option value="大学">🎓 大学题库</option>
            <option value="考公">📋 公务员考试</option>
            <option value="考研">📖 研究生考试</option>
          </select>
          <select v-model="subjectFilter" class="px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-indigo-400/50">
            <option value="">全部科目</option>
            <option v-for="subject in filterSubjects" :key="subject" :value="subject">{{ subject }}</option>
          </select>
          <select v-model="questionTypeFilter" class="px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-indigo-400/50">
            <option value="">全部题型</option>
            <option v-for="type in filterQuestionTypes" :key="type" :value="type">{{ type }}</option>
          </select>
          <button
            @click="startExam"
            :disabled="loading"
            class="px-7 py-2.5 rounded-2xl bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 text-white text-sm font-medium shadow-lg shadow-indigo-500/30 hover:shadow-xl hover:shadow-indigo-500/40 transform hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          >
            {{ loading ? '✨ 加载中...' : '🚀 开始练习' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 考试进度条 -->
    <div v-if="questions.length > 0" class="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-indigo-500/5">
      <div class="flex justify-between items-center mb-4">
        <span class="text-sm font-semibold text-gray-700 dark:text-gray-300">📊 答题进度</span>
        <span class="text-base font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">{{ answeredCount }}/{{ questions.length }}</span>
      </div>
      <div class="h-3 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden shadow-inner">
        <div class="h-full bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 rounded-full transition-all duration-500 ease-out shadow-lg shadow-indigo-500/30" :style="{ width: (answeredCount / questions.length * 100) + '%' }"></div>
      </div>
    </div>

    <!-- 主内容区：左右布局 宽度加大 -->
    <div v-if="questions.length > 0" class="grid grid-cols-1 lg:grid-cols-5 gap-6">
      <!-- 左侧：题号导航区 (占1份，宽度稍小) -->
      <div class="lg:col-span-1">
        <div class="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-3xl border border-white/20 dark:border-gray-700/30 sticky top-24 shadow-2xl shadow-indigo-500/5">
          <div class="p-5 border-b border-gray-200/30 dark:border-gray-700/30 bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-transparent dark:to-transparent">
            <div class="flex justify-between items-center">
              <h3 class="font-bold text-gray-800 dark:text-gray-200">🧭 题目导航</h3>
              <div class="flex gap-3 text-xs">
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-emerald-500 shadow-sm"></span>已答</span>
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-gray-300 dark:bg-gray-600"></span>未答</span>
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-amber-500"></span>标记</span>
              </div>
            </div>
          </div>
          <div class="p-4">
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="(question, idx) in questions"
                :key="question.id"
                @click="currentIndex = idx"
                :class="[
                  'w-10 h-10 rounded-xl text-sm font-medium transition-all duration-200',
                  currentIndex === idx ? 'ring-2 ring-indigo-500 ring-offset-2 dark:ring-offset-gray-800' : '',
                  isAnswered(question.id) ? 'bg-green-500 text-white hover:bg-green-600' : '',
                  !isAnswered(question.id) && !isMarked(question.id) ? 'bg-gray-100 dark:bg-black text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-black' : '',
                  isMarked(question.id) && !isAnswered(question.id) ? 'bg-yellow-500 text-white hover:bg-yellow-600' : ''
                ]"
              >
                {{ idx + 1 }}
              </button>
            </div>
          </div>
          <div class="p-4 border-t border-gray-200/50 dark:border-gray-700/50 flex flex-col gap-2">
            <div class="flex gap-2">
              <button
                @click="submitAllAnswers"
                :disabled="isAllSubmitted"
                class="flex-1 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 text-white text-sm font-medium hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed">
                {{ isAllSubmitted ? '已提交' : '提交全部' }}
              </button>
              <button
                @click="submitAllMistakes"
                :disabled="isAllSubmitted"
                class="flex-1 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 text-white text-sm font-medium hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed">
                提交错题
              </button>
              <button
                @click="clearAnswers"
                :disabled="isAllSubmitted"
                class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
                清空
              </button>
            </div>
            <button @click="showAnswerDetails = !showAnswerDetails" class="w-full py-2 rounded-xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white text-sm font-medium hover:shadow-lg transition">
              {{ showAnswerDetails ? '隐藏答题详情' : '查看答题详情' }}
            </button>
          </div>

          <!-- 答题详情 -->
          <div v-if="showAnswerDetails" class="p-4 border-t border-gray-200/50 dark:border-gray-700/50">
            <h4 class="font-semibold text-gray-800 dark:text-gray-200 mb-3">答题情况</h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-green-500"></span>已答</span>
                <span>{{ answeredCount }} 题</span>
              </div>
              <div class="flex justify-between">
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-gray-300 dark:bg-gray-600"></span>未答</span>
                <span>{{ questions.length - answeredCount }} 题</span>
              </div>
              <div class="flex justify-between">
                <span class="flex items-center gap-1"><span class="w-3 h-3 rounded-full bg-yellow-500"></span>标记</span>
                <span>{{ markedQuestions.length }} 题</span>
              </div>
            </div>
            <div class="mt-4 space-y-1 max-h-64 overflow-y-auto">
              <div v-for="(question, idx) in questions" :key="question.id" class="flex items-center gap-2 py-1">
                <span class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium" :class="[
                  isAnswered(question.id) ? 'bg-green-500 text-white' : 'bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300',
                  markedQuestions.includes(question.id) ? 'ring-2 ring-yellow-500' : ''
                ]">
                  {{ idx + 1 }}
                </span>
                <span class="text-xs truncate flex-1">{{ question.questionTitle?.substring(0, 35) || '' }}{{ question.questionTitle?.length > 35 ? '...' : '' }}</span>
                <span class="text-xs" :class="isAnswered(question.id) ? 'text-green-600 dark:text-green-400' : 'text-gray-500'">
                  {{ isAnswered(question.id) ? '已答' : '未答' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：答题区 (占4份，更宽敞) -->
      <div class="lg:col-span-4">
        <div class="bg-white dark:bg-gray-800/50 rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
          <!-- 题目头部 -->
          <div class="p-6 border-b border-gray-200/50 dark:border-gray-700/50 bg-gradient-to-r from-indigo-50/30 to-purple-50/30 dark:from-transparent dark:to-transparent">
            <div class="flex justify-between items-center flex-wrap gap-3">
              <div class="flex items-center gap-3">
                <span class="w-10 h-10 rounded-full bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400 flex items-center justify-center text-lg font-bold">
                  {{ currentIndex + 1 }}
                </span>
                <div>
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="px-2 py-1 rounded-lg text-xs font-medium" :class="getTypeClass(currentQuestion.questionType)">
                      {{ currentQuestion.questionType }}
                    </span>
                    <span class="px-2 py-1 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs">
                      {{ currentQuestion.categoryLevel }}
                    </span>
                    <span v-if="currentQuestion.knowledgePoint || currentQuestion.knowledge_point" class="px-2 py-1 rounded-lg bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs">
                      📌 {{ currentQuestion.knowledgePoint || currentQuestion.knowledge_point }}
                    </span>
                    <span class="text-sm text-gray-500">{{ currentQuestion.difficultyLevel }}</span>
                  </div>
                </div>
              </div>
              <div class="flex gap-2">
                <button
                  @click="toggleScratchPaper"
                  class="px-3 py-1.5 rounded-lg bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 text-sm font-medium transition hover:bg-blue-200"
                >
                  📝 草稿纸
                </button>
                <button
                  @click="toggleMark(currentQuestion.id)"
                  :class="[
                    'px-3 py-1.5 rounded-lg text-sm font-medium transition',
                    isMarked(currentQuestion.id)
                      ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'
                      : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400 hover:bg-gray-200'
                  ]"
                >
                  {{ isMarked(currentQuestion.id) ? '⭐ 已标记' : '☆ 标记此题' }}
                </button>
                <button
                  @click="reportQuestion(currentQuestion.id)"
                  class="px-3 py-1.5 rounded-lg bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400 text-sm hover:bg-gray-200 transition"
                >
                  📢 反馈
                </button>
              </div>
            </div>
          </div>

          <!-- 题目内容 -->
          <div class="p-8">
            <div class="mb-8">
              <p class="text-lg text-gray-800 dark:text-gray-200 leading-relaxed">{{ currentQuestion.questionTitle }}</p>
            </div>

            <!-- 选项区域 (更宽敞的选项按钮) -->
            <div v-if="currentQuestion.questionType !== '解答' && currentQuestion.questionType !== '填空'" class="space-y-4 mb-8">
              <div
                v-for="(option, optIndex) in parseOptions(currentQuestion.options)"
                :key="optIndex"
                @click="selectAnswer(currentQuestion.id, getOptionValue(option), currentQuestion.questionType)"
                class="flex items-center gap-4 p-4 rounded-xl cursor-pointer transition-all"
                :class="[
                  (currentQuestion.questionType === '多选' ? userAnswers[currentQuestion.id]?.includes(getOptionValue(option)) : userAnswers[currentQuestion.id] === getOptionValue(option))
                    ? 'bg-indigo-50 dark:bg-indigo-900/30 border-2 border-indigo-300 dark:border-indigo-700'
                    : 'bg-gray-50 dark:bg-gray-900/50 border-2 border-transparent hover:border-indigo-200 dark:hover:border-indigo-800'
                ]"
              >
                <div :class="[
                  'w-6 h-6 border-2 flex items-center justify-center transition-all',
                  currentQuestion.questionType === '多选' ? 'rounded' : 'rounded-full',
                  (currentQuestion.questionType === '多选' ? userAnswers[currentQuestion.id]?.includes(getOptionValue(option)) : userAnswers[currentQuestion.id] === getOptionValue(option))
                    ? 'border-indigo-500 bg-indigo-500'
                    : 'border-gray-300 dark:border-gray-600'
                ]">
                  <span v-if="currentQuestion.questionType === '多选' ? userAnswers[currentQuestion.id]?.includes(getOptionValue(option)) : userAnswers[currentQuestion.id] === getOptionValue(option)" class="text-white text-sm">✓</span>
                </div>
                <span class="text-base text-gray-700 dark:text-gray-300">{{ option }}</span>
              </div>
            </div>

            <!-- 填空题/解答题 (更宽敞的输入框) -->
            <div v-else class="mb-8">
              <textarea
                v-model="userAnswers[currentQuestion.id]"
                :placeholder="currentQuestion.questionType === '填空' ? '请输入答案（多个空用逗号分隔）' : '请输入解答过程...'"
                rows="6"
                class="w-full px-5 py-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 text-gray-700 dark:text-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-base"
              ></textarea>
            </div>

            <!-- 解析区域（提交后显示） -->
            <div v-if="submittedQuestions[currentQuestion.id]" class="mt-8 p-5 rounded-xl" :class="isAnswerCorrect(currentQuestion) ? 'bg-green-50 dark:bg-green-950/20' : 'bg-red-50 dark:bg-red-950/20'">
              <div class="flex items-start gap-3">
                <span class="text-2xl">{{ isAnswerCorrect(currentQuestion) ? '✅' : '❌' }}</span>
                <div class="flex-1">
                  <p class="font-medium text-base" :class="isAnswerCorrect(currentQuestion) ? 'text-green-700 dark:text-green-400' : 'text-red-700 dark:text-red-400'">
                    {{ isAnswerCorrect(currentQuestion) ? '回答正确！' : '回答错误' }}
                  </p>
                  <p class="text-gray-600 dark:text-gray-400 text-sm mt-2">
                    <span class="font-medium">正确答案：</span>{{ currentQuestion.correctAnswer }}
                  </p>
                  <p v-if="currentQuestion.answerAnalysis" class="text-gray-500 dark:text-gray-400 text-sm mt-2">
                    📝 {{ currentQuestion.answerAnalysis }}
                  </p>
                  <!-- 错题加入按钮 -->
                  <button
                    v-if="!isAnswerCorrect(currentQuestion) && !addedToMistake[currentQuestion.id]"
                    @click="addToMistake(currentQuestion)"
                    class="mt-3 px-3 py-1.5 rounded-lg bg-amber-500 text-white text-sm hover:bg-amber-600 transition"
                  >
                    + 加入错题本
                  </button>
                  <span v-else-if="addedToMistake[currentQuestion.id]" class="inline-block mt-3 text-sm text-green-600">
                    ✓ 已加入错题本
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部导航按钮 -->
          <div class="p-6 border-t border-gray-200/50 dark:border-gray-700/50 bg-gray-50/50 dark:bg-gray-900/30 flex justify-between">
            <button
              @click="prevQuestion"
              :disabled="currentIndex === 0"
              class="px-6 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100 dark:hover:bg-gray-800 transition"
            >
              ← 上一题
            </button>
            <button
              @click="submitCurrentAnswer"
              class="px-6 py-2.5 rounded-xl bg-indigo-500 text-white hover:bg-indigo-600 transition"
            >
              提交本题
            </button>
            <button
              @click="nextQuestion"
              :disabled="currentIndex === questions.length - 1"
              class="px-6 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100 dark:hover:bg-gray-800 transition"
            >
              下一题 →
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="flex justify-center py-20">
      <div class="text-center">
        <div class="w-12 h-12 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto"></div>
        <p class="text-gray-500 mt-4">加载题目中...</p>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="text-center py-20 bg-white dark:bg-gray-800/50 rounded-2xl border border-gray-200/50 dark:border-gray-700/50">
      <span class="text-6xl opacity-50">📚</span>
      <p class="text-gray-500 mt-4">点击"开始练习"按钮开始答题</p>
    </div>

    <!-- 提交结果弹窗 -->
    <div v-if="showResultModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showResultModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-md p-6">
        <div class="text-center">
          <span class="text-5xl">{{ resultData.score >= 60 ? '🎉' : '📚' }}</span>
          <h3 class="text-xl font-bold mt-3">练习完成</h3>
          <div class="mt-4 p-4 bg-gray-100 dark:bg-gray-700 rounded-xl">
            <div class="text-3xl font-bold text-indigo-600">{{ resultData.score }}分</div>
            <div class="text-sm text-gray-500 mt-1">正确率 {{ resultData.accuracy }}%</div>
          </div>
          <div class="mt-4 space-y-2 text-left">
            <p>✅ 正确：{{ resultData.correctCount }} 题</p>
            <p>❌ 错误：{{ resultData.wrongCount }} 题</p>
            <p>📝 未答：{{ resultData.unansweredCount }} 题</p>
          </div>
          <div class="flex gap-3 mt-6">
            <button @click="showResultModal = false" class="flex-1 py-2 rounded-xl border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400">关闭</button>
            <button @click="goToAnswerRecords" class="flex-1 py-2 rounded-xl bg-indigo-500 text-white">查看答题记录</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 草稿纸弹窗 (尺寸调大) -->
    <div v-if="showScratchPaper" class="fixed inset-0 z-50" @click.self="toggleScratchPaper">
      <div
        class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl overflow-hidden absolute"
        :style="{
          width: scratchPaperSize.width + 'px',
          height: scratchPaperSize.height + 'px',
          left: scratchPaperPosition.x + 'px',
          top: scratchPaperPosition.y + 'px'
        }"
      >
        <!-- 标题栏（可拖动） -->
        <div
          class="px-5 py-4 bg-gradient-to-r from-blue-500 to-indigo-500 flex justify-between items-center cursor-move select-none"
          @mousedown="startScratchPaperDrag"
          @touchstart="startScratchPaperDrag"
        >
          <span class="text-white font-medium">📝 草稿纸</span>
          <div class="flex items-center gap-3">
            <button @click.stop="clearScratchPaper" class="text-white/80 hover:text-white text-sm">清空</button>
            <button @click.stop="toggleScratchPaper" class="text-white/80 hover:text-white text-xl">&times;</button>
          </div>
        </div>
        <!-- 画布区域 -->
        <div class="relative" style="height: calc(100% - 56px);">
          <canvas
            ref="scratchCanvas"
            class="w-full h-full cursor-crosshair"
            @mousedown="startDrawing"
            @mousemove="draw"
            @mouseup="stopDrawing"
            @mouseleave="stopDrawing"
            @touchstart="startDrawing"
            @touchmove="draw"
            @touchend="stopDrawing"
          ></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const emit = defineEmits(['switchToAnswerRecords'])

// 状态变量
const loading = ref(false)
const questions = ref([])
const userAnswers = ref({})           // 用户答案
const submittedQuestions = ref({})     // 已提交的题目
const markedQuestions = ref([])        // 标记的题目（存储题目ID）
const addedToMistake = ref({})         // 已加入错题本
const currentIndex = ref(0)
const examType = ref('高中')
const isAllSubmitted = ref(false)       // 是否已全部提交

// 草稿纸相关
const showScratchPaper = ref(false)
const scratchCanvas = ref(null)
const isDrawing = ref(false)
const scratchPaperSize = reactive({ width: 900, height: 650 })
const scratchPaperPosition = reactive({ x: 100, y: 100 })
const isDragging = ref(false)
const dragOffset = reactive({ x: 0, y: 0 })
let ctx = null
let lastX = 0
let lastY = 0
const subjectFilter = ref('')
const questionTypeFilter = ref('')
const showResultModal = ref(false)
const showAnswerDetails = ref(false)   // 显示答题详情
const resultData = ref({
  score: 0,
  accuracy: 0,
  correctCount: 0,
  wrongCount: 0,
  unansweredCount: 0
})

// 筛选条件
const filterSubjects = ref([])
const filterQuestionTypes = ref([])

// 计算属性
const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
const answeredCount = computed(() => {
  return Object.keys(userAnswers.value).filter(id => {
    const answer = userAnswers.value[id]
    return answer && (Array.isArray(answer) ? answer.length > 0 : answer.trim() !== '')
  }).length
})

// 判断是否已答
const isAnswered = (questionId) => {
  const answer = userAnswers.value[questionId]
  return answer && (Array.isArray(answer) ? answer.length > 0 : answer.trim() !== '')
}

// 判断是否已标记
const isMarked = (questionId) => {
  return markedQuestions.value.includes(questionId)
}

// 判断答案是否正确
const isAnswerCorrect = (question) => {
  const userAnswer = userAnswers.value[question.id]
  if (!userAnswer) return false
  if (Array.isArray(userAnswer)) {
    return userAnswer.sort().join(',') === question.correctAnswer
  }
  return userAnswer.trim() === question.correctAnswer
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  try {
    return JSON.parse(options)
  } catch {
    return options.split(',').map(o => o.trim())
  }
}

// 获取选项值（A、B、C、D）
const getOptionValue = (option) => {
  const match = option.match(/^([A-D])[\.、]/)
  return match ? match[1] : option.charAt(0)
}

// 获取题型样式
const getTypeClass = (type) => {
  const map = {
    '单选': 'bg-blue-100 text-blue-600 dark:bg-blue-900/50 dark:text-blue-400',
    '多选': 'bg-purple-100 text-purple-600 dark:bg-purple-900/50 dark:text-purple-400',
    '判断': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/50 dark:text-emerald-400',
    '填空': 'bg-amber-100 text-amber-600 dark:bg-amber-900/50 dark:text-amber-400',
    '解答': 'bg-rose-100 text-rose-600 dark:bg-rose-900/50 dark:text-rose-400'
  }
  return map[type] || 'bg-gray-100 text-gray-600'
}

// 选择答案
const selectAnswer = (questionId, value, questionType) => {
  if (questionType === '多选') {
    if (!userAnswers.value[questionId]) {
      userAnswers.value[questionId] = []
    }
    const index = userAnswers.value[questionId].indexOf(value)
    if (index > -1) {
      userAnswers.value[questionId].splice(index, 1)
    } else {
      userAnswers.value[questionId].push(value)
    }
  } else if (questionType === '判断') {
    userAnswers.value[questionId] = value === 'A' ? '正确' : '错误'
  } else {
    userAnswers.value[questionId] = value
  }
}

// 标记题目
const toggleMark = (questionId) => {
  const index = markedQuestions.value.indexOf(questionId)
  if (index > -1) {
    markedQuestions.value.splice(index, 1)
    ElMessage.info('已取消标记')
  } else {
    markedQuestions.value.push(questionId)
    ElMessage.success('已标记，可在导航区查看')
  }
}

// 提交当前答案
const submitCurrentAnswer = async () => {
  const question = currentQuestion.value
  const answer = userAnswers.value[question.id]

  if (!answer || (Array.isArray(answer) ? answer.length === 0 : answer.trim() === '')) {
    ElMessage.warning('请先填写答案')
    return
  }

  let isCorrect = false
  let userAnswerStr = answer
  if (Array.isArray(answer)) {
    userAnswerStr = answer.sort().join(',')
    isCorrect = userAnswerStr === question.correctAnswer
  } else {
    isCorrect = answer.trim() === question.correctAnswer
  }

  try {
    await request.post('/question/record', {
      questionId: question.id,
      isCorrect,
      userAnswer: userAnswerStr
    })
  } catch (error) {
    console.error('记录失败', error)
  }

  submittedQuestions.value[question.id] = true

  if (isCorrect) {
    ElMessage.success('回答正确！')
  } else {
    ElMessage.error('回答错误')
  }

  if (currentIndex.value < questions.value.length - 1) {
    setTimeout(() => {
      currentIndex.value++
    }, 800)
  }
}

// 提交全部答案
const submitAllAnswers = async () => {
  if (isAllSubmitted.value) {
    ElMessage.warning('您已提交过了！')
    return
  }

  let correctCount = 0
  let wrongCount = 0
  let unansweredCount = 0
  const answerRecords = []
  const examSession = Date.now().toString()
  const answerDate = new Date().toISOString().split('T')[0]

  for (const question of questions.value) {
    const answer = userAnswers.value[question.id]
    if (!answer || (typeof answer === 'string' && answer.trim() === '') || (Array.isArray(answer) && answer.length === 0)) {
      unansweredCount++
      continue
    }

    let userAnswerStr = answer
    if (Array.isArray(answer)) {
      userAnswerStr = answer.sort().join(',')
    }

    const isCorrect = userAnswerStr === question.correctAnswer
    if (isCorrect) {
      correctCount++
    } else {
      wrongCount++
    }

    const record = {
      userId: props.studentId || 1,
      questionId: question.id,
      subjectName: question.subjectName,
      questionType: question.questionType,
      categoryLevel: question.categoryLevel,
      knowledgePoint: question.knowledgePoint || '',
      userAnswer: userAnswerStr,
      correctAnswer: question.correctAnswer,
      isCorrect: isCorrect ? 1 : 0,
      score: isCorrect ? 2 : 0,
      answerTime: 0,
      mistakeAdded: 0,
      examSession: examSession,
      answerDate: answerDate
    }
    answerRecords.push(record)

    submittedQuestions.value[question.id] = true
  }

  if (answerRecords.length > 0) {
    try {
      await request.post('/question/record-batch', answerRecords)
    } catch (error) {
      console.error('批量记录失败', error)
    }
  }

  const total = questions.value.length
  const score = Math.round((correctCount / total) * 100)

  resultData.value = {
    score,
    accuracy: Math.round((correctCount / total) * 100),
    correctCount,
    wrongCount,
    unansweredCount
  }

  isAllSubmitted.value = true
  showResultModal.value = true
}

// 跳转到答题记录页面
const goToAnswerRecords = () => {
  showResultModal.value = false
  emit('switchToAnswerRecords')
}

// 提交所有错题
const submitAllMistakes = async () => {
  let addedCount = 0
  let errorCount = 0

  for (const question of questions.value) {
    const answer = userAnswers.value[question.id]
    if (!answer || (Array.isArray(answer) ? answer.length === 0 : answer.trim() === '')) continue

    let isCorrect = false
    if (Array.isArray(answer)) {
      isCorrect = answer.sort().join(',') === question.correctAnswer
    } else {
      isCorrect = answer.trim() === question.correctAnswer
    }
    if (isCorrect) continue

    if (addedToMistake.value[question.id]) continue

    try {
      const mistakeData = {
        userId: props.studentId || 1,
        subjectName: question.subjectName,
        mistakeName: question.questionTitle?.substring(0, 100) || '',
        mistakeType: question.questionType,
        wrongAnswer: answer,
        correctAnswer: question.correctAnswer,
        answerAnalysis: question.answerAnalysis,
        mistakeDate: new Date().toISOString().split('T')[0]
      }

      const res = await request.post('/mistake/add', mistakeData)
      if (res.code === 200) {
        addedToMistake.value[question.id] = true
        addedCount++
      }
    } catch (error) {
      console.error('加入错题本失败', error)
      errorCount++
    }
  }

  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 道错题到错题本`)
  } else if (errorCount === 0) {
    ElMessage.info('没有需要添加的错题')
  } else {
    ElMessage.error('添加错题失败')
  }
}

// 清空答案
const clearAnswers = () => {
  if (confirm('确定清空所有答案吗？')) {
    userAnswers.value = {}
    submittedQuestions.value = {}
    ElMessage.success('已清空')
  }
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

// 加入错题本
const addToMistake = async (question) => {
  try {
    let userAnswer = userAnswers.value[question.id] || ''
    if (Array.isArray(userAnswer)) {
      userAnswer = userAnswer.sort().join(',')
    }
    const mistakeData = {
      userId: props.studentId || 1,
      subjectName: question.subjectName,
      mistakeName: question.questionTitle?.substring(0, 100) || '',
      mistakeType: question.questionType,
      wrongAnswer: userAnswer,
      correctAnswer: question.correctAnswer,
      answerAnalysis: question.answerAnalysis,
      mistakeDate: new Date().toISOString().split('T')[0]
    }

    const res = await request.post('/mistake/add', mistakeData)
    if (res.code === 200) {
      addedToMistake.value[question.id] = true
      ElMessage.success('已加入错题本')
    }
  } catch (error) {
    console.error('加入错题本失败', error)
    ElMessage.error('加入失败')
  }
}

// 反馈题目
const reportQuestion = (questionId) => {
  ElMessage.info('反馈功能开发中...')
}

// 草稿纸功能
const toggleScratchPaper = () => {
  showScratchPaper.value = !showScratchPaper.value
  if (showScratchPaper.value) {
    setTimeout(() => {
      initScratchCanvas()
    }, 100)
  }
}

// 初始化草稿纸画布
const initScratchCanvas = () => {
  const canvas = scratchCanvas.value
  if (!canvas) return

  canvas.width = scratchPaperSize.width
  canvas.height = scratchPaperSize.height - 56

  ctx = canvas.getContext('2d')
  ctx.strokeStyle = props.isDark ? '#ffffff' : '#000000'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  ctx.fillStyle = props.isDark ? '#1f2937' : '#ffffff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
}

// 开始绘画
const startDrawing = (e) => {
  isDrawing.value = true
  const canvas = scratchCanvas.value
  const rect = canvas.getBoundingClientRect()
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  lastX = clientX - rect.left
  lastY = clientY - rect.top
}

// 绘画
const draw = (e) => {
  if (!isDrawing.value || !ctx) return

  const canvas = scratchCanvas.value
  const rect = canvas.getBoundingClientRect()
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  const currentX = clientX - rect.left
  const currentY = clientY - rect.top

  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
  ctx.lineTo(currentX, currentY)
  ctx.stroke()

  lastX = currentX
  lastY = currentY
}

// 停止绘画
const stopDrawing = () => {
  isDrawing.value = false
}

// 清空草稿纸
const clearScratchPaper = () => {
  const canvas = scratchCanvas.value
  if (!canvas || !ctx) {
    initScratchCanvas()
    if (!ctx) return
  }
  ctx.fillStyle = props.isDark ? '#1f2937' : '#ffffff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ElMessage.success('草稿纸已清空')
}

// 开始拖动草稿纸
const startScratchPaperDrag = (e) => {
  isDragging.value = true
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  dragOffset.x = clientX - scratchPaperPosition.x
  dragOffset.y = clientY - scratchPaperPosition.y

  document.addEventListener('mousemove', onScratchPaperDrag)
  document.addEventListener('mouseup', stopScratchPaperDrag)
  document.addEventListener('touchmove', onScratchPaperDrag)
  document.addEventListener('touchend', stopScratchPaperDrag)
}

// 拖动草稿纸
const onScratchPaperDrag = (e) => {
  if (!isDragging.value) return
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  scratchPaperPosition.x = clientX - dragOffset.x
  scratchPaperPosition.y = clientY - dragOffset.y
}

// 停止拖动草稿纸
const stopScratchPaperDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onScratchPaperDrag)
  document.removeEventListener('mouseup', stopScratchPaperDrag)
  document.removeEventListener('touchmove', onScratchPaperDrag)
  document.removeEventListener('touchend', stopScratchPaperDrag)
}

// 获取筛选条件
const fetchFilters = async () => {
  try {
    const res = await request.get('/question/filters')
    if (res.code === 200 && res.data) {
      filterSubjects.value = res.data.subjects || []
      filterQuestionTypes.value = res.data.questionTypes || []
    }
  } catch (error) {
    console.error('获取筛选条件失败', error)
  }
}

// 开始考试
const startExam = async () => {
  loading.value = true

  try {
    const params = {
      categoryLevel: examType.value,
      limit: 15
    }
    if (subjectFilter.value) {
      params.subjectName = subjectFilter.value
    }
    if (questionTypeFilter.value) {
      params.questionType = questionTypeFilter.value
    }

    const res = await request.get('/question/random', params)

    if (res.code === 200 && res.data && res.data.length > 0) {
      questions.value = res.data
      userAnswers.value = {}
      submittedQuestions.value = {}
      markedQuestions.value = []
      addedToMistake.value = {}
      currentIndex.value = 0
      isAllSubmitted.value = false
      ElMessage.success(`已加载 ${res.data.length} 道题目`)
    } else {
      ElMessage.warning('暂无题目，请先添加题库')
      questions.value = []
    }
  } catch (error) {
    console.error('获取题目失败', error)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFilters()
})
</script>

<style scoped>
@keyframes spin {
  to { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 0.8s linear infinite;
}
</style>
