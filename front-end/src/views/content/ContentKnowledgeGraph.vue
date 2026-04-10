<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { message } from 'ant-design-vue'
import Nav from '@/components/Nav.vue';

const router = useRouter()
const isDark = ref(false)

// 菜单配置
const menuItems = [
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' },
  { key: 'publish', label: '分享小札', icon: '✍️', path: '/Publish' },
  { key: 'knowledge', label: '知识图谱', icon: '🔗', path: '/ContentKnowledgeGraph' },
]

// 处理菜单点击
const handleMenuClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

// 处理 Logo 点击
const handleLogoClick = () => {
  router.push('/home')
}

const checkTheme = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

const observer = new MutationObserver(() => {
  checkTheme()
})

const loading = ref(false)
const activeTab = ref('graph')
const statistics = ref({})
const graphData = ref(null)
const tagCloudData = ref([])
const cooccurrenceData = ref(null)
const categoryData = ref([])
const topicsData = ref({})

let graphChart = null
let tagBarChart = null
let cooccurrenceChart = null
let categoryChart = null
let pieChart = null

const tabs = [
  { key: 'graph', name: '标签分类图谱', icon: '🔗' },
  { key: 'cloud', name: '标签排行', icon: '📊' },
  { key: 'cooccurrence', name: '标签共现网络', icon: '🕸️' },
  { key: 'category', name: '分类统计', icon: '📁' },
  { key: 'topics', name: '主题分布', icon: '🎯' }
]

const goBack = () => {
  router.push('/')
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadGraphData(),
      loadTagCloud(),
      loadCooccurrence(),
      loadCategoryStats(),
      loadTopicsData()
    ])
  } catch (error) {
    console.error('加载数据失败', error)
    message.error('加载知识图谱失败')
  } finally {
    loading.value = false
  }
}

const loadGraphData = async () => {
  try {
    const res = await request.get('/content/knowledge-graph', { limit: 50 })
    if (res.code === 200) {
      graphData.value = res.data
      statistics.value = res.data.statistics || {}
      await nextTick()
      renderGraph()
    }
  } catch (error) {
    console.error('加载图谱数据失败', error)
  }
}

const loadTagCloud = async () => {
  try {
    const res = await request.get('/content/tag-cloud', { limit: 50 })
    if (res.code === 200) {
      tagCloudData.value = res.data
      await nextTick()
      renderTagBarChart()
    }
  } catch (error) {
    console.error('加载标签数据失败', error)
  }
}

const loadCooccurrence = async () => {
  try {
    const res = await request.get('/content/tag-cooccurrence', { limit: 40 })
    if (res.code === 200) {
      cooccurrenceData.value = res.data
      await nextTick()
      renderCooccurrence()
    }
  } catch (error) {
    console.error('加载共现网络失败', error)
  }
}

const loadCategoryStats = async () => {
  try {
    const res = await request.get('/content/category-stats')
    if (res.code === 200) {
      categoryData.value = res.data
      await nextTick()
      renderCategoryChart()
    }
  } catch (error) {
    console.error('加载分类统计失败', error)
  }
}

const loadTopicsData = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
    const userId = userInfo.id
    if (userId) {
      const res = await request.get(`/content/user-topics/${userId}`)
      if (res.code === 200) {
        topicsData.value = res.data
        await nextTick()
        renderPieChart()
      }
    }
  } catch (error) {
    console.error('加载主题分布失败', error)
  }
}

