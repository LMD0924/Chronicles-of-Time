<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

defineProps({ isDark: Boolean })
const hideScores = ref(true)
const hideMood = ref(true)
const summary = ref({ name: '', stage: '高中', gpaOrScore: '-', volunteer: '-', recent: [] })

const load = async () => {
  const u = await request.get('/user/getUserById')
  if (u.code === 200) summary.value.name = u.data?.name || '同学'
  const g = await request.post('/growth/list', { page: 1, size: 5 })
  if (g.code === 200) {
    summary.value.recent = (g.data?.records || g.data?.list || []).map((r) => ({
      date: r.recordDate,
      title: r.examName || r.activityName || '成长记录',
      detail: hideScores.value ? '（成绩已隐藏）' : (r.totalScore ? `${r.totalScore} 分` : r.studyNotes),
    }))
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4 max-w-2xl">
    <div class="app-card-surface p-4 flex flex-wrap gap-4 text-sm">
      <label class="flex items-center gap-2"><input v-model="hideScores" type="checkbox" @change="load" /> 隐藏成绩</label>
      <label class="flex items-center gap-2"><input v-model="hideMood" type="checkbox" /> 隐藏心情压力</label>
      <span class="text-zinc-500">家长只读视图（无编辑权限）</span>
    </div>
    <div class="app-card-surface p-6">
      <h3 class="font-semibold text-lg mb-2">{{ summary.name }} 的学习概况</h3>
      <p class="text-sm text-zinc-500 mb-4">阶段：{{ summary.stage }}</p>
      <h4 class="font-medium mb-2">近期动态</h4>
      <ul class="space-y-2 text-sm">
        <li v-for="(r, i) in summary.recent" :key="i" class="py-2 border-b last:border-0">
          <span class="text-zinc-400">{{ r.date }}</span> — {{ r.title }}
          <span v-if="!hideMood" class="block text-zinc-500">{{ r.detail }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>
