<script setup>
import * as echarts from 'echarts'
import request from '@/utils/request'
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Nav from "@/components/Nav.vue";

const router = useRouter()

// 数据响应式
const isDark = ref(false)
const loading = ref(false)
const userId = ref(null)
const activeTab = ref('graph')
const filters = reactive({
  categoryLevel: '',
  subjectName: ''
})

// 固定数据
const subjects = ref(['数学', '英语', '语文', '物理', '化学', '生物', '历史', '地理', '政治'])

// 接口数据
const statistics = ref({})
const graphData = ref(null)
const heatmapData = ref(null)
const trendData = ref(null)
const learningPath = ref([])
const radarData = ref(null)

// 图表实例（用于销毁）
const charts = ref({})

// 计算属性
const tabs = computed(() => [
  { key: 'graph', name: '知识图谱', icon: '🔗' },
  { key: 'heatmap', name: '热力图', icon: '🔥' },
  { key: 'trend', name: '掌握趋势', icon: '📈' },
  { key: 'path', name: '学习路径', icon: '🎯' },
  { key: 'radar', name: '雷达图', icon: '📡' }
])

// 导航菜单配置
const menuItems = [
  {
    key: 'CourseSelection',
    label: '明确目标',
    icon: '🎯',
    path: '/CourseSelection'
  },
  {
    key: 'Volunteer',
    label: '规划未来',
    icon: '🎓',
    path: '/Volunteer'
  },
  {
    key: 'StudyDashboard',
    label: '温故而知新',
    icon: '📚',
    children: [
      { key: 'practice', label: '实战练习', icon: '⚡', path: '/StudyDashboard?tab=practice' },
      { key: 'mistake', label: '错题本', icon: '📖', path: '/StudyDashboard?tab=mistake' },
      { key: 'analysis', label: '成绩分析', icon: '📊', path: '/StudyDashboard?tab=analysis' },
      { key: 'questionBank', label: '题库管理', icon: '📚', path: '/StudyDashboard?tab=questionBank' },
      { key: 'answerRecords', label: '答题记录', icon: '✍️', path: '/StudyDashboard?tab=answerRecords' }
    ]
  },
  {
    key: '个人图谱分析',
    label: '图谱',
    icon: '👤',
    path: '/GraphView'
  }
]

// 方法
const checkTheme = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

const getCurrentUser = () => {
  request.get('/user/getUserById', {}, (message, data) => {
    if (data && data.id) {
      userId.value = data.id
      loadData()
    }
  })
}

const goBack = () => {
  router.push('/')
}

