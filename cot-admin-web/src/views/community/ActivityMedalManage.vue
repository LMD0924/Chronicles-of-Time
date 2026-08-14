<script setup>
import messageApi from '@/utils/messageApi'
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const users = ref([])
const rules = ref([])
const keyword = ref('')
const ruleDialog = ref(false)
const ruleForm = reactive({
  id: null,
  code: '',
  name: '',
  description: '',
  medalType: 'LOGIN_DAYS',
  thresholdValue: 1,
  icon: 'Award',
  color: '#2563eb',
  enabled: true,
})

const totals = computed(() => ({
  users: users.value.length,
  online: users.value.filter(item => item.online).length,
  medals: users.value.reduce((sum, item) => sum + (item.medalCount || 0), 0),
  loginDays: users.value.reduce((sum, item) => sum + (item.totalLoginDays || 0), 0),
}))

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await request.get('/activity/admin/users', { keyword: keyword.value, onlineMinutes: 5 }) || []
  } finally {
    loading.value = false
  }
}

const fetchRules = async () => {
  rules.value = await request.get('/activity/admin/medal-rules') || []
}

const resetRule = () => {
  Object.assign(ruleForm, {
    id: null,
    code: '',
    name: '',
    description: '',
    medalType: 'LOGIN_DAYS',
    thresholdValue: 1,
    icon: 'Award',
    color: '#2563eb',
    enabled: true,
  })
}

const openRule = (row) => {
  resetRule()
  if (row) Object.assign(ruleForm, row)
  ruleDialog.value = true
}

const saveRule = async () => {
  if (!ruleForm.name || !ruleForm.medalType || !ruleForm.thresholdValue) {
    return messageApi.warning('请填写规则名称、类型和阈值')
  }
  await request.post('/activity/admin/medal-rules', ruleForm)
  messageApi.success('勋章规则已保存')
  ruleDialog.value = false
  fetchRules()
}

const switchRule = async (row) => {
  await request.post(`/activity/admin/medal-rules/${row.id}/status`, { enabled: row.enabled })
  messageApi.success('状态已更新')
}

const formatDuration = (seconds = 0) => {
  const safe = Number(seconds || 0)
  const hours = Math.floor(safe / 3600)
  const minutes = Math.floor((safe % 3600) / 60)
  return `${hours}h ${minutes}m`
}

onMounted(() => {
  fetchUsers()
  fetchRules()
})
</script>

<template>
  <div class="cot-page activity-admin-page">
    <section class="admin-hero">
      <div>
        <span>Activity Ops</span>
        <h1 class="cot-page-title">打卡与勋章管理</h1>
        <p class="cot-page-desc">查看用户登录天数、在线时长、勋章发放情况，并维护自动发放规则。</p>
      </div>
      <el-button type="primary" @click="openRule()">新增勋章规则</el-button>
    </section>

    <section class="metric-grid">
      <article class="cot-card metric"><span>活跃用户</span><strong>{{ totals.users }}</strong></article>
      <article class="cot-card metric"><span>当前在线</span><strong>{{ totals.online }}</strong></article>
      <article class="cot-card metric"><span>已发勋章</span><strong>{{ totals.medals }}</strong></article>
      <article class="cot-card metric"><span>累计登录天数</span><strong>{{ totals.loginDays }}</strong></article>
    </section>

    <section class="cot-card toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索用户 ID / 账号 / 昵称" @keyup.enter="fetchUsers" />
      <el-button type="primary" @click="fetchUsers">查询</el-button>
      <el-button @click="keyword = ''; fetchUsers()">重置</el-button>
    </section>

    <section class="cot-card table-card">
      <div class="table-title">用户活跃统计</div>
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="userId" label="用户ID" min-width="160" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <img v-if="row.avatar" :src="row.avatar" alt="">
              <span v-else class="avatar-fallback">{{ (row.name || row.username || 'U').slice(0, 1) }}</span>
              <div>
                <strong>{{ row.name || row.username || '未命名用户' }}</strong>
                <small>@{{ row.username || row.userId }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalLoginDays" label="累计登录" min-width="100" />
        <el-table-column prop="continuousLoginDays" label="连续登录" min-width="100" />
        <el-table-column label="今日在线" min-width="120"><template #default="{ row }">{{ formatDuration(row.todayOnlineSeconds) }}</template></el-table-column>
        <el-table-column label="累计在线" min-width="120"><template #default="{ row }">{{ formatDuration(row.totalOnlineSeconds) }}</template></el-table-column>
        <el-table-column prop="medalCount" label="勋章数" min-width="90" />
        <el-table-column prop="medalScore" label="积分" min-width="90" />
        <el-table-column label="在线" min-width="90">
          <template #default="{ row }"><el-tag :type="row.online ? 'success' : 'info'">{{ row.online ? '在线' : '离线' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="lastSeenAt" label="最近在线" min-width="180" />
      </el-table>
    </section>

    <section class="cot-card table-card">
      <div class="table-title">勋章规则</div>
      <el-table :data="rules" stripe>
        <el-table-column prop="code" label="编码" min-width="170" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="medalType" label="类型" min-width="160" />
        <el-table-column prop="thresholdValue" label="阈值" min-width="90" />
        <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="启用" min-width="90">
          <template #default="{ row }"><el-switch v-model="row.enabled" @change="switchRule(row)" /></template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }"><el-button link type="primary" @click="openRule(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="ruleDialog" :title="ruleForm.id ? '编辑勋章规则' : '新增勋章规则'" width="620px">
      <el-form label-width="110px">
        <el-form-item label="规则编码"><el-input v-model="ruleForm.code" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="ruleForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="ruleForm.medalType" class="w-full">
            <el-option label="累计登录天数" value="LOGIN_DAYS" />
            <el-option label="连续登录天数" value="STREAK_DAYS" />
            <el-option label="累计在线小时" value="ONLINE_HOURS" />
            <el-option label="今日在线分钟" value="TODAY_ONLINE_MINUTES" />
            <el-option label="综合积分" value="SCORE" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值"><el-input-number v-model="ruleForm.thresholdValue" :min="1" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="ruleForm.icon" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="ruleForm.color" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="ruleForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="ruleForm.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.activity-admin-page {
  gap: 18px;
}

.admin-hero,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-hero {
  border-radius: 14px;
  background: linear-gradient(135deg, #0f766e, #1d4ed8);
  padding: 24px;
  color: white;
}

.admin-hero span {
  font-size: 12px;
  font-weight: 800;
  opacity: 0.8;
}

.admin-hero .cot-page-title,
.admin-hero .cot-page-desc {
  color: white;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric {
  padding: 18px;
}

.metric span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 8px;
  font-size: 30px;
}

.toolbar {
  padding: 14px;
}

.toolbar .el-input {
  max-width: 380px;
}

.table-card {
  overflow: hidden;
}

.table-title {
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  font-weight: 800;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell img,
.avatar-fallback {
  width: 36px;
  height: 36px;
  border-radius: 999px;
}

.avatar-fallback {
  display: grid;
  place-items: center;
  background: var(--cot-primary-soft);
  color: var(--cot-primary);
  font-weight: 800;
}

.user-cell small {
  display: block;
  color: var(--el-text-color-secondary);
}

.w-full {
  width: 100%;
}

@media (max-width: 900px) {
  .admin-hero,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
