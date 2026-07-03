<!--
  文件说明：拾光记前台应用高中阶段页面组件，承载高中阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

defineProps({ isDark: Boolean })
const router = useRouter()
const records = ref([])

const load = async () => {
  const res = await request.post('/growth/list', { page: 1, size: 20, stage: '高中' })
  if (res.code === 200) {
    records.value = (res.data?.records || res.data?.list || res.data || []).filter(
      (r) => r.activityName || r.competitionName || r.skillLearned || r.interestTested
    )
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <button type="button" class="app-btn-secondary text-sm" @click="router.push('/Records')">打开完整时光记录</button>
    <div class="grid md:grid-cols-2 gap-4">
      <div v-for="r in records" :key="r.id" class="app-card-surface p-4 text-sm">
        <p class="font-medium">{{ r.activityName || r.competitionName || r.skillLearned || '综合素质' }}</p>
        <p class="text-zinc-500 mt-1">{{ r.recordDate }} · {{ r.stage }}</p>
        <p v-if="r.competitionAward" class="text-brand-600 mt-2">{{ r.competitionAward }}</p>
      </div>
    </div>
    <p v-if="!records.length" class="text-zinc-500 text-center py-8">在「记录拾光」中添加活动/竞赛类记录后在此展示</p>
  </div>
</template>