const loadData = async () => {
  if (!userId.value) {
    console.warn('用户未登录')
    return
  }
  loading.value = true
  try {
    await Promise.all([
      loadGraphData(),
      loadHeatmapData(),
      loadTrendData(),
      loadLearningPath(),
      loadRadarData()
    ])
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const loadGraphData = async () => {
  try {
    const res = await request.get(`/question/knowledge-graph/${userId.value}`, filters)
    if (res.code === 200) {
      graphData.value = res.data
      statistics.value = res.data.statistics || {}
      await nextTick()
      renderGraph()
    }
  } catch (error) {
    console.error('加载知识图谱失败', error)
  }
}

const loadHeatmapData = async () => {
  try {
    const res = await request.get(`/question/knowledge-heatmap/${userId.value}`, {
      categoryLevel: filters.categoryLevel
    })
    if (res.code === 200) {
      heatmapData.value = res.data
      await nextTick()
      renderHeatmap()
    }
  } catch (error) {
    console.error('加载热力图失败', error)
  }
}

const loadTrendData = async () => {
  try {
    const res = await request.get(`/question/knowledge-trend/${userId.value}`, {
      subjectName: filters.subjectName,
      days: 30
    })
    if (res.code === 200) {
      trendData.value = res.data
      await nextTick()
      renderTrend()
    }
  } catch (error) {
    console.error('加载趋势失败', error)
  }
}

const loadLearningPath = async () => {
  try {
    const res = await request.get(`/question/learning-path/${userId.value}`, {
      categoryLevel: filters.categoryLevel
    })
    if (res.code === 200) {
      learningPath.value = res.data
    }
  } catch (error) {
    console.error('加载学习路径失败', error)
  }
}

const loadRadarData = async () => {
  try {
    const res = await request.get(`/question/knowledge-radar/${userId.value}`, {
      categoryLevel: filters.categoryLevel
    })
    if (res.code === 200) {
      radarData.value = res.data
      await nextTick()
      renderRadar()
    }
  } catch (error) {
    console.error('加载雷达图失败', error)
  }
}

const renderGraph = () => {
  if (!graphData.value) return

  const chartDom = document.getElementById('knowledgeGraphChart')
  if (!chartDom) return

  if (charts.value.graph) charts.value.graph.dispose()
  charts.value.graph = echarts.init(chartDom)

  const nodes = (graphData.value.nodes || []).map(node => ({
    id: node.id,
    name: node.name,
    symbolSize: Math.min(50, 15 + (node.totalCount || 5) / 2),
    category: node.type === 'subject' ? 0 : (node.type === 'category' ? 1 : 2),
    correctRate: node.correctRate,
    totalCount: node.totalCount,
    itemStyle: {
      color: getColorByCorrectRate(node.correctRate)
    },
    label: {
      show: true,
      formatter: `${node.name}\n${((node.correctRate || 0) * 100).toFixed(1)}%`,
      fontSize: 10
    }
  }))

  const edges = (graphData.value.edges || []).map(edge => ({
    source: edge.source,
    target: edge.target,
    lineStyle: {
      width: Math.min(4, (edge.weight || 1) / 2),
      curveness: 0.2,
      color: '#999'
    }
  }))

  const categories = [
    { name: '科目', itemStyle: { color: '#5470c6' } },
    { name: '分类', itemStyle: { color: '#fac858' } },
    { name: '知识点', itemStyle: { color: '#ee6666' } }
  ]

  charts.value.graph.setOption({
    title: {
      text: '知识图谱',
      subtext: '节点大小=答题次数 | 颜色=正确率(绿高红低)',
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `${params.data.name}<br/>答题次数: ${params.data.totalCount || 0}<br/>正确率: ${((params.data.correctRate || 0) * 100).toFixed(1)}%`
        }
        return `${params.data.source} → ${params.data.target}<br/>关联强度: ${params.data.lineStyle.width}`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: {
        repulsion: 500,
        edgeLength: [80, 200],
        gravity: 0.1,
        friction: 0.1
      },
      categories: categories,
      data: nodes,
      links: edges,
      roam: true,
      draggable: true,
      focusNodeAdjacency: true,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 6],
      label: {
        show: true,
        position: 'right',
        fontSize: 10,
        offset: [5, 0]
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3, color: '#2c3e50' }
      },
      lineStyle: { color: 'source', curveness: 0.3 }
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => charts.value.graph?.resize())
}

const renderHeatmap = () => {
  if (!heatmapData.value || !heatmapData.value.heatmapData) return

  const chartDom = document.getElementById('heatmapChart')
  if (!chartDom) return

  if (charts.value.heatmap) charts.value.heatmap.dispose()
  charts.value.heatmap = echarts.init(chartDom)

  const subjects = []
  const knowledgePoints = []
  const data = []

  Object.entries(heatmapData.value.heatmapData).forEach(([subject, points]) => {
    subjects.push(subject)
    Object.entries(points).forEach(([kp, stats]) => {
      if (!knowledgePoints.includes(kp)) knowledgePoints.push(kp)
      data.push([subject, kp, stats.correctRate])
    })
  })

  charts.value.heatmap.setOption({
    title: {
      text: '知识点掌握热力图',
      subtext: `总答题数: ${heatmapData.value.totalRecords || 0} | 整体正确率: ${((heatmapData.value.overallCorrectRate || 0) * 100).toFixed(1)}%`,
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: {
      position: 'top',
      formatter: (params) => `${params.data[0]} - ${params.data[1]}<br/>正确率: ${(params.data[2] * 100).toFixed(1)}%`
    },
    xAxis: {
      type: 'category',
      data: subjects,
      name: '科目',
      axisLabel: { rotate: 45, interval: 0 }
    },
    yAxis: {
      type: 'category',
      data: knowledgePoints,
      name: '知识点'
    },
    visualMap: {
      min: 0,
      max: 1,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 10,
      inRange: { color: ['#f5222d', '#faad14', '#52c41a'] },
      formatter: (value) => (value * 100).toFixed(0) + '%'
    },
    series: [{
      name: '正确率',
      type: 'heatmap',
      data: data,
      label: {
        show: true,
        formatter: (params) => (params.data[2] * 100).toFixed(0) + '%',
        fontSize: 10
      },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } }
    }],
    backgroundColor: 'transparent'
  })
}

