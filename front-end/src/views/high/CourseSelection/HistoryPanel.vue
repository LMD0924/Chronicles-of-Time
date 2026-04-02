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
    '1': 'bg-green-100 text-green-600',
    '2': 'bg-yellow-100 text-yellow-600',
    '3': 'bg-red-100 text-red-600',
    '4': 'bg-blue-100 text-blue-600'
  }
  return map[type] || 'bg-gray-100 text-gray-600'
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

const getApproveStatusClass = (status) => {
  if (status === 0) return 'bg-yellow-100 text-yellow-600'
  if (status === 1) return 'bg-green-100 text-green-600'
  if (status === 2) return 'bg-red-100 text-red-600'
  return 'bg-gray-100 text-gray-600'
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
  <div class="space-y-4">
    <!-- 筛选条件 -->
    <div class="flex gap-3">
      <select v-model="filterType" class="px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800">
        <option value="all">全部</option>
        <option value="1">新增</option>
        <option value="2">修改</option>
        <option value="3">退选</option>
        <option value="4">确认</option>
      </select>
      <input type="date" v-model="startDate" class="px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800">
      <input type="date" v-model="endDate" class="px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800">
      <button @click="fetchHistory" class="px-4 py-2 rounded-xl bg-indigo-600 text-white">查询</button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-3">
      <div class="rounded-xl p-3 bg-white/50 dark:bg-white/10 text-center">
        <div class="text-2xl font-bold">{{ statistics.totalCount || 0 }}</div>
        <div class="text-xs text-slate-500">总变更次数</div>
      </div>
      <div class="rounded-xl p-3 bg-white/50 dark:bg-white/10 text-center">
        <div class="text-2xl font-bold text-yellow-600">{{ statistics.pendingCount || 0 }}</div>
        <div class="text-xs text-slate-500">待审批</div>
      </div>
      <div class="rounded-xl p-3 bg-white/50 dark:bg-white/10 text-center">
        <div class="text-2xl font-bold text-green-600">{{ statistics.approvedCount || 0 }}</div>
        <div class="text-xs text-slate-500">已通过</div>
      </div>
      <div class="rounded-xl p-3 bg-white/50 dark:bg-white/10 text-center">
        <div class="text-2xl font-bold text-red-600">{{ statistics.rejectedCount || 0 }}</div>
        <div class="text-xs text-slate-500">已拒绝</div>
      </div>
    </div>

    <!-- 历史列表 -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-8 h-8 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
    </div>
    <div v-else-if="histories.length === 0" class="text-center py-12 text-slate-500">
      暂无历史记录
    </div>
    <div v-else class="space-y-3">
      <div v-for="item in histories" :key="item.id"
           class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
        <div class="flex items-start justify-between mb-2">
          <div class="flex items-center gap-2">
            <span :class="getChangeTypeClass(item.changeType)" class="px-2 py-0.5 rounded-full text-xs">
              {{ getChangeTypeText(item.changeType) }}
            </span>
            <span class="text-sm text-slate-500">{{ formatDateTime(item.changeTime) }}</span>
          </div>
          <span :class="getApproveStatusClass(item.approveStatus)" class="px-2 py-0.5 rounded-full text-xs">
            {{ getApproveStatusText(item.approveStatus) }}
          </span>
        </div>

        <div class="text-sm">
          <div v-if="item.changeType === '1'" class="text-slate-600 dark:text-slate-300">
            新增选课：{{ item.newFirstSubject }} + {{ item.newSecondSubject1 }} + {{ item.newSecondSubject2 }}
          </div>
          <div v-else-if="item.changeType === '2'" class="text-slate-600 dark:text-slate-300">
            修改选课：{{ item.oldFirstSubject }} + {{ item.oldSecondSubject1 }} + {{ item.oldSecondSubject2 }}
            → {{ item.newFirstSubject }} + {{ item.newSecondSubject1 }} + {{ item.newSecondSubject2 }}
          </div>
          <div v-else-if="item.changeType === '3'" class="text-slate-600 dark:text-slate-300">
            退选：{{ item.oldFirstSubject }} + {{ item.oldSecondSubject1 }} + {{ item.oldSecondSubject2 }}
          </div>
          <div v-else-if="item.changeType === '4'" class="text-slate-600 dark:text-slate-300">
            确认选课：{{ item.newFirstSubject }} + {{ item.newSecondSubject1 }} + {{ item.newSecondSubject2 }}
          </div>
        </div>

        <div v-if="item.changeReason" class="mt-2 text-xs text-slate-400">
          原因：{{ item.changeReason }}
        </div>

        <div v-if="item.approveComment" class="mt-1 text-xs text-slate-400">
          审批意见：{{ item.approveComment }}
        </div>
        <div class="mt-2">
          <button class="text-indigo-600 text-xs" @click="openDetail(item.id)">查看详情</button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="变更详情" width="720px">
      <div v-if="detail" class="grid grid-cols-2 gap-3 text-sm">
        <div>历史ID：{{ detail.id }}</div>
        <div>选课记录ID：{{ detail.selectionId }}</div>
        <div>学生ID：{{ detail.studentId }}</div>
        <div>学生姓名：{{ detail.studentName }}</div>
        <div>变更类型：{{ detail.changeType }}</div>
        <div>审批状态：{{ detail.approveStatus }}</div>
        <div>原首选：{{ detail.oldFirstSubject || '-' }}</div>
        <div>新首选：{{ detail.newFirstSubject || '-' }}</div>
        <div>原再选1：{{ detail.oldSecondSubject1 || '-' }}</div>
        <div>新再选1：{{ detail.newSecondSubject1 || '-' }}</div>
        <div>原再选2：{{ detail.oldSecondSubject2 || '-' }}</div>
        <div>新再选2：{{ detail.newSecondSubject2 || '-' }}</div>
        <div class="col-span-2">变更原因：{{ detail.changeReason || '-' }}</div>
        <div>审批人：{{ detail.approver || '-' }}</div>
        <div>审批意见：{{ detail.approveComment || '-' }}</div>
        <div class="col-span-2">变更时间：{{ formatDateTime(detail.changeTime) }}</div>
      </div>
    </el-dialog>
  </div>
</template>
