<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const year = ref('2024-2025')
const list = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const form = ref({
  id: null,
  subjectId: '',
  subjectName: '',
  gradeLevel: 'A',
  percentageTop: 0,
  percentageBottom: 0,
  assignedScoreMin: 0,
  assignedScoreMax: 0,
  rawScoreMin: 0,
  rawScoreMax: 0,
  academicYear: '2024-2025',
  isActive: true
})

const fetchList = async () => {
  const res = await request.get('/grading/list', { academicYear: year.value })
  if (res.code === 200) list.value = res.data || []
}

const openDetail = async (id) => {
  const res = await request.get(`/grading/detail/${id}`)
  if (res.code === 200) {
    detail.value = res.data
    detailVisible.value = true
  }
}

const edit = (row) => { form.value = { ...row } }

const save = async () => {
  const res = await request.post('/grading/save', form.value)
  if (res.code === 200) {
    ElMessage.success('保存成功')
    await fetchList()
  }
}

const remove = async (id) => {
  const res = await request.post('/grading/delete', null, null, { params: { id } })
  if (res.code === 200) {
    ElMessage.success('删除成功')
    await fetchList()
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="cs-panel space-y-4">
    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <div class="flex gap-2 mb-3">
        <input v-model="year" class="px-3 py-2 rounded-lg border" placeholder="学年" />
        <button class="px-3 py-2 rounded-lg bg-indigo-600 text-white" @click="fetchList">查询</button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
        <input v-model="form.subjectId" class="px-2 py-2 rounded border" placeholder="科目ID" />
        <input v-model="form.subjectName" class="px-2 py-2 rounded border" placeholder="科目名" />
        <input v-model="form.gradeLevel" class="px-2 py-2 rounded border" placeholder="等级" />
        <input v-model="form.academicYear" class="px-2 py-2 rounded border" placeholder="学年" />
        <input v-model="form.percentageTop" class="px-2 py-2 rounded border" placeholder="前百分比" />
        <input v-model="form.percentageBottom" class="px-2 py-2 rounded border" placeholder="后百分比" />
        <input v-model="form.assignedScoreMin" class="px-2 py-2 rounded border" placeholder="赋分最小" />
        <input v-model="form.assignedScoreMax" class="px-2 py-2 rounded border" placeholder="赋分最大" />
        <input v-model="form.rawScoreMin" class="px-2 py-2 rounded border" placeholder="原始分最小" />
        <input v-model="form.rawScoreMax" class="px-2 py-2 rounded border" placeholder="原始分最大" />
      </div>
      <div class="mt-3"><button class="px-4 py-2 rounded-lg bg-emerald-600 text-white" @click="save">保存</button></div>
    </div>
    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <div v-for="item in list" :key="item.id" class="p-2 flex justify-between border-b">
        <span>{{ item.subjectName }} {{ item.gradeLevel }} {{ item.rawScoreMin }}-{{ item.rawScoreMax }}</span>
        <div class="flex gap-2">
          <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
          <button class="text-indigo-600 text-sm" @click="edit(item)">编辑</button>
          <button class="text-red-600 text-sm" @click="remove(item.id)">删除</button>
        </div>
      </div>
    </div>
    <el-dialog v-model="detailVisible" title="赋分详情" width="680px">
      <pre class="text-xs whitespace-pre-wrap">{{ detail }}</pre>
    </el-dialog>
  </div>
</template>
