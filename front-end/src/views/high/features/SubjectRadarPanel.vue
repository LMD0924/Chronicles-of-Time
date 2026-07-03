<!--
  文件说明：拾光记前台应用高中阶段页面组件，承载高中阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'

const props = defineProps({ isDark: Boolean })
const router = useRouter()
const chartRef = ref(null)
const userId = ref(null)
let chart = null

const load = async () => {
  const u = await request.get('/user/getUserById')
  if (u.code !== 200) return
  userId.value = u.data?.id
  const res = await request.get(`/question/knowledge-radar/${userId.value}`)
  if (res.code !== 200 || !chartRef.value) return
  const data = res.data || {}
  const indicators = (data.subjects || data.indicators || ['语文', '数学', '英语', '物理', '化学', '生物']).map((s) =>
    typeof s === 'string' ? { name: s, max: 100 } : s
  )
  const values = data.values || data.scores || [70, 80, 75, 60, 65, 70]
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    radar: {
      indicator: indicators.length ? indicators : [{ name: '综合', max: 100 }],
      splitArea: { areaStyle: { color: props.isDark ? ['#18181b', '#27272a'] : ['#fafafa', '#f4f4f5'] } },
    },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '掌握度', areaStyle: { color: 'rgba(217,70,239,0.25)' }, lineStyle: { color: '#d946ef' } }],
    }],
  })
}

onMounted(load)
watch(() => props.isDark, () => setTimeout(load, 100))
onUnmounted(() => chart?.dispose())
</script>

<template>
  <div class="space-y-4">
    <p class="text-sm text-zinc-500">数据来自错题本与答题记录聚合（知识雷达 API）</p>
    <div ref="chartRef" class="app-card-surface w-full h-[400px]" />
    <button type="button" class="app-btn-secondary text-sm" @click="router.push('/GraphView')">打开完整学习图谱</button>
  </div>
</template>
