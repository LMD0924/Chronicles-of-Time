<!--
  文件说明：拾光记前台应用数据驾驶舱页面组件，承载数据驾驶舱场景的界面展示、交互操作和数据承接。
-->
<script setup>
import * as echarts from 'echarts'
import request from '@/utils/request'
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
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

const formatPercent = (value) => `${((Number(value) || 0) * 100).toFixed(1)}%`
const formatNumber = (value) => Number(value || 0).toLocaleString('zh-CN')

// 图表实例（用于销毁）
const charts = ref({})
let themeObserver = null
let scrollObserver = null

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

const getThemeColor = (name, fallback) => {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

const getChartTheme = () => {
  const primary = getThemeColor('--theme-primary', '#c026d3')
  const secondary = getThemeColor('--theme-secondary', '#6d28d9')
  const primaryRgb = getThemeColor('--theme-primary-rgb', '192 38 211').replace(/\s+/g, ', ')
  const secondaryRgb = getThemeColor('--theme-secondary-rgb', '109 40 217').replace(/\s+/g, ', ')

  return {
    primary,
    secondary,
    primaryRgb,
    secondaryRgb,
    text: isDark.value ? '#fff7ff' : '#1d1428',
    muted: isDark.value ? '#aa9ab8' : '#7b7087',
    line: isDark.value ? 'rgba(255, 255, 255, 0.1)' : `rgba(${primaryRgb}, 0.12)`,
    tooltipBg: isDark.value ? 'rgba(20, 12, 30, 0.94)' : 'rgba(255, 255, 255, 0.96)',
    tooltipBorder: isDark.value ? `rgba(${primaryRgb}, 0.34)` : `rgba(${primaryRgb}, 0.18)`
  }
}

const hexToRgba = (hex, alpha = 1) => {
  const normalized = (hex || '#c026d3').replace('#', '')
  const value = normalized.length === 3
    ? normalized.split('').map(char => char + char).join('')
    : normalized
  const r = parseInt(value.slice(0, 2), 16)
  const g = parseInt(value.slice(2, 4), 16)
  const b = parseInt(value.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const chartTitle = (text, subtext = '') => {
  return { show: false, text, subtext }
}

const chartTooltip = (formatter, trigger = 'item') => {
  const theme = getChartTheme()
  return {
    trigger,
    borderWidth: 1,
    borderColor: theme.tooltipBorder,
    backgroundColor: theme.tooltipBg,
    textStyle: {
      color: theme.text,
      fontSize: 12
    },
    extraCssText: 'box-shadow:0 18px 45px rgba(40,16,68,.16);border-radius:12px;backdrop-filter:blur(16px);',
    formatter
  }
}

const axisTextStyle = () => {
  const theme = getChartTheme()
  return {
    axisLabel: { color: theme.muted, fontSize: 11 },
    axisLine: { lineStyle: { color: theme.line } },
    axisTick: { lineStyle: { color: theme.line } },
    splitLine: { lineStyle: { color: theme.line, type: 'dashed' } },
    nameTextStyle: { color: theme.muted, fontWeight: 700 }
  }
}

const themedGradient = (direction = 'vertical', opacity = 1) => {
  const theme = getChartTheme()
  const coords = direction === 'horizontal' ? [0, 0, 1, 0] : [0, 0, 0, 1]
  return new echarts.graphic.LinearGradient(...coords, [
    { offset: 0, color: opacity === 1 ? theme.primary : `rgba(${theme.primaryRgb}, ${opacity})` },
    { offset: 1, color: opacity === 1 ? theme.secondary : `rgba(${theme.secondaryRgb}, ${Math.max(0.06, opacity * 0.24)})` }
  ])
}

const refreshCharts = () => {
  renderGraph()
  renderHeatmap()
  renderTrend()
  renderRadar()
}

const resizeCharts = () => {
  Object.values(charts.value).forEach(chart => chart?.resize())
}

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
  const theme = getChartTheme()

  const nodes = (graphData.value.nodes || []).map(node => ({
    id: node.id,
    name: node.name,
    symbolSize: Math.min(64, 18 + (node.totalCount || 5) / 2),
    category: node.type === 'subject' ? 0 : (node.type === 'category' ? 1 : 2),
    correctRate: node.correctRate,
    totalCount: node.totalCount,
    itemStyle: {
      color: node.type === 'subject'
        ? theme.primary
        : node.type === 'category'
          ? theme.secondary
          : getColorByCorrectRate(node.correctRate),
      borderColor: isDark.value ? 'rgba(255, 255, 255, 0.38)' : 'rgba(255, 255, 255, 0.92)',
      borderWidth: 2,
      shadowBlur: 18,
      shadowColor: node.type === 'knowledge' ? 'rgba(245, 34, 45, 0.16)' : `rgba(${theme.primaryRgb}, 0.24)`
    },
    label: {
      show: true,
      formatter: `${node.name}\n${((node.correctRate || 0) * 100).toFixed(1)}%`,
      fontSize: 11,
      color: theme.text,
      lineHeight: 16
    }
  }))

  const edges = (graphData.value.edges || []).map(edge => ({
    source: edge.source,
    target: edge.target,
    lineStyle: {
      width: Math.min(4, (edge.weight || 1) / 2),
      curveness: 0.22,
      color: `rgba(${theme.primaryRgb}, 0.28)`,
      opacity: 0.76
    }
  }))

  const categories = [
    { name: '科目', itemStyle: { color: theme.primary } },
    { name: '分类', itemStyle: { color: theme.secondary } },
    { name: '知识点', itemStyle: { color: '#ee6666' } }
  ]

  charts.value.graph.setOption({
    color: [theme.primary, theme.secondary, '#f43f5e'],
    title: chartTitle('知识图谱', '节点大小代表答题频次，颜色体现掌握状态'),
    tooltip: chartTooltip((params) => {
      if (params.dataType === 'node') {
        return `${params.data.name}<br/>答题次数：${params.data.totalCount || 0}<br/>正确率：${((params.data.correctRate || 0) * 100).toFixed(1)}%`
      }
      return `${params.data.source} → ${params.data.target}<br/>关联强度：${params.data.lineStyle.width}`
    }),
    legend: {
      top: 24,
      right: 24,
      textStyle: { color: theme.muted },
      itemWidth: 10,
      itemHeight: 10
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: {
        repulsion: 680,
        edgeLength: [92, 210],
        gravity: 0.08,
        friction: 0.22
      },
      categories: categories,
      data: nodes,
      links: edges,
      roam: true,
      draggable: true,
      focusNodeAdjacency: true,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 7],
      label: {
        show: true,
        position: 'right',
        fontSize: 11,
        color: theme.text,
        offset: [5, 0]
      },
      emphasis: {
        focus: 'adjacency',
        scale: true,
        lineStyle: { width: 4, color: theme.secondary, opacity: 0.9 }
      },
      lineStyle: { color: 'source', curveness: 0.3 },
      animationDurationUpdate: 900,
      animationEasingUpdate: 'quinticInOut'
    }],
    backgroundColor: 'transparent'
  })
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
  const theme = getChartTheme()

  Object.entries(heatmapData.value.heatmapData).forEach(([subject, points]) => {
    subjects.push(subject)
    Object.entries(points).forEach(([kp, stats]) => {
      if (!knowledgePoints.includes(kp)) knowledgePoints.push(kp)
      data.push([subject, kp, stats.correctRate])
    })
  })

  charts.value.heatmap.setOption({
    title: chartTitle('知识点掌握热力图', `总答题数 ${heatmapData.value.totalRecords || 0} · 整体正确率 ${formatPercent(heatmapData.value.overallCorrectRate)}`),
    tooltip: {
      ...chartTooltip((params) => `${params.data[0]} - ${params.data[1]}<br/>正确率：${(params.data[2] * 100).toFixed(1)}%`),
      position: 'top',
    },
    xAxis: {
      type: 'category',
      data: subjects,
      name: '科目',
      ...axisTextStyle(),
      axisLabel: { ...axisTextStyle().axisLabel, rotate: 35, interval: 0 }
    },
    yAxis: {
      type: 'category',
      data: knowledgePoints,
      name: '知识点',
      ...axisTextStyle()
    },
    visualMap: {
      min: 0,
      max: 1,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 10,
      textStyle: { color: theme.muted },
      inRange: { color: ['#f43f5e', '#f59e0b', '#10b981'] },
      formatter: (value) => (value * 100).toFixed(0) + '%'
    },
    series: [{
      name: '正确率',
      type: 'heatmap',
      data: data,
      label: {
        show: true,
        formatter: (params) => (params.data[2] * 100).toFixed(0) + '%',
        fontSize: 10,
        color: isDark.value ? '#fff' : '#251333'
      },
      itemStyle: { borderRadius: 6, borderWidth: 2, borderColor: isDark.value ? '#12091a' : '#fff' },
      emphasis: { itemStyle: { shadowBlur: 16, shadowColor: `rgba(${theme.primaryRgb}, 0.34)` } }
    }],
    grid: { containLabel: true, top: 34, left: 90, right: 38, bottom: 86 },
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
  const theme = getChartTheme()
  const axis = axisTextStyle()

  const trendDirection = trendData.value.trendDirection === 'up' ? '📈 上升' : (trendData.value.trendDirection === 'down' ? '📉 下降' : '➡️ 平稳')
  const trendColor = trendData.value.trendDirection === 'up' ? '#10b981' : (trendData.value.trendDirection === 'down' ? '#f43f5e' : '#f59e0b')

  charts.value.trend.setOption({
    color: [trendColor, theme.secondary],
    title: {
      ...chartTitle('知识掌握趋势', `平均正确率 ${formatPercent(trendData.value.averageCorrectRate)} · 趋势 ${trendDirection}`),
      subtextStyle: { color: trendColor, fontSize: 12, lineHeight: 18 }
    },
    tooltip: chartTooltip((params) => {
      let res = params[0].axisValue + '<br/>'
      res += `正确率：${params[0].value}%<br/>`
      res += `答题数：${totals[params[0].dataIndex]}`
      return res
    }, 'axis'),
    legend: {
      data: ['正确率', '答题数'],
      top: 24,
      right: 24,
      textStyle: { color: theme.muted }
    },
    xAxis: {
      type: 'category',
      data: dates,
      name: '日期',
      ...axis,
      axisLabel: { ...axis.axisLabel, rotate: 35, interval: Math.floor(dates.length / 10) }
    },
    yAxis: [
      { type: 'value', name: '正确率 (%)', min: 0, max: 100, ...axis, axisLabel: { ...axis.axisLabel, formatter: '{value}%' } },
      { type: 'value', name: '答题数', min: 0, ...axis }
    ],
    series: [
      {
        name: '正确率',
        type: 'line',
        data: correctRates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: trendColor },
        itemStyle: { color: trendColor, borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: hexToRgba(trendColor, 0.28) },
            { offset: 1, color: hexToRgba(trendColor, 0.04) }
          ])
        }
      },
      {
        name: '答题数',
        type: 'bar',
        data: totals,
        yAxisIndex: 1,
        barWidth: '30%',
        itemStyle: { color: themedGradient('vertical'), borderRadius: [8, 8, 2, 2] }
      }
    ],
    grid: { containLabel: true, top: 72, left: 58, right: 58, bottom: 42 },
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
  const theme = getChartTheme()

  charts.value.radar.setOption({
    color: [theme.primary, theme.secondary],
    title: chartTitle('知识点掌握雷达图', `共 ${radarData.value.knowledgeCount} 个知识点`),
    tooltip: chartTooltip((params) => `${params.name}<br/>掌握度：${params.value.toFixed(1)}%`),
    radar: {
      indicator: indicators.map(ind => ({ name: ind.length > 15 ? ind.slice(0, 12) + '...' : ind, max: 100 })),
      shape: 'circle',
      center: ['50%', '50%'],
      radius: '58%',
      axisName: { color: theme.muted, fontSize: 11 },
      axisLine: { lineStyle: { color: theme.line } },
      splitLine: { lineStyle: { color: theme.line } },
      splitArea: {
        areaStyle: {
          color: [
            `rgba(${theme.primaryRgb}, 0.11)`,
            `rgba(${theme.secondaryRgb}, 0.05)`
          ]
        }
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: seriesData,
        name: '掌握度',
        areaStyle: { color: `rgba(${theme.primaryRgb}, 0.25)` },
        lineStyle: { width: 3, color: theme.primary },
        itemStyle: { color: theme.secondary, borderColor: '#fff', borderWidth: 2 },
        symbolSize: 7
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

const renderActiveChart = () => {
  if (activeTab.value === 'graph') renderGraph()
  if (activeTab.value === 'heatmap') renderHeatmap()
  if (activeTab.value === 'trend') renderTrend()
  if (activeTab.value === 'radar') renderRadar()
}

const handleThemeColorChange = async () => {
  await nextTick()
  refreshCharts()
}

watch(activeTab, async () => {
  await nextTick()
  renderActiveChart()
  resizeCharts()
})

// 生命周期
onMounted(() => {
  checkTheme()
  getCurrentUser()

  // 监听主题变化
  themeObserver = new MutationObserver(async () => {
    checkTheme()
    await nextTick()
    refreshCharts()
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class', 'style'] })
  window.addEventListener('theme-color-change', handleThemeColorChange)
  window.addEventListener('resize', resizeCharts)

  // 滚动动画
  scrollObserver = new IntersectionObserver((entries) => {
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
  themeObserver?.disconnect()
  scrollObserver?.disconnect()
  window.removeEventListener('theme-color-change', handleThemeColorChange)
  window.removeEventListener('resize', resizeCharts)

  // 销毁所有图表
  Object.values(charts.value).forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<template>
  <div class="knowledge-graph-container" :class="isDark ? 'dark' : 'app-page-bg'">
    <Nav :isDark="isDark" :menuItems="menuItems"/>

    <main class="graph-shell">
      <section class="graph-hero scroll-animate">
        <div class="hero-copy">
          <span class="hero-kicker">Learning Intelligence</span>
          <h1>学习知识图谱</h1>
          <p>把答题记录、掌握趋势和薄弱知识点聚合成一张可追踪的成长地图。</p>
        </div>
        <div class="hero-panel">
          <span class="panel-label">当前视图</span>
          <strong>{{ tabs.find(tab => tab.key === activeTab)?.name }}</strong>
          <div class="panel-meta">
            <span>{{ filters.categoryLevel || '全部层级' }}</span>
            <span>{{ filters.subjectName || '全部科目' }}</span>
          </div>
        </div>
      </section>

      <!-- 筛选栏 -->
      <div class="filter-bar scroll-animate">
        <div class="filter-item">
          <label>分类层级</label>
          <el-select v-model="filters.categoryLevel" placeholder="全部" clearable @change="loadData">
            <el-option label="高中" value="高中" />
            <el-option label="大学" value="大学" />
            <el-option label="考公" value="考公" />
            <el-option label="考研" value="考研" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>科目</label>
          <el-select v-model="filters.subjectName" placeholder="全部" clearable @change="loadData">
            <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="loadData">刷新图谱</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card scroll-animate">
          <div class="stat-icon">📊</div>
          <div class="stat-value">{{ formatNumber(statistics.totalQuestions) }}</div>
          <div class="stat-label">总答题数</div>
        </div>
        <div class="stat-card correct scroll-animate">
          <div class="stat-icon">✅</div>
          <div class="stat-value">{{ formatNumber(statistics.correctCount) }}</div>
          <div class="stat-label">正确数</div>
        </div>
        <div class="stat-card wrong scroll-animate">
          <div class="stat-icon">❌</div>
          <div class="stat-value">{{ formatNumber(statistics.wrongCount) }}</div>
          <div class="stat-label">错误数</div>
        </div>
        <div class="stat-card rate scroll-animate">
          <div class="stat-icon">📈</div>
          <div class="stat-value">{{ formatPercent(statistics.overallCorrectRate) }}</div>
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
    </main>

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
  transition: all 0.3s ease;
}

.knowledge-graph-container.dark {
  background: linear-gradient(135deg, #0a0a0b 0%, #141416 100%);
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
  color: #c026d3;
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
  background: linear-gradient(135deg, #c026d3, #7c3aed);
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
  background: linear-gradient(135deg, #c026d3, #7c3aed);
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
  border-top-color: #c026d3;
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
/* Modern graph dashboard overrides */
.knowledge-graph-container {
  min-height: 100vh;
  padding: 0;
  color: var(--app-text);
  background: var(--app-surface);
}

.knowledge-graph-container.dark {
  background: var(--app-surface);
}

.graph-shell {
  width: min(1320px, calc(100% - 32px));
  margin: 0 auto;
  padding: 104px 0 56px;
}

.graph-hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: stretch;
  margin-bottom: 20px;
}

.hero-copy,
.hero-panel,
.filter-bar,
.stat-card,
.chart-container,
.learning-path {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--app-card-border);
  background: var(--app-card);
  box-shadow: var(--app-card-shadow);
  backdrop-filter: blur(20px);
}

.hero-copy {
  min-height: 180px;
  border-radius: 22px;
  padding: 30px;
}

.hero-copy::after,
.chart-container::after,
.learning-path::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(135deg, rgba(var(--theme-primary-rgb), 0.12), transparent 34%),
    linear-gradient(315deg, rgba(var(--theme-secondary-rgb), 0.1), transparent 38%);
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid rgba(var(--theme-primary-rgb), 0.2);
  color: rgb(var(--color-brand-600));
  background: rgba(var(--theme-primary-rgb), 0.1);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.hero-copy h1 {
  margin: 18px 0 10px;
  color: var(--app-text);
  font-size: clamp(32px, 4vw, 56px);
  font-weight: 900;
  line-height: 1.04;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 680px;
  margin: 0;
  color: var(--app-text-muted);
  font-size: 16px;
  line-height: 1.75;
}

.hero-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-radius: 22px;
  padding: 26px;
  background:
    linear-gradient(135deg, rgba(var(--theme-primary-rgb), 0.16), rgba(var(--theme-secondary-rgb), 0.1)),
    var(--app-card);
}

.panel-label {
  color: var(--app-text-muted);
  font-size: 12px;
  font-weight: 800;
}

.hero-panel strong {
  margin-top: 14px;
  color: var(--app-text);
  font-size: 30px;
  font-weight: 900;
  line-height: 1.12;
}

.panel-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 22px;
}

.panel-meta span {
  padding: 7px 11px;
  border-radius: 999px;
  border: 1px solid rgba(var(--theme-primary-rgb), 0.18);
  color: var(--app-text-secondary);
  background: rgba(255, 255, 255, 0.48);
  font-size: 12px;
  font-weight: 700;
}

.dark .panel-meta span {
  background: rgba(255, 255, 255, 0.06);
}

.filter-bar {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 1fr)) auto;
  gap: 16px;
  align-items: end;
  max-width: none;
  margin: 0 0 18px;
  padding: 18px;
  border-radius: 18px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.filter-item label {
  color: var(--app-text-secondary);
  font-size: 12px;
  font-weight: 800;
}

.filter-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.stats-cards {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  max-width: none;
  margin: 0 0 22px;
}

.stat-card {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr);
  grid-template-areas:
    'icon value'
    'icon label';
  column-gap: 14px;
  align-items: center;
  min-height: 118px;
  border-radius: 18px;
  padding: 20px;
  text-align: left;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: auto 18px 0;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: var(--theme-gradient);
  opacity: 0.8;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--app-card-shadow-hover);
}

.stat-icon {
  grid-area: icon;
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  margin: 0;
  border-radius: 14px;
  color: white;
  background: var(--theme-gradient);
  box-shadow: 0 16px 32px -22px rgba(var(--theme-primary-rgb), 0.9);
  font-size: 21px;
}

.stat-value {
  grid-area: value;
  background: var(--theme-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.stat-label {
  grid-area: label;
  margin: 6px 0 0;
  color: var(--app-text-muted);
  font-size: 13px;
  font-weight: 700;
}

.tabs-container {
  max-width: none;
}

.tabs-bar {
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 16px;
  padding: 8px;
  border: 1px solid var(--app-card-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: var(--app-card-shadow);
  backdrop-filter: blur(16px);
}

.dark .tabs-bar {
  background: rgba(255, 255, 255, 0.06);
}

.tab-btn {
  min-height: 42px;
  border: 1px solid transparent;
  border-radius: 13px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 800;
}

.tab-btn.light,
.tab-btn.dark {
  color: var(--app-text-secondary);
  background: transparent;
}

.tab-btn:hover {
  border-color: rgba(var(--theme-primary-rgb), 0.18);
  color: rgb(var(--color-brand-600));
  background: rgba(var(--theme-primary-rgb), 0.08);
}

.tab-btn.active {
  color: white;
  background: var(--theme-gradient);
  box-shadow: 0 18px 34px -24px rgba(var(--theme-primary-rgb), 0.92);
}

.chart-header {
  position: absolute;
  z-index: 2;
  top: 22px;
  left: 24px;
  text-align: left;
  margin: 0;
}

.chart-header h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 18px;
  font-weight: 900;
}

.chart-desc {
  margin: 6px 0 0;
  color: var(--app-text-muted);
  font-size: 12px;
}

.tab-content {
  position: relative;
}

.chart-container {
  border-radius: 22px;
  padding: 18px;
}

.chart {
  position: relative;
  z-index: 1;
  height: 590px;
}

.learning-path {
  border-radius: 22px;
  padding: 28px;
  background: var(--app-card) !important;
}

.path-header {
  position: relative;
  z-index: 1;
  text-align: left;
}

.path-header h3,
.path-header-row h4 {
  color: var(--app-text);
}

.path-header p,
.path-info,
.progress-label,
.path-stats,
.empty-tip p {
  color: var(--app-text-muted);
}

.path-timeline,
.empty-tip {
  position: relative;
  z-index: 1;
}

.path-item::before {
  content: '';
  position: absolute;
  top: 38px;
  bottom: -28px;
  left: 7px;
  width: 2px;
  background: linear-gradient(180deg, rgba(var(--theme-primary-rgb), 0.32), transparent);
}

.path-item:last-child::before {
  display: none;
}

.path-marker {
  position: relative;
  z-index: 1;
}

.path-content {
  border: 1px solid var(--app-card-border);
  background: rgba(255, 255, 255, 0.72) !important;
  box-shadow: none;
}

.dark .path-content {
  background: rgba(255, 255, 255, 0.06) !important;
}

.progress-bar {
  background: rgba(var(--theme-primary-rgb), 0.1);
}

.loading-overlay {
  background: rgba(12, 6, 20, 0.54);
  backdrop-filter: blur(12px);
}

.loading-spinner {
  border-color: rgba(var(--theme-primary-rgb), 0.18);
  border-top-color: var(--theme-primary);
}

@media (max-width: 1024px) {
  .graph-hero,
  .filter-bar {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    justify-content: flex-start;
  }

  .stats-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .graph-shell {
    width: min(100% - 24px, 1320px);
    padding-top: 88px;
  }

  .hero-copy,
  .hero-panel {
    padding: 22px;
    border-radius: 18px;
  }

  .hero-copy h1 {
    font-size: 34px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
    margin-top: 0;
  }

  .tabs-bar {
    overflow-x: auto;
    flex-wrap: nowrap;
    justify-content: flex-start;
  }

  .tab-btn {
    flex: 0 0 auto;
    padding: 0 14px;
  }

  .chart-header {
    position: static;
    margin: 0 0 12px;
  }

  .chart {
    height: 420px;
  }

  .chart-container,
  .learning-path {
    border-radius: 18px;
    padding: 16px;
  }

  .path-info,
  .path-stats {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
