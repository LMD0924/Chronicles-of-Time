<!--
  文件说明：拾光记后台管理系统通用组件页面组件，承载通用组件场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import { crudApi, makeRows } from '@/api/mock'

// 通用企业 CRUD 组件通过 module 配置驱动页面标题、分类、权限点和模拟数据。
const props = defineProps({
  module: { type: Object, required: true },
})

const loading = ref(false)
const selectedRows = ref([])
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const query = reactive({ keyword: '', status: '全部', page: 1, pageSize: 10 })
const formRef = ref(null)
const form = reactive({ id: '', title: '', name: '', category: '', status: '正常', remark: '' })

const title = computed(() => props.module.title)
const allRows = computed(() => makeRows(props.module))
const statusList = ['正常', '待审核', '已下架', '禁用']

const statusCount = computed(() => statusList.map((status) => ({ status, value: allRows.value.filter((item) => item.status === status).length })))
const categoryCount = computed(() => props.module.categories.map((category) => ({ name: category, value: allRows.value.filter((item) => item.category === category).length })))
const totalViews = computed(() => allRows.value.reduce((sum, item) => sum + item.views, 0))
const pendingCount = computed(() => allRows.value.filter((item) => item.status === '待审核').length)
const riskCount = computed(() => allRows.value.filter((item) => ['待审核', '已下架', '禁用'].includes(item.status)).length)
const activeRate = computed(() => Math.round((statusCount.value.find((item) => item.status === '正常')?.value || 0) / allRows.value.length * 100))



// 顶部指标卡把列表数据转成运营视角，解决后台页面只有表格、不够直观的问题。
const insightCards = computed(() => [
  { label: '总数据量', value: allRows.value.length, suffix: '条', tone: 'primary', desc: '当前模块全部记录' },
  { label: '待处理', value: pendingCount.value, suffix: '条', tone: 'warning', desc: '需要审核或跟进' },
  { label: '健康率', value: activeRate.value, suffix: '%', tone: 'success', desc: '正常记录占比' },
  { label: '累计访问', value: totalViews.value, suffix: '次', tone: 'info', desc: '模拟业务热度' },
])



// 图表数据由同一份模块数据派生，真实接入时只需要替换 crudApi.list 或新增统计接口。
const trendOption = computed(() => {
  const base = props.module.key.length * 13
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 32, right: 18, top: 24, bottom: 28 },
    xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: title.value,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        symbolSize: 7,
        itemStyle: { color: 'var(--cot-primary)' },
        lineStyle: { color: 'var(--cot-primary)', width: 3 },
        data: Array.from({ length: 7 }, (_, index) => 18 + ((base + index * 11) % 52)),
      },
    ],
  }
})

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { type: 'scroll', bottom: 0 },
  series: [
    {
      name: '分类分布',
      type: 'pie',
      radius: ['44%', '70%'],
      center: ['50%', '44%'],
      data: categoryCount.value,
      label: { formatter: '{b}\n{d}%' },
    },
  ],
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 52, right: 18, top: 24, bottom: 28 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: statusCount.value.map((item) => item.status) },
  series: [
    {
      type: 'bar',
      barWidth: 14,
      data: statusCount.value.map((item) => item.value),
      itemStyle: { color: 'var(--cot-primary)', borderRadius: [0, 8, 8, 0] },
    },
  ],
}))

const focusItems = computed(() => [
  { label: `复核 ${pendingCount.value} 条待审核记录`, type: 'warning' },
  { label: `清理 ${riskCount.value} 条异常/下架/禁用记录`, type: 'danger' },
  { label: `维护 ${props.module.categories.length} 个业务分类口径`, type: 'primary' },
])

