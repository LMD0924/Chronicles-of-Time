<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import { adminDataApi } from '@/api/adminData'

const props = defineProps({ module: { type: Object, required: true } })
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const stats = ref({ total: 0, metricTotal: 0, categories: [], statuses: [], trend: [] })
const capabilities = ref({ view: false, create: false, update: false, delete: false, fields: [] })
const query = reactive({ keyword: '', status: '全部', page: 1, pageSize: 10 })
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const saving = ref(false)
const dialogMode = ref('create')
const editingId = ref('')
const form = ref({})

const title = computed(() => props.module.title)
const statusOptions = computed(() => ['全部', ...stats.value.statuses.map((item) => String(item.name))])
const formFields = computed(() => capabilities.value.fields || [])
const hasActions = computed(() => capabilities.value.view || capabilities.value.update || capabilities.value.delete)
const dialogTitle = computed(() => ({ create: `新增${title.value}`, edit: `编辑${title.value}`, view: `查看${title.value}` }[dialogMode.value]))
const readonlyDialog = computed(() => dialogMode.value === 'view')
const attentionStatuses = computed(() => stats.value.statuses.filter((item) => /待|异常|离线|禁用|未|风险|错误|薄弱/.test(String(item.name))))
const attentionCount = computed(() => attentionStatuses.value.reduce((sum, item) => sum + Number(item.value || 0), 0))
const insightCards = computed(() => [
  { label: '数据总量', value: Number(stats.value.total || total.value), suffix: '条', desc: '数据库实时记录' },
  { label: '当前页', value: rows.value.length, suffix: '条', desc: `第 ${query.page} 页` },
  { label: '需关注', value: attentionCount.value, suffix: '条', desc: attentionStatuses.value.length ? attentionStatuses.value.map((item) => item.name).join('、') : '当前状态正常' },
  { label: '指标合计', value: Number(stats.value.metricTotal || 0), suffix: '', desc: '当前模块业务指标汇总' },
])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 38, right: 18, top: 24, bottom: 28 },
  xAxis: { type: 'category', boundaryGap: false, data: stats.value.trend.map((item) => item.label) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{ name: title.value, type: 'line', smooth: true, areaStyle: { opacity: 0.16 }, symbolSize: 7, data: stats.value.trend.map((item) => Number(item.value || 0)), itemStyle: { color: 'var(--cot-primary)' }, lineStyle: { color: 'var(--cot-primary)', width: 3 } }],
}))
const pieOption = computed(() => ({ tooltip: { trigger: 'item' }, legend: { type: 'scroll', bottom: 0 }, series: [{ name: '分类分布', type: 'pie', radius: ['44%', '70%'], center: ['50%', '44%'], data: stats.value.categories, label: { formatter: '{b}\\n{d}%' } }] }))
const statusOption = computed(() => ({ tooltip: { trigger: 'axis' }, grid: { left: 70, right: 18, top: 24, bottom: 28 }, xAxis: { type: 'value', minInterval: 1 }, yAxis: { type: 'category', data: stats.value.statuses.map((item) => item.name) }, series: [{ type: 'bar', barWidth: 14, data: stats.value.statuses.map((item) => Number(item.value || 0)), itemStyle: { color: 'var(--cot-primary)', borderRadius: [0, 8, 8, 0] } }] }))
const focusItems = computed(() => attentionStatuses.value.length ? attentionStatuses.value.slice(0, 4).map((item) => ({ label: `${item.name} ${Number(item.value || 0)} 条`, type: /异常|错误|离线|禁用/.test(String(item.name)) ? 'danger' : 'warning' })) : [{ label: '当前没有异常状态记录', type: 'success' }])