const renderTrend = () => {
  if (!trendData.value || !trendData.value.trendData) return

  const chartDom = document.getElementById('trendChart')
  if (!chartDom) return

  if (charts.value.trend) charts.value.trend.dispose()
  charts.value.trend = echarts.init(chartDom)

  const dates = trendData.value.trendData.map(d => d.date)
  const correctRates = trendData.value.trendData.map(d => (d.correctRate * 100).toFixed(1))
  const totals = trendData.value.trendData.map(d => d.total)

  const trendDirection = trendData.value.trendDirection === 'up' ? '📈 上升' : (trendData.value.trendDirection === 'down' ? '📉 下降' : '➡️ 平稳')
  const trendColor = trendData.value.trendDirection === 'up' ? '#52c41a' : (trendData.value.trendDirection === 'down' ? '#f5222d' : '#faad14')

  charts.value.trend.setOption({
    title: {
      text: '知识掌握趋势',
      subtext: `平均正确率: ${(trendData.value.averageCorrectRate * 100).toFixed(1)}% | 趋势: ${trendDirection}`,
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' },
      subtextStyle: { color: trendColor }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let res = params[0].axisValue + '<br/>'
        res += `正确率: ${params[0].value}%<br/>`
        res += `答题数: ${totals[params[0].dataIndex]}`
        return res
      }
    },
    legend: { data: ['正确率', '答题数'], left: 'left' },
    xAxis: {
      type: 'category',
      data: dates,
      name: '日期',
      axisLabel: { rotate: 45, interval: Math.floor(dates.length / 10) }
    },
    yAxis: [
      { type: 'value', name: '正确率 (%)', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
      { type: 'value', name: '答题数', min: 0 }
    ],
    series: [
      {
        name: '正确率',
        type: 'line',
        data: correctRates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#52c41a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        }
      },
      {
        name: '答题数',
        type: 'bar',
        data: totals,
        yAxisIndex: 1,
        barWidth: '30%',
        itemStyle: { color: '#5470c6', borderRadius: [4, 4, 0, 0] }
      }
    ],
    grid: { containLabel: true, top: 60, bottom: 30 },
    backgroundColor: 'transparent'
  })
}

const renderRadar = () => {
  if (!radarData.value || !radarData.value.indicators || radarData.value.indicators.length === 0) return

  const chartDom = document.getElementById('radarChart')
  if (!chartDom) return

  if (charts.value.radar) charts.value.radar.dispose()
  charts.value.radar = echarts.init(chartDom)

  let indicators = radarData.value.indicators
  let seriesData = radarData.value.seriesData

  if (indicators.length > 10) {
    const combined = indicators.map((ind, i) => ({ name: ind, value: seriesData[i] }))
    combined.sort((a, b) => b.value - a.value)
    const top = combined.slice(0, 10)
    indicators = top.map(t => t.name)
    seriesData = top.map(t => t.value)
  }

  charts.value.radar.setOption({
    title: {
      text: '知识点掌握雷达图',
      subtext: `共 ${radarData.value.knowledgeCount} 个知识点`,
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: { trigger: 'item', formatter: (params) => `${params.name}<br/>掌握度: ${params.value.toFixed(1)}%` },
    radar: {
      indicator: indicators.map(ind => ({ name: ind.length > 15 ? ind.slice(0, 12) + '...' : ind, max: 100 })),
      shape: 'circle',
      center: ['50%', '50%'],
      radius: '60%',
      name: { textStyle: { fontSize: 10 } },
      splitArea: { areaStyle: { color: ['rgba(84,112,198,0.1)', 'rgba(84,112,198,0.05)'] } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: seriesData,
        name: '掌握度',
        areaStyle: { color: 'rgba(84,112,198,0.3)' },
        lineStyle: { width: 2, color: '#5470c6' },
        itemStyle: { color: '#5470c6' }
      }]
    }],
    backgroundColor: 'transparent'
  })
}

