<template>
  <div class="space-y-6">
    <div :class="[isDark ? 'bg-gray-800/50' : 'bg-white', 'backdrop-blur-xl rounded-2xl p-6 shadow-lg', isDark ? 'border border-gray-700' : 'border border-gray-200']">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 bg-gradient-to-r from-purple-500 to-pink-500 rounded-xl flex items-center justify-center">
          <span class="text-xl">🎲</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">模拟录取</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">基于历年数据，模拟您的录取概率</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">选择志愿方案</label>
          <el-select v-model="simulatePlanId" placeholder="请选择志愿方案" class="w-full">
            <el-option v-for="plan in props.volunteerPlans" :key="plan.id" :value="plan.id" :label="`${plan.name || plan.year}年方案 - ${plan.score}分`" />
          </el-select>
        </div>
        <div class="flex items-end gap-3">
          <el-button type="primary" size="large" @click="runBatchSimulation" :disabled="!simulatePlanId" :loading="simulating" class="!bg-gradient-to-r !from-purple-500 !to-pink-500 !border-0">
            <el-icon><VideoPlay /></el-icon>
            {{ simulating ? '模拟中...' : '批量模拟录取' }}
          </el-button>
          <el-button size="large" @click="getAdmissionAnalysis" :disabled="!simulatePlanId">
            <el-icon><Document /></el-icon>
            分析报告
          </el-button>
        </div>
      </div>

      <!-- 模拟结果 -->
      <div v-if="simulationResults.length > 0" class="mt-6">
        <h3 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-medium mb-4">📊 模拟结果</h3>
        <div class="space-y-3">
          <div v-for="(result, index) in simulationResults" :key="index" :class="[
            isDark ? 'bg-gray-700/50 border-gray-600' : 'bg-gray-50 border-gray-200',
            'rounded-xl p-4 border'
          ]">
            <div class="flex flex-wrap justify-between items-center gap-4">
              <div class="flex items-center gap-3">
                <div :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center font-bold',
                  isDark ? 'bg-gray-600 text-white' : 'bg-gray-200 text-gray-800'
                ]">
                  {{ result.priority }}
                </div>
                <div>
                  <div :class="isDark ? 'text-white' : 'text-gray-900'" class="font-medium">{{ result.universityName }}</div>
                  <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ result.majorName }}</div>
                </div>
              </div>
              <div class="flex flex-wrap items-center gap-4">
                <div class="text-right">
                  <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">目标分数</div>
                  <div :class="isDark ? 'text-white' : 'text-gray-900'" class="font-medium">{{ result.targetScore }}分</div>
                </div>
                <div class="text-right">
                  <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">分差</div>
                  <div :class="result.scoreDiff >= 0 ? 'text-green-500' : 'text-red-500'">{{ result.scoreDiff }}分</div>
                </div>
                <div>
                  <span :class="[
                    'px-3 py-1 rounded-full text-sm font-medium',
                    result.status === '录取' ? 'bg-green-500/20 text-green-500' :
                    result.status === '待定' ? 'bg-yellow-500/20 text-yellow-500' : 'bg-red-500/20 text-red-500'
                  ]">
                    {{ result.status }}
                  </span>
                  <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs mt-1">概率 {{ result.probability }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分析报告 -->
      <div v-if="admissionAnalysis" :class="[
        isDark ? 'bg-blue-500/10 border-blue-500/20' : 'bg-blue-50 border-blue-200',
        'mt-6 p-5 rounded-xl border'
      ]">
        <h3 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-medium mb-4">📈 录取分析报告</h3>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <div class="text-center">
            <div :class="isDark ? 'text-white' : 'text-gray-900'" class="text-2xl font-bold">{{ admissionAnalysis.totalVolunteers }}</div>
            <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">总志愿数</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-green-500">{{ admissionAnalysis.admittedCount }}</div>
            <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">预计录取</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-yellow-500">{{ admissionAnalysis.waitingCount }}</div>
            <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">待定</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-red-500">{{ admissionAnalysis.rejectedCount }}</div>
            <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">未录取</div>
          </div>
        </div>
        <div :class="[
          isDark ? 'bg-gray-700/50 text-gray-300' : 'bg-gray-100 text-gray-700',
          'text-center text-sm rounded-lg p-3'
        ]">
          {{ admissionAnalysis.suggestion }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { VideoPlay, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

const isDark = inject('isDark', ref(false))

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  },
  volunteerPlans: {
    type: Array,
    default: () => []
  }
})

const simulating = ref(false)
const simulatePlanId = ref(null)
const simulationResults = ref([])
const admissionAnalysis = ref(null)

const runBatchSimulation = async () => {
  if (!simulatePlanId.value) return
  simulating.value = true
  try {
    const res = await request.get(`/volunteer/simulate/batch/${simulatePlanId.value}`)
    if (res.code === 200) {
      simulationResults.value = res.data || []
      await getAdmissionAnalysis()
    }
  } catch (error) {
    console.error('批量模拟失败', error)
  } finally {
    simulating.value = false
  }
}

const getAdmissionAnalysis = async () => {
  if (!simulatePlanId.value) return
  try {
    const res = await request.get(`/volunteer/simulate/analysis/${simulatePlanId.value}`)
    if (res.code === 200) {
      admissionAnalysis.value = res.data
    }
  } catch (error) {
    console.error('获取分析报告失败', error)
  }
}
</script>

<style scoped>
</style>
