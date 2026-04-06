<template>
  <div class="space-y-6 max-w-[1400px] mx-auto px-4 lg:px-6 mt-24">
    <!-- 头部 -->
    <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-purple-500/10">
      <div class="flex flex-wrap justify-between items-center gap-4">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-purple-500 via-indigo-500 to-blue-500 flex items-center justify-center shadow-lg shadow-purple-500/30">
            <span class="text-white text-2xl">📚</span>
          </div>
          <div>
            <h2 class="text-2xl font-bold bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 bg-clip-text text-transparent">题库管理</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">✨ 共 {{ totalQuestions }} 道题目</p>
          </div>
        </div>
        <button @click="showAddModal = true" class="px-6 py-2.5 rounded-2xl bg-gradient-to-r from-purple-500 via-indigo-500 to-blue-500 text-white text-sm font-medium shadow-lg shadow-purple-500/30 hover:shadow-xl hover:shadow-purple-500/40 transform hover:-translate-y-0.5 transition-all">
          ➕ 添加题目
        </button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white dark:bg-black rounded-2xl p-4 border border-gray-200/50 dark:border-gray-700/50">
      <div class="flex flex-wrap gap-3">
        <select v-model="filters.categoryLevel" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-sm">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
        <select v-model="filters.subjectName" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm">
          <option value="">全部科目</option>
          <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>
        <select v-model="filters.questionType" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm">
          <option value="">全部题型</option>
          <option v-for="type in questionTypes" :key="type" :value="type">{{ type }}</option>
        </select>
        <select v-model="filters.knowledgePoint" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm">
          <option value="">全部知识点</option>
          <option v-for="kp in knowledgePoints" :key="kp" :value="kp">{{ kp }}</option>
        </select>
        <button @click="fetchQuestions" class="px-4 py-2 rounded-xl bg-indigo-500 text-white text-sm">筛选</button>
        <button @click="resetFilters" class="px-4 py-2 rounded-xl border border-gray-200 text-gray-600 text-sm">重置</button>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="space-y-4">
      <div v-for="(question, index) in questions" :key="question.id" class="bg-white dark:bg-gray-800/50 rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
        <div class="p-6">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3 flex-wrap">
                <span class="text-gray-500 text-sm">#{{ question.id }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs" :class="getTypeClass(question.questionType)">
                  {{ question.questionType }}
                </span>
                <span class="px-2 py-0.5 rounded-full bg-gray-100 dark:bg-black text-gray-600 text-xs">
                  {{ question.subjectName }}
                </span>
                <span v-if="question.knowledgePoint || question.knowledge_point" class="px-2 py-0.5 rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs">
                  📌 {{ question.knowledgePoint || question.knowledge_point }}
                </span>
                <span class="px-2 py-0.5 rounded-full bg-gray-100 dark:bg-black text-gray-600 text-xs">
                  {{ question.categoryLevel }}
                </span>
                <span class="text-sm text-gray-500">{{ question.difficultyLevel }}</span>
              </div>
              <p class="text-gray-800 dark:text-gray-200 mb-3">{{ question.questionTitle }}</p>
              <div v-if="question.options" class="space-y-1 text-sm text-gray-600 dark:text-gray-400">
                <div v-for="(opt, idx) in parseOptions(question.options)" :key="idx">{{ opt }}</div>
              </div>
              <div class="mt-3 space-y-2">
                <div class="p-3 rounded-xl bg-green-50 dark:bg-black">
                  <p class="text-sm"><span class="font-medium">正确答案：</span>{{ question.correctAnswer }}</p>
                </div>
                <div v-if="question.answerAnalysis" class="p-3 rounded-xl bg-indigo-50 dark:bg-indigo-950/20">
                  <p class="text-sm"><span class="font-medium">答案解析：</span>{{ question.answerAnalysis }}</p>
                </div>
                <div class="flex gap-4 text-xs text-gray-500">
                  <span>使用次数：{{ question.useCount }}</span>
                  <span>错误次数：{{ question.mistakeCount }}</span>
                  <span>错误率：{{ question.mistakeRate }}%</span>
                </div>
              </div>
            </div>
            <div class="flex gap-2 ml-4">
              <button @click="editQuestion(question)" class="p-2 rounded-lg text-blue-500 hover:bg-blue-50 transition">✏️</button>
              <button @click="deleteQuestion(question.id)" class="p-2 rounded-lg text-red-500 hover:bg-red-50 transition">🗑️</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加题目弹窗 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAddModal = false">
      <div class="bg-white dark:bg-black rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-800 dark:text-gray-200 mb-4">添加题目</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">科目</label>
              <input
                v-model="newQuestion.subjectName"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"
                placeholder="请输入科目，如：数学、语文、英语"
              >
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">知识点</label>
              <input
                v-model="newQuestion.knowledgePoint"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"
                placeholder="请输入知识点，如：函数、三角函数"
              >
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">题型</label>
              <select v-model="newQuestion.questionType" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                <option v-for="type in questionTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">分类</label>
              <select v-model="newQuestion.categoryLevel" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">题目内容</label>
              <textarea v-model="newQuestion.questionTitle" rows="3" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"></textarea>
            </div>
            <div v-if="newQuestion.questionType !== '填空' && newQuestion.questionType !== '解答'">
              <label class="block text-sm font-medium mb-1">选项（点击选择正确答案）</label>

              <!-- 判断题专用 -->
              <div v-if="newQuestion.questionType === '判断'" class="space-y-2">
                <div
                  v-for="(label, index) in optionLabels.slice(0, 2)"
                  :key="index"
                  @click="selectCorrectAnswer(label)"
                  class="flex gap-2 p-2 rounded-xl cursor-pointer transition-all"
                  :class="newQuestion.correctAnswer === label
                    ? 'bg-green-50 dark:bg-green-900/30 border-2 border-green-500'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700/50 border-2 border-transparent'"
                >
                  <span class="flex items-center justify-center w-8 h-8 rounded-xl text-sm font-medium"
                    :class="newQuestion.correctAnswer === label
                      ? 'bg-green-500 text-white'
                      : 'bg-gray-100 dark:bg-gray-700'">
                    {{ label }}
                  </span>
                  <input
                    v-model="newQuestion.optionInputs[index]"
                    class="flex-1 px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"
                    :placeholder="index === 0 ? '正确' : '错误'"
                    @click.stop
                  >
                  <span v-if="newQuestion.correctAnswer === label" class="flex items-center text-green-500 text-xl">✓</span>
                </div>
              </div>

              <!-- 选择题专用 -->
              <div v-else class="space-y-2">
                <div
                  v-for="(label, index) in optionLabels.slice(0, 4)"
                  :key="index"
                  @click="selectCorrectAnswer(label)"
                  class="flex gap-2 p-2 rounded-xl cursor-pointer transition-all"
                  :class="isAnswerSelected(label)
                    ? 'bg-green-50 dark:bg-green-900/30 border-2 border-green-500'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700/50 border-2 border-transparent'"
                >
                  <span class="flex items-center justify-center w-8 h-8 rounded-xl text-sm font-medium"
                    :class="isAnswerSelected(label)
                      ? 'bg-green-500 text-white'
                      : 'bg-gray-100 dark:bg-gray-700'">
                    {{ label }}
                  </span>
                  <input
                    v-model="newQuestion.optionInputs[index]"
                    class="flex-1 px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"
                    :placeholder="`请输入选项 ${label} 的内容`"
                    @click.stop
                  >
                  <span v-if="isAnswerSelected(label)" class="flex items-center text-green-500 text-xl">✓</span>
                </div>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">正确答案</label>

              <!-- 填空题和解答题：文本输入 -->
              <input
                v-if="newQuestion.questionType === '填空' || newQuestion.questionType === '解答'"
                v-model="newQuestion.correctAnswer"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"
                placeholder="请输入正确答案"
              >

              <!-- 选择题：显示已选答案 -->
              <div v-else class="px-4 py-2 rounded-xl bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-700">
                <span class="text-gray-500 dark:text-gray-400">
                  {{ newQuestion.correctAnswer || '请点击上方选项选择正确答案' }}
                </span>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">答案解析</label>
              <textarea v-model="newQuestion.answerAnalysis" rows="2" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">难度</label>
              <select v-model="newQuestion.difficultyLevel" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                <option value="简单">简单</option>
                <option value="中等">中等</option>
                <option value="困难">困难</option>
              </select>
            </div>
          </div>
          <div class="flex gap-3 mt-6">
            <button @click="showAddModal = false" class="flex-1 px-4 py-2 rounded-xl border border-gray-200">取消</button>
            <button @click="addQuestion" class="flex-1 px-4 py-2 rounded-xl bg-indigo-500 text-white">确认添加</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const questions = ref([])
