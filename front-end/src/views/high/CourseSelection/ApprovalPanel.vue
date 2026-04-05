<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean
})

const loading = ref(false)
const pendingList = ref([])

// 获取待审批列表
const fetchPendingList = async () => {
  loading.value = true
  try {
    const res = await request.get('/history/pending')
    if (res.code === 200) {
      pendingList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取待审批列表失败')
  } finally {
    loading.value = false
  }
}

// 审批
const approve = async (id, isApproved) => {
  const status = isApproved ? 1 : 2
  const comment = isApproved ? '审批通过' : await promptRejectReason()

  if (!isApproved && !comment) return

  try {
    const res = await request.put(`/history/approve/${id}`, null, null, {
      params: { approver: '管理员', status, comment }
    })
    if (res.code === 200) {
      ElMessage.success(isApproved ? '审批通过' : '已拒绝')
      fetchPendingList()
    } else {
      ElMessage.error('审批失败')
    }
  } catch (error) {
    ElMessage.error('审批失败')
  }
}

// 拒绝原因输入
const promptRejectReason = async () => {
  return new Promise((resolve) => {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    }).then(({ value }) => {
      resolve(value)
    }).catch(() => {
      resolve(null)
    })
  })
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

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchPendingList()
})
</script>
<template>
  <div class="cs-panel space-y-4">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-semibold">待审批列表</h3>
      <button @click="fetchPendingList" class="px-3 py-1 rounded-lg bg-indigo-100 text-indigo-600 text-sm">刷新</button>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-8 h-8 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
    </div>
    <div v-else-if="pendingList.length === 0" class="text-center py-12 text-slate-500">
      暂无待审批记录
    </div>
    <div v-else class="space-y-3">
      <div v-for="item in pendingList" :key="item.id"
           class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
        <div class="flex items-start justify-between mb-2">
          <div class="flex items-center gap-2">
            <span class="font-medium">{{ item.studentName }}</span>
            <span :class="getChangeTypeClass(item.changeType)" class="px-2 py-0.5 rounded-full text-xs">
              {{ getChangeTypeText(item.changeType) }}
            </span>
          </div>
          <span class="text-sm text-slate-500">{{ formatDateTime(item.changeTime) }}</span>
        </div>

        <div class="text-sm mb-3">
          <div v-if="item.changeType === '2'" class="text-slate-600 dark:text-slate-300">
            修改选课：{{ item.oldFirstSubject }} + {{ item.oldSecondSubject1 }} + {{ item.oldSecondSubject2 }}
            → {{ item.newFirstSubject }} + {{ item.newSecondSubject1 }} + {{ item.newSecondSubject2 }}
          </div>
          <div v-else-if="item.changeType === '3'" class="text-slate-600 dark:text-slate-300">
            退选：{{ item.oldFirstSubject }} + {{ item.oldSecondSubject1 }} + {{ item.oldSecondSubject2 }}
          </div>
          <div v-else class="text-slate-600 dark:text-slate-300">
            {{ getChangeTypeText(item.changeType) }}：{{ item.newFirstSubject }} + {{ item.newSecondSubject1 }} + {{ item.newSecondSubject2 }}
          </div>
        </div>

        <div v-if="item.changeReason" class="text-xs text-slate-400 mb-3">
          申请原因：{{ item.changeReason }}
        </div>

        <div class="flex gap-2">
          <button @click="approve(item.id, true)"
                  class="px-4 py-1.5 rounded-lg bg-green-500 text-white text-sm hover:bg-green-600 transition-all">
            通过
          </button>
          <button @click="approve(item.id, false)"
                  class="px-4 py-1.5 rounded-lg bg-red-500 text-white text-sm hover:bg-red-600 transition-all">
            拒绝
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
