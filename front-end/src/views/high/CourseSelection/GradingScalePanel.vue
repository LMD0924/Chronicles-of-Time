以下是修改后的代码，暗色模式下所有输入框都改为纯黑色背景。
```vue
<template>
  <div class="cs-panel space-y-4">
    <div class="rounded-xl bg-white/70 dark:bg-black border border-white/30 p-4">
      <div class="flex gap-2 mb-3">
        <input v-model="year" class="px-3 py-2 rounded-lg border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="学年" />
        <button class="px-3 py-2 rounded-lg bg-indigo-600 text-white" @click="fetchList">查询</button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
        <input v-model="form.subjectId" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="科目ID" />
        <input v-model="form.subjectName" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="科目名" />
        <input v-model="form.gradeLevel" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="等级" />
        <input v-model="form.academicYear" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="学年" />
        <input v-model="form.percentageTop" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="前百分比" />
        <input v-model="form.percentageBottom" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="后百分比" />
        <input v-model="form.assignedScoreMin" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="赋分最小" />
        <input v-model="form.assignedScoreMax" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="赋分最大" />
        <input v-model="form.rawScoreMin" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="原始分最小" />
        <input v-model="form.rawScoreMax" class="px-2 py-2 rounded border dark:bg-black dark:border-gray-700 dark:text-white" placeholder="原始分最大" />
      </div>
      <div class="mt-3"><button class="px-4 py-2 rounded-lg bg-emerald-600 text-white" @click="save">保存</button></div>
    </div>
    <div class="rounded-xl bg-white/70 dark:bg-black border border-white/30 p-4">
      <h4 class="font-semibold mb-2 dark:text-white">赋分规则列表</h4>
      <div v-for="item in list" :key="item.id" class="p-2 flex justify-between border-b dark:border-gray-800">
        <span class="dark:text-gray-300">{{ item.subjectName }} {{ item.gradeLevel }} {{ item.rawScoreMin }}-{{ item.rawScoreMax }}</span>
        <div class="flex gap-2">
          <button class="text-blue-600 text-sm" @click="openDetail(item.id)">详情</button>
          <button class="text-indigo-600 text-sm" @click="edit(item)">编辑</button>
          <button class="text-red-600 text-sm" @click="remove(item.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 美化后的详情弹窗 -->
    <el-dialog v-model="detailVisible" title="赋分详情" width="680px" :class="{ 'dark-dialog': isDark }">
      <div v-if="detail" class="detail-container">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">📊</span>
            <span>基本信息</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">科目ID：</span>
              <span class="detail-value">{{ detail.subjectId || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">科目名称：</span>
              <span class="detail-value">{{ detail.subjectName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">等级：</span>
              <span class="detail-value grade-badge">{{ detail.gradeLevel || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">学年：</span>
              <span class="detail-value">{{ detail.academicYear || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 百分比区间 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">📈</span>
            <span>百分比区间</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">前百分比：</span>
              <span class="detail-value">{{ detail.percentageTop }}%</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">后百分比：</span>
              <span class="detail-value">{{ detail.percentageBottom }}%</span>
            </div>
          </div>
          <div class="progress-bar-container mt-2">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (detail.percentageTop || 0) + '%' }"></div>
            </div>
            <div class="progress-labels">
              <span>前{{ detail.percentageTop }}%</span>
              <span>后{{ detail.percentageBottom }}%</span>
            </div>
          </div>
        </div>

        <!-- 分数区间 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">🎯</span>
            <span>分数区间</span>
          </div>
          <div class="score-range">
            <div class="range-box">
              <div class="range-label">原始分区间</div>
              <div class="range-value">{{ detail.rawScoreMin || 0 }} — {{ detail.rawScoreMax || 0 }} 分</div>
            </div>
            <div class="range-arrow">→</div>
            <div class="range-box">
              <div class="range-label">赋分区间</div>
              <div class="range-value">{{ detail.assignedScoreMin || 0 }} — {{ detail.assignedScoreMax || 0 }} 分</div>
            </div>
          </div>
        </div>

        <!-- 状态信息 -->
        <div class="detail-section">
          <div class="section-title">
            <span class="title-icon">⚙️</span>
            <span>状态信息</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">启用状态：</span>
              <span class="detail-value">
                <span :class="detail.isActive ? 'status-active' : 'status-inactive'">
                  {{ detail.isActive ? '已启用' : '已禁用' }}
                </span>
              </span>
            </div>
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

<style scoped>
/* 详情弹窗样式 */
.detail-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 4px;
}

.detail-section {
  margin-bottom: 20px;
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
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
  color: #1f2937;
}

.dark .section-title {
  color: #e5e7eb;
}

.title-icon {
  font-size: 18px;
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

.grade-badge {
  display: inline-block;
  padding: 2px 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* 进度条样式 */
.progress-bar-container {
  margin-top: 8px;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.dark .progress-bar {
  background: #374151;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 10px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

.dark .progress-labels {
  color: #9ca3af;
}

/* 分数区间样式 */
.score-range {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.range-box {
  flex: 1;
  text-align: center;
  padding: 12px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 12px;
}

.dark .range-box {
  background: rgba(99, 102, 241, 0.15);
}

.range-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.dark .range-label {
  color: #9ca3af;
}

.range-value {
  font-size: 16px;
  font-weight: 600;
  color: #6366f1;
}

.range-arrow {
  font-size: 24px;
  color: #6366f1;
}

/* 状态标签 */
.status-active {
  display: inline-block;
  padding: 2px 10px;
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.dark .status-active {
  background: rgba(16, 185, 129, 0.25);
  color: #34d399;
}

.status-inactive {
  display: inline-block;
  padding: 2px 10px;
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
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

  .score-range {
    flex-direction: column;
  }

  .range-arrow {
    transform: rotate(90deg);
  }
}
</style>
```
