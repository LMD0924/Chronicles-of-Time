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
          <div class="stat-icon-wrapper bg-blue-500/20">
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
          <div class="stat-icon-wrapper bg-purple-500/20">
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
    <el-dialog v-model="showPlanModal" :title="planModalTitle" width="520px" :class="{ 'dark-dialog': isDark }" class="custom-dialog">
      <el-form :model="planForm" label-width="100px" class="dialog-form">
        <el-form-item label="方案名称">
          <el-input v-model="planForm.name" placeholder="请输入方案名称" class="dialog-input" />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="planForm.year" :min="2020" :max="2030" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="planForm.province" placeholder="请输入省份" class="dialog-input" />
        </el-form-item>
        <el-form-item label="高考分数">
          <el-input-number v-model="planForm.score" :min="0" :max="750" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="全省排名">
          <el-input-number v-model="planForm.rank" :min="0" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="是否提交">
          <el-switch v-model="planForm.isFinal" class="dialog-switch" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button @click="showPlanModal = false" class="dialog-btn cancel">取消</button>
          <button @click="submitPlan" class="dialog-btn confirm">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 志愿弹窗 -->
    <el-dialog v-model="showDetailModal" :title="detailModalTitle" width="520px" :class="{ 'dark-dialog': isDark }" class="custom-dialog">
      <el-form :model="detailForm" label-width="100px" class="dialog-form">
        <el-form-item label="大学ID">
          <el-input-number v-model="detailForm.universityId" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="专业ID">
          <el-input-number v-model="detailForm.majorId" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="志愿顺序">
          <el-input-number v-model="detailForm.priority" :min="1" class="w-full dialog-input" />
        </el-form-item>
        <el-form-item label="服从调剂">
          <el-switch v-model="detailForm.isMajorAdjusted" class="dialog-switch" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button @click="showDetailModal = false" class="dialog-btn cancel">取消</button>
          <button @click="submitDetail" class="dialog-btn confirm">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量添加弹窗 -->
    <el-dialog v-model="showBatchModal" title="批量添加志愿" width="700px" :class="{ 'dark-dialog': isDark }" class="custom-dialog">
      <div class="batch-modal-content">
        <div v-for="(item, idx) in batchDetails" :key="idx" class="batch-item">
          <div class="batch-item-header">
            <span class="batch-item-index">志愿 {{ idx + 1 }}</span>
            <button v-if="batchDetails.length > 1" @click="batchDetails.splice(idx, 1)" class="batch-remove">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          <div class="batch-item-fields">
            <el-input-number v-model="item.universityId" placeholder="大学ID" class="batch-input" />
            <el-input-number v-model="item.majorId" placeholder="专业ID" class="batch-input" />
            <el-input-number v-model="item.priority" :min="1" placeholder="顺序" class="batch-input" />
          </div>
        </div>
        <button @click="addBatchDetailRow" class="batch-add-btn">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          添加一行
        </button>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button @click="showBatchModal = false" class="dialog-btn cancel">取消</button>
          <button @click="batchAddDetails" class="dialog-btn confirm">提交</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

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
        ElMessage.success('创建成功')
        await getVolunteerPlans()
        showPlanModal.value = false
      }
    } else {
      const res = await request.put('/volunteer/plan/update', planForm.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        await getVolunteerPlans()
        showPlanModal.value = false
      }
    }
  } catch (error) {
    console.error('提交方案失败', error)
  }
}

const deletePlan = async (id) => {
  try {
    const res = await request.delete(`/volunteer/plan/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await getVolunteerPlans()
      if (selectedPlan.value?.id === id) {
        selectedPlan.value = null
        volunteerDetails.value = []
      }
    }
  } catch (error) {
    console.error('删除方案失败', error)
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
        ElMessage.success('添加成功')
        await selectPlan(selectedPlan.value)
        showDetailModal.value = false
      }
    } else {
      const res = await request.put('/volunteer/detail/update', { ...detailForm.value, id: editingDetailId.value })
      if (res.code === 200) {
        ElMessage.success('更新成功')
        await selectPlan(selectedPlan.value)
        showDetailModal.value = false
      }
    }
  } catch (error) {
    console.error('提交详情失败', error)
  }
}

const deleteDetail = async (id) => {
  try {
    const res = await request.delete(`/volunteer/detail/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await selectPlan(selectedPlan.value)
    }
  } catch (error) {
    console.error('删除志愿失败', error)
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
      ElMessage.success('批量添加成功')
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
        html += `<div class="p-3 bg-gray-50 rounded-lg dark:bg-black">
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
        html += `<div class="p-2 bg-gray-50 rounded dark:bg-black"><p>${s.year}年：${s.count}个方案，最高分${s.maxScore}，最低分${s.minScore}</p></div>`
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
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
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
  color: #6366f1;
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
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
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
  color: #6366f1;
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
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
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
  background: linear-gradient(180deg, #6366f1, transparent);
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

/* 弹窗样式 */
.custom-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.dark-dialog :deep(.el-dialog) {
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.dialog-form {
  padding: 0 8px;
}

.dialog-input :deep(.el-input__wrapper),
.dialog-input :deep(.el-input-number__wrapper) {
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid #e5e7eb;
}

.dark .dialog-input :deep(.el-input__wrapper),
.dark .dialog-input :deep(.el-input-number__wrapper) {
  background: rgba(51, 65, 85, 0.8);
  border-color: #475569;
}

.dialog-switch :deep(.el-switch__core) {
  border-radius: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.dialog-btn.cancel {
  background: #f3f4f6;
  color: #4b5563;
}

.dark .dialog-btn.cancel {
  background: #374151;
  color: #9ca3af;
}

.dialog-btn.cancel:hover {
  background: #e5e7eb;
}

.dialog-btn.confirm {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
}

.dialog-btn.confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

/* 批量添加弹窗样式 */
.batch-modal-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 8px;
}

.batch-item {
  background: rgba(243, 244, 246, 0.5);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
}

.dark .batch-item {
  background: rgba(30, 41, 59, 0.5);
}

.batch-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.batch-item-index {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
}

.batch-remove {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-remove:hover {
  background: rgba(239, 68, 68, 0.2);
}

.batch-item-fields {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.batch-input :deep(.el-input-number__wrapper) {
  width: 100%;
  border-radius: 12px;
}

.batch-add-btn {
  width: 100%;
  padding: 12px;
  border-radius: 40px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px dashed rgba(99, 102, 241, 0.3);
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-add-btn:hover {
  background: rgba(99, 102, 241, 0.15);
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
