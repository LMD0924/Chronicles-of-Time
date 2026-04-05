<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const emit = defineEmits(['refresh', 'edit'])

const loading = ref(false)
const selections = ref([])

// 获取我的选课
const fetchSelections = async () => {
  loading.value = true
  try {
    const res = await request.get(`/selection/student/${props.studentId}`)
    if (res.code === 200) {
      selections.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取选课记录失败')
  } finally {
    loading.value = false
  }
}

// 确认选课
const confirmSelection = async (id) => {
  ElMessageBox.confirm('确认后无法修改，确定要确认选课吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.put(`/selection/confirm/${id}`)
      if (res.code === 200) {
        ElMessage.success('确认成功')
        fetchSelections()
        emit('refresh')
      }
    } catch (error) {
      ElMessage.error('确认失败')
    }
  })
}

// 退选
const cancelSelection = async (id) => {
  ElMessageBox.prompt('请输入退选原因', '退选', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入退选原因'
  }).then(async ({ value }) => {
    try {
      const res = await request.delete(`/selection/cancel/${id}`, { reason: value })
      if (res.code === 200) {
        ElMessage.success('退选成功')
        fetchSelections()
        emit('refresh')
      }
    } catch (error) {
      ElMessage.error('退选失败')
    }
  })
}

// 更新公开状态
const updatePublicStatus = async (item) => {
  try {
    const res = await request.put(`/selection/public/${item.id}`, null, null, {
      params: { isPublic: item.isPublic }
    })
    if (res.code !== 200) {
      item.isPublic = !item.isPublic
      ElMessage.error('更新失败')
    }
  } catch (error) {
    item.isPublic = !item.isPublic
    ElMessage.error('更新失败')
  }
}

// 编辑
const editRecord = (item) => {
  emit('edit', item)
}

const getStatusClass = (status, isConfirmed) => {
  if (isConfirmed) return 'px-2 py-0.5 rounded-full text-xs bg-green-100 text-green-600'
  if (status === 1) return 'px-2 py-0.5 rounded-full text-xs bg-yellow-100 text-yellow-600'
  if (status === 3) return 'px-2 py-0.5 rounded-full text-xs bg-blue-100 text-blue-600'
  if (status === 4) return 'px-2 py-0.5 rounded-full text-xs bg-red-100 text-red-600'
  return 'px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-600'
}

const getStatusText = (status, isConfirmed) => {
  if (isConfirmed) return '已确认'
  if (status === 1) return '待确认'
  if (status === 3) return '已修改'
  if (status === 4) return '已退选'
  return '未确认'
}

watch(() => props.studentId, (val) => {
  if (val) fetchSelections()
}, { immediate: true })
</script>
<template>
  <div class="cs-panel space-y-4">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-8 h-8 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
    </div>

    <!-- 无选课记录 -->
    <div v-else-if="selections.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">📭</div>
      <p class="text-slate-500">暂无选课记录，请前往"选课中心"进行选课</p>
    </div>

    <!-- 选课列表 -->
    <div v-else class="space-y-4">
      <div v-for="item in selections" :key="item.id"
           class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-5 hover:shadow-lg transition-all">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <!-- 基本信息 -->
            <div class="flex flex-wrap items-center gap-3 mb-3">
              <span class="text-sm font-mono text-slate-500">{{ item.academicYear }} {{ item.semester }}</span>
              <span :class="getStatusClass(item.status, item.isConfirmed)">
                {{ getStatusText(item.status, item.isConfirmed) }}
              </span>
              <label class="flex items-center gap-2 text-sm cursor-pointer" @click.stop>
                <input type="checkbox" v-model="item.isPublic" @change="updatePublicStatus(item)" class="rounded">
                <span class="text-slate-500">公开</span>
              </label>
            </div>

            <!-- 选科信息 -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div class="p-3 rounded-lg bg-indigo-50/50 dark:bg-indigo-950/30">
                <div class="text-xs text-slate-500 mb-1">首选科目</div>
                <div class="font-semibold text-lg">{{ item.firstSubjectName }}</div>
                <div class="text-sm text-slate-500">成绩: {{ item.firstSubjectScore || '-' }}分</div>
              </div>
              <div class="p-3 rounded-lg bg-emerald-50/50 dark:bg-emerald-950/30">
                <div class="text-xs text-slate-500 mb-1">再选科目1</div>
                <div class="font-semibold text-lg">{{ item.secondSubject1Name }}</div>
                <div class="text-sm text-slate-500">成绩: {{ item.secondSubject1Score || '-' }}分</div>
              </div>
              <div class="p-3 rounded-lg bg-amber-50/50 dark:bg-amber-950/30">
                <div class="text-xs text-slate-500 mb-1">再选科目2</div>
                <div class="font-semibold text-lg">{{ item.secondSubject2Name }}</div>
                <div class="text-sm text-slate-500">成绩: {{ item.secondSubject2Score || '-' }}分</div>
              </div>
            </div>

            <!-- 成绩信息 -->
            <div v-if="item.totalScoreWeighted" class="flex flex-wrap gap-4 text-sm mb-3">
              <div class="flex items-center gap-1">
                <span class="text-slate-500">原始总分:</span>
                <span class="font-medium">{{ item.totalScore || '-' }}</span>
              </div>
              <div class="flex items-center gap-1">
                <span class="text-slate-500">赋分总分:</span>
                <span class="font-bold text-indigo-600">{{ item.totalScoreWeighted }}</span>
              </div>
              <div v-if="item.classRank" class="flex items-center gap-1">
                <span class="text-slate-500">班级排名:</span>
                <span>{{ item.classRank }}</span>
              </div>
              <div v-if="item.gradeRank" class="flex items-center gap-1">
                <span class="text-slate-500">年级排名:</span>
                <span>{{ item.gradeRank }}</span>
              </div>
            </div>

            <!-- 选课理由 -->
            <div v-if="item.selectionReason" class="p-2 rounded-lg bg-slate-50 dark:bg-slate-800/40 text-sm">
              <span class="text-slate-500">选课理由：</span>
              {{ item.selectionReason }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex flex-col gap-2 ml-4">
            <button v-if="!item.isConfirmed"
                    @click="confirmSelection(item.id)"
                    class="px-3 py-1.5 rounded-lg bg-green-500/10 text-green-600 hover:bg-green-500/20 transition-all text-sm">
              确认
            </button>
            <button @click="editRecord(item)"
                    class="px-3 py-1.5 rounded-lg bg-indigo-500/10 text-indigo-600 hover:bg-indigo-500/20 transition-all text-sm">
              修改
            </button>
            <button @click="cancelSelection(item.id)"
                    class="px-3 py-1.5 rounded-lg bg-red-500/10 text-red-600 hover:bg-red-500/20 transition-all text-sm">
              退选
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
