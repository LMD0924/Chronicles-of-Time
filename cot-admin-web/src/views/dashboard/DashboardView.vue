<!--
  文件说明：拾光记后台管理系统数据驾驶舱页面组件，承载数据驾驶舱场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Document, Histogram, Notebook, User, UserFilled } from '@element-plus/icons-vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import CountTo from '@/components/charts/CountTo.vue'
import { dashboardApi } from '@/api/mock'

const range = ref('day')
const overview = ref({ cards: [], logs: [], todos: [] })
const refreshedAt = ref('')
const iconMap = { User, UserFilled, Notebook, Bell, Document, Histogram }

const refresh = async () => {
  overview.value = await dashboardApi.overview()
  refreshedAt.value = new Date().toLocaleTimeString()
  ElMessage.success('数据已刷新')
}

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 34, right: 18, bottom: 28, top: 28 },
  xAxis: { type: 'category', boundaryGap: false, data: range.value === 'day' ? ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00'] : range.value === 'week' ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] : ['1月', '2月', '3月', '4月', '5月', '6月'] },
  yAxis: { type: 'value' },
  series: [{ name: '用户增长', type: 'line', smooth: true, areaStyle: {}, data: range.value === 'day' ? [42, 68, 86, 124, 156, 186] : range.value === 'week' ? [420, 510, 640, 720, 860, 930, 1020] : [2120, 2860, 3420, 4380, 5260, 6420], itemStyle: { color: '#2f9e8f' } }],
}))

const barOption = { tooltip: {}, grid: { left: 34, right: 18, bottom: 28, top: 28 }, xAxis: { type: 'category', data: ['笔记', '动态', '相册', '时光笺', '论文', '文件'] }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: [820, 520, 360, 240, 120, 460], itemStyle: { color: '#78c6b7', borderRadius: [8, 8, 0, 0] } }] }
const pieOption = { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '44%'], data: [{ name: '学习中心', value: 22 }, { name: '升学规划', value: 20 }, { name: '内容图谱', value: 18 }, { name: '大学学业', value: 22 }, { name: '人生阶段', value: 18 }] }] }
const heatOption = { tooltip: {}, grid: { left: 44, right: 18, bottom: 28, top: 28 }, xAxis: { type: 'category', data: ['0点', '4点', '8点', '12点', '16点', '20点'] }, yAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五'] }, visualMap: { min: 0, max: 100, right: 0, top: 0, calculable: true, inRange: { color: ['#e7f6f2', '#2f9e8f'] } }, series: [{ type: 'heatmap', data: Array.from({ length: 30 }, (_, i) => [i % 6, Math.floor(i / 6), (i * 17) % 100]) }] }

onMounted(refresh)
</script>

<template>
  <div class="cot-page dashboard">
    <section class="cot-page-header">
      <div>
        <h1 class="cot-page-title">首页控制台</h1>
        <p class="cot-page-desc">拾光记后台全域总览，目标是弥补当时迷茫的自己，覆盖题库、错题、答题、成绩、知识图谱、内容图谱、选科志愿、大学课程和系统状态。</p>
      </div>
      <div class="header-actions">
        <span class="refresh-time">刷新时间：{{ refreshedAt || '未刷新' }}</span>
        <el-button type="primary" @click="refresh">实时刷新</el-button>
      </div>
    </section>

    <section class="stat-grid">
      <article v-for="card in overview.cards" :key="card.label" class="cot-card stat-card">
        <div class="stat-icon"><el-icon><component :is="iconMap[card.icon]" /></el-icon></div>
        <div class="stat-meta">
          <span>{{ card.label }}</span>
          <strong><CountTo :value="card.value" /> <em>{{ card.unit }}</em></strong>
          <small>{{ card.trend }} 较昨日</small>
        </div>
      </article>
    </section>

    <section class="chart-grid">
      <div class="cot-card chart-card large">
        <div class="card-head"><strong>用户增长趋势</strong><el-radio-group v-model="range" size="small"><el-radio-button label="day">日</el-radio-button><el-radio-button label="week">周</el-radio-button><el-radio-button label="month">月</el-radio-button></el-radio-group></div>
        <BaseChart :option="lineOption" />
      </div>
      <div class="cot-card chart-card"><div class="card-head"><strong>功能模块占比</strong></div><BaseChart :option="pieOption" /></div>
      <div class="cot-card chart-card"><div class="card-head"><strong>内容发布量</strong></div><BaseChart :option="barOption" /></div>
      <div class="cot-card chart-card"><div class="card-head"><strong>访问热力图</strong></div><BaseChart :option="heatOption" /></div>
    </section>

    <section class="info-grid">
      <div class="cot-card panel-list"><div class="card-head"><strong>最新动态日志</strong></div><div v-for="log in overview.logs" :key="log.text" class="log-item"><el-tag size="small">{{ log.type }}</el-tag><span>{{ log.text }}</span><small>{{ log.time }}</small></div></div>
      <div class="cot-card panel-list"><div class="card-head"><strong>待办事项</strong></div><el-checkbox-group><el-checkbox v-for="todo in overview.todos" :key="todo" :label="todo" /></el-checkbox-group></div>
      <div class="cot-card quick-actions"><div class="card-head"><strong>快捷操作入口</strong></div><RouterLink to="/learning/questions">题目管理</RouterLink><RouterLink to="/learning/knowledge-graph">知识图谱</RouterLink><RouterLink to="/content/content-graph">内容图谱</RouterLink><RouterLink to="/planning/volunteer">志愿方案</RouterLink><RouterLink to="/academic/course-tree">课程树</RouterLink><RouterLink to="/monitor/services">服务监控</RouterLink></div>
    </section>
  </div>
</template>

<style scoped>
.header-actions,
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.refresh-time {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}
.stat-card {
  display: flex;
  gap: 12px;
  padding: 18px;
}
.stat-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 12px;
  color: var(--cot-primary);
  background: var(--cot-primary-soft);
  font-size: 22px;
}
.stat-meta span,
.stat-meta small {
  display: block;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.stat-meta strong {
  display: block;
  margin: 6px 0;
  color: var(--el-text-color-primary);
  font-size: 24px;
}
.stat-meta em {
  font-style: normal;
  font-size: 12px;
}
.chart-grid {
  display: grid;
  grid-template-columns: 1.3fr 0.7fr;
  gap: 16px;
}
.chart-card {
  padding: 18px;
}
.info-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr 0.8fr;
  gap: 16px;
}
.panel-list,
.quick-actions {
  padding: 18px;
}
.log-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
}
.log-item small {
  color: var(--el-text-color-secondary);
}
.quick-actions {
  display: grid;
  gap: 10px;
}
.quick-actions a {
  padding: 12px;
  border-radius: 8px;
  color: var(--cot-primary);
  text-decoration: none;
  background: var(--cot-primary-soft);
  font-weight: 700;
}
@media (max-width: 1280px) {
  .stat-grid { grid-template-columns: repeat(3, 1fr); }
  .chart-grid,
  .info-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .stat-grid { grid-template-columns: 1fr; }
  .header-actions { align-items: stretch; flex-direction: column; }
}
</style>


