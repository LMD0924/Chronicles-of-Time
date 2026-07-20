<!--
  文件说明：用户私有题库管理页面。
-->
<template>
  <div class="space-y-6">
    <section class="app-card-surface p-6 border border-white/20 dark:border-gray-700/30">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">我的题库</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">题目只属于当前用户，审核通过后才会进入在线考试和错题练习。</p>
        </div>
        <button @click="openCreate" class="px-5 py-2.5 rounded-xl bg-brand-500 text-white font-medium">添加题目</button>
      </div>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div v-for="item in stats" :key="item.label" class="bg-white dark:bg-dark-surface rounded-2xl p-5 border border-gray-200/50 dark:border-gray-700/50">
        <p class="text-sm text-gray-500">{{ item.label }}</p>
        <p class="text-3xl font-bold mt-2" :class="item.class">{{ item.value }}</p>
      </div>
    </section>

    <section class="bg-white dark:bg-dark-surface rounded-2xl p-4 border border-gray-200/50 dark:border-gray-700/50">
      <div class="grid grid-cols-1 md:grid-cols-6 gap-3">
        <select v-model="filters.categoryLevel" class="form-control">
          <option value="">全部分类</option>
          <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="filters.subjectName" class="form-control" placeholder="科目 / 专业">
        <select v-model="filters.questionType" class="form-control">
          <option value="">全部题型</option>
          <option v-for="item in questionTypes" :key="item" :value="item">{{ item }}</option>
        </select>
        <select v-model="filters.difficultyLevel" class="form-control">
          <option value="">全部难度</option>
          <option value="简单">简单</option>
          <option value="中等">中等</option>
          <option value="困难">困难</option>
        </select>
        <select v-model="filters.auditStatus" class="form-control">
          <option value="">全部状态</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已驳回</option>
        </select>
        <div class="flex gap-2">
          <button @click="fetchQuestions" class="flex-1 rounded-xl bg-gray-900 text-white text-sm">查询</button>
          <button @click="resetFilters" class="px-4 rounded-xl border border-gray-200 text-sm">重置</button>
        </div>
      </div>
    </section>

    <section class="space-y-4">
      <article v-for="question in questions" :key="question.id" class="bg-white dark:bg-dark-surface rounded-2xl border border-gray-200/50 dark:border-gray-700/50 p-5">
        <div class="flex items-start justify-between gap-4">
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2 mb-3">
              <span class="badge">#{{ question.id }}</span>
              <span class="badge">{{ question.categoryLevel || '未分类' }}</span>
              <span class="badge">{{ question.subjectName || '未填科目' }}</span>
              <span class="badge">{{ question.questionType }}</span>
              <span class="badge">{{ question.difficultyLevel || '未标难度' }}</span>
              <span :class="auditClass(question.auditStatus)" class="px-2 py-1 rounded-full text-xs font-medium">{{ auditText(question.auditStatus) }}</span>
            </div>
            <h3 class="font-semibold text-gray-900 dark:text-gray-100 leading-relaxed">{{ question.questionTitle }}</h3>
            <div v-if="question.knowledgePoint" class="mt-2 flex flex-wrap gap-2">
              <span v-for="point in splitKnowledge(question.knowledgePoint)" :key="point" class="px-2 py-1 rounded-lg bg-blue-50 text-blue-600 text-xs">{{ point }}</span>
            </div>
            <div v-if="question.options" class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-2 text-sm text-gray-600 dark:text-gray-300">
              <div v-for="option in parseOptions(question.options)" :key="option" class="p-2 rounded-lg bg-gray-50 dark:bg-gray-800">{{ option }}</div>
            </div>
            <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
              <div class="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-300">答案：{{ question.correctAnswer }}</div>
              <div class="p-3 rounded-xl bg-gray-50 dark:bg-gray-800 text-gray-600 dark:text-gray-300">分值：{{ question.scoreValue || 2 }}</div>
            </div>
            <p v-if="question.answerAnalysis" class="mt-3 text-sm text-gray-600 dark:text-gray-300">解析：{{ question.answerAnalysis }}</p>
            <p v-if="question.auditRemark" class="mt-3 text-sm text-red-600">审核意见：{{ question.auditRemark }}</p>
          </div>
          <button @click="deleteQuestion(question.id)" class="px-3 py-1.5 rounded-lg border border-red-200 text-red-600 text-sm">删除</button>
        </div>
      </article>
      <div v-if="questions.length === 0" class="bg-white dark:bg-dark-surface rounded-2xl p-12 text-center text-gray-500">暂无题目</div>
    </section>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" @click.self="showModal = false">
      <div class="bg-white dark:bg-dark-surface rounded-2xl w-full max-w-3xl max-h-[92vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200/50 dark:border-gray-700/50">
          <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100">添加题目</h3>
        </div>
        <div class="p-6 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="form-label">第一层分类</label>
              <select v-model="form.categoryLevel" class="form-control">
                <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">科目 / 专业方向</label>
              <input v-model="form.subjectName" class="form-control" placeholder="如 数学、公务员行测、Java 面试">
            </div>
            <div>
              <label class="form-label">题型</label>
              <select v-model="form.questionType" class="form-control">
                <option v-for="item in questionTypes" :key="item" :value="item">{{ item }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">难度</label>
              <select v-model="form.difficultyLevel" class="form-control">
                <option value="简单">简单</option>
                <option value="中等">中等</option>
                <option value="困难">困难</option>
              </select>
            </div>
          </div>
          <div>
            <label class="form-label">知识点</label>
            <input v-model="form.knowledgePoint" class="form-control" placeholder="多个知识点用逗号分隔，例如 函数,导数,极值">
          </div>
          <div>
            <label class="form-label">题干</label>
            <textarea v-model="form.questionTitle" rows="4" class="form-control" placeholder="请输入题目内容"></textarea>
          </div>

          <div v-if="isChoiceType" class="space-y-3">
            <div class="flex items-center justify-between">
              <label class="form-label mb-0">选项</label>
              <button @click="addOption" class="text-sm text-brand-600">增加选项</button>
            </div>
            <div v-for="(option, index) in form.optionInputs" :key="index" class="grid grid-cols-[44px_minmax(0,1fr)_64px] gap-2">
              <span class="flex items-center justify-center rounded-xl bg-gray-100 dark:bg-gray-800 font-semibold">{{ optionLabels[index] }}</span>
              <input v-model="form.optionInputs[index]" class="form-control" :placeholder="`选项 ${optionLabels[index]}`">
              <button @click="toggleCorrect(optionLabels[index])" class="rounded-xl border text-sm" :class="isCorrect(optionLabels[index]) ? 'border-emerald-400 text-emerald-600 bg-emerald-50' : 'border-gray-200 text-gray-500'">
                答案
              </button>
            </div>
          </div>

          <div v-else>
            <label class="form-label">正确答案</label>
            <input v-model="form.correctAnswer" class="form-control" placeholder="请输入正确答案">
          </div>

          <div>
            <label class="form-label">答案解析</label>
            <textarea v-model="form.answerAnalysis" rows="3" class="form-control" placeholder="可填写解析，帮助复盘"></textarea>
          </div>
          <div>
            <label class="form-label">分值</label>
            <input v-model.number="form.scoreValue" type="number" min="1" max="100" class="form-control">
          </div>
        </div>
        <div class="p-6 border-t border-gray-200/50 dark:border-gray-700/50 flex gap-3">
          <button @click="showModal = false" class="flex-1 py-2 rounded-xl border border-gray-200 text-gray-600">取消</button>
          <button @click="saveQuestion" :disabled="saving" class="flex-1 py-2 rounded-xl bg-brand-500 text-white disabled:opacity-50">
            {{ saving ? '提交中...' : '提交审核' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  userId: [String, Number]
})

const categories = ['高中', '大学', '考公', '考研', '考证', '专业面试']
const questionTypes = ['单选', '多选', '判断', '填空', '解答']
const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F']

const userId = ref('')
const questions = ref([])
const showModal = ref(false)
const saving = ref(false)

const filters = reactive({
  categoryLevel: '',
  subjectName: '',
  questionType: '',
  difficultyLevel: '',
  auditStatus: ''
})

const defaultForm = () => ({
  subjectName: '',
  knowledgePoint: '',
  questionType: '单选',
  categoryLevel: '高中',
  questionTitle: '',
  optionInputs: ['', '', '', ''],
  correctAnswer: '',
  answerAnalysis: '',
  difficultyLevel: '中等',
  scoreValue: 2
})

const form = reactive(defaultForm())

const stats = computed(() => [
  { label: '全部题目', value: questions.value.length, class: 'text-gray-900 dark:text-gray-100' },
  { label: '待审核', value: questions.value.filter(q => q.auditStatus === 'pending').length, class: 'text-amber-600' },
  { label: '已通过', value: questions.value.filter(q => q.auditStatus === 'approved').length, class: 'text-emerald-600' },
  { label: '已驳回', value: questions.value.filter(q => q.auditStatus === 'rejected').length, class: 'text-red-600' }
])

const isChoiceType = computed(() => ['单选', '多选', '判断'].includes(form.questionType))

const getUserInfo = async () => {
  try {
    const res = await request.get('/user/getUserById')
    const data = res.data || res
    userId.value = data?.id || props.userId || 1
  } catch {
    userId.value = props.userId || 1
  }
  await fetchQuestions()
}

const fetchQuestions = async () => {
  if (!userId.value) return
  try {
    const params = { userId: userId.value }
    Object.entries(filters).forEach(([key, value]) => {
      if (value) params[key] = value
    })
    const res = await request.get('/question/list', params)
    if (res.code === 200) questions.value = res.data || []
  } catch (error) {
    console.error('获取题目失败', error)
    ElMessage.error('题目加载失败')
  }
}

const resetFilters = () => {
  Object.assign(filters, {
    categoryLevel: '',
    subjectName: '',
    questionType: '',
    difficultyLevel: '',
    auditStatus: ''
  })
  fetchQuestions()
}

const openCreate = () => {
  Object.assign(form, defaultForm())
  showModal.value = true
}

const addOption = () => {
  if (form.optionInputs.length < optionLabels.length) form.optionInputs.push('')
}

const toggleCorrect = (label) => {
  if (form.questionType === '多选') {
    const selected = form.correctAnswer ? form.correctAnswer.split(',') : []
    const index = selected.indexOf(label)
    if (index >= 0) selected.splice(index, 1)
    else selected.push(label)
    form.correctAnswer = selected.sort().join(',')
  } else {
    form.correctAnswer = label
  }
}

const isCorrect = (label) => form.correctAnswer.split(',').filter(Boolean).includes(label)

const saveQuestion = async () => {
  if (!form.subjectName || !form.questionTitle || !form.correctAnswer) {
    ElMessage.warning('请填写科目、题干和正确答案')
    return
  }
  saving.value = true
  try {
    const options = isChoiceType.value
      ? form.optionInputs.map((item, index) => item.trim() ? `${optionLabels[index]}. ${item.trim()}` : '').filter(Boolean)
      : []
    const res = await request.post('/question/add', {
      ...form,
      userId: userId.value,
      createdBy: userId.value,
      options: options.length ? JSON.stringify(options) : null,
      auditStatus: 'pending'
    })
    if (res.code === 200) {
      ElMessage.success('已提交，等待管理员审核')
      showModal.value = false
      await fetchQuestions()
    }
  } catch (error) {
    ElMessage.error(error.msg || '提交失败')
  } finally {
    saving.value = false
  }
}

const deleteQuestion = async (id) => {
  if (!confirm('确定删除这道题吗？')) return
  try {
    const res = await request.delete(`/question/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchQuestions()
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(options).split(/[,，]/).map(item => item.trim()).filter(Boolean)
  }
}

const splitKnowledge = (value) => String(value || '').split(/[,，、;]/).map(item => item.trim()).filter(Boolean)

const auditText = (status) => ({ pending: '待审核', approved: '已通过', rejected: '已驳回' }[status] || '待审核')
const auditClass = (status) => ({
  pending: 'bg-amber-50 text-amber-600',
  approved: 'bg-emerald-50 text-emerald-600',
  rejected: 'bg-red-50 text-red-600'
}[status] || 'bg-gray-100 text-gray-600')

onMounted(getUserInfo)
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

.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: rgb(243 244 246);
  padding: 4px 10px;
  color: rgb(75 85 99);
  font-size: 12px;
}

:global(.dark) .form-control {
  border-color: rgb(55 65 81);
  background: rgb(31 41 55);
  color: rgb(229 231 235);
}

:global(.dark) .badge {
  background: rgb(31 41 55);
  color: rgb(209 213 219);
}
</style>
