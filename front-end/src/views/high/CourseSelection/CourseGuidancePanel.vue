<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number],
  studentName: String
})

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)
const form = ref({
  id: null,
  guidanceDate: new Date().toISOString().split('T')[0],
  guidanceType: '1',
  guidanceContent: '',
  suggestedCombination: '',
  suggestedMajor: '',
  strengthAnalysis: '',
  weaknessAnalysis: '',
  opportunityAnalysis: '',
  threatAnalysis: '',
  actionPlan: '',
  advisorName: '',
  advisorPosition: '',
  studentFeedback: '',
  parentFeedback: '',
  followUpDate: '',
  status: 1
})

const fetchList = async () => {
  if (!props.studentId) return
  loading.value = true
  try {
    const res = await request.get('/guidance/list')
    if (res.code === 200) list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!props.studentId) return ElMessage.warning('用户信息未加载')
  saving.value = true
  try {
    const payload = {
      ...form.value,
      studentId: String(props.studentId),
      studentName: props.studentName || '当前学生'
    }
    const res = await request.post('/guidance/save', payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      form.value.id = res.data?.id || form.value.id
      await fetchList()
    }
  } finally {
    saving.value = false
  }
}

const edit = (row) => { form.value = { ...row } }

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该指导记录？', '提示', { type: 'warning' })
  const res = await request.post('/guidance/delete', null, null, { params: { id } })
  if (res.code === 200) {
    ElMessage.success('删除成功')
    await fetchList()
  }
}

const updateStatus = async (row, status) => {
  const res = await request.post('/guidance/status', null, null, { params: { id: row.id, status } })
  if (res.code === 200) {
    ElMessage.success('状态已更新')
    await fetchList()
  }
}

const openDetail = async (id) => {
  const res = await request.get('/guidance/detail', { id })
  if (res.code === 200) {
    currentDetail.value = res.data
    detailVisible.value = true
  }
}

watch(() => props.studentId, fetchList, { immediate: true })
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">选科指导记录</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
        <input v-model="form.guidanceDate" type="date" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" />
        <select v-model="form.guidanceType" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option value="1">个别咨询</option>
          <option value="2">团体辅导</option>
          <option value="3">家长会</option>
          <option value="4">讲座</option>
        </select>
        <input v-model="form.suggestedCombination" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="建议组合" />
        <input v-model="form.suggestedMajor" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="建议专业" />
        <input v-model="form.advisorName" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="指导老师" />
        <input v-model="form.advisorPosition" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="老师职位" />
        <input v-model="form.followUpDate" type="date" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" />
        <select v-model="form.status" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option :value="1">进行中</option>
          <option :value="2">已完成</option>
          <option :value="3">已采纳</option>
          <option :value="4">已放弃</option>
        </select>
      </div>
      <textarea v-model="form.guidanceContent" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="指导内容" />
      <textarea v-model="form.strengthAnalysis" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="优势分析" />
      <textarea v-model="form.weaknessAnalysis" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="劣势分析" />
      <textarea v-model="form.opportunityAnalysis" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="机会分析" />
      <textarea v-model="form.threatAnalysis" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="挑战分析" />
      <textarea v-model="form.actionPlan" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="行动计划" />
      <textarea v-model="form.studentFeedback" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="学生反馈" />
      <textarea v-model="form.parentFeedback" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="家长反馈" />
      <div class="mt-3 flex justify-end">
        <button @click="submit" :disabled="saving" class="px-4 py-2 rounded-lg bg-indigo-600 text-white">{{ saving ? '保存中...' : '保存指导' }}</button>
      </div>
    </div>

    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">指导历史</h3>
      <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
      <div v-else-if="list.length === 0" class="text-sm text-slate-500">暂无记录</div>
      <div v-else class="space-y-2">
        <div v-for="item in list" :key="item.id" class="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40 flex justify-between items-center">
          <div class="text-sm">{{ item.guidanceDate }} · {{ item.suggestedCombination || '-' }} · {{ item.suggestedMajor || '-' }} · 状态: {{ item.status }}</div>
          <div class="flex gap-2">
            <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
            <button class="text-indigo-600 text-sm" @click="edit(item)">编辑</button>
            <button class="text-emerald-600 text-sm" @click="updateStatus(item, 2)">完成</button>
            <button class="text-red-600 text-sm" @click="remove(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="指导详情" width="760px">
      <div v-if="currentDetail" class="grid grid-cols-2 gap-3 text-sm">
        <div>学生：{{ currentDetail.studentName }}</div>
        <div>日期：{{ currentDetail.guidanceDate }}</div>
        <div>类型：{{ currentDetail.guidanceType }}</div>
        <div>状态：{{ currentDetail.status }}</div>
        <div class="col-span-2">指导内容：{{ currentDetail.guidanceContent || '-' }}</div>
        <div>建议组合：{{ currentDetail.suggestedCombination || '-' }}</div>
        <div>建议专业：{{ currentDetail.suggestedMajor || '-' }}</div>
        <div class="col-span-2">优势分析：{{ currentDetail.strengthAnalysis || '-' }}</div>
        <div class="col-span-2">劣势分析：{{ currentDetail.weaknessAnalysis || '-' }}</div>
        <div class="col-span-2">机会分析：{{ currentDetail.opportunityAnalysis || '-' }}</div>
        <div class="col-span-2">挑战分析：{{ currentDetail.threatAnalysis || '-' }}</div>
        <div class="col-span-2">行动计划：{{ currentDetail.actionPlan || '-' }}</div>
        <div>指导老师：{{ currentDetail.advisorName || '-' }}</div>
        <div>老师职位：{{ currentDetail.advisorPosition || '-' }}</div>
        <div class="col-span-2">学生反馈：{{ currentDetail.studentFeedback || '-' }}</div>
        <div class="col-span-2">家长反馈：{{ currentDetail.parentFeedback || '-' }}</div>
        <div>下次跟进：{{ currentDetail.followUpDate || '-' }}</div>
        <div>更新时间：{{ currentDetail.updateTime || '-' }}</div>
      </div>
    </el-dialog>
  </div>
</template>
