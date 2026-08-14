<!--
  文件说明：拾光记前台应用高中阶段页面组件，承载高中阶段场景的界面展示、交互操作和数据承接。
-->
<template>
  <div class="space-y-8">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="stat-card" :class="[isDark ? 'stat-card-dark' : 'stat-card-light']">
        <div class="stat-card-inner">
          <div>
            <p class="stat-label">方案总数</p>
            <p class="stat-value">{{ volunteerPlans.length }}</p>
          </div>
          <div class="stat-icon-wrapper bg-brand-500/20">
            <span class="text-2xl">📋</span>
          </div>
        </div>
        <div class="stat-progress-bar" style="width: 0%"></div>
      </div>

      <div class="stat-card" :class="[isDark ? 'stat-card-dark' : 'stat-card-light']">
        <div class="stat-card-inner">
          <div>
            <p class="stat-label">已提交方案</p>
            <p class="stat-value">{{ volunteerPlans.filter(p => p.isFinal).length }}</p>
          </div>
          <div class="stat-icon-wrapper bg-green-500/20">
            <span class="text-2xl">✅</span>
          </div>
        </div>
      </div>

      <div class="stat-card" :class="[isDark ? 'stat-card-dark' : 'stat-card-light']">
        <div class="stat-card-inner">
          <div>
            <p class="stat-label">平均分数</p>
            <p class="stat-value">{{ avgScore || '--' }}</p>
          </div>
          <div class="stat-icon-wrapper bg-fuchsia-500/20">
            <span class="text-2xl">📊</span>
          </div>
        </div>
      </div>

      <div class="stat-card" :class="[isDark ? 'stat-card-dark' : 'stat-card-light']">
        <div class="stat-card-inner">
          <div>
            <p class="stat-label">志愿总数</p>
            <p class="stat-value">{{ volunteerDetails.length }}</p>
          </div>
          <div class="stat-icon-wrapper bg-orange-500/20">
            <span class="text-2xl">🎯</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 方案列表 -->
    <div class="rounded-2xl overflow-hidden shadow-xl" :class="[isDark ? 'bg-gray-800/50 border-gray-700' : 'bg-white border-gray-200', 'backdrop-blur-xl border']">
      <div class="px-8 py-6 border-b flex flex-wrap justify-between items-center gap-5" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-xl font-semibold mb-1">我的志愿方案</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">管理您的高考志愿填报方案</p>
        </div>
        <button
          @click="openPlanModal('add')"
          class="create-btn"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          新建方案
        </button>
      </div>

      <div class="p-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="plan in volunteerPlans"
            :key="plan.id"
            @click="selectPlan(plan)"
            class="plan-card group"
            :class="[
              selectedPlan?.id === plan.id
                ? 'plan-card-selected'
                : isDark ? 'plan-card-dark' : 'plan-card-light'
            ]"
          >
            <div class="plan-card-header">
              <div class="plan-icon" :class="plan.isFinal ? 'plan-icon-final' : 'plan-icon-draft'">
                {{ plan.isFinal ? '✅' : '📝' }}
              </div>
              <div class="plan-actions">
                <button @click.stop="editPlan(plan)" class="action-btn edit-btn">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                  </svg>
                </button>
                <button @click.stop="deletePlan(plan.id)" class="action-btn delete-btn">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
            </div>

            <div class="plan-card-body">
              <h3 class="plan-name">{{ plan.name || `方案${plan.year}` }}</h3>
              <p class="plan-meta">{{ plan.year }}年 · {{ plan.province }}</p>
              <div class="plan-stats">
                <div class="plan-stat">
                  <span class="plan-stat-label">高考分数</span>
                  <span class="plan-stat-value">{{ plan.score }}分</span>
                </div>
                <div class="plan-stat">
                  <span class="plan-stat-label">全省排名</span>
                  <span class="plan-stat-rank">{{ plan.rank?.toLocaleString() }}名</span>
                </div>
              </div>
            </div>

            <div class="plan-card-footer">
              <span class="plan-status" :class="plan.isFinal ? 'status-final' : 'status-draft'">
                {{ plan.isFinal ? '已提交' : '草稿' }}
              </span>
              <span class="plan-time">{{ formatDate(plan.submitTime) }}</span>
            </div>
          </div>
        </div>

        <div v-if="volunteerPlans.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <p class="empty-text">暂无志愿方案，点击上方按钮创建</p>
        </div>
      </div>
    </div>

    <!-- 志愿详情 -->
    <div v-if="selectedPlan" class="rounded-2xl overflow-hidden shadow-xl" :class="[isDark ? 'bg-gray-800/50 border-gray-700' : 'bg-white border-gray-200', 'backdrop-blur-xl border']">
      <div class="px-8 py-6 border-b" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
        <div class="flex flex-wrap justify-between items-center gap-4">
          <div>
            <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-xl font-semibold mb-1">
              志愿详情 - {{ selectedPlan.name || selectedPlan.year }}
            </h2>
            <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">管理该方案下的所有志愿填报顺序</p>
          </div>
          <div class="flex flex-wrap gap-3">
            <button @click="openMatchingReport" class="btn-outline-purple">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
              匹配报告
            </button>
            <button @click="openStatistics" class="btn-outline-orange">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
              统计分析
            </button>
            <button @click="openDetailModal('add')" class="btn-success">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
              添加志愿
            </button>
            <button @click="openBatchModal" class="btn-primary">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
              </svg>
              批量添加
            </button>
          </div>
        </div>
      </div>

      <div class="p-8">
        <AiInsightPanel
          class="mb-6"
          scenario="volunteer"
          title="AI 志愿方案分析"
          description="基于当前方案和志愿明细，检查梯度、保底、选科匹配和专业风险。"
          button-text="分析当前方案"
          :is-dark="isDark"
          :payload="planAiPayload"
          :disabled="!selectedPlan"
        />

        <div class="space-y-4">
          <div v-for="detail in volunteerDetails" :key="detail.id" class="volunteer-item group">
            <div class="volunteer-priority">
              <div class="priority-number">{{ detail.priority }}</div>
              <div class="priority-line"></div>
            </div>
            <div class="volunteer-content" :class="isDark ? 'volunteer-content-dark' : 'volunteer-content-light'">
              <div class="volunteer-header">
                <div class="volunteer-info">
                  <h4 class="university-name">{{ detail.universityName || '加载中...' }}</h4>
                  <p class="major-name">{{ detail.majorName || '加载中...' }}</p>
                  <div class="volunteer-tags">
                    <span class="tag tag-gray">
                      服从调剂: {{ detail.isMajorAdjusted ? '是' : '否' }}
                    </span>
                    <span class="tag" :class="detail.matchingCheck ? 'tag-success' : 'tag-danger'">
                      {{ detail.matchingCheck ? '✓ 选科匹配' : '✗ 选科不匹配' }}
                    </span>
                  </div>
                </div>
                <div class="volunteer-actions">
                  <button @click="checkMatching(detail.id)" class="action-icon-btn text-blue-500">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </button>
                  <button @click="singleSimulate(detail.id)" class="action-icon-btn text-orange-500">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                    </svg>
                  </button>
                  <button @click="editDetail(detail)" class="action-icon-btn text-gray-500">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                    </svg>
                  </button>
                  <button @click="deleteDetail(detail.id)" class="action-icon-btn text-red-500">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="volunteerDetails.length === 0" class="empty-state">
          <div class="empty-icon">🎯</div>
          <p class="empty-text">暂无志愿，点击上方按钮添加</p>
        </div>
      </div>
    </div>

    <!-- 方案弹窗 -->
    <transition name="modal-fade">
      <div v-if="showPlanModal" class="modal-overlay" @click.self="showPlanModal = false">
        <div class="modal-shell modal-shell-md" :class="isDark ? 'modal-shell-dark' : 'modal-shell-light'">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">规划未来</p>
              <h3 class="modal-title">{{ planModalTitle }}</h3>
            </div>
            <button type="button" class="modal-close" aria-label="关闭" @click="showPlanModal = false">
              <span>×</span>
            </button>
          </div>

          <div class="modal-body">
            <div class="modal-form modal-form-grid">
              <label class="field field-wide">
                <span class="field-label">方案名称</span>
                <input v-model="planForm.name" type="text" class="field-input" placeholder="请输入方案名称" />
              </label>
              <label class="field">
                <span class="field-label">年份</span>
                <input v-model.number="planForm.year" type="number" min="2020" max="2030" class="field-input" />
              </label>
              <label class="field">
                <span class="field-label">省份</span>
                <input v-model="planForm.province" type="text" class="field-input" placeholder="例如：浙江" />
              </label>
              <label class="field">
                <span class="field-label">分数</span>
                <input v-model.number="planForm.score" type="number" min="0" max="750" class="field-input" placeholder="0-750" />
              </label>
              <label class="field">
                <span class="field-label">位次</span>
                <input v-model.number="planForm.rank" type="number" min="0" class="field-input" placeholder="请输入位次" />
              </label>
              <label class="field field-wide switch-field">
                <span>
                  <span class="field-label">最终方案</span>
                  <span class="field-hint">用于标记当前最想保留的一版规划</span>
                </span>
                <span class="switch-toggle">
                  <input v-model="planForm.isFinal" type="checkbox" />
                  <span class="switch-track"><span class="switch-thumb"></span></span>
                  <span class="switch-text">{{ planForm.isFinal ? '已标记' : '未标记' }}</span>
                </span>
              </label>
            </div>
          </div>

          <div class="dialog-footer">
            <button type="button" @click="showPlanModal = false" class="dialog-btn cancel">取消</button>
            <button type="button" @click="submitPlan" class="dialog-btn confirm">保存</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 志愿弹窗 -->
    <transition name="modal-fade">
      <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
        <div class="modal-shell modal-shell-md" :class="isDark ? 'modal-shell-dark' : 'modal-shell-light'">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">志愿管理</p>
              <h3 class="modal-title">{{ detailModalTitle }}</h3>
            </div>
            <button type="button" class="modal-close" aria-label="关闭" @click="showDetailModal = false">
              <span>×</span>
            </button>
          </div>

          <div class="modal-body">
            <div class="modal-form modal-form-grid">
              <label class="field">
                <span class="field-label">院校 ID</span>
                <input v-model.number="detailForm.universityId" type="number" min="1" class="field-input" placeholder="请输入院校ID" />
              </label>
              <label class="field">
                <span class="field-label">专业 ID</span>
                <input v-model.number="detailForm.majorId" type="number" min="1" class="field-input" placeholder="请输入专业ID" />
              </label>
              <label class="field">
                <span class="field-label">志愿顺序</span>
                <input v-model.number="detailForm.priority" type="number" min="1" class="field-input" />
              </label>
              <label class="field switch-field">
                <span>
                  <span class="field-label">服从调剂</span>
                  <span class="field-hint">影响录取模拟的专业风险判断</span>
                </span>
                <span class="switch-toggle">
                  <input v-model="detailForm.isMajorAdjusted" type="checkbox" />
                  <span class="switch-track"><span class="switch-thumb"></span></span>
                  <span class="switch-text">{{ detailForm.isMajorAdjusted ? '是' : '否' }}</span>
                </span>
              </label>
            </div>
          </div>

          <div class="dialog-footer">
            <button type="button" @click="showDetailModal = false" class="dialog-btn cancel">取消</button>
            <button type="button" @click="submitDetail" class="dialog-btn confirm">保存</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 批量添加弹窗 -->
    <transition name="modal-fade">
      <div v-if="showBatchModal" class="modal-overlay" @click.self="showBatchModal = false">
        <div class="modal-shell modal-shell-lg" :class="isDark ? 'modal-shell-dark' : 'modal-shell-light'">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">批量编辑</p>
              <h3 class="modal-title">批量添加志愿</h3>
            </div>
            <button type="button" class="modal-close" aria-label="关闭" @click="showBatchModal = false">
              <span>×</span>
            </button>
          </div>

          <div class="batch-modal-content">
            <div v-for="(item, idx) in batchDetails" :key="idx" class="batch-item">
              <div class="batch-item-header">
                <span class="batch-item-index">志愿 {{ idx + 1 }}</span>
                <button v-if="batchDetails.length > 1" type="button" @click="batchDetails.splice(idx, 1)" class="batch-remove" aria-label="移除">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>
              </div>
              <div class="batch-item-fields">
                <label class="field compact-field">
                  <span class="field-label">院校 ID</span>
                  <input v-model.number="item.universityId" type="number" min="1" class="field-input" />
                </label>
                <label class="field compact-field">
                  <span class="field-label">专业 ID</span>
                  <input v-model.number="item.majorId" type="number" min="1" class="field-input" />
                </label>
                <label class="field compact-field">
                  <span class="field-label">顺序</span>
                  <input v-model.number="item.priority" type="number" min="1" class="field-input" />
                </label>
              </div>
            </div>
            <button type="button" @click="addBatchDetailRow" class="batch-add-btn">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
              添加一条志愿
            </button>
          </div>

          <div class="dialog-footer">
            <button type="button" @click="showBatchModal = false" class="dialog-btn cancel">取消</button>
            <button type="button" @click="batchAddDetails" class="dialog-btn confirm">确认添加</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 删除确认弹层 -->
    <transition name="modal-fade">
      <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="closeDeleteConfirm">
        <div class="modal-shell modal-shell-confirm" :class="isDark ? 'modal-shell-dark' : 'modal-shell-light'">
          <div class="confirm-badge">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v4m0 4h.01M10.29 3.86l-8.5 15A2 2 0 003.5 22h17a2 2 0 001.71-3.14l-8.5-15a2 2 0 00-3.42 0z"></path>
            </svg>
          </div>
          <div class="confirm-copy">
            <p class="modal-kicker danger">确认删除</p>
            <h3 class="modal-title">{{ deleteTarget.label || '当前记录' }}</h3>
            <p class="modal-description">删除后无法恢复，是否继续删除这条记录？</p>
          </div>
          <div class="confirm-actions">
            <button type="button" @click="closeDeleteConfirm" class="dialog-btn cancel">取消</button>
            <button type="button" @click="confirmDelete" class="dialog-btn danger">删除</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import messageApi from '@/utils/messageApi'
