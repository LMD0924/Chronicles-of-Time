<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Document, Histogram, Notebook, User, UserFilled } from '@element-plus/icons-vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import CountTo from '@/components/charts/CountTo.vue'
import { adminDataApi } from '@/api/adminData'

const loading = ref(false)
const overview = ref({ cards: [], logs: [], todos: [], userTrend: [], contentTrend: [], contentDistribution: [], moduleDistribution: [], accessHeatmap: [] })
const refreshedAt = ref('')
const iconMap = { User, UserFilled, Notebook, Bell, Document, Histogram }

const refresh = async (notify = true) => {
  loading.value = true
  try {
    overview.value = await adminDataApi.dashboard()
    refreshedAt.value = new Date().toLocaleTimeString()
    if (notify) ElMessage.success('真实数据已刷新')
  } finally { loading.value = false }
}

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 40, right: 18, bottom: 28, top: 38 },
  xAxis: { type: 'category', boundaryGap: false, data: overview.value.userTrend.map((item) => item.label) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    { name: '新增用户', type: 'line', smooth: true, data: overview.value.userTrend.map((item) => Number(item.value || 0)), areaStyle: { opacity: 0.12 }, itemStyle: { color: '#2f9e8f' } },
    { name: '新增内容', type: 'line', smooth: true, data: overview.value.contentTrend.map((item) => Number(item.value || 0)), areaStyle: { opacity: 0.08 }, itemStyle: { color: '#d97706' } },
  ],
}))
const barOption = computed(() => ({ tooltip: { trigger: 'axis' }, grid: { left: 42, right: 18, bottom: 28, top: 28 }, xAxis: { type: 'category', data: overview.value.contentDistribution.map((item) => item.name) }, yAxis: { type: 'value', minInterval: 1 }, series: [{ type: 'bar', data: overview.value.contentDistribution.map((item) => Number(item.value || 0)), itemStyle: { color: '#78c6b7', borderRadius: [6, 6, 0, 0] } }] }))
const pieOption = computed(() => ({ tooltip: { trigger: 'item' }, legend: { type: 'scroll', bottom: 0 }, series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '44%'], data: overview.value.moduleDistribution, label: { formatter: '{b}\n{d}%' } }] }))
const heatMax = computed(() => Math.max(1, ...overview.value.accessHeatmap.map((item) => Number(item.value || 0))))
const heatOption = computed(() => ({ tooltip: {}, grid: { left: 52, right: 68, bottom: 28, top: 28 }, xAxis: { type: 'category', data: ['0点', '4点', '8点', '12点', '16点', '20点'] }, yAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] }, visualMap: { min: 0, max: heatMax.value, right: 0, top: 0, calculable: true, inRange: { color: ['#e7f6f2', '#2f9e8f'] } }, series: [{ type: 'heatmap', data: overview.value.accessHeatmap.map((item) => [item.period, item.weekday, item.value]) }] }))

onMounted(() => refresh(false))
</script>

