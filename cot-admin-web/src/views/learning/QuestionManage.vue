<!--
  文件说明：后台题目审核管理页面。
-->
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const rows = ref([])
const detailVisible = ref(false)
const current = ref(null)

const query = reactive({
  auditStatus: 'pending',
  userId: '',
  categoryLevel: '',
  subjectName: '',
  keyword: '',
})

const statusOptions = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
  { label: '全部', value: '' },
]

const categories = ['高中', '大学', '考公', '考研', '考证', '专业面试']

const stats = computed(() => [
  { label: '当前列表', value: rows.value.length, type: 'primary' },
  { label: '待审核', value: rows.value.filter((item) => item.auditStatus === 'pending').length, type: 'warning' },
  { label: '已通过', value: rows.value.filter((item) => item.auditStatus === 'approved').length, type: 'success' },
  { label: '已驳回', value: rows.value.filter((item) => item.auditStatus === 'rejected').length, type: 'danger' },
])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    Object.entries(query).forEach(([key, value]) => {
      if (value !== '') params[key] = value
    })
    const data = await request.get('/question/admin/audit-list', { params })
    rows.value = Array.isArray(data) ? data : data?.data || []
  } catch (error) {
    console.error('题目审核列表加载失败', error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, {
    auditStatus: 'pending',
    userId: '',
    categoryLevel: '',
    subjectName: '',
    keyword: '',
  })
  fetchData()
}

const openDetail = (row) => {
  current.value = row
  detailVisible.value = true
}

const auditQuestion = async (row, auditStatus) => {
  const action = auditStatus === 'approved' ? '通过' : '驳回'
  let auditRemark = ''
  if (auditStatus === 'rejected') {
    try {
      const result = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputValidator: (value) => !!value?.trim(),
        inputErrorMessage: '驳回原因不能为空',
      })
      auditRemark = result.value
    } catch {
      return
    }
  } else {
    try {
      await ElMessageBox.confirm(`确认${action}这道题？`, '审核确认', { type: 'warning' })
    } catch {
      return
    }
  }

  try {
    await request.put(`/question/admin/audit/${row.id}`, {
      auditStatus,
      auditRemark,
    })
    ElMessage.success(`已${action}`)
    await fetchData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(options).split(/[,，]/).map((item) => item.trim()).filter(Boolean)
  }
}

const auditText = (status) => ({ pending: '待审核', approved: '已通过', rejected: '已驳回' }[status] || '待审核')
const auditType = (status) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[status] || 'info')

onMounted(fetchData)
</script>

<template>
  <div class="cot-page question-audit-page">
    <section class="module-hero">
      <div>
        <span class="module-kicker">学习中心 · 题目审核</span>
        <h1 class="cot-page-title">题目管理</h1>
        <p class="cot-page-desc">用户新增题目需要管理员审核，通过后才会进入用户自己的在线考试题库。</p>
      </div>
      <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
    </section>

    <section class="stat-grid">
      <article v-for="item in stats" :key="item.label" class="cot-card stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
      </article>
    </section>

    <section class="cot-card cot-toolbar">
      <el-select v-model="query.auditStatus" placeholder="审核状态">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.categoryLevel" clearable placeholder="第一层分类">
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input v-model="query.subjectName" clearable placeholder="科目 / 专业" />
      <el-input v-model="query.userId" clearable placeholder="用户 ID" />
      <el-input v-model="query.keyword" clearable placeholder="题干 / 知识点关键词" :prefix-icon="Search" @keyup.enter="fetchData" />
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
    </section>

    <section class="cot-card cot-table-card">
      <el-table v-loading="loading" :data="rows" row-key="id" stripe>
        <el-table-column prop="id" label="题目ID" min-width="120" />
        <el-table-column prop="createdBy" label="用户ID" min-width="120" />
        <el-table-column prop="categoryLevel" label="分类" min-width="100" />
        <el-table-column prop="subjectName" label="科目 / 专业" min-width="140" />
        <el-table-column prop="questionType" label="题型" min-width="90" />
        <el-table-column prop="difficultyLevel" label="难度" min-width="90" />
        <el-table-column prop="knowledgePoint" label="知识点" min-width="180" show-overflow-tooltip />
        <el-table-column prop="questionTitle" label="题干" min-width="300" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="审核状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)">{{ auditText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.auditStatus !== 'approved'" link type="success" :icon="Check" @click="auditQuestion(row, 'approved')">通过</el-button>
            <el-button v-if="row.auditStatus !== 'rejected'" link type="danger" :icon="Close" @click="auditQuestion(row, 'rejected')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="detailVisible" title="题目详情" width="760px" destroy-on-close>
      <div v-if="current" class="detail-body">
        <div class="meta-row">
          <el-tag>{{ current.categoryLevel }}</el-tag>
          <el-tag>{{ current.subjectName }}</el-tag>
          <el-tag>{{ current.questionType }}</el-tag>
          <el-tag>{{ current.difficultyLevel }}</el-tag>
          <el-tag :type="auditType(current.auditStatus)">{{ auditText(current.auditStatus) }}</el-tag>
        </div>
        <h3>{{ current.questionTitle }}</h3>
        <div v-if="current.knowledgePoint" class="plain-box">知识点：{{ current.knowledgePoint }}</div>
        <div v-if="current.options" class="option-list">
          <div v-for="item in parseOptions(current.options)" :key="item">{{ item }}</div>
        </div>
        <div class="answer-grid">
          <div><strong>正确答案</strong><p>{{ current.correctAnswer }}</p></div>
          <div><strong>分值</strong><p>{{ current.scoreValue || 2 }}</p></div>
        </div>
        <div v-if="current.answerAnalysis" class="plain-box">解析：{{ current.answerAnalysis }}</div>
        <div v-if="current.auditRemark" class="plain-box danger">审核意见：{{ current.auditRemark }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="current?.auditStatus !== 'approved'" type="success" @click="auditQuestion(current, 'approved')">通过</el-button>
        <el-button v-if="current?.auditStatus !== 'rejected'" type="danger" @click="auditQuestion(current, 'rejected')">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.question-audit-page {
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
  background: linear-gradient(135deg, color-mix(in srgb, var(--cot-primary) 88%, #111827), #0f172a);
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px;
}

.stat-card span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.stat-card strong {
  color: var(--el-text-color-primary);
  font-size: 26px;
}

.detail-body {
  display: grid;
  gap: 14px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-body h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  line-height: 1.7;
}

.plain-box,
.option-list div,
.answer-grid > div {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-extra-light);
  padding: 12px;
  color: var(--el-text-color-regular);
}

.plain-box.danger {
  color: var(--el-color-danger);
}

.option-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.answer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.answer-grid strong {
  color: var(--el-text-color-primary);
}

.answer-grid p {
  margin: 8px 0 0;
}

@media (max-width: 960px) {
  .stat-grid,
  .answer-grid,
  .option-list {
    grid-template-columns: 1fr;
  }

  .module-hero {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