const rules = {
  title: [{ required: true, message: '请输入标题 / 名称', trigger: 'blur' }],
  name: [{ required: true, message: '请输入负责人 / 用户', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}



// 所有列表查询统一通过 query 对象驱动，分页、筛选和重置保持同一套状态。
const fetchData = async () => {
  loading.value = true
  const data = await crudApi.list(props.module, query)
  rows.value = data.list
  total.value = data.total
  loading.value = false
}

const resetQuery = () => {
  query.keyword = ''
  query.status = '全部'
  query.page = 1
  fetchData()
}

const openCreate = () => {
  dialogMode.value = 'create'
  Object.assign(form, { id: '', title: '', name: '', category: props.module.categories[0], status: '正常', remark: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}



// 弹窗提交前先走 Element Plus 表单校验，再触发新增或编辑后的列表刷新。
const submit = async () => {
  await formRef.value.validate()
  ElMessage.success(dialogMode.value === 'create' ? '新增成功' : '编辑成功')
  dialogVisible.value = false
  fetchData()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除 ${row.title}？`, '删除确认', { type: 'warning' })
  ElMessage.success('删除成功')
  fetchData()
}

const batchRemove = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择数据')
    return
  }
  await ElMessageBox.confirm(`确认批量删除 ${selectedRows.value.length} 条数据？`, '批量删除', { type: 'warning' })
  ElMessage.success('批量删除成功')
  selectedRows.value = []
  fetchData()
}

const exportData = () => {
  ElMessage.success(`${title.value} 导出任务已创建`)
}

const updateStatus = (row, status) => {
  row.status = status
  ElMessage.success(`已${status}`)
}

watch(() => props.module.key, resetQuery, { immediate: true })
</script>

<template>
  <div class="cot-page enterprise-page">
    <section class="module-hero">
      <div>
        <span class="module-kicker">全域后台 · 图表洞察</span>
        <h1 class="cot-page-title">{{ title }}</h1>
        <p class="cot-page-desc">{{ module.description }}</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Download" @click="exportData">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
      </div>
    </section>

    <section class="insight-grid">
      <article v-for="card in insightCards" :key="card.label" class="cot-card insight-card" :class="`tone-${card.tone}`">
        <span>{{ card.label }}</span>
        <strong>{{ card.value.toLocaleString() }}<em>{{ card.suffix }}</em></strong>
        <small>{{ card.desc }}</small>
      </article>
    </section>

    <section class="visual-grid">
      <div class="cot-card chart-card large">
        <div class="chart-head"><strong>近 7 日业务趋势</strong><el-tag size="small" type="success">实时统计</el-tag></div>
        <BaseChart :option="trendOption" height="280px" />
      </div>
      <div class="cot-card chart-card">
        <div class="chart-head"><strong>分类分布</strong><el-tag size="small">{{ module.categories.length }} 类</el-tag></div>
        <BaseChart :option="pieOption" height="280px" />
      </div>
      <div class="cot-card chart-card">
        <div class="chart-head"><strong>状态结构</strong><el-tag size="small" type="warning">{{ riskCount }} 风险</el-tag></div>
        <BaseChart :option="statusOption" height="240px" />
      </div>
      <div class="cot-card focus-card">
        <div class="chart-head"><strong>重点待办</strong><el-tag size="small" type="danger">{{ focusItems.length }}</el-tag></div>
        <div class="focus-list">
          <div v-for="item in focusItems" :key="item.label" class="focus-item">
            <el-tag :type="item.type" effect="light">待办</el-tag>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="cot-card cot-toolbar">
      <el-input v-model="query.keyword" clearable placeholder="关键词检索：ID / 标题 / 用户 / 分类" :prefix-icon="Search" @keyup.enter="fetchData" />
      <el-select v-model="query.status" placeholder="状态">
        <el-option v-for="item in ['全部', '正常', '待审核', '已下架', '禁用']" :key="item" :label="item" :value="item" />
      </el-select>
      <el-date-picker type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
    </section>

    <section class="cot-card cot-table-card">
      <div class="table-operator">
        <el-alert :title="`已选择 ${selectedRows.length} 条。上方图表用于看趋势、结构和风险，下方列表用于精细化操作。`" type="info" show-icon :closable="false" />
        <el-button type="danger" :icon="Delete" @click="batchRemove">批量删除</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" row-key="id" stripe @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="编号" min-width="110" />
        <el-table-column prop="title" label="标题 / 名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="name" label="用户 / 负责人" min-width="120" />
        <el-table-column prop="category" label="分类" min-width="120" />
        <el-table-column prop="views" label="访问量" min-width="100" sortable />
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : row.status === '待审核' ? 'warning' : row.status === '已下架' ? 'danger' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" v-permission="module.auditPermission" @click="updateStatus(row, '正常')">审核</el-button>
            <el-button link type="warning" @click="updateStatus(row, '已下架')">下架</el-button>
            <el-button link type="danger" v-permission="module.deletePermission" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" background layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? `新增${title}` : `编辑${title}`" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="标题 / 名称" prop="title"><el-input v-model="form.title" maxlength="60" show-word-limit /></el-form-item>
        <el-form-item label="用户 / 负责人" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类" prop="category"><el-select v-model="form.category" class="w-full"><el-option v-for="item in module.categories" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio-button label="正常" /><el-radio-button label="待审核" /><el-radio-button label="已下架" /><el-radio-button label="禁用" /></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="4" maxlength="200" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.enterprise-page {
  gap: 18px;
}

.module-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border-radius: 14px;
  color: white;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--cot-primary) 92%, #111827), color-mix(in srgb, var(--cot-primary) 48%, #0f172a)),
    radial-gradient(circle at 88% 16%, rgba(255, 255, 255, 0.24), transparent 26%);
  box-shadow: var(--cot-shadow);
}

.module-hero .cot-page-title,
.module-hero .cot-page-desc {
  color: white;
}

.module-hero .cot-page-desc {
  opacity: 0.78;
}

.module-kicker {
  display: inline-flex;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 800;
  opacity: 0.78;
}

.page-actions,
.table-operator,
.chart-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-head {
  justify-content: space-between;
  margin-bottom: 10px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.insight-card {
  position: relative;
  overflow: hidden;
  padding: 18px;
}

.insight-card::after {
  position: absolute;
  right: -18px;
  top: -24px;
  width: 76px;
  height: 76px;
  content: '';
  border-radius: 999px;
  background: var(--cot-primary-soft);
}

.insight-card span,
.insight-card small {
  display: block;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.insight-card strong {
  display: block;
  margin: 8px 0;
  color: var(--el-text-color-primary);
  font-size: 28px;
  line-height: 1;
}

.insight-card em {
  margin-left: 4px;
  font-size: 12px;
  font-style: normal;
}

.visual-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: 14px;
}

.chart-card,
.focus-card {
  padding: 18px;
}

.focus-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.focus-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-extra-light);
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.table-operator {
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}

.w-full {
  width: 100%;
}

@media (max-width: 1280px) {
  .insight-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .visual-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .module-hero,
  .table-operator {
    align-items: stretch;
    flex-direction: column;
  }

  .insight-grid {
    grid-template-columns: 1fr;
  }
}
</style>