const getColorByCorrectRate = (rate) => {
  if (rate >= 0.8) return '#52c41a'
  if (rate >= 0.6) return '#faad14'
  if (rate >= 0.4) return '#fa8c16'
  return '#f5222d'
}

const getProgressColor = (rate) => {
  if (rate >= 0.8) return 'linear-gradient(90deg, #52c41a, #95de64)'
  if (rate >= 0.6) return 'linear-gradient(90deg, #faad14, #ffd666)'
  if (rate >= 0.4) return 'linear-gradient(90deg, #fa8c16, #ffa940)'
  return 'linear-gradient(90deg, #f5222d, #ff7875)'
}

const getPathType = (rate) => {
  if (rate >= 0.6) return 'success'
  if (rate >= 0.3) return 'warning'
  return 'danger'
}

const getTagType = (rate) => {
  if (rate >= 0.6) return 'success'
  if (rate >= 0.3) return 'warning'
  return 'danger'
}

const getSuggestAction = (rate) => {
  if (rate < 0.3) return '重点复习'
  if (rate < 0.6) return '加强练习'
  return '保持练习'
}

const resetFilters = () => {
  filters.categoryLevel = ''
  filters.subjectName = ''
  loadData()
}

// 生命周期
onMounted(() => {
  checkTheme()
  getCurrentUser()

  // 监听主题变化
  const observer = new MutationObserver(() => {
    checkTheme()
  })
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })

  // 滚动动画
  const scrollObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
      }
    })
  }, { threshold: 0.2 })

  setTimeout(() => {
    document.querySelectorAll('.scroll-animate').forEach(el => scrollObserver.observe(el))
  }, 100)
})

