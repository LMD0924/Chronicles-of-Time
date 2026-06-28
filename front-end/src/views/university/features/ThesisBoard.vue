<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

defineProps({ majorId: Number, isDark: Boolean })
const router = useRouter()

const STAGES = [
  { key: 'topic', label: '选题', icon: '💡' },
  { key: 'proposal', label: '开题', icon: '📋' },
  { key: 'draft', label: '初稿', icon: '✍️' },
  { key: 'defense', label: '答辩', icon: '🎤' },
  { key: 'done', label: '完成', icon: '✅' },
]

const papers = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/paper/list')
    if (res.code === 200) papers.value = res.data || []
  } finally {
    loading.value = false
  }
}

const stageIndex = (stage) => STAGES.findIndex((s) => s.key === (stage || 'topic'))

const advanceStage = async (paper) => {
  const idx = stageIndex(paper.stage)
  const next = STAGES[Math.min(idx + 1, STAGES.length - 1)]
  await request.put('/paper/update', {
    id: paper.id,
    title: paper.title,
    supervisor: paper.supervisor,
    direction: paper.direction,
    content: paper.content,
    stage: next.key,
    status: next.key === 'done' ? 'passed' : 'in_progress',
  })
  load()
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <p class="text-sm text-zinc-500">拖拽式看板：点击卡片推进阶段</p>
      <button type="button" class="app-btn-secondary text-sm" @click="router.push('/Paper')">打开论文编辑</button>
    </div>
    <div v-if="loading" class="app-card-surface p-8 text-center text-zinc-500">加载中...</div>
    <div v-else class="grid grid-cols-2 md:grid-cols-5 gap-3 min-h-[320px]">
      <div v-for="stage in STAGES" :key="stage.key" class="app-card-surface-flat p-3 flex flex-col">
        <div class="text-center mb-3 pb-2 border-b" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
          <span class="text-lg">{{ stage.icon }}</span>
          <p class="text-xs font-semibold mt-1">{{ stage.label }}</p>
        </div>
        <div class="flex-1 space-y-2 overflow-y-auto">
          <div
            v-for="p in papers.filter(x => (x.stage || 'topic') === stage.key)"
            :key="p.id"
            class="p-2 rounded-lg text-xs cursor-pointer hover:ring-2 hover:ring-brand-300 transition-all"
            :class="isDark ? 'bg-zinc-800' : 'bg-brand-50'"
            @click="advanceStage(p)"
          >
            <p class="font-medium line-clamp-2">{{ p.title }}</p>
            <p class="text-zinc-500 mt-1">{{ p.supervisor || '未填导师' }}</p>
            <p class="text-brand-500 mt-1">点击推进 →</p>
          </div>
        </div>
      </div>
    </div>
    <p v-if="!papers.length" class="text-center text-zinc-500">暂无论文，请先在「写论文」创建</p>
  </div>
</template>