const fetchData = async () => {
  loading.value = true
  try {
    const data = await adminDataApi.list(props.module.key, query)
    rows.value = data.list || []
    total.value = Number(data.total || 0)
    stats.value = { ...stats.value, ...(data.stats || {}) }
    capabilities.value = { view: false, create: false, update: false, delete: false, fields: [], ...(data.capabilities || {}) }
  } finally {
    loading.value = false
  }
}
const resetQuery = () => { Object.assign(query, { keyword: '', status: '全部', page: 1 }); fetchData() }
const statusType = (status) => {
  const value = String(status || '')
  if (/正常|有效|在线|正确|已掌握|已记录|已定稿|通过|DONE/.test(value)) return 'success'
  if (/异常|错误|离线|禁用|驳回|CANCELLED/.test(value)) return 'danger'
  if (/待|未|薄弱|编辑中|学习中|TODO|DOING/.test(value)) return 'warning'
  return 'info'
}
const fieldValue = (field) => form.value[field.name]
const setFieldValue = (field, value) => { form.value[field.name] = value }
const initialForm = () => Object.fromEntries(formFields.value.map((field) => [field.name, field.defaultValue ?? (field.type === 'boolean' ? false : '')]))
const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = ''
  form.value = initialForm()
  dialogVisible.value = true
}
const openRecord = async (row, mode) => {
  dialogMode.value = mode
  editingId.value = row.id
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    form.value = await adminDataApi.detail(props.module.key, row.id)
  } finally {
    dialogLoading.value = false
  }
}
const saveRecord = async () => {
  const missing = formFields.value.find((field) => field.required && (form.value[field.name] === '' || form.value[field.name] == null))
  if (missing) return ElMessage.warning(`请填写${missing.label}`)
  saving.value = true
  try {
    const payload = Object.fromEntries(formFields.value.map((field) => [field.name, form.value[field.name]]))
    if (dialogMode.value === 'create') await adminDataApi.create(props.module.key, payload)
    else await adminDataApi.update(props.module.key, editingId.value, payload)
    ElMessage.success(dialogMode.value === 'create' ? '新增成功' : '保存成功')
    dialogVisible.value = false
    await fetchData()
  } finally {
    saving.value = false
  }
}
const removeRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除“${row.title || row.id}”吗？删除后无法恢复。`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  } catch {
    return
  }
  await adminDataApi.remove(props.module.key, row.id)
  ElMessage.success('删除成功')
  if (rows.value.length === 1 && query.page > 1) query.page -= 1
  await fetchData()
}
const exportData = () => {
  if (!rows.value.length) return ElMessage.warning('当前没有可导出的数据')
  const headers = ['编号', '标题 / 名称', '用户 / 负责人', '分类', '状态', '指标值', '创建时间', '更新时间']
  const values = rows.value.map((row) => [row.id, row.title, row.name, row.category, row.status, row.metric, row.createdAt, row.updatedAt])
  const escapeCell = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`
  const csv = [headers, ...values].map((line) => line.map(escapeCell).join(',')).join('\\r\\n')
  const url = URL.createObjectURL(new Blob([`\\ufeff${csv}`], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `${title.value}-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
watch(() => props.module.key, resetQuery, { immediate: true })
</script>

<template>
  <div class="cot-page enterprise-page">
    <section class="module-hero">
      <div><span class="module-kicker">实时业务数据</span><h1 class="cot-page-title">{{ title }}</h1><p class="cot-page-desc">{{ module.description }}</p></div>
      <div class="page-actions">
        <el-button v-if="capabilities.create" :icon="Plus" type="primary" @click="openCreate">新增</el-button>
        <el-button :icon="Download" @click="exportData">导出当前页</el-button>
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
    </section>
    <section class="insight-grid"><article v-for="card in insightCards" :key="card.label" class="cot-card insight-card"><span>{{ card.label }}</span><strong>{{ card.value.toLocaleString() }}<em>{{ card.suffix }}</em></strong><small>{{ card.desc }}</small></article></section>
    <section class="visual-grid">
      <div class="cot-card chart-card large"><div class="chart-head"><strong>近 7 日新增趋势</strong><el-tag size="small" type="success">实时</el-tag></div><BaseChart :option="trendOption" height="280px" /></div>
      <div class="cot-card chart-card"><div class="chart-head"><strong>分类分布</strong><el-tag size="small">{{ stats.categories.length }} 类</el-tag></div><BaseChart :option="pieOption" height="280px" /></div>
      <div class="cot-card chart-card"><div class="chart-head"><strong>状态结构</strong><el-tag size="small" type="warning">{{ attentionCount }} 关注</el-tag></div><BaseChart :option="statusOption" height="240px" /></div>
      <div class="cot-card focus-card"><div class="chart-head"><strong>重点关注</strong><el-tag size="small" type="danger">{{ focusItems.length }}</el-tag></div><div class="focus-list"><div v-for="item in focusItems" :key="item.label" class="focus-item"><el-tag :type="item.type" effect="light">状态</el-tag><span>{{ item.label }}</span></div></div></div>
    </section>
    <section class="cot-card cot-toolbar"><el-input v-model="query.keyword" clearable placeholder="关键词检索：ID / 标题 / 用户 / 分类" :prefix-icon="Search" @keyup.enter="fetchData" /><el-select v-model="query.status" placeholder="状态"><el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" /></el-select><el-button :icon="Refresh" @click="resetQuery">重置</el-button><el-button type="primary" :icon="Search" @click="fetchData">查询</el-button></section>
    <section class="cot-card cot-table-card">
      <el-table v-loading="loading" :data="rows" row-key="id" stripe empty-text="当前模块暂无真实数据">
        <el-table-column prop="id" label="编号" min-width="120" /><el-table-column prop="title" label="标题 / 名称" min-width="220" show-overflow-tooltip /><el-table-column prop="name" label="用户 / 负责人" min-width="130" show-overflow-tooltip /><el-table-column prop="category" label="分类" min-width="130" show-overflow-tooltip /><el-table-column prop="metric" label="指标值" min-width="100" sortable /><el-table-column prop="createdAt" label="创建时间" min-width="165" /><el-table-column prop="updatedAt" label="更新时间" min-width="165" /><el-table-column prop="status" label="状态" min-width="110"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column v-if="hasActions" fixed="right" label="操作" min-width="180"><template #default="{ row }"><el-button v-if="capabilities.view" link type="primary" :icon="View" @click="openRecord(row, 'view')">查看</el-button><el-button v-if="capabilities.update" link type="primary" :icon="Edit" @click="openRecord(row, 'edit')">编辑</el-button><el-button v-if="capabilities.delete" link type="danger" :icon="Delete" @click="removeRecord(row)">删除</el-button></template></el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" background layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="fetchData" @current-change="fetchData" /></div>
    </section>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <el-scrollbar max-height="62vh" v-loading="dialogLoading"><el-form label-position="top" class="crud-form"><el-form-item v-for="field in formFields" :key="field.name" :label="field.label" :required="field.required"><el-select v-if="field.type === 'select'" :model-value="fieldValue(field)" :disabled="readonlyDialog" @update:model-value="setFieldValue(field, $event)"><el-option v-for="option in field.options" :key="option" :label="option" :value="option" /></el-select><el-switch v-else-if="field.type === 'boolean'" :model-value="Boolean(fieldValue(field))" :disabled="readonlyDialog" @update:model-value="setFieldValue(field, $event)" /><el-input-number v-else-if="field.type === 'number'" :model-value="fieldValue(field)" :disabled="readonlyDialog" controls-position="right" @update:model-value="setFieldValue(field, $event)" /><el-date-picker v-else-if="field.type === 'date'" :model-value="fieldValue(field)" :disabled="readonlyDialog" type="date" value-format="YYYY-MM-DD" @update:model-value="setFieldValue(field, $event)" /><el-date-picker v-else-if="field.type === 'datetime'" :model-value="fieldValue(field)" :disabled="readonlyDialog" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" @update:model-value="setFieldValue(field, $event)" /><el-input v-else-if="field.type === 'textarea'" :model-value="fieldValue(field)" :disabled="readonlyDialog" type="textarea" :rows="4" @update:model-value="setFieldValue(field, $event)" /><el-input v-else :model-value="fieldValue(field)" :disabled="readonlyDialog" @update:model-value="setFieldValue(field, $event)" /></el-form-item></el-form></el-scrollbar>
      <template #footer><el-button @click="dialogVisible = false">关闭</el-button><el-button v-if="!readonlyDialog" type="primary" :loading="saving" @click="saveRecord">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.enterprise-page{gap:18px}.module-hero{display:flex;align-items:flex-end;justify-content:space-between;gap:18px;padding:22px;border-radius:8px;color:#fff;background:linear-gradient(135deg,color-mix(in srgb,var(--cot-primary) 88%,#111827),color-mix(in srgb,var(--cot-primary) 48%,#0f172a));box-shadow:var(--cot-shadow)}.module-hero .cot-page-title,.module-hero .cot-page-desc{color:#fff}.module-hero .cot-page-desc{opacity:.78}.module-kicker{display:inline-flex;margin-bottom:8px;font-size:12px;font-weight:800;opacity:.78}.page-actions,.chart-head{display:flex;align-items:center;gap:10px}.chart-head{justify-content:space-between;margin-bottom:10px}.insight-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px}.insight-card{padding:18px}.insight-card span,.insight-card small{display:block;color:var(--el-text-color-secondary);font-size:12px}.insight-card strong{display:block;margin:8px 0;color:var(--el-text-color-primary);font-size:28px;line-height:1}.insight-card em{margin-left:4px;font-size:12px;font-style:normal}.visual-grid{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(320px,.75fr);gap:14px}.chart-card,.focus-card{padding:18px}.focus-list{display:grid;gap:12px;margin-top:16px}.focus-item{display:flex;align-items:center;gap:10px;padding:12px;border:1px solid var(--el-border-color-lighter);border-radius:8px;background:var(--el-fill-color-extra-light);color:var(--el-text-color-regular);font-size:13px}.pagination-wrap{display:flex;justify-content:flex-end;padding:16px}.crud-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));column-gap:18px}.crud-form :deep(.el-form-item){min-width:0}.crud-form :deep(.el-input),.crud-form :deep(.el-select),.crud-form :deep(.el-input-number),.crud-form :deep(.el-date-editor){width:100%}@media(max-width:1280px){.insight-grid{grid-template-columns:repeat(2,1fr)}.visual-grid{grid-template-columns:1fr}}@media(max-width:768px){.module-hero{align-items:stretch;flex-direction:column}.insight-grid{grid-template-columns:1fr}.crud-form{grid-template-columns:1fr}}
</style>