<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const keyword = ref('')
const majors = ref([])
const matchings = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const form = ref({
  id: null,
  majorCode: '',
  majorName: '',
  category: '',
  firstSubjectRequired: '',
  secondSubjectRequired: '',
  requirementDetail: '',
  universityName: '',
  universityLevel: '',
  province: '',
  admissionYear: ''
})
const matchForm = ref({
  id: null,
  majorCode: '',
  majorName: '',
  subjectId: '',
  subjectName: '',
  importanceLevel: 1,
  matchingScore: 0,
  description: ''
})

const fetchMajors = async () => {
  const res = await request.get('/major/list', { keyword: keyword.value })
  if (res.code === 200) majors.value = res.data || []
}
const fetchMatchings = async () => {
  const res = await request.get('/major/matching/list')
  if (res.code === 200) matchings.value = res.data || []
}
const openDetail = async (id) => {
  const res = await request.get(`/major/detailById/${id}`)
  if (res.code === 200) { detail.value = res.data; detailVisible.value = true }
}
const saveMajor = async () => {
  const res = await request.post('/major/save', form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); fetchMajors() }
}
const deleteMajor = async (id) => {
  const res = await request.post('/major/delete', null, null, { params: { id } })
  if (res.code === 200) { ElMessage.success('删除成功'); fetchMajors() }
}
const saveMatching = async () => {
  const res = await request.post('/major/matching/save', matchForm.value)
  if (res.code === 200) { ElMessage.success('匹配保存成功'); fetchMatchings() }
}
const deleteMatching = async (id) => {
  const res = await request.post('/major/matching/delete', null, null, { params: { id } })
  if (res.code === 200) { ElMessage.success('删除成功'); fetchMatchings() }
}
const editMajor = (r) => form.value = { ...r }
const editMatching = (r) => matchForm.value = { ...r }

onMounted(() => { fetchMajors(); fetchMatchings() })
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <div class="flex gap-2 mb-2">
        <input v-model="keyword" class="px-3 py-2 rounded border" placeholder="专业关键字" />
        <button class="px-3 py-2 rounded bg-indigo-600 text-white" @click="fetchMajors">查询</button>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <input v-model="form.majorCode" class="px-2 py-2 rounded border" placeholder="专业代码" />
        <input v-model="form.majorName" class="px-2 py-2 rounded border" placeholder="专业名称" />
        <input v-model="form.category" class="px-2 py-2 rounded border" placeholder="类别" />
        <input v-model="form.firstSubjectRequired" class="px-2 py-2 rounded border" placeholder="首选要求" />
        <input v-model="form.secondSubjectRequired" class="px-2 py-2 rounded border" placeholder="再选要求" />
        <input v-model="form.universityName" class="px-2 py-2 rounded border" placeholder="大学名称" />
        <input v-model="form.universityLevel" class="px-2 py-2 rounded border" placeholder="大学层次" />
        <input v-model="form.province" class="px-2 py-2 rounded border" placeholder="省份" />
      </div>
      <textarea v-model="form.requirementDetail" rows="2" class="w-full mt-2 px-2 py-2 rounded border" placeholder="详细要求" />
      <div class="mt-2"><button class="px-4 py-2 rounded bg-emerald-600 text-white" @click="saveMajor">保存专业</button></div>
    </div>
    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <h4 class="font-semibold mb-2">专业列表</h4>
      <div v-for="item in majors" :key="item.id" class="p-2 flex justify-between border-b">
        <span>{{ item.majorCode }} {{ item.majorName }} {{ item.universityName }}</span>
        <div class="flex gap-2">
          <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
          <button class="text-indigo-600 text-sm" @click="editMajor(item)">编辑</button>
          <button class="text-red-600 text-sm" @click="deleteMajor(item.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="rounded-xl bg-white/70 dark:bg-white/10 border border-white/30 p-4">
      <h4 class="font-semibold mb-2">专业-科目匹配</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <input v-model="matchForm.majorCode" class="px-2 py-2 rounded border" placeholder="专业代码" />
        <input v-model="matchForm.majorName" class="px-2 py-2 rounded border" placeholder="专业名称" />
        <input v-model="matchForm.subjectId" class="px-2 py-2 rounded border" placeholder="科目ID" />
        <input v-model="matchForm.subjectName" class="px-2 py-2 rounded border" placeholder="科目名" />
        <input v-model="matchForm.importanceLevel" class="px-2 py-2 rounded border" placeholder="重要程度" />
        <input v-model="matchForm.matchingScore" class="px-2 py-2 rounded border" placeholder="匹配度" />
      </div>
      <textarea v-model="matchForm.description" rows="2" class="w-full mt-2 px-2 py-2 rounded border" placeholder="匹配说明" />
      <div class="mt-2"><button class="px-4 py-2 rounded bg-emerald-600 text-white" @click="saveMatching">保存匹配</button></div>
      <div class="mt-3">
        <div v-for="m in matchings" :key="m.id" class="p-2 flex justify-between border-b">
          <span>{{ m.majorCode }} - {{ m.subjectName }} ({{ m.matchingScore }})</span>
          <div class="flex gap-2">
            <button class="text-indigo-600 text-sm" @click="editMatching(m)">编辑</button>
            <button class="text-red-600 text-sm" @click="deleteMatching(m.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="专业详情" width="760px">
      <pre class="text-xs whitespace-pre-wrap">{{ detail }}</pre>
    </el-dialog>
  </div>
</template>
