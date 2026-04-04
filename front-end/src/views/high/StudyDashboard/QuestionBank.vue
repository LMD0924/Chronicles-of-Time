<template>
  <div class="space-y-6">
    <!-- 头部 -->
    <div class="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-purple-500/10">
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
    <div class="bg-white dark:bg-gray-800/50 rounded-2xl p-4 border border-gray-200/50 dark:border-gray-700/50">
      <div class="flex flex-wrap gap-3">
        <select v-model="filters.categoryLevel" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm">
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
                <span class="px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 text-xs">
                  {{ question.subjectName }}
                </span>
                <span class="px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 text-xs">
                  {{ question.categoryLevel }}
                </span>
                <span class="text-sm text-gray-500">{{ question.difficultyLevel }}</span>
              </div>
              <p class="text-gray-800 dark:text-gray-200 mb-3">{{ question.questionTitle }}</p>
              <div v-if="question.options" class="space-y-1 text-sm text-gray-600 dark:text-gray-400">
                <div v-for="(opt, idx) in parseOptions(question.options)" :key="idx">{{ opt }}</div>
              </div>
              <div class="mt-3 space-y-2">
                <div class="p-3 rounded-xl bg-green-50 dark:bg-green-950/20">
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
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-800 dark:text-gray-200 mb-4">添加题目</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">科目</label>
              <select v-model="newQuestion.subjectName" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
              </select>
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
              <label class="block text-sm font-medium mb-1">选项 (JSON格式)</label>
              <textarea v-model="newQuestion.options" rows="3" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder='["A. 选项1", "B. 选项2"]'></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">正确答案</label>
              <input v-model="newQuestion.correctAnswer" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
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
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const questions = ref([])
const showAddModal = ref(false)
const filters = reactive({ categoryLevel: '', subjectName: '', questionType: '' })
const categories = ref(['高中', '大学', '考公', '考研', '考证'])
const subjects = ref([])
const questionTypes = ref([])

const newQuestion = ref({
  subjectName: '',
  questionType: '单选',
  categoryLevel: '高中',
  questionTitle: '',
  options: '',
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

const addQuestion = async () => {
  try {
    const res = await request.post('/question/add', newQuestion.value)
    if (res.code === 200) {
      showAddModal.value = false
      fetchQuestions()
      newQuestion.value = {
        subjectName: '',
        questionType: '单选',
        categoryLevel: '高中',
        questionTitle: '',
        options: '',
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
