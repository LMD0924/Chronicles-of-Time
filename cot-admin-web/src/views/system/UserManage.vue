<script setup>
import messageApi from '@/utils/messageApi'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Delete, Edit, Key, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { adminUserApi } from '@/api/adminUsers'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const passwordDialog = ref(false)
const passwordForm = reactive({ id: null, username: '', password: '' })

const query = reactive({
  keyword: '',
  status: null,
  userType: 1,
  page: 1,
  pageSize: 10,
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  roleCode: 'USER',
  userType: 1,
  status: 1,
  avatar: '',
  introduction: '',
})

const statusOptions = [
  { label: '启用', value: 1, type: 'success' },
  { label: '禁用', value: 0, type: 'info' },
  { label: '锁定', value: 2, type: 'warning' },
  { label: '注销', value: 3, type: 'danger' },
]

const userTypeOptions = [
  { label: '个人用户', value: 1 },
  { label: '管理员', value: 2 },
]

const roleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' },
  { label: '超级管理员', value: 'SUPER_ADMIN' },


]

const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: dialogMode.value === 'create' ? [{ required: true, message: '请输入初始密码', trigger: 'blur' }] : [],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}))

const metrics = computed(() => ({
  total: total.value,
  enabled: rows.value.filter(item => item.status === 1).length,
  disabled: rows.value.filter(item => item.status === 0).length,
  admins: rows.value.filter(item => ['ADMIN', 'SUPER_ADMIN'].includes(item.role)).length,
}))

const statusMeta = (status) => statusOptions.find(item => item.value === status) || statusOptions[0]
const userTypeLabel = (value) => userTypeOptions.find(item => item.value === value)?.label || '个人用户'

const normalizePage = (data) => {
  if (!data) return { list: [], total: 0 }
  if (Array.isArray(data)) return { list: data, total: data.length }
  return {
    list: data.list || data.records || [],
    total: Number(data.total || 0),
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await adminUserApi.list({
      keyword: query.keyword || undefined,
      status: query.status ?? undefined,
      userType: query.userType ?? undefined,
      page: query.page,
      pageSize: query.pageSize,
    })
    const pageData = normalizePage(data)
    rows.value = pageData.list
    total.value = pageData.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.status = null
  query.userType = 1
  query.page = 1
  fetchData()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    name: '',
    email: '',
    phone: '',
    roleCode: 'USER',
    userType: 1,
    status: 1,
    avatar: '',
    introduction: '',
  })
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  resetForm()
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    name: row.name || '',
    email: row.email || '',
    phone: row.phone || '',
    roleCode: row.role || row.roles?.[0] || 'USER',
    userType: row.userType || 1,
    status: row.status ?? 1,
    avatar: row.avatar || '',
    introduction: row.introduction || '',
  })
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  const payload = {
    username: form.username.trim(),
    password: form.password || undefined,
    name: form.name.trim(),
    email: form.email || null,
    phone: form.phone || null,
    roleCode: form.roleCode,
    userType: form.userType,
    status: form.status,
    avatar: form.avatar || null,
    introduction: form.introduction || null,
  }
  if (dialogMode.value === 'create') {
    await adminUserApi.create(payload)
    messageApi.success('用户已创建')
  } else {
    await adminUserApi.update(form.id, payload)
    messageApi.success('用户已更新')
  }
  dialogVisible.value = false
  fetchData()
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await adminUserApi.updateStatus(row.id, nextStatus)
  messageApi.success(nextStatus === 1 ? '用户已启用' : '用户已禁用')
  fetchData()
}

const openResetPassword = (row) => {
  Object.assign(passwordForm, { id: row.id, username: row.username, password: '' })
  passwordDialog.value = true
}

const submitPassword = async () => {
  if (!passwordForm.password) return messageApi.warning('请输入新密码')
  await adminUserApi.resetPassword(passwordForm.id, passwordForm.password)
  messageApi.success('密码已重置')
  passwordDialog.value = false
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '删除确认', { type: 'warning' })
  await adminUserApi.remove(row.id)
  messageApi.success('用户已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <div class="cot-page personal-user-page">
    <section class="user-hero">
      <div>
        <span>Identity Ops</span>
        <h1 class="cot-page-title">个人用户管理</h1>
        <p class="cot-page-desc">直接管理 auth-center 的 iam_user、iam_user_role 数据，支持账号查询、新增、编辑、启停和重置密码。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增个人用户</el-button>
    </section>

    <section class="metric-grid">
      <article class="cot-card metric"><span>查询总数</span><strong>{{ metrics.total }}</strong></article>
      <article class="cot-card metric"><span>当前页启用</span><strong>{{ metrics.enabled }}</strong></article>
      <article class="cot-card metric"><span>当前页禁用</span><strong>{{ metrics.disabled }}</strong></article>
      <article class="cot-card metric"><span>当前页管理员</span><strong>{{ metrics.admins }}</strong></article>
    </section>

    <section class="cot-card toolbar">
      <el-input v-model="query.keyword" clearable placeholder="搜索用户名 / 姓名 / 邮箱 / 手机号" :prefix-icon="Search" @keyup.enter="fetchData" />
      <el-select v-model="query.userType" clearable placeholder="用户类型">
        <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="账号状态">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
    </section>

    <section class="cot-card table-card">
      <el-table v-loading="loading" :data="rows" row-key="id" stripe>
        <el-table-column prop="id" label="用户ID" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <img v-if="row.avatar" :src="row.avatar" alt="">
              <span v-else class="avatar-fallback">{{ (row.name || row.username || 'U').slice(0, 1) }}</span>
              <div>
                <strong>{{ row.name || row.username }}</strong>
                <small>@{{ row.username }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="类型" min-width="110">
          <template #default="{ row }">{{ userTypeLabel(row.userType) }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="130">
          <template #default="{ row }"><el-tag>{{ row.role || row.roles?.[0] || 'USER' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="180" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button link type="info" :icon="Key" @click="openResetPassword(row)">重置密码</el-button>
            <el-button link type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增个人用户' : '编辑个人用户'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" maxlength="64" /></el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="初始密码" prop="password"><el-input v-model="form.password" type="password" show-password maxlength="64" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" maxlength="80" /></el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" class="w-full">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" class="w-full">
            <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" maxlength="128" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" maxlength="32" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="form.avatar" maxlength="512" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.introduction" type="textarea" :rows="3" maxlength="512" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialog" title="重置密码" width="420px">
      <el-form label-width="88px">
        <el-form-item label="用户"><el-input :model-value="passwordForm.username" disabled /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="passwordForm.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.personal-user-page {
  gap: 18px;
}

.user-hero,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.user-hero {
  border-radius: 14px;
  background: linear-gradient(135deg, #0f766e, #1d4ed8);
  padding: 24px;
  color: white;
}

.user-hero span {
  font-size: 12px;
  font-weight: 800;
  opacity: 0.82;
}

.user-hero .cot-page-title,
.user-hero .cot-page-desc {
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
  max-width: 360px;
}

.table-card {
  overflow: hidden;
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
  border-radius: 8px;
}

.user-cell img {
  object-fit: cover;
}

.avatar-fallback {
  display: grid;
  place-items: center;
  background: var(--cot-primary-soft);
  color: var(--cot-primary);
  font-weight: 800;
}

.user-cell strong,
.user-cell small {
  display: block;
}

.user-cell small {
  color: var(--el-text-color-secondary);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}

.w-full {
  width: 100%;
}

@media (max-width: 900px) {
  .user-hero,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar .el-input {
    max-width: none;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
