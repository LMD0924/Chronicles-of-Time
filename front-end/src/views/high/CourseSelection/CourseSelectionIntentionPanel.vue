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
const intentions = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)
const form = ref({
  id: null,
  grade: '高一',
  firstSubjectIntention: '物理',
  secondSubjectIntention1: '',
  secondSubjectIntention2: '',
  secondSubjectBackup1: '',
  secondSubjectBackup2: '',
  intentionReason: '',
  targetMajor: '',
  targetUniversity: '',
  strengthSubjects: '',
  weakSubjects: '',
  careerInterest: '',
  teacherFeedback: '',
  parentFeedback: ''
})

const fetchList = async () => {
  if (!props.studentId) return
  loading.value = true
  try {
    const res = await request.get('/intention/list')
    if (res.code === 200) intentions.value = res.data || []
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!props.studentId) return ElMessage.warning('用户信息未加载')
  if (!form.value.secondSubjectIntention1 || !form.value.secondSubjectIntention2) {
    return ElMessage.warning('请至少填写两门再选科目意向')
  }
  saving.value = true
  try {
    const payload = {
      ...form.value,
      studentId: String(props.studentId),
      studentName: props.studentName || '当前学生'
    }
    const res = await request.post('/intention/save', payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      form.value.id = res.data?.id || form.value.id
      await fetchList()
    }
  } finally {
    saving.value = false
  }
}

const edit = (row) => {
  form.value = { ...row }
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该意向记录？', '提示', { type: 'warning' })
  const res = await request.post('/intention/delete', null, null, { params: { id } })
  if (res.code === 200) {
    ElMessage.success('删除成功')
    await fetchList()
  }
}

const updateStatus = async (row, status) => {
  const res = await request.post('/intention/status', null, null, { params: { id: row.id, status } })
  if (res.code === 200) {
    ElMessage.success('状态已更新')
    await fetchList()
  }
}

const openDetail = async (id) => {
  const res = await request.get('/intention/detail', { id })
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
      <h3 class="text-lg font-semibold mb-3">选课意向填报</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
        <input v-model="form.grade" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="年级" />
        <select v-model="form.firstSubjectIntention" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option value="物理">物理</option>
          <option value="历史">历史</option>
        </select>
        <input v-model="form.secondSubjectIntention1" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="再选意向1" />
        <input v-model="form.secondSubjectIntention2" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="再选意向2" />
        <input v-model="form.secondSubjectBackup1" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="备选1" />
        <input v-model="form.secondSubjectBackup2" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="备选2" />
        <input v-model="form.targetMajor" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="目标专业" />
        <input v-model="form.targetUniversity" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="目标大学" />
        <input v-model="form.strengthSubjects" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="优势科目" />
        <input v-model="form.weakSubjects" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="弱势科目" />
        <input v-model="form.careerInterest" class="px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="职业兴趣" />
      </div>
      <textarea v-model="form.intentionReason" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="选课理由" />
      <textarea v-model="form.teacherFeedback" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="老师反馈" />
      <textarea v-model="form.parentFeedback" rows="2" class="w-full mt-3 px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" placeholder="家长反馈" />
      <div class="mt-3 flex justify-end">
        <button @click="submit" :disabled="saving" class="px-4 py-2 rounded-lg bg-indigo-600 text-white">{{ saving ? '保存中...' : '保存意向' }}</button>
      </div>
    </div>

    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">我的意向记录</h3>
      <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
      <div v-else-if="intentions.length === 0" class="text-sm text-slate-500">暂无记录</div>
      <div v-else class="space-y-2">
        <div v-for="item in intentions" :key="item.id" class="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40 flex justify-between items-center">
          <div class="text-sm">
            {{ item.firstSubjectIntention }} + {{ item.secondSubjectIntention1 }} + {{ item.secondSubjectIntention2 }}
            <span class="ml-2 text-xs text-slate-500">状态: {{ item.status }}</span>
          </div>
          <div class="flex gap-2">
            <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
            <button class="text-indigo-600 text-sm" @click="edit(item)">编辑</button>
            <button class="text-emerald-600 text-sm" @click="updateStatus(item, 3)">确认</button>
            <button class="text-red-600 text-sm" @click="remove(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="意向详情" width="720px">
      <div v-if="currentDetail" class="grid grid-cols-2 gap-3 text-sm">
        <div>学生：{{ currentDetail.studentName }}</div>
        <div>年级：{{ currentDetail.grade }}</div>
        <div>首选：{{ currentDetail.firstSubjectIntention }}</div>
        <div>再选：{{ currentDetail.secondSubjectIntention1 }} / {{ currentDetail.secondSubjectIntention2 }}</div>
        <div>备选：{{ currentDetail.secondSubjectBackup1 || '-' }} / {{ currentDetail.secondSubjectBackup2 || '-' }}</div>
        <div>状态：{{ currentDetail.status }}</div>
        <div class="col-span-2">选课理由：{{ currentDetail.intentionReason || '-' }}</div>
        <div class="col-span-2">目标专业/大学：{{ currentDetail.targetMajor || '-' }} / {{ currentDetail.targetUniversity || '-' }}</div>
        <div class="col-span-2">优势/弱势科目：{{ currentDetail.strengthSubjects || '-' }} / {{ currentDetail.weakSubjects || '-' }}</div>
        <div class="col-span-2">职业兴趣：{{ currentDetail.careerInterest || '-' }}</div>
        <div class="col-span-2">老师反馈：{{ currentDetail.teacherFeedback || '-' }}</div>
        <div class="col-span-2">家长反馈：{{ currentDetail.parentFeedback || '-' }}</div>
        <div>评估人：{{ currentDetail.evaluateBy || '-' }}</div>
        <div>评估时间：{{ currentDetail.evaluateTime || '-' }}</div>
      </div>
    </el-dialog>
  </div>
</template>
