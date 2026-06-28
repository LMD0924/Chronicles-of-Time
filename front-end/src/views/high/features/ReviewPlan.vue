<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

defineProps({ isDark: Boolean })
const router = useRouter()
const STORAGE = 'hs_review_plan'
const examDate = ref('2026-06-07')
const tasks = ref([])

const daysLeft = computed(() => {
  const d = Math.ceil((new Date(examDate.value) - new Date()) / 86400000)
  return d > 0 ? d : 0
})

const weekPlan = computed(() => {
  const weeks = Math.ceil(daysLeft.value / 7) || 1
  return Array.from({ length: Math.min(weeks, 12) }, (_, i) => ({
    week: i + 1,
    focus: tasks.value[i]?.focus || `第 ${i + 1} 周：薄弱科目强化 + 套卷`,
    done: tasks.value[i]?.done || false,
  }))
})

const load = () => {
  const saved = JSON.parse(localStorage.getItem(STORAGE) || '{}')
  examDate.value = saved.examDate || examDate.value
  tasks.value = saved.tasks || []
}

const save = () => {
  localStorage.setItem(STORAGE, JSON.stringify({ examDate: examDate.value, tasks: tasks.value }))
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="app-card-surface p-5 flex flex-wrap items-center gap-4">
      <div>
        <p class="text-sm text-zinc-500">高考倒计时</p>
        <p class="text-4xl font-bold app-gradient-text">{{ daysLeft }} 天</p>
      </div>
      <input v-model="examDate" type="date" class="px-3 py-2 rounded-lg border text-sm" @change="save" />
      <button type="button" class="app-btn-secondary text-sm" @click="router.push('/GraphView')">关联知识点图谱</button>
    </div>
    <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
      <div v-for="w in weekPlan" :key="w.week" class="app-card-surface-flat p-4">
        <p class="text-xs text-brand-600 font-semibold mb-1">第 {{ w.week }} 周</p>
        <p class="text-sm">{{ w.focus }}</p>
      </div>
    </div>
  </div>
</template>