// 渲染知识图谱
const renderGraph = () => {
  if (!graphData.value) return

  const chartDom = document.getElementById('graphChart')
  if (!chartDom) return

  if (graphChart) graphChart.dispose()
  graphChart = echarts.init(chartDom)

  const nodes = (graphData.value.nodes || []).map(node => ({
    id: node.id,
    name: node.name,
    symbolSize: Math.min(50, 15 + (node.count || 5) / 2),
    category: node.type,
    count: node.count,
    itemStyle: {
      color: node.type === 'tag' ? '#ee6666' : '#5470c6'
    },
    label: {
      show: true,
      formatter: node.name,
      fontSize: 11,
      position: 'right'
    }
  }))

  const edges = (graphData.value.edges || []).map(edge => ({
    source: edge.source,
    target: edge.target,
    lineStyle: {
      width: Math.min(4, (edge.weight || 1) / 3),
      curveness: 0.2,
      color: '#999'
    }
  }))

  const categories = [
    { name: '标签', itemStyle: { color: '#ee6666' } },
    { name: '分类', itemStyle: { color: '#5470c6' } }
  ]

  graphChart.setOption({
    title: {
      text: '标签分类关系图谱',
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `${params.data.name}<br/>使用次数: ${params.data.count || 0}次`
        }
        return `${params.data.source} → ${params.data.target}<br/>关联强度: ${params.data.lineStyle.width}`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: {
        repulsion: 500,
        edgeLength: [80, 150],
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
        fontSize: 11,
        offset: [5, 0]
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3, color: '#2c3e50' }
      }
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => graphChart?.resize())
}

// 渲染标签柱状图（替代标签云）
const renderTagBarChart = () => {
  if (!tagCloudData.value.length) return

  const chartDom = document.getElementById('tagBarChart')
  if (!chartDom) return

  if (tagBarChart) tagBarChart.dispose()
  tagBarChart = echarts.init(chartDom)

  // 按使用次数排序，取前20个
  const sortedData = [...tagCloudData.value].sort((a, b) => b.value - a.value).slice(0, 20)
  const names = sortedData.map(item => item.name)
  const values = sortedData.map(item => item.value)

  tagBarChart.setOption({
    title: {
      text: '标签使用排行 TOP 20',
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        return `${params[0].name}<br/>使用次数: ${params[0].value}次`
      }
    },
    grid: {
      containLabel: true,
      left: '12%',
      right: '5%',
      bottom: '5%',
      top: '15%'
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: 45,
        interval: 0,
        fontSize: 11,
        color: isDark.value ? '#9ca3af' : '#6b7280'
      }
    },
    yAxis: {
      type: 'value',
      name: '使用次数',
      nameTextStyle: { color: isDark.value ? '#9ca3af' : '#6b7280' }
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4f46e5' },
          { offset: 1, color: '#7c3aed' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}',
        fontSize: 11
      }
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => tagBarChart?.resize())
}

// 渲染共现网络
const renderCooccurrence = () => {
  if (!cooccurrenceData.value) return

  const chartDom = document.getElementById('cooccurrenceChart')
  if (!chartDom) return

  if (cooccurrenceChart) cooccurrenceChart.dispose()
  cooccurrenceChart = echarts.init(chartDom)

  const nodes = (cooccurrenceData.value.nodes || []).map(node => ({
    id: node.id,
    name: node.name,
    symbolSize: Math.min(40, 10 + (node.count || 5) / 2),
    itemStyle: { color: '#ee6666' },
    label: { show: true, formatter: node.name, fontSize: 11 }
  }))

  const edges = (cooccurrenceData.value.edges || []).map(edge => ({
    source: edge.source,
    target: edge.target,
    lineStyle: { width: Math.min(5, (edge.weight || 1) / 2), curveness: 0.2 }
  }))

  cooccurrenceChart.setOption({
    title: {
      text: '标签共现网络',
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' },
      subtext: `共 ${cooccurrenceData.value.statistics?.totalTags || 0} 个标签，${cooccurrenceData.value.statistics?.totalRelations || 0} 个关系`
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `${params.data.name}<br/>使用次数: ${params.data.symbolSize || 0}次`
        }
        return `${params.data.source} ↔ ${params.data.target}<br/>共现次数: ${params.data.lineStyle.width * 2}`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      force: {
        repulsion: 800,
        edgeLength: 120,
        gravity: 0.1
      },
      data: nodes,
      links: edges,
      roam: true,
      draggable: true,
      label: { show: true, position: 'right', fontSize: 11 },
      lineStyle: { color: '#999', curveness: 0.3 },
      emphasis: { focus: 'adjacency' }
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => cooccurrenceChart?.resize())
}

// 渲染分类统计柱状图
const renderCategoryChart = () => {
  if (!categoryData.value.length) return

  const chartDom = document.getElementById('categoryChart')
  if (!chartDom) return

  if (categoryChart) categoryChart.dispose()
  categoryChart = echarts.init(chartDom)

  const names = categoryData.value.map(c => c.name)
  const values = categoryData.value.map(c => c.value)

  categoryChart.setOption({
    title: {
      text: '分类文章统计',
      left: 'center',
      textStyle: { color: isDark.value ? '#fff' : '#333' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0]
        const percentage = categoryData.value[data.dataIndex]?.percentage || 0
        return `${data.name}<br/>文章数: ${data.value}<br/>占比: ${percentage.toFixed(1)}%`
      }
    },
    grid: {
      containLabel: true,
      left: '10%',
      right: '5%',
      bottom: '5%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 45, interval: 0, fontSize: 11 },
      name: '分类'
    },
    yAxis: {
      type: 'value',
      name: '文章数'
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: '#5470c6',
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}'
      }
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => categoryChart?.resize())
}

