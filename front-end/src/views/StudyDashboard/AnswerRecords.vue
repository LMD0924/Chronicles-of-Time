<template>
  <div class="space-y-6 max-w-[1400px] mx-auto px-4 lg:px-6 mt-24">
    <!-- 页面头部 -->
    <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-blue-500/10">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 via-indigo-500 to-purple-500 flex items-center justify-center shadow-lg shadow-blue-500/30">
          <span class="text-white text-2xl">📊</span>
        </div>
        <div>
          <h2 class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">答题记录</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">✨ 查看历史答题记录，分析学习情况</p>
        </div>
      </div>
    </div>

    <!-- 三栏布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- 左侧：筛选条件 -->
      <div class="lg:col-span-2">
        <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 sticky top-24 shadow-2xl shadow-blue-500/5">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-5">🔍 筛选条件</h3>
          <div class="space-y-5">
            <div>
              <label class="block text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">科目</label>
              <select v-model="filters.subjectName" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
                <option value="">全部科目</option>
                <option v-for="subject in filterSubjects" :key="subject" :value="subject">{{ subject }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">题型</label>
              <select v-model="filters.questionType" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
                <option value="">全部题型</option>
                <option v-for="type in filterQuestionTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">答题结果</label>
              <select v-model="filters.isCorrect" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
                <option value="">全部结果</option>
                <option value="1">✅ 正确</option>
                <option value="0">❌ 错误</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">知识点</label>
              <select v-model="filters.knowledgePoint" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
                <option value="">全部知识点</option>
                <option v-for="point in knowledgePoints" :key="point" :value="point">{{ point }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">日期范围</label>
              <div class="flex flex-col gap-2">
                <input v-model="filters.startDate" type="date" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
                <input v-model="filters.endDate" type="date" class="w-full px-5 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-sm shadow-sm hover:shadow-md transition-all focus:ring-2 focus:ring-blue-400/50">
              </div>
            </div>
            <div class="flex gap-3 pt-2">
              <button @click="fetchRecords" class="flex-1 px-6 py-2.5 rounded-2xl bg-gradient-to-r from-blue-500 via-indigo-500 to-purple-500 text-white text-sm font-semibold shadow-lg shadow-blue-500/30 hover:shadow-xl hover:shadow-blue-500/40 transform hover:-translate-y-0.5 transition-all">
                🔍 查询
              </button>
              <button @click="resetFilters" class="flex-1 px-6 py-2.5 rounded-2xl border-0 bg-white/60 dark:bg-black backdrop-blur-sm text-gray-600 dark:text-gray-400 text-sm font-semibold shadow-sm hover:shadow-md transition-all">
                🔄 重置
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：答题记录列表（题目形式展示） -->
      <div class="lg:col-span-8">
        <!-- 统计信息 -->
        <div v-if="statistics.total > 0" class="grid grid-cols-2 gap-4 mb-6">
          <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-5 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-blue-500/5">
            <div class="text-3xl mb-2">📝</div>
            <div class="text-3xl font-bold bg-gradient-to-r from-gray-700 to-gray-900 dark:from-white dark:to-gray-200 bg-clip-text text-transparent">{{ statistics.total }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium">总答题数</div>
          </div>
          <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-5 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-emerald-500/5">
            <div class="text-3xl mb-2">✅</div>
            <div class="text-3xl font-bold bg-gradient-to-r from-emerald-600 to-green-600 dark:from-emerald-400 dark:to-green-400 bg-clip-text text-transparent">{{ statistics.correct }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium">正确数</div>
          </div>
          <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-5 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-rose-500/5">
            <div class="text-3xl mb-2">❌</div>
            <div class="text-3xl font-bold bg-gradient-to-r from-red-600 to-rose-600 dark:from-red-400 dark:to-rose-400 bg-clip-text text-transparent">{{ statistics.wrong }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium">错误数</div>
          </div>
          <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-5 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-indigo-500/5">
            <div class="text-3xl mb-2">📈</div>
            <div class="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-400 dark:to-purple-400 bg-clip-text text-transparent">{{ statistics.accuracy }}%</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium">正确率</div>
          </div>
        </div>

        <!-- 答题记录列表（题目卡片形式） -->
        <div class="bg-white dark:bg-black rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
          <div class="p-4 border-b border-gray-200/50 dark:border-gray-700/50">
            <h3 class="font-semibold text-gray-800 dark:text-gray-200">答题记录</h3>
          </div>

          <div v-if="loading" class="flex justify-center py-20">
            <div class="text-center">
              <div class="w-12 h-12 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto"></div>
              <p class="text-gray-500 mt-4">加载中...</p>
            </div>
          </div>

          <div v-else-if="records.length === 0" class="text-center py-20">
            <span class="text-6xl opacity-50">📋</span>
            <p class="text-gray-500 mt-4">暂无答题记录</p>
          </div>

          <div v-else class="space-y-4 p-4 max-h-[800px] overflow-y-auto">
            <div v-for="(record, index) in records" :key="record.id" class="bg-gray-50 dark:bg-black rounded-xl p-4 border border-gray-200/50 dark:border-gray-700/50">
              <!-- 题号 -->
              <div class="flex items-center gap-2 mb-3">
                <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold" :class="record.is_correct === 1 ? 'bg-green-100 text-green-600 dark:bg-black dark:text-green-400' : 'bg-red-100 text-red-600 dark:bg-black dark:text-red-400'">
                  {{ (filters.pageNum - 1) * filters.pageSize + index + 1 }}
                </span>
                <span class="px-2 py-1 rounded-lg text-xs font-medium" :class="getTypeClass(record.question_type)">
                  {{ record.question_type }}
                </span>
                <span class="text-xs text-gray-500">{{ record.subject_name }}</span>
                <span v-if="record.knowledge_point" class="px-2 py-0.5 rounded-full bg-blue-100 dark:bg-black text-blue-600 dark:text-blue-400 text-xs">
                  📌 {{ record.knowledge_point }}
                </span>
              </div>

              <!-- 题目 -->
              <div class="mb-3">
                <p class="text-gray-800 dark:text-gray-200 font-medium">{{ record.question_title }}</p>
              </div>

              <!-- 选项 -->
              <div v-if="record.options" class="mb-3 space-y-1">
                <div v-for="(option, optIndex) in parseOptions(record.options)" :key="optIndex" class="text-sm text-gray-600 dark:text-gray-400 pl-4" :class="{ 'text-green-600 dark:text-green-400 font-medium': isCorrectOption(option, record.correct_answer), 'text-red-600 dark:text-red-400': isWrongUserOption(option, record.user_answer, record.correct_answer) }">
                  {{ option }}
                  <span v-if="isCorrectOption(option, record.correct_answer)" class="ml-2 text-xs">✓ 正确答案</span>
                  <span v-if="isWrongUserOption(option, record.user_answer, record.correct_answer)" class="ml-2 text-xs">✗ 你的选择</span>
                </div>
              </div>

              <!-- 答案对比 -->
              <div class="flex gap-4 text-sm mb-3">
                <div class="flex-1">
                  <span class="text-gray-500">你的答案：</span>
                  <span :class="record.is_correct === 1 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">{{ record.user_answer || '未作答' }}</span>
                </div>
                <div class="flex-1">
                  <span class="text-gray-500">正确答案：</span>
                  <span class="text-green-600 dark:text-green-400">{{ record.correct_answer }}</span>
                </div>
              </div>

              <!-- 解析 -->
              <div v-if="record.answer_analysis" class="bg-white dark:bg-black rounded-lg p-3 text-sm">
                <p class="text-gray-500 mb-1">解析：</p>
                <p class="text-gray-700 dark:text-gray-300">{{ record.answer_analysis }}</p>
              </div>

              <!-- 答题信息 -->
              <div class="flex justify-between items-center mt-3 pt-3 border-t border-gray-200/50 dark:border-gray-700/50 text-xs text-gray-500">
                <span>得分：{{ record.score }}分</span>
                <span>{{ record.answer_date }}</span>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="records.length > 0" class="p-4 border-t border-gray-200/50 dark:border-gray-700/50 flex justify-between items-center">
            <div class="text-sm text-gray-600 dark:text-gray-400">
              共 {{ statistics.total }} 条记录
            </div>
            <div class="flex gap-2">
              <button @click="prevPage" :disabled="filters.pageNum === 1" class="px-3 py-1 rounded-lg border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 text-sm disabled:opacity-50 disabled:cursor-not-allowed">
                上一页
              </button>
              <span class="px-3 py-1 rounded-lg bg-gray-100 dark:bg-black text-gray-600 dark:text-gray-400 text-sm">
                {{ filters.pageNum }}
              </span>
              <button @click="nextPage" :disabled="records.length < filters.pageSize" class="px-3 py-1 rounded-lg border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 text-sm disabled:opacity-50 disabled:cursor-not-allowed">
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：知识点统计 -->
      <div class="lg:col-span-2">
        <div class="bg-white dark:bg-black rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50 sticky top-24">
          <h3 class="font-semibold text-gray-800 dark:text-gray-200 mb-4">知识点统计</h3>
          <div v-if="knowledgePointStats.length > 0" class="space-y-3">
            <div v-for="stat in knowledgePointStats" :key="stat.knowledgePoint" class="flex justify-between items-center p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/30">
              <span class="text-sm text-gray-600 dark:text-gray-300">{{ stat.knowledgePoint || '未分类' }}</span>
              <div class="flex items-center gap-2">
                <span class="text-xs text-green-600">{{ stat.correct }}</span>
                <span class="text-xs text-gray-400">/</span>
                <span class="text-xs text-red-600">{{ stat.wrong }}</span>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-gray-500">
            暂无统计数据
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

// 状态变量
const loading = ref(false)
const records = ref([])

// 筛选条件
const filters = reactive({
  userId: props.studentId || 1,
  subjectName: '',
  questionType: '',
  isCorrect: '',
  knowledgePoint: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10
})

// 筛选选项
const filterSubjects = ref([])
const filterQuestionTypes = ref([])
const knowledgePoints = ref([])

// 统计信息
const statistics = reactive({
  total: 0,
  correct: 0,
  wrong: 0,
  accuracy: 0
})

// 知识点统计
const knowledgePointStats = ref([])

// 获取题型样式
const getTypeClass = (type) => {
  const map = {
    '单选': 'bg-blue-100 text-blue-600 dark:bg-black dark:text-blue-400',
    '多选': 'bg-purple-100 text-purple-600 dark:bg-black dark:text-purple-400',
    '判断': 'bg-yellow-100 text-yellow-600 dark:bg-black dark:text-yellow-400',
    '填空': 'bg-green-100 text-green-600 dark:bg-black dark:text-green-400',
    '解答': 'bg-red-100 text-red-600 dark:bg-black dark:text-red-400'
  }
  return map[type] || 'bg-gray-100 text-gray-600 dark:bg-black dark:text-gray-400'
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return options.split(',').map(o => o.trim())
  }
}

// 判断是否为正确选项
const isCorrectOption = (option, correctAnswer) => {
  if (!correctAnswer || !option) return false
  const optionLetter = option.charAt(0)
  return correctAnswer.includes(optionLetter)
}

// 判断是否为用户的错误选项
const isWrongUserOption = (option, userAnswer, correctAnswer) => {
  if (!userAnswer || !option || !correctAnswer) return false
  const optionLetter = option.charAt(0)
  return userAnswer.includes(optionLetter) && !correctAnswer.includes(optionLetter)
}

// 获取筛选条件
const fetchFilters = async () => {
  try {
    const res = await request.get('/question/filters')
    if (res.code === 200 && res.data) {
      filterSubjects.value = res.data.subjects || []
      filterQuestionTypes.value = res.data.questionTypes || []
      knowledgePoints.value = res.data.knowledgePoints || []
    }
  } catch (error) {
    console.error('获取筛选条件失败', error)
  }
}

// 获取答题记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      ...filters,
      userId: filters.userId || 1
    }
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const res = await request.get('/question/answer-records', params)
    if (res.code === 200) {
      records.value = res.data || []
      // 计算统计信息
      calculateStatistics()
      // 计算知识点统计
      calculateKnowledgePointStats()
    }
  } catch (error) {
    console.error('获取答题记录失败', error)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 计算统计信息
const calculateStatistics = () => {
  const total = records.value.length
  const correct = records.value.filter(r => r.is_correct === 1).length
  const wrong = total - correct
  const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0

  statistics.total = total
  statistics.correct = correct
  statistics.wrong = wrong
  statistics.accuracy = accuracy
}

// 计算知识点统计
const calculateKnowledgePointStats = () => {
  const stats = {}
  records.value.forEach(record => {
    const point = record.knowledge_point || '未分类'
    if (!stats[point]) {
      stats[point] = { correct: 0, wrong: 0 }
    }
    if (record.is_correct === 1) {
      stats[point].correct++
    } else {
      stats[point].wrong++
    }
  })
  knowledgePointStats.value = Object.entries(stats).map(([knowledgePoint, data]) => ({
    knowledgePoint,
    ...data
  }))
}

// 重置筛选条件
const resetFilters = () => {
  filters.subjectName = ''
  filters.questionType = ''
  filters.isCorrect = ''
  filters.knowledgePoint = ''
  filters.startDate = ''
  filters.endDate = ''
  filters.pageNum = 1
  fetchRecords()
}

// 上一页
const prevPage = () => {
  if (filters.pageNum > 1) {
    filters.pageNum--
    fetchRecords()
  }
}

// 下一页
const nextPage = () => {
  filters.pageNum++
  fetchRecords()
}

// 页面加载时获取数据
onMounted(() => {
  fetchFilters()
  fetchRecords()
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
