<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const emit = defineEmits(['success'])

const submitting = ref(false)
const firstSubjects = ref([])
const secondSubjects = ref([])
const selectedFirstSubject = ref(null)
const selectedSecondSubjects = ref([])
const selectionReason = ref('')
const futurePlan = ref('')
const advice = ref('')

// 获取科目列表
const fetchSubjects = async () => {
  try {
    const [firstRes, secondRes] = await Promise.all([
      request.get('/subject/first'),
      request.get('/subject/second')
    ])
    if (firstRes.code === 200) firstSubjects.value = firstRes.data
    if (secondRes.code === 200) secondSubjects.value = secondRes.data
  } catch (error) {
    console.error('获取科目失败', error)
  }
}

// 切换再选科目
const toggleSecondSubject = (subject) => {
  const index = selectedSecondSubjects.value.findIndex(s => s.id === subject.id)
  if (index > -1) {
    selectedSecondSubjects.value.splice(index, 1)
  } else {
    if (selectedSecondSubjects.value.length >= 2) {
      ElMessage.warning('最多只能选择2门再选科目')
      return
    }
    selectedSecondSubjects.value.push(subject)
  }
  updateAdvice()
}

// 更新选课建议
const updateAdvice = () => {
  if (!selectedFirstSubject.value) {
    advice.value = ''
    return
  }

  const subjects = [selectedFirstSubject.value.name, ...selectedSecondSubjects.value.map(s => s.name)]
  const combination = subjects.join('')

  const adviceMap = {
    '物理化学生物': '物化生组合，专业覆盖率达96%以上，适合医学、生物工程、计算机等专业',
    '物理化学地理': '物化地组合，专业覆盖广，适合地质、气象、环境科学等专业',
    '物理化学政治': '物化政组合，适合公安、政治学、法学等专业',
    '历史政治地理': '史政地组合，传统文科，适合文史哲、法学、新闻等专业',
    '历史生物政治': '史生政组合，适合心理学、教育学、法学等专业'
  }

  advice.value = adviceMap[combination] || '请完成选课组合选择'
}

// 检查是否已经选过课
const checkIfSelected = async () => {
  try {
    const res = await request.get(`/selection/student/${props.studentId}`)
    if (res.code === 200 && res.data && res.data.length > 0) {
      return true
    }
  } catch (error) {
    console.error('检查选课状态失败', error)
  }
  return false
}

