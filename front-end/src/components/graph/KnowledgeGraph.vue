<template>
  <div class="graph-container w-full h-full relative bg-[#0f1219] rounded-2xl overflow-hidden">
    <div class="absolute top-4 left-4 z-10 flex gap-2 bg-white/10 backdrop-blur-md rounded-lg p-1 shadow-lg">
      <button
        v-for="type in graphTypes"
        :key="type.value"
        @click="switchGraphType(type.value)"
        :class="[
          'px-4 py-2 rounded-md text-sm font-medium transition-all',
          currentGraphType === type.value
            ? 'bg-blue-500 text-white shadow-lg'
            : 'text-gray-300 hover:bg-white/10 hover:text-white'
        ]"
      >
        {{ type.label }}
      </button>
    </div>

    <div ref="chartRef" class="w-full h-full"></div>

    <div
      v-if="selectedNode"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50"
      @click="closeDetail"
    >
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full mx-4" @click.stop>
        <div class="p-5 border-b border-gray-100">
          <div class="flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-800">{{ selectedNode.name }}</h3>
            <button @click="closeDetail" class="text-gray-400 hover:text-gray-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
        <div class="p-5 space-y-3">
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">节点类型</span>
            <span class="font-medium">
              <span :class="getNodeTypeClass(selectedNode.type)" class="px-2 py-1 rounded-full text-xs">
                {{ getNodeTypeLabel(selectedNode.type) }}
              </span>
            </span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">关联权重</span>
            <span class="font-medium text-blue-600">{{ selectedNode.value }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="absolute inset-0 bg-[#0f1219]/90 flex flex-col items-center justify-center z-20">
      <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mb-3"></div>
      <span class="text-gray-300 text-sm">知识图谱构建中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  userId: { type: Number, default: null },
  initialType: { type: String, default: 'content' }
})

const chartRef = ref(null)
let chart = null
const loading = ref(false)
const currentGraphType = ref(props.initialType)
const selectedNode = ref(null)

const graphTypes = [
  { value: 'content', label: '内容知识图谱' },
  { value: 'answer', label: '答题能力图谱' }
]

const getNodeTypeLabel = (type) => {
  const map = { tag: '标签', category: '分类', knowledge_point: '知识点', subject: '科目' }
  return map[type] || type
}

const getNodeTypeClass = (type) => {
  const cls = {
    tag: 'bg-purple-100 text-purple-600',
    category: 'bg-blue-100 text-blue-600',
    knowledge_point: 'bg-green-100 text-green-600',
    subject: 'bg-orange-100 text-orange-600'
  }
  return cls[type] || 'bg-gray-100 text-gray-600'
}

const switchGraphType = (type) => {
  currentGraphType.value = type
  fetchGraphData()
}

// ==========================
// ✅ 修复 401：自动带 Token
// ==========================
const getAuthHeader = () => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  return token ? `Bearer ${token}` : ''
}

const getUserId = () => {
  return localStorage.getItem('userId') || props.userId || ''
}

// ==========================
// ✅ 修复接口请求
// ==========================
const fetchGraphData = async () => {
  loading.value = true
  try {
    let url = `http://localhost:8500/api/graph/${currentGraphType.value}`
    const uid = getUserId()

    if (currentGraphType.value === 'answer' && uid) {
      url += `?userId=${uid}`
    }

    const res = await fetch(url, {
      headers: {
        'Authorization': getAuthHeader(),
        'X-User-Id': uid
      }
    })

    const data = await res.json()
    if (data.code === 200) {
      renderAdvancedGraph(data.data)
    }
  } catch (e) {
    console.error('图谱加载失败', e)
  } finally {
    loading.value = false
  }
}

const renderAdvancedGraph = (data) => {
  if (!chart) return

  const nodes = data.nodes.map(node => ({
    id: node.id,
    name: node.name,
    category: node.type,
    value: node.value || 1,
    symbolSize: Math.min(60, 18 + (node.value || 1) * 1.5),
    itemStyle: {
      color: getNodeGradient(node.type),
      shadowBlur: 20,
      shadowColor: getNodeColor(node.type)
    },
    label: { color: '#fff' }
  }))

  const links = data.links.map(link => ({
    source: link.source,
    target: link.target,
    value: link.value || 1,
    lineStyle: {
      width: Math.min(4, 0.5 + link.value / 15),
      color: new echarts.graphic.LinearGradient(0,0,1,0,[
        { offset: 0, color: '#6366f1' },
        { offset: 1, color: '#8b5cf6' }
      ])
    }
  }))

  chart.setOption({
    backgroundColor: '#0f1219',
    tooltip: {
      trigger: 'item',
      textStyle: { color: '#fff' },
      backgroundColor: 'rgba(17,25,40,0.9)'
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: { repulsion: 800, edgeLength: [80, 200] },
      roam: true,
      data: nodes,
      links: links,
      label: { show: true, color: '#fff' }
    }]
  })

  chart.off('click')
  chart.on('click', (params) => {
    if (params.dataType === 'node') {
      selectedNode.value = data.nodes.find(n => n.id === params.data.id)
    }
  })
}

const getNodeColor = (type) => {
  const colors = {
    tag: '#a855f7',
    category: '#3b82f6',
    knowledge_point: '#10b981',
    subject: '#f59e0b'
  }
  return colors[type] || '#6b7280'
}

const getNodeGradient = (type) => {
  const c = getNodeColor(type)
  return new echarts.graphic.LinearGradient(0,0,0,1,[
    { offset: 0, color: c },
    { offset: 1, color: c + 'cc' }
  ])
}

const closeDetail = () => selectedNode.value = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  fetchGraphData()
  window.addEventListener('resize', () => chart.resize())
})

onUnmounted(() => {
  chart?.dispose()
})

watch(() => props.userId, fetchGraphData)
</script>

<style scoped>
.graph-container { min-height: 550px; }
</style>