const showAddModal = ref(false)
const filters = reactive({ categoryLevel: '', subjectName: '', questionType: '', knowledgePoint: '' })
const categories = ref(['高中', '大学', '考公', '考研', '考证'])
const subjects = ref([])
const questionTypes = ref([])
const knowledgePoints = ref([])

const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F']

const newQuestion = ref({
  subjectName: '',
  knowledgePoint: '',
  questionType: '单选',
  categoryLevel: '高中',
  questionTitle: '',
  options: '',
  optionInputs: ['', '', '', ''], // 用于输入A、B、C、D
  correctAnswer: '',
  answerAnalysis: '',
  difficultyLevel: '中等'
})

const totalQuestions = computed(() => questions.value.length)

const fetchFilters = async () => {
  try {
    const res = await request.get('/question/filters')
    if (res.code === 200 && res.data) {
      subjects.value = res.data.subjects || []
      questionTypes.value = res.data.questionTypes || []
      knowledgePoints.value = res.data.knowledgePoints || []
    }
  } catch (error) {
    console.error('获取筛选条件失败', error)
  }
}

const fetchQuestions = async () => {
  try {
    const res = await request.get('/question/list', { params: filters })
    if (res.code === 200) questions.value = res.data || []
  } catch (error) {
    console.error('获取题目失败', error)
  }
}