// 渲染饼图
const renderPieChart = () => {
  if (!topicsData.value.topCategories || !topicsData.value.topCategories.length) return

  const chartDom = document.getElementById('categoriesPieChart')
  if (!chartDom) return

  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(chartDom)

  const data = topicsData.value.topCategories.map(item => ({
    name: item.name,
    value: item.value
  }))

  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}% ({c}篇)'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: isDark.value ? '#1a1a1a' : '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%',
        fontSize: 11
      },
      emphasis: {
        scale: true,
        label: { show: true, fontWeight: 'bold' }
      },
      data: data
    }],
    backgroundColor: 'transparent'
  })

  window.addEventListener('resize', () => pieChart?.resize())
}

// 监听主题变化
watch(isDark, async () => {
  await nextTick()
  renderGraph()
  renderTagBarChart()
  renderCooccurrence()
  renderCategoryChart()
  renderPieChart()
})

// 监听tab切换
watch(activeTab, async (newTab) => {
  await nextTick()
  if (newTab === 'graph') renderGraph()
  else if (newTab === 'cloud') renderTagBarChart()
  else if (newTab === 'cooccurrence') renderCooccurrence()
  else if (newTab === 'category') renderCategoryChart()
  else if (newTab === 'topics') renderPieChart()
})

onMounted(() => {
  checkTheme()
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
  loadData()

  const observerScroll = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
      }
    })
  }, { threshold: 0.2 })
  document.querySelectorAll('.scroll-animate').forEach(el => observerScroll.observe(el))
})

