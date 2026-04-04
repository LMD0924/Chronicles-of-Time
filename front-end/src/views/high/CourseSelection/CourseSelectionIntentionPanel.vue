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
      // 重置表单（如果是新增）
      if (!form.value.id) {
        form.value = {
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
        }
      }
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

const getStatusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-evaluated',
    2: 'status-confirmed',
    3: 'status-adopted'
  }
  return map[status] || 'status-default'
}

const getStatusText = (status) => {
  const map = { 0: '待评估', 1: '已评估', 2: '已确认', 3: '已采纳' }
  return map[status] || status
}

watch(() => props.studentId, fetchList, { immediate: true })
</script>

<template>
  <div class="intention-container space-y-4">
    <!-- 意向填报表单 -->
    <div class="form-card">
      <div class="form-header">
        <div class="header-icon">📝</div>
        <h3 class="header-title">选课意向填报</h3>
        <span class="header-badge">3+1+2</span>
      </div>

      <div class="form-body">
        <!-- 基本信息行 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">年级</label>
            <select v-model="form.grade" class="form-select">
              <option value="高一">高一</option>
              <option value="高二">高二</option>
              <option value="高三">高三</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">首选科目</label>
            <div class="subject-toggle">
              <button
                @click="form.firstSubjectIntention = '物理'"
                :class="['toggle-btn', { active: form.firstSubjectIntention === '物理' }]">
                📖 物理
              </button>
              <button
                @click="form.firstSubjectIntention = '历史'"
                :class="['toggle-btn', { active: form.firstSubjectIntention === '历史' }]">
                📜 历史
              </button>
            </div>
          </div>
        </div>

        <!-- 再选科目 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">再选科目意向1</label>
            <select v-model="form.secondSubjectIntention1" class="form-select">
              <option value="">请选择</option>
              <option value="化学">化学</option>
              <option value="生物">生物</option>
              <option value="政治">政治</option>
              <option value="地理">地理</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">再选科目意向2</label>
            <select v-model="form.secondSubjectIntention2" class="form-select">
              <option value="">请选择</option>
              <option value="化学">化学</option>
              <option value="生物">生物</option>
              <option value="政治">政治</option>
              <option value="地理">地理</option>
            </select>
          </div>
        </div>

        <!-- 备选科目 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">备选科目1</label>
            <select v-model="form.secondSubjectBackup1" class="form-select">
              <option value="">请选择</option>
              <option value="化学">化学</option>
              <option value="生物">生物</option>
              <option value="政治">政治</option>
              <option value="地理">地理</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">备选科目2</label>
            <select v-model="form.secondSubjectBackup2" class="form-select">
              <option value="">请选择</option>
              <option value="化学">化学</option>
              <option value="生物">生物</option>
              <option value="政治">政治</option>
              <option value="地理">地理</option>
            </select>
          </div>
        </div>

        <!-- 目标信息 -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">🎯 目标专业</label>
            <input v-model="form.targetMajor" class="form-input" placeholder="例：计算机科学与技术" />
          </div>
          <div class="form-group">
            <label class="form-label">🏫 目标大学</label>
            <input v-model="form.targetUniversity" class="form-input" placeholder="例：清华大学" />
          </div>
        </div>

        <!-- 学情分析 -->
        <div class="section-divider">
          <span class="divider-icon">📊</span>
          <span class="divider-text">学情分析</span>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">优势科目</label>
            <input v-model="form.strengthSubjects" class="form-input" placeholder="例：数学、物理" />
          </div>
          <div class="form-group">
            <label class="form-label">弱势科目</label>
            <input v-model="form.weakSubjects" class="form-input" placeholder="例：英语、化学" />
          </div>
          <div class="form-group">
            <label class="form-label">职业兴趣</label>
            <input v-model="form.careerInterest" class="form-input" placeholder="例：程序员、医生" />
          </div>
        </div>

        <!-- 意向理由 -->
        <div class="form-group full-width">
          <label class="form-label">💡 选课理由</label>
          <textarea v-model="form.intentionReason" rows="2" class="form-textarea" placeholder="请说明选择该组合的原因..." />
        </div>

        <!-- 反馈信息 -->
        <div class="section-divider">
          <span class="divider-icon">💬</span>
          <span class="divider-text">反馈信息</span>
        </div>

        <div class="form-row">
          <div class="form-group flex-1">
            <label class="form-label">👨‍🏫 老师反馈</label>
            <textarea v-model="form.teacherFeedback" rows="2" class="form-textarea" placeholder="老师的建议和评价..." />
          </div>
          <div class="form-group flex-1">
            <label class="form-label">👪 家长反馈</label>
            <textarea v-model="form.parentFeedback" rows="2" class="form-textarea" placeholder="家长的意见和期望..." />
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button @click="submit" :disabled="saving" class="submit-btn">
            <span v-if="saving" class="btn-spinner"></span>
            <span v-else>💾</span>
            {{ saving ? '保存中...' : '保存意向' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 意向记录列表 -->
    <div class="history-card">
      <div class="history-header">
        <div class="header-icon">📋</div>
        <h3 class="header-title">我的意向记录</h3>
        <span class="history-count">{{ intentions.length }}条记录</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else-if="intentions.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无意向记录</p>
        <p class="empty-hint">点击上方表单添加第一条意向</p>
      </div>
      <div v-else class="history-list">
        <div v-for="item in intentions" :key="item.id" class="history-item">
          <div class="history-item-left">
            <div class="history-combination">
              <span class="first-subject">{{ item.firstSubjectIntention }}</span>
              <span class="subject-plus">+</span>
              <span class="second-subject">{{ item.secondSubjectIntention1 }}</span>
              <span class="subject-plus">+</span>
              <span class="second-subject">{{ item.secondSubjectIntention2 }}</span>
            </div>
            <div class="history-tags">
              <span v-if="item.targetMajor" class="tag major-tag">{{ item.targetMajor }}</span>
              <span :class="['tag', 'status-tag', getStatusClass(item.status)]">
                {{ getStatusText(item.status) }}
              </span>
            </div>
          </div>
          <div class="history-item-right">
            <button class="action-btn detail" @click="openDetail(item.id)">
              🔍 详情
            </button>
            <button class="action-btn edit" @click="edit(item)">
              ✏️ 编辑
            </button>
            <button v-if="item.status !== 2 && item.status !== 3" class="action-btn confirm" @click="updateStatus(item, 2)">
              ✅ 确认
            </button>
            <button class="action-btn delete" @click="remove(item.id)">
              🗑️ 删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="选课意向详情" width="800px" :class="{ 'dark-dialog': isDark }">
      <div v-if="currentDetail" class="detail-dialog">
        <!-- 头部信息 -->
        <div class="detail-header">
          <div class="detail-student">
            <span class="detail-icon">👨‍🎓</span>
            {{ currentDetail.studentName }}
          </div>
          <div class="detail-grade">
            <span class="detail-icon">📚</span>
            {{ currentDetail.grade }}
          </div>
          <span :class="['detail-status', getStatusClass(currentDetail.status)]">
            {{ getStatusText(currentDetail.status) }}
          </span>
        </div>

        <!-- 选课组合 -->
        <div class="detail-section combo-section">
          <div class="section-title">选课组合</div>
          <div class="combo-display">
            <div class="combo-item first">
              <span class="combo-label">首选</span>
              <span class="combo-value">{{ currentDetail.firstSubjectIntention }}</span>
            </div>
            <div class="combo-arrow">+</div>
            <div class="combo-item second">
              <span class="combo-label">再选1</span>
              <span class="combo-value">{{ currentDetail.secondSubjectIntention1 || '-' }}</span>
            </div>
            <div class="combo-arrow">+</div>
            <div class="combo-item second">
              <span class="combo-label">再选2</span>
              <span class="combo-value">{{ currentDetail.secondSubjectIntention2 || '-' }}</span>
            </div>
          </div>
          <div v-if="currentDetail.secondSubjectBackup1 || currentDetail.secondSubjectBackup2" class="backup-info">
            <span class="backup-label">备选组合：</span>
            <span>{{ currentDetail.secondSubjectBackup1 || '-' }} / {{ currentDetail.secondSubjectBackup2 || '-' }}</span>
          </div>
        </div>

        <!-- 目标信息 -->
        <div class="detail-section">
          <div class="section-title">目标信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">🎯 目标专业</span>
              <span class="info-value">{{ currentDetail.targetMajor || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">🏫 目标大学</span>
              <span class="info-value">{{ currentDetail.targetUniversity || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 学情分析 -->
        <div class="detail-section">
          <div class="section-title">学情分析</div>
          <div class="analysis-grid">
            <div class="analysis-item strength">
              <div class="analysis-label">💪 优势科目</div>
              <div class="analysis-content">{{ currentDetail.strengthSubjects || '-' }}</div>
            </div>
            <div class="analysis-item weakness">
              <div class="analysis-label">⚠️ 弱势科目</div>
              <div class="analysis-content">{{ currentDetail.weakSubjects || '-' }}</div>
            </div>
            <div class="analysis-item interest">
              <div class="analysis-label">🎨 职业兴趣</div>
              <div class="analysis-content">{{ currentDetail.careerInterest || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 意向理由 -->
        <div v-if="currentDetail.intentionReason" class="detail-section">
          <div class="section-title">💡 意向理由</div>
          <div class="content-box">{{ currentDetail.intentionReason }}</div>
        </div>

        <!-- 反馈信息 -->
        <div class="detail-section">
          <div class="section-title">反馈信息</div>
          <div class="feedback-grid">
            <div class="feedback-item">
              <div class="feedback-label">👨‍🏫 老师反馈</div>
              <div class="feedback-content">{{ currentDetail.teacherFeedback || '-' }}</div>
            </div>
            <div class="feedback-item">
              <div class="feedback-label">👪 家长反馈</div>
              <div class="feedback-content">{{ currentDetail.parentFeedback || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 评估信息 -->
        <div v-if="currentDetail.evaluateBy || currentDetail.evaluateTime" class="detail-section">
          <div class="section-title">评估信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">评估人</span>
              <span class="info-value">{{ currentDetail.evaluateBy || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">评估时间</span>
              <span class="info-value">{{ currentDetail.evaluateTime || '-' }}</span>
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
.intention-container {
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

/* 科目切换按钮 */
.subject-toggle {
  display: flex;
  gap: 12px;
}

.toggle-btn {
  flex: 1;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.dark .toggle-btn {
  background: #1e293b;
  border-color: #334155;
  color: #e5e7eb;
}

.toggle-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: transparent;
  color: white;
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
  padding: 10px 28px;
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

.history-combination {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.first-subject {
  padding: 4px 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20px;
  color: white;
}

.second-subject {
  padding: 4px 12px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 20px;
  color: #6366f1;
}

.subject-plus {
  color: #9ca3af;
  font-weight: normal;
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

.major-tag {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.status-tag.status-pending { background: rgba(234, 179, 8, 0.1); color: #eab308; }
.status-tag.status-evaluated { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.status-tag.status-confirmed { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.status-tag.status-adopted { background: rgba(99, 102, 241, 0.1); color: #6366f1; }

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

.action-btn.confirm {
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

.detail-student, .detail-grade {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .detail-student,
.dark .detail-grade {
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

/* 组合展示 */
.combo-section {
  background: rgba(243, 244, 246, 0.3);
  border-radius: 16px;
  padding: 16px;
}

.combo-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.combo-item {
  text-align: center;
  padding: 12px 24px;
  border-radius: 16px;
  min-width: 100px;
}

.combo-item.first {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
}

.combo-item.second {
  background: rgba(99, 102, 241, 0.1);
}

.combo-label {
  display: block;
  font-size: 11px;
  margin-bottom: 6px;
  opacity: 0.8;
}

.combo-value {
  font-size: 18px;
  font-weight: 600;
}

.combo-arrow {
  font-size: 24px;
  color: #6366f1;
  font-weight: bold;
}

.backup-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}

.backup-label {
  font-weight: 500;
}

/* 信息网格 */
.info-grid, .analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
  padding: 12px;
}

.dark .info-grid,
.dark .analysis-grid {
  background: rgba(30, 41, 59, 0.5);
}

.analysis-item {
  padding: 8px;
  border-radius: 10px;
}

.analysis-item.strength { background: rgba(16, 185, 129, 0.08); }
.analysis-item.weakness { background: rgba(239, 68, 68, 0.08); }
.analysis-item.interest { background: rgba(245, 158, 11, 0.08); }

.analysis-label {
  font-size: 11px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #6b7280;
}

.analysis-content {
  font-size: 13px;
  font-weight: 500;
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
  .analysis-grid, .feedback-grid {
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

  .combo-display {
    flex-direction: column;
    gap: 8px;
  }

  .combo-arrow {
    transform: rotate(90deg);
  }
}
</style>
