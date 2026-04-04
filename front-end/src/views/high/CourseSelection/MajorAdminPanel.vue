<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  }
})

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

const getUniversityLevelClass = (level) => {
  if (!level) return ''
  if (level.includes('985')) return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'
  if (level.includes('211')) return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'
  if (level.includes('双一流')) return 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
  return ''
}

onMounted(() => { fetchMajors(); fetchMatchings() })
</script>

<template>
  <div class="space-y-4">
    <!-- 专业表单卡片 -->
    <div class="rounded-2xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 overflow-hidden">
      <!-- 头部 -->
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/30 dark:to-purple-950/30 border-b border-gray-200/50 dark:border-white/10">
        <span class="text-2xl">📚</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">专业信息管理</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400">CRUD</span>
      </div>

      <!-- 搜索栏 -->
      <div class="p-4 border-b border-gray-200/50 dark:border-white/10">
        <div class="flex gap-3">
          <input
            v-model="keyword"
            type="text"
            class="flex-1 px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            placeholder="🔍 输入专业代码、名称或大学名称搜索..."
          />
          <button
            @click="fetchMajors"
            class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 text-white font-medium shadow-md hover:shadow-lg transition-all duration-200"
          >
            查询
          </button>
        </div>
      </div>

      <!-- 表单 -->
      <div class="p-4 space-y-3">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <input v-model="form.majorCode" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="专业代码" />
          <input v-model="form.majorName" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="专业名称" />
          <input v-model="form.category" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="类别" />
          <input v-model="form.firstSubjectRequired" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="首选要求" />
          <input v-model="form.secondSubjectRequired" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="再选要求" />
          <input v-model="form.universityName" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="大学名称" />
          <input v-model="form.universityLevel" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="大学层次" />
          <input v-model="form.province" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="省份" />
        </div>
        <textarea v-model="form.requirementDetail" rows="2" class="w-full px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" placeholder="详细要求" />
        <div class="flex justify-end">
          <button @click="saveMajor" class="px-5 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-medium shadow-md hover:shadow-lg transition-all duration-200">
            💾 保存专业
          </button>
        </div>
      </div>
    </div>

    <!-- 专业列表卡片 -->
    <div class="rounded-2xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/30 dark:to-purple-950/30 border-b border-gray-200/50 dark:border-white/10">
        <span class="text-2xl">📋</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">专业列表</h3>
        <span class="px-2 py-1 rounded-full text-xs bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400">{{ majors.length }}条</span>
      </div>
      <div class="divide-y divide-gray-200/50 dark:divide-white/10">
        <div v-for="item in majors" :key="item.id" class="p-4 flex flex-wrap justify-between items-center gap-3 hover:bg-gray-50/50 dark:hover:bg-white/5 transition-colors">
          <div class="flex flex-wrap items-center gap-3">
            <span class="px-2.5 py-1 rounded-lg bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 text-sm font-mono">{{ item.majorCode }}</span>
            <span class="font-medium text-gray-800 dark:text-gray-200">{{ item.majorName }}</span>
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ item.universityName }}</span>
            <span v-if="item.universityLevel" :class="[getUniversityLevelClass(item.universityLevel), 'px-2 py-0.5 rounded-full text-xs font-medium']">{{ item.universityLevel }}</span>
          </div>
          <div class="flex gap-2">
            <button @click="openDetail(item.id)" class="px-3 py-1.5 rounded-lg text-sm bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors">🔍 详情</button>
            <button @click="editMajor(item)" class="px-3 py-1.5 rounded-lg text-sm bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 hover:bg-amber-100 dark:hover:bg-amber-900/50 transition-colors">✏️ 编辑</button>
            <button @click="deleteMajor(item.id)" class="px-3 py-1.5 rounded-lg text-sm bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/50 transition-colors">🗑️ 删除</button>
          </div>
        </div>
        <div v-if="majors.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">
          <span class="text-4xl opacity-50">📭</span>
          <p class="mt-2">暂无专业数据</p>
        </div>
      </div>
    </div>

    <!-- 专业-科目匹配卡片 -->
    <div class="rounded-2xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/30 dark:to-purple-950/30 border-b border-gray-200/50 dark:border-white/10">
        <span class="text-2xl">🎯</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">专业-科目匹配</h3>
        <span class="px-2 py-1 rounded-full text-xs bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400">{{ matchings.length }}条</span>
      </div>
      <div class="p-4 space-y-3">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <input v-model="matchForm.majorCode" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="专业代码" />
          <input v-model="matchForm.majorName" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="专业名称" />
          <input v-model="matchForm.subjectId" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="科目ID" />
          <input v-model="matchForm.subjectName" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="科目名" />
          <input v-model="matchForm.importanceLevel" type="number" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="重要程度 1-5" />
          <input v-model="matchForm.matchingScore" type="number" class="px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="匹配度 0-100" />
        </div>
        <textarea v-model="matchForm.description" rows="2" class="w-full px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" placeholder="匹配说明" />
        <div class="flex justify-end">
          <button @click="saveMatching" class="px-5 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-medium shadow-md hover:shadow-lg transition-all duration-200">
            💾 保存匹配
          </button>
        </div>

        <!-- 匹配列表 -->
        <div class="mt-4 divide-y divide-gray-200/50 dark:divide-white/10 border-t border-gray-200/50 dark:border-white/10 pt-3">
          <div v-for="m in matchings" :key="m.id" class="py-3 flex flex-wrap justify-between items-center gap-3">
            <div class="flex flex-wrap items-center gap-3">
              <span class="px-2 py-1 rounded-lg bg-purple-100 dark:bg-purple-900/50 text-purple-700 dark:text-purple-300 text-sm font-mono">{{ m.majorCode }}</span>
              <span class="text-gray-700 dark:text-gray-300">{{ m.subjectName }}</span>
              <div class="flex items-center gap-2">
                <div class="w-24 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" :style="{ width: m.matchingScore + '%' }"></div>
                </div>
                <span class="text-sm font-medium text-indigo-600 dark:text-indigo-400">{{ m.matchingScore }}%</span>
              </div>
            </div>
            <div class="flex gap-2">
              <button @click="editMatching(m)" class="px-3 py-1.5 rounded-lg text-sm bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 hover:bg-amber-100 dark:hover:bg-amber-900/50 transition-colors">✏️ 编辑</button>
              <button @click="deleteMatching(m.id)" class="px-3 py-1.5 rounded-lg text-sm bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/50 transition-colors">🗑️ 删除</button>
            </div>
          </div>
          <div v-if="matchings.length === 0" class="py-8 text-center text-gray-500 dark:text-gray-400">
            <span class="text-4xl opacity-50">🔗</span>
            <p class="mt-2">暂无匹配数据</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="专业详情" width="800px" :class="{ 'dark-dialog': props.isDark }">
      <div v-if="detail" class="max-h-[70vh] overflow-y-auto px-1 space-y-5">
        <!-- 基本信息 -->
        <div class="bg-gray-50/50 dark:bg-gray-800/30 rounded-xl p-4">
          <div class="flex items-center gap-2 pb-3 mb-3 border-b border-indigo-200/50 dark:border-indigo-500/30">
            <span class="text-xl">📚</span>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">基本信息</h4>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">专业代码：</span><span class="text-gray-800 dark:text-gray-200 font-mono">{{ detail.majorCode || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">专业名称：</span><span class="text-gray-800 dark:text-gray-200 font-medium">{{ detail.majorName || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">专业类别：</span><span class="text-gray-800 dark:text-gray-200">{{ detail.category || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">招生年份：</span><span class="text-gray-800 dark:text-gray-200">{{ detail.admissionYear || '-' }}</span></div>
          </div>
        </div>

        <!-- 选科要求 -->
        <div class="bg-gray-50/50 dark:bg-gray-800/30 rounded-xl p-4">
          <div class="flex items-center gap-2 pb-3 mb-3 border-b border-indigo-200/50 dark:border-indigo-500/30">
            <span class="text-xl">📖</span>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">选科要求</h4>
          </div>
          <div class="space-y-3">
            <div class="flex items-center gap-3 flex-wrap">
              <span class="text-sm text-gray-500 dark:text-gray-400">首选科目：</span>
              <span class="px-3 py-1 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-sm font-medium">{{ detail.firstSubjectRequired || '不限' }}</span>
            </div>
            <div class="flex items-center gap-3 flex-wrap">
              <span class="text-sm text-gray-500 dark:text-gray-400">再选科目：</span>
              <span class="text-gray-800 dark:text-gray-200">{{ detail.secondSubjectRequired || '不限' }}</span>
            </div>
            <div v-if="detail.requirementDetail" class="pt-2 border-t border-gray-200 dark:border-gray-700">
              <span class="text-sm text-gray-500 dark:text-gray-400 block mb-1">详细要求：</span>
              <span class="text-gray-800 dark:text-gray-200 text-sm">{{ detail.requirementDetail }}</span>
            </div>
          </div>
        </div>

        <!-- 院校信息 -->
        <div class="bg-gray-50/50 dark:bg-gray-800/30 rounded-xl p-4">
          <div class="flex items-center gap-2 pb-3 mb-3 border-b border-indigo-200/50 dark:border-indigo-500/30">
            <span class="text-xl">🏫</span>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">院校信息</h4>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">大学名称：</span><span class="text-gray-800 dark:text-gray-200">{{ detail.universityName || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">大学层次：</span><span :class="[getUniversityLevelClass(detail.universityLevel), 'px-2 py-0.5 rounded-full text-xs font-medium inline-block']">{{ detail.universityLevel || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">所在省份：</span><span class="text-gray-800 dark:text-gray-200">{{ detail.province || '-' }}</span></div>
          </div>
        </div>

        <!-- 科目匹配 -->
        <div v-if="detail.matchings && detail.matchings.length" class="bg-gray-50/50 dark:bg-gray-800/30 rounded-xl p-4">
          <div class="flex items-center gap-2 pb-3 mb-3 border-b border-indigo-200/50 dark:border-indigo-500/30">
            <span class="text-xl">🎯</span>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">科目匹配度</h4>
          </div>
          <div class="space-y-3">
            <div v-for="match in detail.matchings" :key="match.id" class="flex items-center gap-3">
              <span class="w-16 text-gray-700 dark:text-gray-300 font-medium">{{ match.subjectName }}</span>
              <div class="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full transition-all" :style="{ width: match.matchingScore + '%' }"></div>
              </div>
              <span class="w-12 text-right text-indigo-600 dark:text-indigo-400 font-semibold">{{ match.matchingScore }}%</span>
            </div>
          </div>
        </div>

        <!-- 时间信息 -->
        <div class="bg-gray-50/50 dark:bg-gray-800/30 rounded-xl p-4">
          <div class="flex items-center gap-2 pb-3 mb-3 border-b border-indigo-200/50 dark:border-indigo-500/30">
            <span class="text-xl">⏰</span>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">时间信息</h4>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">创建时间：</span><span class="text-gray-800 dark:text-gray-200 text-sm">{{ detail.createTime || '-' }}</span></div>
            <div class="flex items-baseline flex-wrap"><span class="text-sm text-gray-500 dark:text-gray-400 w-24">更新时间：</span><span class="text-gray-800 dark:text-gray-200 text-sm">{{ detail.updateTime || '-' }}</span></div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end">
          <button class="px-4 py-2 rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors" @click="detailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 自定义滚动条 */
.max-h-\[70vh\]::-webkit-scrollbar {
  width: 6px;
}

.max-h-\[70vh\]::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.max-h-\[70vh\]::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 10px;
}

.max-h-\[70vh\]::-webkit-scrollbar-thumb:hover {
  background: #818cf8;
}

.dark .max-h-\[70vh\]::-webkit-scrollbar-track {
  background: #1e293b;
}

.dark .max-h-\[70vh\]::-webkit-scrollbar-thumb {
  background: #4f46e5;
}
</style>
