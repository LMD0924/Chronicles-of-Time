<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

const groupKeyword = ref('')
const friendKeyword = ref('')
const groups = ref([])
const friendships = ref([])
const loadingGroups = ref(false)
const loadingFriends = ref(false)

const metrics = computed(() => ({
  groups: groups.value.length,
  members: groups.value.reduce((sum, item) => sum + (item.memberCount || 0), 0),
  friendships: friendships.value.length,
  activeFriends: friendships.value.filter(item => item.status === 'ACTIVE').length,
}))

const fetchGroups = async () => {
  loadingGroups.value = true
  try {
    groups.value = await request.get('/chat/admin/groups', { keyword: groupKeyword.value }) || []
  } finally {
    loadingGroups.value = false
  }
}

const fetchFriendships = async () => {
  loadingFriends.value = true
  try {
    friendships.value = await request.get('/chat/admin/friends', { keyword: friendKeyword.value }) || []
  } finally {
    loadingFriends.value = false
  }
}

onMounted(() => {
  fetchGroups()
  fetchFriendships()
})
</script>

<template>
  <div class="cot-page chat-admin-page">
    <section class="admin-hero">
      <div>
        <span>Chat Ops</span>
        <h1 class="cot-page-title">在线聊天管理</h1>
        <p class="cot-page-desc">查看群聊、成员规模、好友关系和最近消息概况。</p>
      </div>
    </section>

    <section class="metric-grid">
      <article class="cot-card metric"><span>群聊数量</span><strong>{{ metrics.groups }}</strong></article>
      <article class="cot-card metric"><span>群成员总数</span><strong>{{ metrics.members }}</strong></article>
      <article class="cot-card metric"><span>好友关系</span><strong>{{ metrics.friendships }}</strong></article>
      <article class="cot-card metric"><span>有效关系</span><strong>{{ metrics.activeFriends }}</strong></article>
    </section>

    <section class="grid-section">
      <article class="cot-card table-card">
        <div class="table-head">
          <strong>群聊列表</strong>
          <div class="actions">
            <el-input v-model="groupKeyword" clearable placeholder="群名 / 群号" @keyup.enter="fetchGroups" />
            <el-button type="primary" @click="fetchGroups">查询</el-button>
          </div>
        </div>
        <el-table v-loading="loadingGroups" :data="groups" stripe>
          <el-table-column prop="groupNo" label="群号" min-width="110" />
          <el-table-column prop="name" label="群名称" min-width="150" />
          <el-table-column prop="ownerId" label="群主ID" min-width="160" />
          <el-table-column prop="memberCount" label="成员数" min-width="90" />
          <el-table-column label="最近消息" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.lastMessage || '暂无' }}</template>
          </el-table-column>
          <el-table-column prop="lastMessageAt" label="最近消息时间" min-width="170" />
          <el-table-column label="可搜索" min-width="90">
            <template #default="{ row }"><el-tag :type="row.searchable === false ? 'info' : 'success'">{{ row.searchable === false ? '否' : '是' }}</el-tag></template>
          </el-table-column>
        </el-table>
      </article>

      <article class="cot-card table-card">
        <div class="table-head">
          <strong>好友关系</strong>
          <div class="actions">
            <el-input v-model="friendKeyword" clearable placeholder="用户ID / 账号 / 昵称" @keyup.enter="fetchFriendships" />
            <el-button type="primary" @click="fetchFriendships">查询</el-button>
          </div>
        </div>
        <el-table v-loading="loadingFriends" :data="friendships" stripe>
          <el-table-column prop="id" label="关系ID" min-width="160" />
          <el-table-column prop="userId" label="用户ID" min-width="160" />
          <el-table-column prop="friendId" label="好友ID" min-width="160" />
          <el-table-column label="好友" min-width="150">
            <template #default="{ row }">{{ row.name || row.username || '未命名用户' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100" />
          <el-table-column label="未读" min-width="80">
            <template #default="{ row }">{{ row.unreadCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="最近消息" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.lastMessage || '暂无' }}</template>
          </el-table-column>
          <el-table-column prop="lastMessageAt" label="最近消息时间" min-width="170" />
        </el-table>
      </article>
    </section>
  </div>
</template>

<style scoped>
.chat-admin-page {
  gap: 18px;
}

.admin-hero {
  border-radius: 14px;
  background: linear-gradient(135deg, #1d4ed8, #7c3aed);
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

.grid-section {
  display: grid;
  gap: 18px;
}

.table-card {
  overflow: hidden;
}

.table-head,
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-head {
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.actions .el-input {
  width: 260px;
}

@media (max-width: 900px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .table-head,
  .actions {
    align-items: stretch;
    flex-direction: column;
  }

  .actions .el-input {
    width: 100%;
  }
}
</style>