const resetFilters = () => {
  filters.categoryLevel = ''
  filters.subjectName = ''
  filters.questionType = ''
  filters.knowledgePoint = ''
  fetchQuestions()
}

const parseOptions = (options) => {
  if (!options) return []
  try {
    return JSON.parse(options)
  } catch {
    return options.split(',').map(o => o.trim())
  }
}

const getTypeClass = (type) => {
  const map = {
    '单选': 'bg-blue-100 text-blue-600',
    '多选': 'bg-purple-100 text-purple-600',
    '判断': 'bg-emerald-100 text-emerald-600',
    '填空': 'bg-amber-100 text-amber-600',
    '解答': 'bg-rose-100 text-rose-600'
  }
  return map[type] || 'bg-gray-100 text-gray-600'
}

// 格式化选项为 ["A. xxx", "B. xxx"] 格式
const formatOptions = () => {
  const formatted = []
  for (let i = 0; i < newQuestion.value.optionInputs.length; i++) {
    const input = newQuestion.value.optionInputs[i].trim()
    if (input) {
      formatted.push(`${optionLabels[i]}. ${input}`)
    }
  }
  newQuestion.value.options = JSON.stringify(formatted)
}

// 重置选项输入
const resetOptionInputs = () => {
  newQuestion.value.optionInputs = ['', '', '', '']
}

// 选择正确答案
const selectCorrectAnswer = (label) => {
  if (newQuestion.value.questionType === '多选') {
    // 多选题：支持多选
    let answers = newQuestion.value.correctAnswer ? newQuestion.value.correctAnswer.split(',') : []
    const index = answers.indexOf(label)
    if (index > -1) {
      answers.splice(index, 1)
    } else {
      answers.push(label)
    }
    newQuestion.value.correctAnswer = answers.sort().join(',')
  } else {
    // 单选题和判断题：单选
    newQuestion.value.correctAnswer = label
  }
}

// 检查某个选项是否被选中为正确答案
const isAnswerSelected = (label) => {
  if (newQuestion.value.questionType === '多选') {
    const answers = newQuestion.value.correctAnswer ? newQuestion.value.correctAnswer.split(',') : []
    return answers.includes(label)
  } else {
    return newQuestion.value.correctAnswer === label
  }
}

const addQuestion = async () => {
  try {
    // 如果是选择题，先格式化选项
    if (newQuestion.value.questionType !== '填空' && newQuestion.value.questionType !== '解答') {
      formatOptions()
    }

    const res = await request.post('/question/add', newQuestion.value)
    if (res.code === 200) {
      showAddModal.value = false
      fetchQuestions()
      newQuestion.value = {
        subjectName: '',
        knowledgePoint: '',
        questionType: '单选',
        categoryLevel: '高中',
        questionTitle: '',
        options: '',
        optionInputs: ['', '', '', ''],
        correctAnswer: '',
        answerAnalysis: '',
        difficultyLevel: '中等'
      }
    }
  } catch (error) {
    console.error('添加失败', error)
  }
}

const deleteQuestion = async (id) => {
  if (confirm('确定删除这道题吗？')) {
    await request.delete(`/question/delete/${id}`)
    fetchQuestions()
  }
}

const editQuestion = (question) => {
  // 编辑逻辑
  console.log('编辑', question)
}

onMounted(() => {
  fetchFilters()
  fetchQuestions()
})
</script>
