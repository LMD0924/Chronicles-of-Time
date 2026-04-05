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
      // 重置表单
      if (!form.value.id) {
        form.value = {
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
        }
      }
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

const getGuidanceTypeText = (type) => {
  const map = { '1': '个别咨询', '2': '团体辅导', '3': '家长会', '4': '讲座' }
  return map[type] || type
}

const getStatusClass = (status) => {
  const map = {
    1: 'status-progress',
    2: 'status-completed',
    3: 'status-adopted',
    4: 'status-abandoned'
  }
  return map[status] || 'status-default'
}

const getStatusText = (status) => {
  const map = { 1: '进行中', 2: '已完成', 3: '已采纳', 4: '已放弃' }
  return map[status] || status
}

watch(() => props.studentId, fetchList, { immediate: true })
</script>

<template>
  <div class="cs-panel guidance-container space-y-4">
    <!-- 指导表单 -->
    <div class="form-card">
      <div class="form-header">
        <div class="header-icon">📋</div>
        <h3 class="header-title">选科指导记录</h3>
        <span class="header-badge">新增/编辑</span>
      </div>

      <div class="form-body">
        <!-- 基本信息行 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">指导日期</label>
            <input v-model="form.guidanceDate" type="date" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">指导类型</label>
            <select v-model="form.guidanceType" class="form-select">
              <option value="1">个别咨询</option>
              <option value="2">团体辅导</option>
              <option value="3">家长会</option>
              <option value="4">讲座</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">状态</label>
            <select v-model="form.status" class="form-select">
              <option :value="1">进行中</option>
              <option :value="2">已完成</option>
              <option :value="3">已采纳</option>
              <option :value="4">已放弃</option>
            </select>
          </div>
        </div>

        <!-- 建议信息行 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">建议组合</label>
            <input v-model="form.suggestedCombination" class="form-input" placeholder="例：物化生" />
          </div>
          <div class="form-group">
            <label class="form-label">建议专业</label>
            <input v-model="form.suggestedMajor" class="form-input" placeholder="例：计算机科学与技术" />
          </div>
          <div class="form-group">
            <label class="form-label">下次跟进日期</label>
            <input v-model="form.followUpDate" type="date" class="form-input" />
          </div>
        </div>

        <!-- 指导老师信息 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">指导老师</label>
            <input v-model="form.advisorName" class="form-input" placeholder="老师姓名" />
          </div>
          <div class="form-group">
            <label class="form-label">老师职位</label>
            <input v-model="form.advisorPosition" class="form-input" placeholder="例：年级主任、班主任" />
          </div>
          <div class="form-group"></div>
        </div>

        <!-- 指导内容 -->
        <div class="form-group full-width">
          <label class="form-label">指导内容</label>
          <textarea v-model="form.guidanceContent" rows="2" class="form-textarea" placeholder="记录本次指导的主要内容..." />
        </div>

        <!-- SWOT分析标题 -->
        <div class="section-divider">
          <span class="divider-icon">📊</span>
          <span class="divider-text">SWOT分析</span>
        </div>

        <div class="swot-grid">
          <div class="swot-card strength">
            <div class="swot-header">
              <span class="swot-icon">💪</span>
              <span class="swot-title">优势分析</span>
            </div>
            <textarea v-model="form.strengthAnalysis" rows="2" class="swot-textarea" placeholder="学科优势、能力特长..." />
          </div>
          <div class="swot-card weakness">
            <div class="swot-header">
              <span class="swot-icon">⚠️</span>
              <span class="swot-title">劣势分析</span>
            </div>
            <textarea v-model="form.weaknessAnalysis" rows="2" class="swot-textarea" placeholder="薄弱科目、提升空间..." />
          </div>
          <div class="swot-card opportunity">
            <div class="swot-header">
              <span class="swot-icon">🎯</span>
              <span class="swot-title">机会分析</span>
            </div>
            <textarea v-model="form.opportunityAnalysis" rows="2" class="swot-textarea" placeholder="发展机会、有利条件..." />
          </div>
          <div class="swot-card threat">
            <div class="swot-header">
              <span class="swot-icon">⚡</span>
              <span class="swot-title">挑战分析</span>
            </div>
            <textarea v-model="form.threatAnalysis" rows="2" class="swot-textarea" placeholder="竞争压力、外部挑战..." />
          </div>
        </div>

        <!-- 行动计划 -->
        <div class="form-group full-width">
          <label class="form-label">
            <span class="label-icon">📝</span>
            行动计划
          </label>
          <textarea v-model="form.actionPlan" rows="2" class="form-textarea" placeholder="具体的行动步骤和时间安排..." />
        </div>

        <!-- 反馈信息 -->
        <div class="form-row">
          <div class="form-group flex-1">
            <label class="form-label">学生反馈</label>
            <textarea v-model="form.studentFeedback" rows="2" class="form-textarea" placeholder="学生的想法和意见..." />
          </div>
          <div class="form-group flex-1">
            <label class="form-label">家长反馈</label>
            <textarea v-model="form.parentFeedback" rows="2" class="form-textarea" placeholder="家长的看法和建议..." />
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button @click="submit" :disabled="saving" class="submit-btn">
            <span v-if="saving" class="btn-spinner"></span>
            <span v-else>💾</span>
            {{ saving ? '保存中...' : '保存指导记录' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 指导历史列表 -->
    <div class="history-card">
      <div class="history-header">
        <div class="header-icon">📜</div>
        <h3 class="header-title">指导历史</h3>
        <span class="history-count">{{ list.length }}条记录</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else-if="list.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无指导记录</p>
        <p class="empty-hint">点击上方表单添加第一条记录</p>
      </div>
      <div v-else class="history-list">
        <div v-for="item in list" :key="item.id" class="history-item">
          <div class="history-item-left">
            <div class="history-date">
              <span class="date-icon">📅</span>
              {{ item.guidanceDate }}
            </div>
            <div class="history-tags">
              <span class="tag type-tag">{{ getGuidanceTypeText(item.guidanceType) }}</span>
              <span v-if="item.suggestedCombination" class="tag combo-tag">{{ item.suggestedCombination }}</span>
              <span v-if="item.suggestedMajor" class="tag major-tag">{{ item.suggestedMajor }}</span>
              <span :class="['tag', 'status-tag', getStatusClass(item.status)]">
                {{ getStatusText(item.status) }}
              </span>
            </div>
            <div v-if="item.advisorName" class="history-advisor">
              <span class="advisor-icon">👨‍🏫</span>
              {{ item.advisorName }}
            </div>
          </div>
          <div class="history-item-right">
            <button class="action-btn detail" @click="openDetail(item.id)">
              🔍 详情
            </button>
            <button class="action-btn edit" @click="edit(item)">
              ✏️ 编辑
            </button>
            <button v-if="item.status !== 2" class="action-btn complete" @click="updateStatus(item, 2)">
              ✅ 完成
            </button>
            <button class="action-btn delete" @click="remove(item.id)">
              🗑️ 删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="选科指导详情" width="800px" :class="{ 'dark-dialog': isDark }">
      <div v-if="currentDetail" class="detail-dialog">
        <!-- 头部信息 -->
        <div class="detail-header">
          <div class="detail-student">
            <span class="detail-icon">👨‍🎓</span>
            {{ currentDetail.studentName }}
          </div>
          <div class="detail-date">
            <span class="detail-icon">📅</span>
            {{ currentDetail.guidanceDate }}
          </div>
          <span :class="['detail-status', getStatusClass(currentDetail.status)]">
            {{ getStatusText(currentDetail.status) }}
          </span>
        </div>

        <!-- 基本信息网格 -->
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">指导类型</span>
              <span class="info-value">{{ getGuidanceTypeText(currentDetail.guidanceType) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">建议组合</span>
              <span class="info-value combo-value">{{ currentDetail.suggestedCombination || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">建议专业</span>
              <span class="info-value major-value">{{ currentDetail.suggestedMajor || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">指导老师</span>
              <span class="info-value">{{ currentDetail.advisorName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">老师职位</span>
              <span class="info-value">{{ currentDetail.advisorPosition || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">下次跟进</span>
              <span class="info-value">{{ currentDetail.followUpDate || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 指导内容 -->
        <div v-if="currentDetail.guidanceContent" class="detail-section">
          <div class="section-title">指导内容</div>
          <div class="content-box">{{ currentDetail.guidanceContent }}</div>
        </div>

        <!-- SWOT分析 -->
        <div class="detail-section">
          <div class="section-title">SWOT分析</div>
          <div class="swot-detail-grid">
            <div class="swot-detail-item strength-bg">
              <div class="swot-detail-label">💪 优势</div>
              <div class="swot-detail-content">{{ currentDetail.strengthAnalysis || '-' }}</div>
            </div>
            <div class="swot-detail-item weakness-bg">
              <div class="swot-detail-label">⚠️ 劣势</div>
              <div class="swot-detail-content">{{ currentDetail.weaknessAnalysis || '-' }}</div>
            </div>
            <div class="swot-detail-item opportunity-bg">
              <div class="swot-detail-label">🎯 机会</div>
              <div class="swot-detail-content">{{ currentDetail.opportunityAnalysis || '-' }}</div>
            </div>
            <div class="swot-detail-item threat-bg">
              <div class="swot-detail-label">⚡ 挑战</div>
              <div class="swot-detail-content">{{ currentDetail.threatAnalysis || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 行动计划 -->
        <div v-if="currentDetail.actionPlan" class="detail-section">
          <div class="section-title">📝 行动计划</div>
          <div class="content-box">{{ currentDetail.actionPlan }}</div>
        </div>

        <!-- 反馈信息 -->
        <div class="detail-section">
          <div class="section-title">反馈信息</div>
          <div class="feedback-grid">
            <div class="feedback-item">
              <div class="feedback-label">👨‍🎓 学生反馈</div>
              <div class="feedback-content">{{ currentDetail.studentFeedback || '-' }}</div>
            </div>
            <div class="feedback-item">
              <div class="feedback-label">👪 家长反馈</div>
              <div class="feedback-content">{{ currentDetail.parentFeedback || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 时间信息 -->
        <div class="detail-footer">
          <span>创建时间：{{ currentDetail.createTime || '-' }}</span>
          <span>更新时间：{{ currentDetail.updateTime || '-' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 基础容器 */
.guidance-container {
  padding: 4px;
}

/* 卡片样式 */
.form-card, .history-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
}

.dark .form-card,
.dark .history-card {
  background: rgba(30, 41, 59, 0.6);
}

/* 卡片头部 */
.form-header, .history-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.05));
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark .form-header,
.dark .history-header {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.header-icon {
  font-size: 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.dark .header-title {
  color: #f3f4f6;
}

.header-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20px;
  font-size: 12px;
  color: white;
}

.history-count {
  font-size: 12px;
  padding: 4px 10px;
  background: rgba(99, 102, 241, 0.15);
  border-radius: 20px;
  color: #6366f1;
}

/* 表单主体 */
.form-body {
  padding: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-group {
  flex: 1;
  min-width: 180px;
}

.form-group.full-width {
  flex: 1 1 100%;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 6px;
}

.dark .form-label {
  color: #9ca3af;
}

.label-icon {
  margin-right: 4px;
}

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: white;
  font-size: 14px;
  transition: all 0.2s;
}

.dark .form-input,
.dark .form-select,
.dark .form-textarea {
  background: #1e293b;
  border-color: #334155;
  color: #e5e7eb;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* 分割线 */
.section-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.2);
}

.divider-icon {
  font-size: 18px;
}

.divider-text {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.dark .divider-text {
  color: #e5e7eb;
}

/* SWOT网格 */
.swot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.swot-card {
  border-radius: 16px;
  padding: 12px;
  transition: all 0.2s;
}

.swot-card.strength { background: rgba(16, 185, 129, 0.1); border-left: 3px solid #10b981; }
.swot-card.weakness { background: rgba(239, 68, 68, 0.1); border-left: 3px solid #ef4444; }
.swot-card.opportunity { background: rgba(59, 130, 246, 0.1); border-left: 3px solid #3b82f6; }
.swot-card.threat { background: rgba(245, 158, 11, 0.1); border-left: 3px solid #f59e0b; }

.swot-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.swot-icon {
  font-size: 16px;
}

.swot-title {
  font-size: 14px;
  font-weight: 600;
}

.swot-card.strength .swot-title { color: #10b981; }
.swot-card.weakness .swot-title { color: #ef4444; }
.swot-card.opportunity .swot-title { color: #3b82f6; }
.swot-card.threat .swot-title { color: #f59e0b; }

.swot-textarea {
  width: 100%;
  padding: 8px 10px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  resize: vertical;
}

.dark .swot-textarea {
  background: rgba(0, 0, 0, 0.2);
  color: #e5e7eb;
}

.swot-textarea:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.8);
}

/* 表单操作区 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

/* 历史列表 */
.history-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 14px;
  transition: all 0.2s;
}

.dark .history-item {
  background: rgba(15, 23, 42, 0.4);
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: translateX(4px);
}

.dark .history-item:hover {
  background: rgba(30, 41, 59, 0.8);
}

.history-item-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.history-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.date-icon {
  font-size: 12px;
}

.history-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.type-tag {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.combo-tag {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.major-tag {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.status-tag.status-progress { background: rgba(234, 179, 8, 0.1); color: #eab308; }
.status-tag.status-completed { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.status-tag.status-adopted { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.status-tag.status-abandoned { background: rgba(107, 114, 128, 0.1); color: #6b7280; }

.history-advisor {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.advisor-icon {
  font-size: 12px;
}

.history-item-right {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.action-btn.detail {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.action-btn.edit {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.action-btn.complete {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-btn:hover {
  transform: translateY(-1px);
}

/* 加载和空状态 */
.loading-state, .empty-state {
  text-align: center;
  padding: 48px;
  color: #6b7280;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 3px solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-hint {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.7;
}

/* 详情弹窗样式 */
.detail-dialog {
  max-height: 70vh;
  overflow-y: auto;
  padding: 4px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.05));
  border-radius: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.detail-student, .detail-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .detail-student,
.dark .detail-date {
  color: #e5e7eb;
}

.detail-icon {
  font-size: 16px;
}

.detail-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-left: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
  color: #1f2937;
}

.dark .section-title {
  color: #f3f4f6;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
  padding: 12px;
}

.dark .info-grid {
  background: rgba(30, 41, 59, 0.5);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}

.info-label {
  font-size: 12px;
  color: #6b7280;
}

.info-value {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.dark .info-value {
  color: #e5e7eb;
}

.combo-value, .major-value {
  color: #6366f1;
}

.content-box {
  padding: 12px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
}

.dark .content-box {
  background: rgba(30, 41, 59, 0.5);
  color: #e5e7eb;
}

.swot-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.swot-detail-item {
  padding: 12px;
  border-radius: 12px;
}

.swot-detail-item.strength-bg { background: rgba(16, 185, 129, 0.08); }
.swot-detail-item.weakness-bg { background: rgba(239, 68, 68, 0.08); }
.swot-detail-item.opportunity-bg { background: rgba(59, 130, 246, 0.08); }
.swot-detail-item.threat-bg { background: rgba(245, 158, 11, 0.08); }

.swot-detail-label {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.swot-detail-content {
  font-size: 13px;
  line-height: 1.4;
  color: #4b5563;
}

.dark .swot-detail-content {
  color: #d1d5db;
}

.feedback-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.feedback-item {
  padding: 12px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
}

.feedback-label {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #6b7280;
}

.feedback-content {
  font-size: 13px;
  line-height: 1.4;
  color: #374151;
}

.dark .feedback-content {
  color: #e5e7eb;
}

.detail-footer {
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  font-size: 11px;
  color: #9ca3af;
  display: flex;
  justify-content: space-between;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .swot-grid, .swot-detail-grid, .feedback-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-item-right {
    align-self: flex-end;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-status {
    margin-left: 0;
  }
}
</style>
