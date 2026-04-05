<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const loading = ref(false)
const histories = ref([])
const filterType = ref('all')
const startDate = ref('')
const endDate = ref('')
const statistics = ref({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0
})
const detailVisible = ref(false)
const detail = ref(null)

// 获取历史记录
const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get(`/history/student/${props.studentId}`)
    if (res.code === 200) {
      let data = res.data || []
      if (filterType.value !== 'all') {
        data = data.filter(h => h.changeType === filterType.value)
      }
      histories.value = data
    }

    // 获取统计
    const statsRes = await request.get('/history/statistics', { days: 30 })
    if (statsRes.code === 200) {
      statistics.value = statsRes.data
    }
  } catch (error) {
    console.error('获取历史失败', error)
  } finally {
    loading.value = false
  }
}

const getChangeTypeClass = (type) => {
  const map = {
    '1': 'change-type-add',
    '2': 'change-type-edit',
    '3': 'change-type-cancel',
    '4': 'change-type-confirm'
  }
  return map[type] || 'change-type-default'
}

const getChangeTypeText = (type) => {
  const map = {
    '1': '新增',
    '2': '修改',
    '3': '退选',
    '4': '确认'
  }
  return map[type] || type
}

const getChangeTypeIcon = (type) => {
  const map = {
    '1': '➕',
    '2': '✏️',
    '3': '🗑️',
    '4': '✅'
  }
  return map[type] || '📋'
}

const getApproveStatusClass = (status) => {
  if (status === 0) return 'status-pending'
  if (status === 1) return 'status-approved'
  if (status === 2) return 'status-rejected'
  return 'status-default'
}