onUnmounted(() => {
  observer.disconnect()
  ;[graphChart, tagBarChart, cooccurrenceChart, categoryChart, pieChart].forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<template>
  <div class="knowledge-graph-container" :class="isDark ? 'dark' : ''">
    <Nav
      :isDark="isDark"
      logoIcon="📚"
      logoText="拾光记"
      logoSubtext="知识图谱"
      :menuItems="menuItems"
      :showBackHome="true"
      logoPath="/home"
      @menuClick="handleMenuClick"
      @logoClick="handleLogoClick"
    />

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

      <!-- 标签分类图谱 -->
      <div v-show="activeTab === 'graph'" class="tab-content">
        <div class="chart-header">
          <h3>📊 标签-分类关系图谱</h3>
          <p class="chart-desc">节点大小代表使用频率，连线表示关联强度</p>
        </div>
        <div class="chart-container">
          <div id="graphChart" class="chart"></div>
        </div>
      </div>

      <!-- 标签排行（替代标签云） -->
      <div v-show="activeTab === 'cloud'" class="tab-content">
        <div class="chart-header">
          <h3>🏷️ 标签使用排行</h3>
          <p class="chart-desc">各标签使用频率统计（按使用次数排序）</p>
        </div>
        <div class="chart-container">
          <div id="tagBarChart" class="chart"></div>
        </div>
      </div>

      <!-- 标签共现网络 -->
      <div v-show="activeTab === 'cooccurrence'" class="tab-content">
        <div class="chart-header">
          <h3>🔗 标签共现网络</h3>
          <p class="chart-desc">标签之间的关联关系，线条越粗关联越强</p>
        </div>
        <div class="chart-container">
          <div id="cooccurrenceChart" class="chart"></div>
        </div>
      </div>

      <!-- 分类统计 -->
      <div v-show="activeTab === 'category'" class="tab-content">
        <div class="chart-header">
          <h3>📊 分类文章统计</h3>
          <p class="chart-desc">各分类下的文章数量分布</p>
        </div>
        <div class="chart-container">
          <div id="categoryChart" class="chart"></div>
        </div>
      </div>

      <!-- 用户主题分布 -->
      <div v-show="activeTab === 'topics'" class="tab-content">
        <div class="chart-header">
          <h3>🎯 我的内容主题分布</h3>
          <p class="chart-desc">基于文章标签的主题分析</p>
        </div>
        <div class="topics-container" :class="isDark ? 'bg-gray-900/30' : 'bg-gray-50'">
          <div v-if="topicsData.topTags && topicsData.topTags.length > 0" class="topics-section">
            <h4>🏷️ 热门标签 TOP 10</h4>
            <div class="topics-list">
              <div v-for="(tag, idx) in topicsData.topTags" :key="idx" class="topic-item">
                <span class="topic-rank">{{ idx + 1 }}</span>
                <span class="topic-name">{{ tag.name }}</span>
                <span class="topic-count">{{ tag.value }}次</span>
                <div class="topic-bar">
                  <div class="topic-bar-fill" :style="{ width: (tag.value / (topicsData.topTags[0]?.value || 1) * 100) + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="topicsData.topCategories && topicsData.topCategories.length > 0" class="topics-section">
            <h4>📁 热门分类</h4>
            <div class="categories-pie">
              <div id="categoriesPieChart" class="pie-chart"></div>
            </div>
          </div>
          <div v-if="!topicsData.topTags?.length && !topicsData.topCategories?.length" class="empty-tip">
            <span class="empty-icon">📝</span>
            <p>暂无数据，快去发布文章吧~</p>
          </div>
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
  padding-top: 80px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  transition: all 0.3s ease;
}

.knowledge-graph-container.dark {
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a1a 100%);
}



.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto 30px auto;
}

.stat-card {
  border-radius: 20px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
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
  background: #f3f4f6;
  color: #4b5563;
}

.tab-btn.dark {
  background: #1f2937;
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

.chart {
  width: 100%;
  height: 550px;
}

.topics-container {
  border-radius: 24px;
  padding: 30px;
  min-height: 500px;
}

.topics-section {
  margin-bottom: 40px;
}

.topics-section h4 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #374151;
}

.dark .topics-section h4 {
  color: #e5e7eb;
}

.topics-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.topic-rank {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.topic-name {
  width: 120px;
  font-weight: 500;
  color: #374151;
}

.dark .topic-name {
  color: #d1d5db;
}

.topic-count {
  width: 60px;
  font-size: 13px;
  color: #6b7280;
}

.dark .topic-count {
  color: #9ca3af;
}

.topic-bar {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.dark .topic-bar {
  background: #374151;
}

.topic-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5, #7c3aed);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.categories-pie {
  display: flex;
  justify-content: center;
}

.pie-chart {
  width: 400px;
  height: 350px;
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

  .tab-btn {
    padding: 8px 16px;
    font-size: 12px;
  }

  .chart {
    height: 400px;
  }

  .pie-chart {
    width: 280px;
    height: 280px;
  }
}
</style>