onBeforeUnmount(() => {
  // 销毁所有图表
  Object.values(charts.value).forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<template>
  <div class="knowledge-graph-container" :class="isDark ? 'dark' : ''">
    <Nav :isDark="isDark" :menuItems="menuItems"/>

    <!-- 筛选栏 -->
    <div class="filter-bar mt-24">
      <div class="filter-item">
        <label>分类层级：</label>
        <el-select v-model="filters.categoryLevel" placeholder="全部" clearable @change="loadData">
          <el-option label="高中" value="高中" />
          <el-option label="大学" value="大学" />
          <el-option label="考公" value="考公" />
          <el-option label="考研" value="考研" />
        </el-select>
      </div>
      <div class="filter-item">
        <label>科目：</label>
        <el-select v-model="filters.subjectName" placeholder="全部" clearable @change="loadData">
          <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-button type="primary" @click="loadData">刷新图谱</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card scroll-animate">
        <div class="stat-icon">📊</div>
        <div class="stat-value">{{ statistics.totalQuestions || 0 }}</div>
        <div class="stat-label">总答题数</div>
      </div>
      <div class="stat-card correct scroll-animate">
        <div class="stat-icon">✅</div>
        <div class="stat-value">{{ statistics.correctCount || 0 }}</div>
        <div class="stat-label">正确数</div>
      </div>
      <div class="stat-card wrong scroll-animate">
        <div class="stat-icon">❌</div>
        <div class="stat-value">{{ statistics.wrongCount || 0 }}</div>
        <div class="stat-label">错误数</div>
      </div>
      <div class="stat-card rate scroll-animate">
        <div class="stat-icon">📈</div>
        <div class="stat-value">{{ (statistics.overallCorrectRate * 100).toFixed(1) }}%</div>
        <div class="stat-label">正确率</div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs-container scroll-animate">
      <div class="tabs-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          class="tab-btn"
          :class="{
            active: activeTab === tab.key,
            [isDark ? 'dark' : 'light']: true
          }"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span>{{ tab.name }}</span>
        </button>
      </div>

      <!-- 知识图谱 -->
      <div v-show="activeTab === 'graph'" class="tab-content">
        <div class="chart-header">
          <h3>📊 知识图谱</h3>
          <p class="chart-desc">节点大小=答题次数 | 颜色=正确率(绿高红低)</p>
        </div>
        <div class="chart-container">
          <div id="knowledgeGraphChart" class="chart"></div>
        </div>
      </div>

      <!-- 知识点热力图 -->
      <div v-show="activeTab === 'heatmap'" class="tab-content">
        <div class="chart-header">
          <h3>🔥 知识点掌握热力图</h3>
          <p class="chart-desc">颜色越绿表示掌握越好，越红表示薄弱</p>
        </div>
        <div class="chart-container">
          <div id="heatmapChart" class="chart"></div>
        </div>
      </div>

      <!-- 掌握趋势 -->
      <div v-show="activeTab === 'trend'" class="tab-content">
        <div class="chart-header">
          <h3>📈 知识掌握趋势</h3>
          <p class="chart-desc">展示最近30天的正确率变化</p>
        </div>
        <div class="chart-container">
          <div id="trendChart" class="chart"></div>
        </div>
      </div>

      <!-- 学习路径 -->
      <div v-show="activeTab === 'path'" class="tab-content">
        <div class="learning-path" :class="isDark ? 'bg-gray-900/30' : 'bg-gray-50'">
          <div class="path-header">
            <h3>🎯 学习路径推荐</h3>
            <p>基于薄弱知识点生成的学习建议</p>
          </div>
          <div v-if="learningPath.length > 0" class="path-timeline">
            <div v-for="(item, index) in learningPath" :key="index" class="path-item scroll-animate">
              <div class="path-marker" :class="getPathType(item.correctRate)"></div>
              <div class="path-content" :class="isDark ? 'bg-gray-800' : 'bg-white'">
                <div class="path-header-row">
                  <h4>{{ item.knowledgePoint }}</h4>
                  <el-tag :type="getTagType(item.correctRate)" size="small">{{ getSuggestAction(item.correctRate) }}</el-tag>
                </div>
                <div class="path-info">
                  <span>📚 科目：{{ item.subject }}</span>
                  <span>📁 分类：{{ item.categoryLevel }}</span>
                </div>
                <div class="path-progress">
                  <div class="progress-label">
                    <span>正确率</span>
                    <span>{{ (item.correctRate * 100).toFixed(1) }}%</span>
                  </div>
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: (item.correctRate * 100) + '%', background: getProgressColor(item.correctRate) }"></div>
                  </div>
                </div>
                <div class="path-stats">
                  <span>📝 答题次数：{{ item.total }}</span>
                  <span>❌ 错误次数：{{ item.wrong }}</span>
                  <span>🎯 优先级：{{ item.suggestedPriority?.toFixed(2) || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">
            <span class="empty-icon">🎉</span>
            <p>暂无薄弱知识点，继续保持！</p>
          </div>
        </div>
      </div>

      <!-- 知识点雷达图 -->
      <div v-show="activeTab === 'radar'" class="tab-content">
        <div class="chart-header">
          <h3>📡 知识点掌握雷达图</h3>
          <p class="chart-desc">多维度展示各知识点掌握程度</p>
        </div>
        <div class="chart-container">
          <div id="radarChart" class="chart"></div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>正在加载知识图谱...</p>
    </div>
  </div>
</template>

<style scoped>
.knowledge-graph-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  transition: all 0.3s ease;
}

.knowledge-graph-container.dark {
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a1a 100%);
}

.back-button {
  position: fixed;
  top: 90px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #4f46e5;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  z-index: 100;
}