const getApproveStatusText = (status) => {
  if (status === 0) return '待审批'
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '未知'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const openDetail = async (id) => {
  try {
    const res = await request.get(`/history/detail/${id}`)
    if (res.code === 200) {
      detail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

watch(() => props.studentId, (val) => {
  if (val) fetchHistory()
}, { immediate: true })
</script>

<template>
  <div class="cs-panel history-container space-y-4">
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">变更类型</span>
        <div class="filter-options">
          <button
            @click="filterType = 'all'; fetchHistory()"
            :class="['filter-btn', { active: filterType === 'all' }]">
            全部
          </button>
          <button
            @click="filterType = '1'; fetchHistory()"
            :class="['filter-btn', { active: filterType === '1' }]">
            ➕ 新增
          </button>
          <button
            @click="filterType = '2'; fetchHistory()"
            :class="['filter-btn', { active: filterType === '2' }]">
            ✏️ 修改
          </button>
          <button
            @click="filterType = '3'; fetchHistory()"
            :class="['filter-btn', { active: filterType === '3' }]">
            🗑️ 退选
          </button>
          <button
            @click="filterType = '4'; fetchHistory()"
            :class="['filter-btn', { active: filterType === '4' }]">
            ✅ 确认
          </button>
        </div>
      </div>

      <div class="filter-group">
        <span class="filter-label">时间范围</span>
        <div class="date-range">
          <input type="date" v-model="startDate" class="date-input" placeholder="开始日期">
          <span class="date-separator">至</span>
          <input type="date" v-model="endDate" class="date-input" placeholder="结束日期">
          <button @click="fetchHistory" class="search-btn">
            🔍 查询
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
          <div class="stat-label">总变更次数</div>
        </div>
      </div>
      <div class="stat-card stat-pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
          <div class="stat-label">待审批</div>
        </div>
      </div>
      <div class="stat-card stat-approved">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.approvedCount || 0 }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      <div class="stat-card stat-rejected">
        <div class="stat-icon">❌</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.rejectedCount || 0 }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>
    </div>

    <!-- 历史列表 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    <div v-else-if="histories.length === 0" class="empty-state">
      <div class="empty-icon">📭</div>
      <p>暂无历史记录</p>
    </div>
    <div v-else class="history-timeline">
      <div v-for="(item, index) in histories" :key="item.id" class="timeline-item">
        <!-- 时间线节点 -->
        <div class="timeline-node" :class="getChangeTypeClass(item.changeType)">
          <span class="node-icon">{{ getChangeTypeIcon(item.changeType) }}</span>
        </div>

        <!-- 时间线内容 -->
        <div class="timeline-content">
          <div class="content-header">
            <div class="header-left">
              <span :class="['change-badge', getChangeTypeClass(item.changeType)]">
                {{ getChangeTypeText(item.changeType) }}
              </span>
              <span class="change-time">
                <span class="time-icon">🕐</span>
                {{ formatDateTime(item.changeTime) }}
              </span>
            </div>
            <span :class="['status-badge', getApproveStatusClass(item.approveStatus)]">
              <span class="status-dot"></span>
              {{ getApproveStatusText(item.approveStatus) }}
            </span>
          </div>

          <!-- 变更内容 -->
          <div class="content-body">
            <div v-if="item.changeType === '1'" class="change-detail">
              <div class="detail-row">
                <span class="detail-label">选课组合</span>
                <div class="subject-combination">
                  <span class="subject-tag">{{ item.newFirstSubject }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag">{{ item.newSecondSubject1 }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag">{{ item.newSecondSubject2 }}</span>
                </div>
              </div>
            </div>

            <div v-else-if="item.changeType === '2'" class="change-detail">
              <div class="detail-row">
                <span class="detail-label">变更前</span>
                <div class="subject-combination old">
                  <span class="subject-tag old">{{ item.oldFirstSubject }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag old">{{ item.oldSecondSubject1 }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag old">{{ item.oldSecondSubject2 }}</span>
                </div>
              </div>
              <div class="detail-arrow">↓</div>
              <div class="detail-row">
                <span class="detail-label">变更后</span>
                <div class="subject-combination new">
                  <span class="subject-tag new">{{ item.newFirstSubject }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag new">{{ item.newSecondSubject1 }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag new">{{ item.newSecondSubject2 }}</span>
                </div>
              </div>
            </div>

            <div v-else-if="item.changeType === '3'" class="change-detail">
              <div class="detail-row">
                <span class="detail-label">退选组合</span>
                <div class="subject-combination cancelled">
                  <span class="subject-tag cancelled">{{ item.oldFirstSubject }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag cancelled">{{ item.oldSecondSubject1 }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag cancelled">{{ item.oldSecondSubject2 }}</span>
                </div>
              </div>
            </div>

            <div v-else-if="item.changeType === '4'" class="change-detail">
              <div class="detail-row">
                <span class="detail-label">确认组合</span>
                <div class="subject-combination confirmed">
                  <span class="subject-tag confirmed">{{ item.newFirstSubject }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag confirmed">{{ item.newSecondSubject1 }}</span>
                  <span class="subject-plus">+</span>
                  <span class="subject-tag confirmed">{{ item.newSecondSubject2 }}</span>
                </div>
              </div>
            </div>

            <div v-if="item.changeReason" class="detail-row">
              <span class="detail-label">变更原因</span>
              <span class="detail-value">{{ item.changeReason }}</span>
            </div>

            <div v-if="item.approveComment" class="detail-row">
              <span class="detail-label">审批意见</span>
              <span class="detail-value">{{ item.approveComment }}</span>
            </div>
          </div>

          <div class="content-footer">
            <button class="detail-btn" @click="openDetail(item.id)">
              <span class="btn-icon">📄</span>
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="变更详情" width="720px" :class="{ 'dark-dialog': isDark }">
      <div v-if="detail" class="detail-dialog">
        <div class="detail-header">
          <span :class="['detail-badge', getChangeTypeClass(detail.changeType)]">
            {{ getChangeTypeText(detail.changeType) }}
          </span>
          <span :class="['detail-badge', getApproveStatusClass(detail.approveStatus)]">
            {{ getApproveStatusText(detail.approveStatus) }}
          </span>
        </div>

        <div class="detail-info-grid">
          <div class="info-item">
            <span class="info-label">历史ID</span>
            <span class="info-value">{{ detail.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">选课记录ID</span>
            <span class="info-value">{{ detail.selectionId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">学生ID</span>
            <span class="info-value">{{ detail.studentId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">学生姓名</span>
            <span class="info-value">{{ detail.studentName }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">选课变更详情</div>
          <div class="change-compare">
            <div class="compare-old">
              <div class="compare-label">变更前</div>
              <div class="compare-value">{{ detail.oldFirstSubject || '-' }} + {{ detail.oldSecondSubject1 || '-' }} + {{ detail.oldSecondSubject2 || '-' }}</div>
            </div>
            <div class="compare-arrow">→</div>
            <div class="compare-new">
              <div class="compare-label">变更后</div>
              <div class="compare-value">{{ detail.newFirstSubject || '-' }} + {{ detail.newSecondSubject1 || '-' }} + {{ detail.newSecondSubject2 || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">审批信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">审批人</span>
              <span class="info-value">{{ detail.approver || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">审批意见</span>
              <span class="info-value">{{ detail.approveComment || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">变更原因</span>
              <span class="info-value">{{ detail.changeReason || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">变更时间</span>
              <span class="info-value">{{ formatDateTime(detail.changeTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.history-container {
  padding: 4px;
}

/* 筛选栏样式 */
.filter-bar {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.dark .filter-bar {
  background: rgba(30, 41, 59, 0.5);
}

.filter-group {
  margin-bottom: 12px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 8px;
}

.dark .filter-label {
  color: #9ca3af;
}

.filter-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.dark .filter-btn {
  background: #1e293b;
  color: #94a3b8;
  border-color: #334155;
}

.filter-btn:hover {
  background: #e5e7eb;
}

.filter-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-color: transparent;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.date-input {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: white;
  font-size: 13px;
}

.dark .date-input {
  background: #1e293b;
  border-color: #334155;
  color: #e5e7eb;
}

.date-separator {
  color: #9ca3af;
  font-size: 12px;
}

.search-btn {
  padding: 8px 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

/* 统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.2s;
}

.dark .stat-card {
  background: rgba(30, 41, 59, 0.5);
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.dark .stat-label {
  color: #9ca3af;
}

.stat-pending .stat-value { color: #eab308; }
.stat-approved .stat-value { color: #10b981; }
.stat-rejected .stat-value { color: #ef4444; }

/* 时间线样式 */
.history-timeline {
  position: relative;
  padding-left: 30px;
}

.timeline-item {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  position: relative;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -20px;
  top: 32px;
  bottom: -24px;
  width: 2px;
  background: linear-gradient(180deg, #6366f1, #8b5cf6, transparent);
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-node {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.node-icon {
  font-size: 20px;
}

.timeline-content {
  flex: 1;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.2s;
}

.dark .timeline-content {
  background: rgba(30, 41, 59, 0.6);
}

.timeline-content:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.change-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.change-time {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dark .change-time {
  color: #9ca3af;
}

.time-icon {
  font-size: 12px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* 变更类型颜色 */
.change-type-add { background: rgba(16, 185, 129, 0.15); color: #10b981; }
.change-type-edit { background: rgba(245, 158, 11, 0.15); color: #f59e0b; }
.change-type-cancel { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
.change-type-confirm { background: rgba(59, 130, 246, 0.15); color: #3b82f6; }

/* 审批状态颜色 */
.status-pending { background: rgba(234, 179, 8, 0.15); color: #eab308; }
.status-approved { background: rgba(16, 185, 129, 0.15); color: #10b981; }
.status-rejected { background: rgba(239, 68, 68, 0.15); color: #ef4444; }

.content-body {
  margin-bottom: 12px;
}

.change-detail {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 12px;
  padding: 12px;
}

.dark .change-detail {
  background: rgba(255, 255, 255, 0.05);
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  min-width: 70px;
}

.dark .detail-label {
  color: #9ca3af;
}

.detail-value {
  font-size: 13px;
  color: #374151;
}

.dark .detail-value {
  color: #e5e7eb;
}

.subject-combination {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.subject-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.subject-tag.old {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  text-decoration: line-through;
}

.subject-tag.new {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.subject-tag.cancelled {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.subject-tag.confirmed {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.subject-plus {
  font-size: 14px;
  color: #9ca3af;
}

.detail-arrow {
  text-align: center;
  font-size: 18px;
  color: #6366f1;
  margin: 4px 0;
}

.content-footer {
  display: flex;
  justify-content: flex-end;
}

.detail-btn {
  padding: 6px 12px;
  border-radius: 8px;
  background: transparent;
  color: #6366f1;
  border: 1px solid #6366f1;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.detail-btn:hover {
  background: #6366f1;
  color: white;
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 详情弹窗样式 */
.detail-dialog {
  padding: 8px;
}

.detail-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.detail-badge {
  padding: 6px 16px;
  border-radius: 24px;
  font-size: 13px;
  font-weight: 600;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
}

.dark .detail-info-grid {
  background: rgba(30, 41, 59, 0.5);
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
}

.info-grid {
  display: grid;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark .info-item {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.info-label {
  font-size: 13px;
  color: #6b7280;
}

.info-value {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.dark .info-value {
  color: #f3f4f6;
}

.change-compare {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  background: rgba(243, 244, 246, 0.5);
  border-radius: 12px;
}

.dark .change-compare {
  background: rgba(30, 41, 59, 0.5);
}

.compare-old, .compare-new {
  flex: 1;
  text-align: center;
}

.compare-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.compare-value {
  font-size: 14px;
  font-weight: 600;
}

.compare-old .compare-value {
  color: #ef4444;
  text-decoration: line-through;
}

.compare-new .compare-value {
  color: #10b981;
}

.compare-arrow {
  font-size: 24px;
  color: #6366f1;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .history-timeline {
    padding-left: 0;
  }

  .timeline-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .timeline-item::before {
    left: 22px;
    top: 44px;
  }

  .detail-info-grid {
    grid-template-columns: 1fr;
  }

  .change-compare {
    flex-direction: column;
  }

  .compare-arrow {
    transform: rotate(90deg);
  }
}
</style>
