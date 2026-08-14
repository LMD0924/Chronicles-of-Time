<!--
  文件说明：高中模块 AI 辅助分析组件，统一承接选科、专业和志愿场景的分析请求与结果展示。
-->
<script setup>
import messageApi from '@/utils/messageApi'
import { computed, ref } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  },
  scenario: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: 'AI 辅助分析'
  },
  description: {
    type: String,
    default: '结合当前页面数据，给出可执行的升学规划建议。'
  },
  payload: {
    type: Object,
    default: () => ({})
  },
  disabled: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '开始 AI 分析'
  }
})

const loading = ref(false)
const analysis = ref(null)
const userQuestion = ref('')

const requestBody = computed(() => ({
  userId: props.payload?.userId || null,
  scenario: props.scenario,
  profile: props.payload?.profile || {},
  candidates: props.payload?.candidates || [],
  question: userQuestion.value.trim() || props.payload?.question || ''
}))

const canAnalyze = computed(() => !props.disabled && !loading.value)

const analyze = async () => {
  if (!canAnalyze.value) return
  loading.value = true
  try {
    const res = await request.post('/high/ai/analyze', requestBody.value)
    if (res.code === 200) {
      analysis.value = res.data
      if (!res.data?.aiEnabled) {
        messageApi.info(res.data?.providerStatus || '当前使用本地规则分析')
      }
    }
  } catch (error) {
    console.error('AI 分析失败', error)
  } finally {
    loading.value = false
  }
}

defineExpose({ analyze })
</script>

<template>
  <div
    class="rounded-2xl border p-5"
    :class="isDark ? 'bg-gray-800/50 border-gray-700' : 'bg-white border-gray-200 shadow-sm'"
  >
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <div class="flex items-center gap-2">
          <span class="text-xl">AI</span>
          <h3 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">{{ title }}</h3>
        </div>
        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="mt-1 text-sm">{{ description }}</p>
      </div>
      <el-button type="primary" :loading="loading" :disabled="!canAnalyze" @click="analyze">
        {{ loading ? '分析中...' : buttonText }}
      </el-button>
    </div>

    <div class="mt-4">
      <el-input
        v-model="userQuestion"
        type="textarea"
        :rows="2"
        maxlength="180"
        show-word-limit
        placeholder="可选：补充你的问题，例如更看重地域、专业、院校层次或就业方向"
      />
    </div>

    <div
      v-if="analysis"
      class="mt-5 rounded-xl border p-4"
      :class="analysis.aiEnabled ? 'border-emerald-200 bg-emerald-50/60 dark:bg-emerald-950/20 dark:border-emerald-800' : 'border-amber-200 bg-amber-50/70 dark:bg-amber-950/20 dark:border-amber-800'"
    >
      <div class="flex flex-wrap items-center justify-between gap-2 mb-3">
        <span class="text-sm font-medium" :class="isDark ? 'text-gray-200' : 'text-gray-800'">
          {{ analysis.providerStatus || (analysis.aiEnabled ? '大模型分析' : '本地规则分析') }}
        </span>
        <span class="text-xs" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
          {{ analysis.model }}
        </span>
      </div>

      <p class="text-sm leading-6" :class="isDark ? 'text-gray-200' : 'text-gray-700'">{{ analysis.summary }}</p>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
        <div v-if="analysis.strengths?.length">
          <div class="text-sm font-semibold text-emerald-600 mb-2">优势</div>
          <ul class="space-y-1 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
            <li v-for="item in analysis.strengths" :key="item">• {{ item }}</li>
          </ul>
        </div>
        <div v-if="analysis.risks?.length">
          <div class="text-sm font-semibold text-orange-600 mb-2">风险</div>
          <ul class="space-y-1 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
            <li v-for="item in analysis.risks" :key="item">• {{ item }}</li>
          </ul>
        </div>
        <div v-if="analysis.suggestions?.length">
          <div class="text-sm font-semibold text-blue-600 mb-2">建议</div>
          <ul class="space-y-1 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
            <li v-for="item in analysis.suggestions" :key="item">• {{ item }}</li>
          </ul>
        </div>
      </div>

      <div v-if="analysis.actionPlan" class="mt-4 rounded-lg p-3 text-sm" :class="isDark ? 'bg-black/20 text-gray-300' : 'bg-white/70 text-gray-700'">
        <span class="font-semibold">下一步：</span>{{ analysis.actionPlan }}
      </div>
    </div>
  </div>
</template>