<template>
  <div v-loading="loading" class="cot-page dashboard">
    <section class="cot-page-header">
      <div><h1 class="cot-page-title">首页控制台</h1><p class="cot-page-desc">汇总身份、内容、学习、高中、大学、职场与平台日志数据库的实时数据。</p></div>
      <div class="header-actions"><span class="refresh-time">刷新时间：{{ refreshedAt || '未刷新' }}</span><el-button type="primary" @click="refresh(true)">实时刷新</el-button></div>
    </section>

    <section class="stat-grid">
      <article v-for="card in overview.cards" :key="card.label" class="cot-card stat-card">
        <div class="stat-icon"><el-icon><component :is="iconMap[card.icon]" /></el-icon></div>
        <div class="stat-meta"><span>{{ card.label }}</span><strong><CountTo :value="Number(card.value || 0)" /> <em>{{ card.unit }}</em></strong><small>数据库实时统计</small></div>
      </article>
    </section>

    <section class="chart-grid">
      <div class="cot-card chart-card large"><div class="card-head"><strong>近 7 日新增趋势</strong><el-tag type="success" size="small">实时</el-tag></div><BaseChart :option="lineOption" /></div>
      <div class="cot-card chart-card"><div class="card-head"><strong>业务模块记录占比</strong></div><BaseChart :option="pieOption" /></div>
      <div class="cot-card chart-card"><div class="card-head"><strong>内容与资源规模</strong></div><BaseChart :option="barOption" /></div>
      <div class="cot-card chart-card"><div class="card-head"><strong>近 7 日接口访问时段</strong></div><BaseChart :option="heatOption" /></div>
    </section>

    <section class="info-grid">
      <div class="cot-card panel-list"><div class="card-head"><strong>最新真实日志</strong></div><div v-for="log in overview.logs" :key="`${log.type}-${log.time}-${log.text}`" class="log-item"><el-tag size="small">{{ log.type }}</el-tag><span>{{ log.text }}</span><small>{{ log.time }}</small></div><el-empty v-if="!overview.logs.length" description="暂无日志" :image-size="54" /></div>
      <div class="cot-card panel-list"><div class="card-head"><strong>实时待办</strong></div><div class="todo-list"><div v-for="todo in overview.todos" :key="todo" class="todo-item"><span></span><p>{{ todo }}</p></div></div></div>
      <div class="cot-card quick-actions"><div class="card-head"><strong>快捷操作入口</strong></div><RouterLink to="/system/users">用户管理</RouterLink><RouterLink to="/learning/questions">题目审核</RouterLink><RouterLink to="/content/articles">内容管理</RouterLink><RouterLink to="/planning/selection-approval">选科审批</RouterLink><RouterLink to="/academic/course-tree">课程数据</RouterLink><RouterLink to="/monitor/services">服务监控</RouterLink></div>
    </section>
  </div>
</template>

<style scoped>
.header-actions,.card-head{display:flex;align-items:center;justify-content:space-between;gap:12px}.refresh-time{color:var(--el-text-color-secondary);font-size:13px}.stat-grid{display:grid;grid-template-columns:repeat(6,minmax(0,1fr));gap:16px}.stat-card{display:flex;gap:12px;padding:18px}.stat-icon{display:grid;width:42px;height:42px;place-items:center;border-radius:8px;color:var(--cot-primary);background:var(--cot-primary-soft);font-size:22px}.stat-meta span,.stat-meta small{display:block;color:var(--el-text-color-secondary);font-size:12px}.stat-meta strong{display:block;margin:6px 0;color:var(--el-text-color-primary);font-size:24px}.stat-meta em{font-style:normal;font-size:12px}.chart-grid{display:grid;grid-template-columns:1.3fr .7fr;gap:16px}.chart-card,.panel-list,.quick-actions{padding:18px}.info-grid{display:grid;grid-template-columns:1.2fr .8fr .8fr;gap:16px}.log-item{display:grid;grid-template-columns:auto 1fr auto;gap:10px;align-items:center;padding:12px 0;border-bottom:1px solid var(--el-border-color-lighter);font-size:13px}.log-item small{color:var(--el-text-color-secondary)}.todo-list{display:grid;gap:8px;margin-top:12px}.todo-item{display:flex;align-items:center;gap:9px;border-bottom:1px solid var(--el-border-color-lighter);padding:10px 2px}.todo-item span{width:8px;height:8px;flex:0 0 8px;border-radius:50%;background:var(--cot-primary)}.todo-item p{margin:0;color:var(--el-text-color-regular);font-size:13px}.quick-actions{display:grid;gap:10px}.quick-actions a{padding:12px;border-radius:8px;color:var(--cot-primary);text-decoration:none;background:var(--cot-primary-soft);font-weight:700}@media(max-width:1280px){.stat-grid{grid-template-columns:repeat(3,1fr)}.chart-grid,.info-grid{grid-template-columns:1fr}}@media(max-width:640px){.stat-grid{grid-template-columns:1fr}.header-actions{align-items:stretch;flex-direction:column}.log-item{grid-template-columns:auto 1fr}.log-item small{grid-column:2}}
</style>