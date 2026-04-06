<template>
  <div class="space-y-6 max-w-[1400px] mx-auto px-4 lg:px-6 mt-24">
    <!-- 头部 -->
    <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-rose-500/10">
      <div class="flex flex-wrap justify-between items-center gap-4">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-rose-500 via-red-500 to-orange-500 flex items-center justify-center shadow-lg shadow-rose-500/30">
            <span class="text-white text-2xl">📖</span>
          </div>
          <div>
            <h2 class="text-2xl font-bold bg-gradient-to-r from-rose-600 to-red-600 bg-clip-text text-transparent">错题本</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">✨ 记录你的每一次进步</p>
          </div>
        </div>
        <button @click="showAddModal = true" class="px-6 py-2.5 rounded-2xl bg-gradient-to-r from-rose-500 via-red-500 to-orange-500 text-white text-sm font-medium shadow-lg shadow-rose-500/30 hover:shadow-xl hover:shadow-rose-500/40 transform hover:-translate-y-0.5 transition-all">
          ➕ 添加错题
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white dark:bg-black rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">错题总数</p>
            <p class="text-3xl font-bold text-gray-800 dark:text-gray-200">{{ mistakeList.length }}</p>
          </div>
          <span class="text-3xl">❌</span>
        </div>
      </div>
      <div class="bg-white dark:bg-black rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">未掌握</p>
            <p class="text-3xl font-bold text-red-500">{{ unmasteredCount }}</p>
          </div>
          <span class="text-3xl">⚠️</span>
        </div>
      </div>
      <div class="bg-white dark:bg-black rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">已掌握</p>
            <p class="text-3xl font-bold text-green-500">{{ masteredCount }}</p>
          </div>
          <span class="text-3xl">✅</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white dark:bg-black rounded-2xl p-4 border border-gray-200/50 dark:border-gray-700/50">
      <div class="space-y-4">
        <!-- 科目筛选 -->
        <div class="space-y-2">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300">科目</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="subject in ['全部', ...filterSubjects]"
              :key="subject"
              @click="selectSubject(subject === '全部' ? '' : subject)"
              :class="[
                'px-3 py-1.5 rounded-full text-sm transition-all',
                (subject === '全部' && !filters.subject) || (filters.subject === subject)
                  ? 'bg-gradient-to-r from-rose-500 to-red-500 text-white shadow-md'
                  : 'bg-gray-100 dark:bg-black text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-black'
              ]"
            >
              {{ subject }}
            </button>
          </div>
        </div>

        <!-- 知识点筛选 -->
        <div class="space-y-2" v-if="filterKnowledgePoints.length > 0">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300">知识点</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="kp in ['全部', ...filterKnowledgePoints]"
              :key="kp"
              @click="selectKnowledgePoint(kp === '全部' ? '' : kp)"
              :class="[
                'px-3 py-1.5 rounded-full text-sm transition-all',
                (kp === '全部' && !filters.knowledgePoint) || (filters.knowledgePoint === kp)
                  ? 'bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow-md'
                  : 'bg-gray-100 dark:bg-black text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-black'
              ]"
            >
              {{ kp }}
            </button>
          </div>
        </div>

        <!-- 掌握状态筛选 -->
        <div class="space-y-2">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300">状态</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="status in [
                { label: '全部', value: '' },
                { label: '未掌握', value: 'false' },
                { label: '已掌握', value: 'true' }
              ]"
              :key="status.value"
              @click="selectMastered(status.value)"
              :class="[
                'px-3 py-1.5 rounded-full text-sm transition-all',
                filters.mastered === status.value
                  ? 'bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-md'
                  : 'bg-gray-100 dark:bg-black text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-black'
              ]"
            >
              {{ status.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 错题列表 -->
    <div class="space-y-4">
      <div v-for="mistake in filteredMistakes" :key="mistake.id" class="bg-white dark:bg-gray-800/50 rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
        <div class="p-6">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3 flex-wrap">
                <span class="font-medium text-gray-800 dark:text-gray-200">{{ mistake.mistakeName }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs" :class="getTypeClass(mistake.mistakeType)">
                  {{ mistake.mistakeType }}
                </span>
                <span v-if="!mistake.mastered" class="px-2 py-0.5 rounded-full text-xs bg-red-100 dark:bg-red-900/50 text-red-600">未掌握</span>
                <span v-else class="px-2 py-0.5 rounded-full text-xs bg-green-100 dark:bg-green-900/50 text-green-600">已掌握</span>
              </div>
              <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">
                科目：{{ mistake.subjectName }}
                <span v-if="mistake.knowledgePoint || mistake.knowledge_point" class="ml-3">
                  <span class="text-blue-600 dark:text-blue-400">📌 {{ mistake.knowledgePoint || mistake.knowledge_point }}</span>
                </span>
                | 日期：{{ mistake.mistakeDate }}
              </p>

              <!-- 题目选项 -->
              <div v-if="mistake.questionOptions" class="mb-3">
                <p class="text-gray-500 text-sm mb-2">题目选项：</p>
                <div class="space-y-1">
                  <div v-for="(option, idx) in parseOptions(mistake.questionOptions)" :key="idx" class="text-sm text-gray-600 dark:text-gray-400 pl-4">
                    {{ option }}
                  </div>
                </div>
              </div>

              <div class="space-y-2 mt-3">
                <p class="text-red-600 dark:text-red-400"><span class="text-gray-500">❌ 错答：</span>{{ mistake.wrongAnswer || mistake.studentChoice }}</p>
                <p class="text-green-600 dark:text-green-400"><span class="text-gray-500">✅ 正解：</span>{{ mistake.correctAnswer }}</p>
              </div>
              <div v-if="mistake.answerAnalysis" class="mt-3 p-3 rounded-xl bg-indigo-50 dark:bg-indigo-950/30 text-sm text-gray-600 dark:text-gray-400">
                📝 {{ mistake.answerAnalysis }}
              </div>
            </div>
            <div class="flex flex-col gap-2 ml-4">
              <button @click="reviewMistake(mistake.id)" class="px-3 py-1.5 rounded-lg bg-blue-500 text-white text-sm hover:bg-blue-600 transition">复习</button>
              <button
                v-if="!mistake.mastered"
                @click="markMastered(mistake.id)"
                class="px-3 py-1.5 rounded-lg bg-green-500 text-white text-sm hover:bg-green-600 transition">标记掌握</button>
              <button
                v-else
                @click="markUnmastered(mistake.id)"
                class="px-3 py-1.5 rounded-lg bg-yellow-500 text-white text-sm hover:bg-yellow-600 transition">取消掌握</button>
              <button @click="deleteMistake(mistake.id)" class="px-3 py-1.5 rounded-lg bg-red-500 text-white text-sm hover:bg-red-600 transition">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredMistakes.length === 0" class="text-center py-12">
      <span class="text-5xl opacity-50">📖</span>
      <p class="text-gray-500 mt-3">暂无错题记录</p>
    </div>

    <!-- 添加错题弹窗 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAddModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-800 dark:text-gray-200 mb-4">添加错题</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">错题名称</label>
              <input v-model="newMistake.mistakeName" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入错题名称">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">科目</label>
              <input v-model="newMistake.subjectName" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入科目，如：数学、语文">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">知识点</label>
              <input v-model="newMistake.knowledgePoint" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入知识点，如：函数、三角函数">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">题型</label>
              <select v-model="newMistake.mistakeType" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                <option value="单选">单选</option>
                <option value="多选">多选</option>
                <option value="填空">填空</option>
                <option value="解答">解答</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">错答</label>
              <input v-model="newMistake.wrongAnswer" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入错误答案">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">正确答案</label>
              <input v-model="newMistake.correctAnswer" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入正确答案">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">题目选项 (JSON格式)</label>
              <textarea v-model="newMistake.questionOptions" rows="3" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder='["A. 选项1", "B. 选项2"]'></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">答案解析</label>
              <textarea v-model="newMistake.answerAnalysis" rows="2" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800" placeholder="请输入答案解析"></textarea>
            </div>
          </div>
          <div class="flex gap-3 mt-6">
            <button @click="showAddModal = false" class="flex-1 px-4 py-2 rounded-xl border border-gray-200">取消</button>
            <button @click="addMistake" class="flex-1 px-4 py-2 rounded-xl bg-red-500 text-white">确认添加</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const mistakeList = ref([])
const filters = reactive({ subject: '', mastered: '', knowledgePoint: '' })
const filterSubjects = ref([])
const filterKnowledgePoints = ref([])
const showAddModal = ref(false)
const newMistake = ref({
  mistakeName: '',
  subjectName: '',
  knowledgePoint: '',
  mistakeType: '单选',
  wrongAnswer: '',
  correctAnswer: '',
  questionOptions: '',
  answerAnalysis: ''
})

const filteredMistakes = computed(() => {
  let list = mistakeList.value
  if (filters.subject) list = list.filter(m => m.subjectName === filters.subject)
  if (filters.mastered !== '') list = list.filter(m => m.mastered === (filters.mastered === 'true'))
  if (filters.knowledgePoint) list = list.filter(m => (m.knowledgePoint || m.knowledge_point) === filters.knowledgePoint)
  return list
})

const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return options.split(',').map(o => o.trim())
  }
}

const unmasteredCount = computed(() => mistakeList.value.filter(m => !m.mastered).length)
const masteredCount = computed(() => mistakeList.value.filter(m => m.mastered).length)

const fetchFilters = async () => {
  try {
    const res = await request.get(`/mistake/filters/${props.studentId}`)
    if (res.code === 200 && res.data) {
      filterSubjects.value = res.data.subjects || []
      filterKnowledgePoints.value = res.data.knowledgePoints || []
    }
  } catch (error) {
    console.error('获取筛选条件失败', error)
  }
}

const fetchMistakes = async () => {
  try {
    const params = {}
    if (filters.subject) params.subjectName = filters.subject
    if (filters.mastered !== '') params.mastered = filters.mastered === 'true'
    if (filters.knowledgePoint) params.knowledgePoint = filters.knowledgePoint

    const res = await request.get(`/mistake/list/${props.studentId}`, params)
    if (res.code === 200) mistakeList.value = res.data || []
  } catch (error) {
    console.error('获取错题失败', error)
  }
}

const selectSubject = (subject) => {
  filters.subject = subject
  fetchMistakes()
}

const selectMastered = (mastered) => {
  filters.mastered = mastered
  fetchMistakes()
}

const selectKnowledgePoint = (kp) => {
  filters.knowledgePoint = kp
  fetchMistakes()
}

const reviewMistake = async (id) => {
  try {
    const res = await request.put(`/mistake/review/${id}`)
    if (res.code === 200) {
      ElMessage.success('复习记录已更新')
      fetchMistakes()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('复习失败', error)
    ElMessage.error('更新失败，请稍后重试')
  }
}

const markMastered = async (id) => {
  try {
    const res = await request.put(`/mistake/master/${id}`)
    if (res.code === 200) {
      ElMessage.success('已标记为掌握')
      fetchMistakes()
    } else {
      ElMessage.error(res.message || '标记失败')
    }
  } catch (error) {
    console.error('标记掌握失败', error)
    ElMessage.error('标记失败，请稍后重试')
  }
}

const markUnmastered = async (id) => {
  try {
    const res = await request.put(`/mistake/unmaster/${id}`)
    if (res.code === 200) {
      ElMessage.success('已取消掌握')
      fetchMistakes()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    console.error('取消掌握失败', error)
    ElMessage.error('取消失败，请稍后重试')
  }
}

const deleteMistake = async (id) => {
  if (confirm('确定删除这条错题吗？')) {
    await request.delete(`/mistake/delete/${id}`)
    fetchMistakes()
  }
}
const addMistake = async () => {
  if (!newMistake.value.mistakeName || !newMistake.value.subjectName || !newMistake.value.correctAnswer) {
    ElMessage.warning('请填写必要信息')
    return
  }
  try {
    const res = await request.post('/mistake/add', {
      ...newMistake.value,
      userId: props.studentId
    })
    if (res.code === 200) {
      showAddModal.value = false
      fetchMistakes()
      newMistake.value = {
        mistakeName: '',
        subjectName: '',
        knowledgePoint: '',
        mistakeType: '单选',
        wrongAnswer: '',
        correctAnswer: '',
        questionOptions: '',
        answerAnalysis: ''
      }
      ElMessage.success('添加成功')
    }
  } catch (error) {
    console.error('添加失败', error)
    ElMessage.error('添加失败，请稍后重试')
  }
}
const resetFilters = () => {
  filters.subject = ''
  filters.mastered = ''
  filters.knowledgePoint = ''
  fetchMistakes()
}

const getTypeClass = (type) => {
  const map = {
    '单选': 'bg-blue-100 text-blue-600',
    '多选': 'bg-purple-100 text-purple-600',
    '填空': 'bg-amber-100 text-amber-600',
    '解答': 'bg-rose-100 text-rose-600'
  }
  return map[type] || 'bg-gray-100 text-gray-600'
}

onMounted(() => {
  fetchFilters()
  fetchMistakes()
})
</script>