// 提交选课
const submitSelection = async () => {
  if (!selectedFirstSubject.value) {
    ElMessage.warning('请选择首选科目')
    return
  }
  if (selectedSecondSubjects.value.length !== 2) {
    ElMessage.warning('请选择2门再选科目')
    return
  }
  if (!selectionReason.value) {
    ElMessage.warning('请填写选课理由')
    return
  }

  // 检查是否已经选过课
  const hasSelected = await checkIfSelected()
  if (hasSelected) {
    ElMessage.warning('您已经选过课，不能重复选课。请在"我的选课"中修改。')
    return
  }

  submitting.value = true
  try {
    const selectionData = {
      studentId: props.studentId,
      studentName: '当前学生', // 从用户信息获取
      grade: '高一',
      academicYear: '2024-2025',
      semester: '上学期',
      firstSubjectId: selectedFirstSubject.value.id,
      firstSubjectName: selectedFirstSubject.value.name,
      secondSubject1Id: selectedSecondSubjects.value[0].id,
      secondSubject1Name: selectedSecondSubjects.value[0].name,
      secondSubject2Id: selectedSecondSubjects.value[1].id,
      secondSubject2Name: selectedSecondSubjects.value[1].name,
      selectionReason: selectionReason.value,
      futurePlan: futurePlan.value
    }

    const res = await request.post('/selection/add', selectionData)
    if (res.code === 200) {
      ElMessage.success('选课提交成功')
      resetForm()
      emit('success')
    } else {
      ElMessage.error(res.message || '选课失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  selectedFirstSubject.value = null
  selectedSecondSubjects.value = []
  selectionReason.value = ''
  futurePlan.value = ''
  advice.value = ''
}

// 监听首选科目变化
watch(selectedFirstSubject, () => {
  updateAdvice()
})

// 监听再选科目变化
watch(selectedSecondSubjects, () => {
  updateAdvice()
}, { deep: true })

fetchSubjects()
</script>
<template>
  <div class="cs-panel space-y-6">
    <!-- 选课步骤提示 -->
    <div class="rounded-xl bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-950/30 dark:to-purple-950/30 p-4">
      <div class="flex items-center gap-2 mb-2">
        <span class="text-2xl">📖</span>
        <span class="font-semibold">选课步骤</span>
      </div>
      <div class="flex flex-wrap gap-4 text-sm">
        <div class="flex items-center gap-2">1️⃣ 选择首选科目（物理/历史）</div>
        <div class="flex items-center gap-2">2️⃣ 选择再选科目（化学/生物/政治/地理，二选一）</div>
        <div class="flex items-center gap-2">3️⃣ 填写选课理由</div>
        <div class="flex items-center gap-2">4️⃣ 提交并等待确认</div>
      </div>
    </div>

    <!-- 选课表单 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-6">
      <h3 class="text-lg font-semibold mb-4">新高考选课</h3>

      <!-- 首选科目 -->
      <div class="mb-6">
        <label class="block text-sm font-medium mb-2">首选科目（二选一）</label>
        <div class="grid grid-cols-2 gap-3">
          <div v-for="subject in firstSubjects" :key="subject.id"
               @click="selectedFirstSubject = subject"
               class="p-4 rounded-xl border-2 cursor-pointer transition-all text-center"
               :class="[
                 selectedFirstSubject?.id === subject.id
                   ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/30'
                   : 'border-slate-200 dark:border-slate-700 hover:border-indigo-300'
               ]">
            <div class="text-2xl mb-1">{{ subject.name === '物理' ? '⚡' : '📜' }}</div>
            <div class="font-semibold">{{ subject.name }}</div>
            <div class="text-xs text-slate-500 mt-1">{{ subject.description || '' }}</div>
          </div>
        </div>
      </div>

      <!-- 再选科目 -->
      <div class="mb-6">
        <label class="block text-sm font-medium mb-2">再选科目（四选二）</label>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div v-for="subject in secondSubjects" :key="subject.id"
               @click="toggleSecondSubject(subject)"
               class="p-3 rounded-xl border-2 cursor-pointer transition-all text-center"
               :class="[
                 selectedSecondSubjects.some(s => s.id === subject.id)
                   ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/30'
                   : 'border-slate-200 dark:border-slate-700 hover:border-indigo-300',
                 selectedSecondSubjects.length >= 2 && !selectedSecondSubjects.some(s => s.id === subject.id)
                   ? 'opacity-50 cursor-not-allowed'
                   : ''
               ]">
            <div class="font-medium">{{ subject.name }}</div>
            <div class="text-xs text-slate-500 mt-1">{{ subject.description || '' }}</div>
          </div>
        </div>
        <div class="text-sm text-slate-500 mt-2">
          已选择 {{ selectedSecondSubjects.length }}/2 门再选科目
        </div>
      </div>

      <!-- 选课理由 -->
      <div class="mb-6">
        <label class="block text-sm font-medium mb-2">选课理由</label>
        <textarea v-model="selectionReason" rows="3"
                  class="w-full px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800"
                  placeholder="请填写选课理由，如：目标专业要求、个人兴趣、学科优势等"></textarea>
      </div>

      <!-- 未来规划 -->
      <div class="mb-6">
        <label class="block text-sm font-medium mb-2">未来规划（可选）</label>
        <input v-model="futurePlan" type="text"
               class="w-full px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800"
               placeholder="如：目标大学、目标专业">
      </div>

      <!-- 提交按钮 -->
      <div class="flex gap-3">
        <button @click="submitSelection" :disabled="submitting"
                class="flex-1 py-3 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 text-white font-medium hover:shadow-lg transition-all disabled:opacity-50">
          {{ submitting ? '提交中...' : '提交选课' }}
        </button>
        <button @click="resetForm" class="px-6 py-3 rounded-xl border dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all">
          重置
        </button>
      </div>
    </div>

    <!-- 选课建议 -->
    <div v-if="advice" class="rounded-xl bg-amber-50/50 dark:bg-amber-950/30 p-4">
      <div class="flex items-center gap-2 mb-2">
        <span>💡</span>
        <span class="font-semibold">选课建议</span>
      </div>
      <p class="text-sm text-slate-600 dark:text-slate-300">{{ advice }}</p>
    </div>
  </div>
</template>
