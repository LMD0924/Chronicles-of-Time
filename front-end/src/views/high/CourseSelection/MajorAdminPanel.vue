<template>
  <div class="cs-panel space-y-4">
    <div class="rounded-xl bg-white/70 dark:bg-black border border-white/30 p-4">
      <div class="flex gap-2 mb-2">
        <input v-model="keyword" class="px-3 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="专业关键字" />
        <button class="px-3 py-2 rounded bg-indigo-600 text-white" @click="fetchMajors">查询</button>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <input v-model="form.majorCode" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="专业代码" />
        <input v-model="form.majorName" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="专业名称" />
        <input v-model="form.category" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="类别" />
        <input v-model="form.firstSubjectRequired" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="首选要求" />
        <input v-model="form.secondSubjectRequired" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="再选要求" />
        <input v-model="form.universityName" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="大学名称" />
        <input v-model="form.universityLevel" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="大学层次" />
        <input v-model="form.province" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="省份" />
      </div>
      <textarea v-model="form.requirementDetail" rows="2" class="w-full mt-2 px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="详细要求" />
      <div class="mt-2"><button class="px-4 py-2 rounded bg-emerald-600 text-white" @click="saveMajor">保存专业</button></div>
    </div>
    <div class="rounded-xl bg-white/70 dark:bg-black border border-white/30 p-4">
      <h4 class="font-semibold mb-2 dark:text-white">专业列表</h4>
      <div v-for="item in majors" :key="item.id" class="p-2 flex justify-between border-b dark:border-gray-800">
        <span class="dark:text-gray-300">{{ item.majorCode }} {{ item.majorName }} {{ item.universityName }}</span>
        <div class="flex gap-2">
          <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
          <button class="text-indigo-600 text-sm" @click="editMajor(item)">编辑</button>
          <button class="text-red-600 text-sm" @click="deleteMajor(item.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="rounded-xl bg-white/70 dark:bg-black border border-white/30 p-4">
      <h4 class="font-semibold mb-2 dark:text-white">专业-科目匹配</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <input v-model="matchForm.majorCode" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="专业代码" />
        <input v-model="matchForm.majorName" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="专业名称" />
        <input v-model="matchForm.subjectId" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="科目ID" />
        <input v-model="matchForm.subjectName" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="科目名" />
        <input v-model="matchForm.importanceLevel" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="重要程度" />
        <input v-model="matchForm.matchingScore" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="匹配度" />
      </div>
      <textarea v-model="matchForm.description" rows="2" class="w-full mt-2 px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="匹配说明" />
      <div class="mt-2"><button class="px-4 py-2 rounded bg-emerald-600 text-white" @click="saveMatching">保存匹配</button></div>
      <div class="mt-3">
        <div v-for="m in matchings" :key="m.id" class="p-2 flex justify-between border-b dark:border-gray-800">
          <span class="dark:text-gray-300">{{ m.majorCode }} - {{ m.subjectName }} ({{ m.matchingScore }})</span>
          <div class="flex gap-2">
            <button class="text-indigo-600 text-sm" @click="editMatching(m)">编辑</button>
            <button class="text-red-600 text-sm" @click="deleteMatching(m.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 美化后的详情弹窗 -->
    <el-dialog v-model="detailVisible" title="专业详情" width="800px" :class="{ 'dark-dialog': isDark }">
      <div v-if="detail" class="detail-container">
        <!-- 基本信息卡片 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">📚</span>
            <span>基本信息</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">专业代码：</span>
              <span class="detail-value">{{ detail.majorCode || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">专业名称：</span>
              <span class="detail-value">{{ detail.majorName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">专业类别：</span>
              <span class="detail-value">{{ detail.category || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">招生年份：</span>
              <span class="detail-value">{{ detail.admissionYear || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 选科要求 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">📖</span>
            <span>选科要求</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">首选科目：</span>
              <span class="detail-value requirement-badge">{{ detail.firstSubjectRequired || '不限' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">再选科目：</span>
              <span class="detail-value">{{ detail.secondSubjectRequired || '不限' }}</span>
            </div>
          </div>
          <div v-if="detail.requirementDetail" class="detail-item full-width mt-2">
            <span class="detail-label">详细要求：</span>
            <span class="detail-value">{{ detail.requirementDetail }}</span>
          </div>
        </div>

        <!-- 院校信息 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">🏫</span>
            <span>院校信息</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">大学名称：</span>
              <span class="detail-value">{{ detail.universityName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">大学层次：</span>
              <span class="detail-value">
                <span :class="getUniversityLevelClass(detail.universityLevel)">{{ detail.universityLevel || '-' }}</span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">所在省份：</span>
              <span class="detail-value">{{ detail.province || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 科目匹配信息（如果有） -->
        <div v-if="detail.matchings && detail.matchings.length" class="detail-section">
          <div class="section-title">
            <span class="title-icon">🎯</span>
            <span>科目匹配度</span>
          </div>
          <div class="matching-list">
            <div v-for="match in detail.matchings" :key="match.id" class="matching-item">
              <span class="subject-name">{{ match.subjectName }}</span>
              <div class="matching-score-bar">
                <div class="score-fill" :style="{ width: match.matchingScore + '%' }"></div>
              </div>
              <span class="score-value">{{ match.matchingScore }}%</span>
            </div>
          </div>
        </div>

        <!-- 时间信息 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">⏰</span>
            <span>时间信息</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">创建时间：</span>
              <span class="detail-value">{{ detail.createTime || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新时间：</span>
              <span class="detail-value">{{ detail.updateTime || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="px-4 py-2 rounded bg-gray-200 dark:bg-gray-800 dark:text-white" @click="detailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

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

// 获取大学层次样式类
const getUniversityLevelClass = (level) => {
  if (!level) return ''
  if (level.includes('985')) return 'text-amber-600 dark:text-amber-400 font-semibold'
  if (level.includes('211')) return 'text-emerald-600 dark:text-emerald-400 font-semibold'
  if (level.includes('双一流')) return 'text-blue-600 dark:text-blue-400 font-semibold'
  return ''
}

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

<style scoped>
.detail-container {
  max-height: 70vh;
  overflow-y: auto;
  padding: 4px;
}

.detail-section {
  margin-bottom: 24px;
  background: rgba(249, 250, 251, 0.5);
  border-radius: 12px;
  padding: 16px;
}

.dark .detail-section {
  background: rgba(0, 0, 0, 0.5);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
  color: #1f2937;
}

.dark .section-title {
  color: #e5e7eb;
}

.title-icon {
  font-size: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
}

.detail-item.full-width {
  grid-column: span 2;
}

.detail-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  min-width: 80px;
}

.dark .detail-label {
  color: #9ca3af;
}

.detail-value {
  font-size: 14px;
  color: #1f2937;
  word-break: break-word;
  flex: 1;
}

.dark .detail-value {
  color: #f3f4f6;
}

.requirement-badge {
  display: inline-block;
  padding: 2px 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 匹配度样式 */
.matching-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.matching-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.subject-name {
  width: 80px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .subject-name {
  color: #e5e7eb;
}

.matching-score-bar {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.dark .matching-score-bar {
  background: #374151;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.score-value {
  width: 45px;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 滚动条美化 */
.detail-container::-webkit-scrollbar {
  width: 6px;
}

.detail-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.dark .detail-container::-webkit-scrollbar-track {
  background: #1a1a2e;
}

.detail-container::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 10px;
}

.detail-container::-webkit-scrollbar-thumb:hover {
  background: #818cf8;
}

/* Element Plus 弹窗暗色模式覆盖 */
:deep(.dark-dialog .el-dialog) {
  background: #000000 !important;
  border: 1px solid #1a1a2e !important;
}

:deep(.dark-dialog .el-dialog__title) {
  color: #e5e7eb !important;
}

:deep(.dark-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #9ca3af !important;
}

:deep(.dark-dialog .el-dialog__body) {
  background: #000000 !important;
}

@media (max-width: 640px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-item.full-width {
    grid-column: span 1;
  }
}
</style>