.dark .back-button {
  background: rgba(30, 30, 40, 0.9);
  color: #818cf8;
}

.back-button:hover {
  transform: translateX(-4px);
}

.filter-bar {
  display: flex;
  gap: 20px;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.dark .filter-bar {
  background: rgba(30, 30, 40, 0.8);
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
}

.dark .filter-item label {
  color: #e5e7eb;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto 30px auto;
}

.stat-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.dark .stat-card {
  background: rgba(30, 30, 40, 0.8);
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.dark .stat-value {
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  -webkit-background-clip: text;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
}

.dark .stat-label {
  color: #9ca3af;
}

.stat-card.correct .stat-value {
  background: linear-gradient(135deg, #52c41a, #95de64);
  -webkit-background-clip: text;
  background-clip: text;
}

.stat-card.wrong .stat-value {
  background: linear-gradient(135deg, #f5222d, #ff7875);
  -webkit-background-clip: text;
  background-clip: text;
}

.stat-card.rate .stat-value {
  background: linear-gradient(135deg, #faad14, #ffd666);
  -webkit-background-clip: text;
  background-clip: text;
}

.tabs-container {
  max-width: 1300px;
  margin: 0 auto;
}

.tabs-bar {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border: none;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.light {
  background: rgba(243, 244, 246, 0.8);
  color: #4b5563;
}

.tab-btn.dark {
  background: rgba(31, 41, 55, 0.8);
  color: #9ca3af;
}

.tab-btn.active {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: white;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3);
}

.tab-icon {
  font-size: 16px;
}

.chart-header {
  text-align: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1f2937;
}

.dark .chart-header h3 {
  color: #f3f4f6;
}

.chart-desc {
  font-size: 13px;
  color: #6b7280;
}

.dark .chart-desc {
  color: #9ca3af;
}

.chart-container {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.dark .chart-container {
  background: rgba(30, 30, 40, 0.8);
}

.chart {
  width: 100%;
  height: 550px;
}

.learning-path {
  border-radius: 24px;
  padding: 30px;
  min-height: 500px;
}

.path-header {
  text-align: center;
  margin-bottom: 30px;
}

.path-header h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1f2937;
}

.dark .path-header h3 {
  color: #f3f4f6;
}

.path-header p {
  font-size: 13px;
  color: #6b7280;
}

.path-timeline {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.path-item {
  display: flex;
  gap: 20px;
  position: relative;
}

.path-marker {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-top: 20px;
  flex-shrink: 0;
}

.path-marker.success {
  background: #52c41a;
  box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.2);
}

.path-marker.warning {
  background: #faad14;
  box-shadow: 0 0 0 4px rgba(250, 173, 20, 0.2);
}

.path-marker.danger {
  background: #f5222d;
  box-shadow: 0 0 0 4px rgba(245, 34, 45, 0.2);
}

.path-content {
  flex: 1;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.path-content:hover {
  transform: translateX(4px);
}

.path-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.path-header-row h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .path-header-row h4 {
  color: #f3f4f6;
}

.path-info {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  font-size: 13px;
  color: #6b7280;
}

.dark .path-info {
  color: #9ca3af;
}

.path-progress {
  margin-bottom: 12px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 6px;
  color: #6b7280;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.dark .progress-bar {
  background: #374151;
}

.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s ease;
}

.path-stats {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 12px;
}

.empty-tip {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-tip p {
  font-size: 16px;
  color: #6b7280;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(79, 70, 229, 0.2);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-overlay p {
  margin-top: 20px;
  color: white;
  font-size: 14px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.scroll-animate {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.scroll-animate.animated {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .knowledge-graph-container {
    padding: 15px;
  }

  .back-button {
    top: 80px;
    left: 15px;
    padding: 8px 15px;
    font-size: 12px;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-top: 60px;
  }

  .stat-card {
    padding: 15px;
  }

  .stat-value {
    font-size: 22px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .tab-btn {
    padding: 8px 16px;
    font-size: 12px;
  }

  .chart {
    height: 400px;
  }

  .path-info, .path-stats {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