import { ref, computed, onMounted, watch, inject } from 'vue'
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import AiInsightPanel from '@/views/high/components/AiInsightPanel.vue'

const isDark = inject('isDark', ref(false))

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['update-count'])

const volunteerPlans = ref([])
const selectedPlan = ref(null)
const volunteerDetails = ref([])

const avgScore = computed(() => {
  const scores = volunteerPlans.value.map(p => p.score).filter(s => s)
  if (scores.length === 0) return '--'
  return (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(0)
})

const showPlanModal = ref(false)
const planModalType = ref('add')
const planModalTitle = computed(() => planModalType.value === 'add' ? '新建志愿方案' : '编辑志愿方案')
const planForm = ref({ userId: props.userId, name: '', year: new Date().getFullYear(), province: '浙江', score: null, rank: null, isFinal: false })

const showDetailModal = ref(false)
const detailModalType = ref('add')
const detailModalTitle = computed(() => detailModalType.value === 'add' ? '添加志愿' : '编辑志愿')
const detailForm = ref({ volunteerId: null, universityId: null, majorId: null, priority: 1, isMajorAdjusted: false })
const editingDetailId = ref(null)

const showBatchModal = ref(false)
const batchDetails = ref([{ universityId: null, majorId: null, priority: 1, isMajorAdjusted: false }])

const planAiPayload = computed(() => ({
  userId: props.userId,
  profile: {
    ...(selectedPlan.value || {}),
    volunteerCount: volunteerDetails.value.length,
    selectedSubjects: selectedPlan.value?.selectedSubjects || ''
  },
  candidates: volunteerDetails.value.map(detail => ({
    ...detail,
    strategy: detail.strategy || '',
    scoreDiff: detail.scoreDiff,
    matchingCheck: detail.matchingCheck
  })),
  question: '请分析当前志愿方案的梯度、选科匹配、专业风险、保底充足性和下一步修改建议。'
}))

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
const getVolunteerPlans = async () => {
  try {
    const res = await request.get(`/volunteer/plan/list/${props.userId}`)
    if (res.code === 200) {
      volunteerPlans.value = res.data || []
      emit('update-count', volunteerPlans.value)
    }
  } catch (error) {
    console.error('获取志愿方案失败', error)
  }
}

const selectPlan = async (plan) => {
  selectedPlan.value = plan
  try {
    const res = await request.get(`/volunteer/detail/list/${plan.id}`)
    if (res.code === 200) {
      volunteerDetails.value = res.data || []
    }
  } catch (error) {
    console.error('获取志愿详情失败', error)
  }
}

const openPlanModal = (type, plan = null) => {
  planModalType.value = type
  if (type === 'edit' && plan) {
    planForm.value = { ...plan }
  } else {
    planForm.value = { userId: props.userId, name: '', year: new Date().getFullYear(), province: '浙江', score: null, rank: null, isFinal: false }
  }
  showPlanModal.value = true
}

const editPlan = (plan) => openPlanModal('edit', plan)

const submitPlan = async () => {
  try {
    if (planModalType.value === 'add') {
      const res = await request.post('/volunteer/plan/save', planForm.value)
      if (res.code === 200) {
        messageApi.success('创建成功')
        await getVolunteerPlans()
        showPlanModal.value = false
      }
    } else {
      const res = await request.put('/volunteer/plan/update', planForm.value)
      if (res.code === 200) {
        messageApi.success('更新成功')
        await getVolunteerPlans()
        showPlanModal.value = false
      }
    }
  } catch (error) {
    console.error('提交方案失败', error)
  }
}

const showDeleteConfirm = ref(false)
const deleteTarget = ref({ type: '', id: null, label: '' })

const openDeleteConfirm = (type, id, label) => {
  deleteTarget.value = { type, id, label }
  showDeleteConfirm.value = true
}

const closeDeleteConfirm = () => {
  showDeleteConfirm.value = false
  deleteTarget.value = { type: '', id: null, label: '' }
}

const deletePlan = (id) => {
  const plan = volunteerPlans.value.find(item => item.id === id)
  openDeleteConfirm('plan', id, plan ? `${plan.name || `方案${plan.year}`}` : `方案 ${id}`)
}

const deleteDetail = (id) => {
  const detail = volunteerDetails.value.find(item => item.id === id)
  const label = detail
    ? `第${detail.priority || ''}志愿${detail.universityName ? ` · ${detail.universityName}` : ''}${detail.majorName ? ` - ${detail.majorName}` : ''}`
    : `志愿记录 ${id}`
  openDeleteConfirm('detail', id, label)
}
const confirmDelete = async () => {
  const target = deleteTarget.value
  if (!target.id) return
  try {
    if (target.type === 'plan') {
      const res = await request.delete(`/volunteer/plan/delete/${target.id}`)
      if (res.code === 200) {
        messageApi.success('删除成功')
        await getVolunteerPlans()
        if (selectedPlan.value?.id === target.id) {
          selectedPlan.value = null
          volunteerDetails.value = []
        }
        closeDeleteConfirm()
      }
    } else if (target.type === 'detail') {
      const res = await request.delete(`/volunteer/detail/delete/${target.id}`)
      if (res.code === 200) {
        messageApi.success('删除成功')
        await selectPlan(selectedPlan.value)
        closeDeleteConfirm()
      }
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

const openDetailModal = (type, detail = null) => {
  detailModalType.value = type
  if (type === 'edit' && detail) {
    detailForm.value = { ...detail }
    editingDetailId.value = detail.id
  } else {
    detailForm.value = { volunteerId: selectedPlan.value?.id, universityId: null, majorId: null, priority: volunteerDetails.value.length + 1, isMajorAdjusted: false }
    editingDetailId.value = null
  }
  showDetailModal.value = true
}

const editDetail = (detail) => openDetailModal('edit', detail)

const submitDetail = async () => {
  try {
    if (detailModalType.value === 'add') {
      detailForm.value.volunteerId = selectedPlan.value.id
      const res = await request.post('/volunteer/detail/add', detailForm.value)
      if (res.code === 200) {
        messageApi.success('添加成功')
        await selectPlan(selectedPlan.value)
        showDetailModal.value = false
      }
    } else {
      const res = await request.put('/volunteer/detail/update', { ...detailForm.value, id: editingDetailId.value })
      if (res.code === 200) {
        messageApi.success('更新成功')
        await selectPlan(selectedPlan.value)
        showDetailModal.value = false
      }
    }
  } catch (error) {
    console.error('提交详情失败', error)
  }
}

const openBatchModal = () => {
  batchDetails.value = [{ universityId: null, majorId: null, priority: volunteerDetails.value.length + 1, isMajorAdjusted: false }]
  showBatchModal.value = true
}

const addBatchDetailRow = () => {
  batchDetails.value.push({ universityId: null, majorId: null, priority: batchDetails.value.length + volunteerDetails.value.length + 1, isMajorAdjusted: false })
}

const batchAddDetails = async () => {
  const details = batchDetails.value.map(d => ({ ...d, volunteerId: selectedPlan.value.id }))
  try {
    const res = await request.post('/volunteer/detail/batchAdd', details)
    if (res.code === 200) {
      messageApi.success('批量添加成功')
      await selectPlan(selectedPlan.value)
      showBatchModal.value = false
    }
  } catch (error) {
    console.error('批量添加失败', error)
  }
}

const checkMatching = async (detailId) => {
  try {
    const res = await request.post(`/volunteer/matching/check/${detailId}`, ['物理', '化学', '生物'])
    if (res.code === 200) {
      const result = res.data
      ElMessageBox.alert(
        `<div class="space-y-2">
          <h4 class="font-bold text-lg">📊 选科匹配分析</h4>
          <p>匹配度：<span class="font-bold text-blue-500">${result.matchScore}</span>分</p>
          <p>${result.details || ''}</p>
          <p>要求科目：${result.requiredSubjects || '无特殊要求'}</p>
        </div>`,
        '匹配检查结果',
        { dangerouslyUseHTMLString: true }
      )
    }
  } catch (error) {
    console.error('匹配检查失败', error)
  }
}

const openMatchingReport = async () => {
  try {
    const res = await request.get(`/volunteer/matching/report/${selectedPlan.value.id}`, {
      params: { selectedSubjects: '物理,化学,生物' }
    })
    if (res.code === 200) {
      const report = res.data || []
      let html = '<div class="space-y-3"><h4 class="font-bold text-lg">📊 选科匹配报告</h4>'
      report.forEach(r => {
        html += `<div class="p-3 bg-gray-50 rounded-lg dark:bg-dark-surface">
          <p class="font-medium">${r.priority}. ${r.universityName} - ${r.majorName}</p>
          <p>匹配度：${r.matchingScore || '未计算'}分</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">建议：${r.suggestion || ''}</p>
        </div>`
      })
      html += '</div>'
      ElMessageBox.alert(html, '匹配报告', { dangerouslyUseHTMLString: true })
    }
  } catch (error) {
    console.error('获取匹配报告失败', error)
  }
}

const singleSimulate = async (detailId) => {
  try {
    const res = await request.post(`/volunteer/simulate/single/${detailId}`)
    if (res.code === 200) {
      const result = res.data
      ElMessageBox.alert(
        `<div class="space-y-2">
          <h4 class="font-bold text-lg">🎲 模拟录取结果</h4>
          <p>状态：<span class="font-bold">${result.simulationStatus === 'admitted' ? '✅ 录取' : result.simulationStatus === 'waiting' ? '⏳ 待定' : '❌ 未录取'}</span></p>
          <p>录取概率：${(result.predictedProbability * 100).toFixed(0)}%</p>
          <p>分差：${result.scoreDiff}分</p>
          ${result.rejectReason ? `<p class="text-red-500">原因：${result.rejectReason}</p>` : ''}
        </div>`,
        '模拟结果',
        { dangerouslyUseHTMLString: true }
      )
    }
  } catch (error) {
    console.error('模拟失败', error)
  }
}

const openStatistics = async () => {
  try {
    const res = await request.get(`/volunteer/statistics/${props.userId}`)
    if (res.code === 200) {
      const stats = res.data || []
      let html = '<div class="space-y-2"><h4 class="font-bold text-lg">📊 志愿统计分析</h4>'
      stats.forEach(s => {
        html += `<div class="p-2 bg-gray-50 rounded dark:bg-dark-surface"><p>${s.year}年：${s.count}个方案，最高分${s.maxScore}，最低分${s.minScore}</p></div>`
      })
      html += '</div>'
      ElMessageBox.alert(html, '统计分析', { dangerouslyUseHTMLString: true })
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

watch(() => props.userId, () => {
  getVolunteerPlans()
})

onMounted(() => {
  getVolunteerPlans()
})
</script>

<style scoped>
/* 统计卡片样式 */
.stat-card {
  border-radius: 20px;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card-light {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.stat-card-dark {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.stat-card-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 13px;
  margin-bottom: 8px;
  color: #6b7280;
}

.dark .stat-label {
  color: #9ca3af;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
}

.dark .stat-value {
  color: #f3f4f6;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #d946ef, #8b5cf6);
  transition: width 0.5s ease;
}

/* 方案卡片样式 */
.plan-card {
  border-radius: 20px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan-card-light {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.plan-card-dark {
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.plan-card-selected {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.1));
  border: 1px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.15);
}

.plan-card:hover {
  transform: translateY(-4px);
}

.plan-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.plan-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.plan-icon-final {
  background: rgba(16, 185, 129, 0.15);
}

.plan-icon-draft {
  background: rgba(245, 158, 11, 0.15);
}

.plan-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.plan-card:hover .plan-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  cursor: pointer;
  background: transparent;
  border: none;
}

.edit-btn {
  color: #6b7280;
}

.edit-btn:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #d946ef;
}

.delete-btn {
  color: #ef4444;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.plan-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1f2937;
}

.dark .plan-name {
  color: #f3f4f6;
}

.plan-meta {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 16px;
}

.dark .plan-meta {
  color: #9ca3af;
}

.plan-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.plan-stat-label {
  font-size: 11px;
  color: #6b7280;
  display: block;
  margin-bottom: 4px;
}

.plan-stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.dark .plan-stat-value {
  color: #f3f4f6;
}

.plan-stat-rank {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
}

.plan-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark .plan-card-footer {
  border-top-color: rgba(255, 255, 255, 0.05);
}

.plan-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.status-final {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.status-draft {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.plan-time {
  font-size: 11px;
  color: #9ca3af;
}

/* 按钮样式 */
.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #d946ef, #8b5cf6);
  border: none;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.btn-outline-purple, .btn-outline-orange, .btn-success, .btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline-purple {
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #8b5cf6;
}

.btn-outline-purple:hover {
  background: rgba(139, 92, 246, 0.2);
}

.btn-outline-orange {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  color: #f59e0b;
}

.btn-outline-orange:hover {
  background: rgba(245, 158, 11, 0.2);
}

.btn-success {
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.btn-success:hover {
  background: rgba(16, 185, 129, 0.25);
}

.btn-primary {
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.3);
  color: #d946ef;
}

.btn-primary:hover {
  background: rgba(99, 102, 241, 0.25);
}

/* 志愿详情样式 */
.volunteer-item {
  display: flex;
  gap: 16px;
  position: relative;
}

.volunteer-priority {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.priority-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d946ef, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
}

.priority-line {
  flex: 1;
  width: 2px;
  background: linear-gradient(180deg, #d946ef, transparent);
  margin-top: 8px;
}

.volunteer-content {
  flex: 1;
  border-radius: 16px;
  padding: 16px 20px;
  transition: all 0.2s;
}

.volunteer-content-light {
  background: rgba(243, 244, 246, 0.6);
}

.volunteer-content-dark {
  background: rgba(30, 41, 59, 0.4);
}

.volunteer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.university-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1f2937;
}

.dark .university-name {
  color: #f3f4f6;
}

.major-name {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
}

.dark .major-name {
  color: #9ca3af;
}

.volunteer-tags {
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

.tag-gray {
  background: rgba(107, 114, 128, 0.1);
  color: #6b7280;
}

.tag-success {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.tag-danger {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.volunteer-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.volunteer-item:hover .volunteer-actions {
  opacity: 1;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.action-icon-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  transform: scale(1.05);
}

/* 自定义弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(18px);
}

.modal-shell {
  width: min(100%, 560px);
  max-height: min(86vh, 760px);
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.28);
}

.modal-shell-md { width: min(100%, 620px); }
.modal-shell-lg { width: min(100%, 820px); }

.modal-shell-confirm {
  width: min(100%, 430px);
  padding: 28px;
  text-align: center;
}

.modal-shell-light {
  background: rgba(255, 255, 255, 0.96);
  color: #111827;
}

.modal-shell-dark {
  background: rgba(15, 23, 42, 0.94);
  color: #f8fafc;
  border-color: rgba(148, 163, 184, 0.26);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 28px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.modal-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  color: #0f766e;
}

.modal-kicker.danger { color: #ef4444; }

.modal-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0;
}

.modal-close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.14);
  color: inherit;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close:hover {
  transform: translateY(-1px);
  background: rgba(20, 184, 166, 0.16);
}

.modal-body { padding: 24px 28px 8px; }

.modal-form {
  display: grid;
  gap: 18px;
}

.modal-form-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.field { display: grid; gap: 8px; }
.field-wide { grid-column: 1 / -1; }

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.modal-shell-dark .field-label { color: #cbd5e1; }

.field-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.field-input {
  width: 100%;
  height: 42px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.78);
  color: #111827;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field-input:focus {
  border-color: rgba(15, 118, 110, 0.74);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.modal-shell-dark .field-input {
  background: rgba(30, 41, 59, 0.82);
  color: #f8fafc;
  border-color: rgba(148, 163, 184, 0.28);
}

.switch-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(20, 184, 166, 0.08);
}

.switch-toggle {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  white-space: nowrap;
}

.switch-toggle input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.switch-track {
  position: relative;
  width: 46px;
  height: 26px;
  border-radius: 999px;
  background: #cbd5e1;
  transition: background 0.2s ease;
}

.switch-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.2);
  transition: transform 0.2s ease;
}

.switch-toggle input:checked + .switch-track {
  background: linear-gradient(135deg, #0f766e, #2563eb);
}

.switch-toggle input:checked + .switch-track .switch-thumb { transform: translateX(20px); }

.switch-text {
  font-size: 13px;
  font-weight: 700;
  color: #0f766e;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px 26px;
}

.dialog-btn {
  min-width: 92px;
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.dialog-btn:hover { transform: translateY(-1px); }

.dialog-btn.cancel {
  background: rgba(148, 163, 184, 0.16);
  color: #475569;
}

.modal-shell-dark .dialog-btn.cancel { color: #e2e8f0; }

.dialog-btn.confirm {
  background: linear-gradient(135deg, #0f766e, #2563eb);
  color: #fff;
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.22);
}

.dialog-btn.danger {
  background: linear-gradient(135deg, #ef4444, #f97316);
  color: #fff;
  box-shadow: 0 12px 28px rgba(239, 68, 68, 0.24);
}

.batch-modal-content {
  max-height: 520px;
  overflow-y: auto;
  padding: 24px 28px 8px;
}

.batch-item {
  padding: 16px;
  margin-bottom: 14px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.82);
}

.modal-shell-dark .batch-item { background: rgba(30, 41, 59, 0.62); }

.batch-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.batch-item-index {
  font-size: 13px;
  font-weight: 800;
  color: #0f766e;
}

.batch-remove {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: none;
  cursor: pointer;
}

.batch-remove:hover { background: rgba(239, 68, 68, 0.2); }

.batch-item-fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.compact-field .field-input { height: 38px; }

.batch-add-btn {
  width: 100%;
  height: 44px;
  border-radius: 999px;
  background: rgba(20, 184, 166, 0.1);
  border: 1px dashed rgba(15, 118, 110, 0.35);
  color: #0f766e;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 800;
  transition: all 0.2s ease;
}

.batch-add-btn:hover {
  background: rgba(20, 184, 166, 0.16);
  transform: translateY(-1px);
}

.confirm-badge {
  width: 60px;
  height: 60px;
  margin: 0 auto 18px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.12);
}

.confirm-copy { display: grid; gap: 8px; }

.modal-description {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.modal-shell-dark .modal-description { color: #cbd5e1; }

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.modal-fade-enter-active,
.modal-fade-leave-active { transition: opacity 0.18s ease; }
.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }

@media (max-width: 640px) {
  .modal-overlay { padding: 14px; }
  .modal-form-grid,
  .batch-item-fields { grid-template-columns: 1fr; }
  .switch-field {
    align-items: flex-start;
    flex-direction: column;
  }
  .dialog-footer,
  .confirm-actions { flex-direction: column-reverse; }
  .dialog-btn { width: 100%; }
}
/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: #6b7280;
}

.dark .empty-text {
  color: #9ca3af;
}
</style>



