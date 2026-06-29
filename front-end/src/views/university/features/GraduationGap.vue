<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const props = defineProps({ majorId: Number, isDark: Boolean })
const router = useRouter()
const gap = ref(null)
const loading = ref(false)

const load = async () => {
  if (!props.majorId) return
  loading.value = true
  try {
    const res = await request.get('/student-course/gap-analysis', { majorId: props.majorId })
    if (res.code === 200) gap.value = res.data
  } finally {
    loading.value = false
  }
}

watch(() => props.majorId, load, { immediate: true })
</script>

<template>
  <div v-if="loading" class="app-card-surface p-8 text-center text-zinc-500">分析中...</div>
  <div v-else-if="gap" class="space-y-4">
    <div class="grid md:grid-cols-3 gap-4">
      <div class="app-card-surface p-4 text-center">
        <p class="text-xs text-zinc-500 mb-1">总学分缺口</p>
        <p class="text-2xl font-bold text-brand-600">{{ gap.totalCreditsShort }}</p>
      </div>
      <div class="app-card-surface p-4 text-center">
        <p class="text-xs text-zinc-500 mb-1">必修缺口</p>
        <p class="text-2xl font-bold text-red-500">{{ gap.compulsoryCreditsShort }}</p>
      </div>
      <div class="app-card-surface p-4 text-center">
        <p class="text-xs text-zinc-500 mb-1">选修缺口</p>
        <p class="text-2xl font-bold text-amber-500">{{ gap.electiveCreditsShort }}</p>
      </div>
    </div>

    <div class="app-card-surface p-5">
      <h3 class="font-semibold mb-3">未修/未通过 · 必修 ({{ gap.missingCompulsory?.length || 0 }})</h3>
      <ul class="space-y-2">
        <li v-for="c in gap.missingCompulsory" :key="c.courseId" class="flex justify-between text-sm py-2 border-b last:border-0" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
          <span>{{ c.name }} <span class="text-zinc-400">({{ c.credit }}学分)</span></span>
          <span class="text-zinc-400">第{{ c.term }}学期</span>
        </li>
        <li v-if="!gap.missingCompulsory?.length" class="text-zinc-500 text-sm">必修已满足</li>
      </ul>
    </div>

    <div class="app-card-surface p-5">
      <h3 class="font-semibold mb-3">未修/未通过 · 选修 ({{ gap.missingElective?.length || 0 }})</h3>
      <ul class="space-y-2">
        <li v-for="c in gap.missingElective" :key="c.courseId" class="flex justify-between text-sm py-2 border-b last:border-0" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
          <span>{{ c.name }}</span>
          <span class="text-zinc-400">{{ c.credit }} 学分</span>
        </li>
      </ul>
    </div>

    <div v-if="gap.prerequisiteGaps?.length" class="app-card-surface p-5 border-l-4 border-amber-400">
      <h3 class="font-semibold mb-3">先修未满足 ({{ gap.prerequisiteGaps.length }})</h3>
      <ul class="space-y-2 text-sm">
        <li v-for="(p, i) in gap.prerequisiteGaps" :key="i">
          <span class="font-medium">{{ p.courseName }}</span>
          <span class="text-zinc-500"> 需先修：{{ p.prerequisite }}</span>
        </li>
      </ul>
    </div>

    <button type="button" class="app-btn-primary" @click="router.push('/CourseTree')">去课程树补修规划</button>
  </div>
</template>
