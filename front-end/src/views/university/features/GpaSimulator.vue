<script setup>
import { ref, watch, onMounted } from 'vue'
import request from '@/utils/request'

const props = defineProps({ majorId: Number, isDark: Boolean })
const courses = ref([])
const scores = ref({})
const result = ref(null)
const loading = ref(false)

const loadCourses = async () => {
  if (!props.majorId) return
  const [listRes, scRes] = await Promise.all([
    request.get('/course/list', { majorId: props.majorId }),
    request.get('/student-course/list'),
  ])
  if (listRes.code === 200) {
    const scMap = {}
    if (scRes.code === 200) {
      (scRes.data || []).forEach((s) => { scMap[s.courseId] = s })
    }
    courses.value = (listRes.data || []).map((c) => ({
      ...c,
      currentScore: scMap[c.id]?.score,
    }))
    const init = {}
    courses.value.forEach((c) => {
      init[c.id] = c.currentScore ?? ''
    })
    scores.value = init
  }
}

const simulate = async () => {
  loading.value = true
  try {
    const payload = {
      majorId: props.majorId,
      scores: Object.entries(scores.value)
        .filter(([, v]) => v !== '' && v != null)
        .map(([courseId, score]) => ({ courseId: Number(courseId), score: Number(score) })),
    }
    const res = await request.post('/student-course/simulate-gpa', payload)
    if (res.code === 200) result.value = res.data
  } finally {
    loading.value = false
  }
}

watch(() => props.majorId, loadCourses, { immediate: true })
onMounted(loadCourses)
</script>

<template>
  <div class="grid lg:grid-cols-2 gap-6">
    <div class="app-card-surface p-5 max-h-[70vh] overflow-y-auto">
      <h3 class="font-semibold mb-4">输入预计成绩</h3>
      <div v-for="c in courses" :key="c.id" class="flex items-center gap-3 py-2 border-b text-sm" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
        <span class="flex-1 truncate">{{ c.name }}</span>
        <input
          v-model.number="scores[c.id]"
          type="number"
          min="0"
          max="100"
          step="0.5"
          placeholder="分"
          class="w-20 px-2 py-1 rounded-lg border text-right focus:ring-2 focus:ring-brand-500/50"
          :class="isDark ? 'bg-dark-surface border-dark-border' : 'border-zinc-200'"
        />
      </div>
      <button type="button" class="app-btn-primary w-full mt-4" :disabled="loading" @click="simulate">
        {{ loading ? '计算中...' : '模拟 GPA' }}
      </button>
    </div>
    <div v-if="result" class="app-card-surface p-6 space-y-4">
      <div>
        <p class="text-sm text-zinc-500">当前 GPA</p>
        <p class="text-3xl font-bold">{{ result.currentGpa }}</p>
      </div>
      <div>
        <p class="text-sm text-zinc-500">模拟后 GPA</p>
        <p class="text-4xl font-bold app-gradient-text">{{ result.projectedGpa }}</p>
      </div>
      <div class="p-4 rounded-xl" :class="result.meetsScholarship ? 'bg-green-50 dark:bg-green-950/30' : 'bg-amber-50 dark:bg-amber-950/30'">
        <p class="font-medium" :class="result.meetsScholarship ? 'text-green-700' : 'text-amber-700'">
          {{ result.scholarshipTip }}
        </p>
        <p class="text-xs text-zinc-500 mt-1">奖学金参考线：{{ result.scholarshipLine }} · 计入 {{ result.countedCourses }} 门课</p>
      </div>
    </div>
    <div v-else class="app-card-surface p-8 text-center text-zinc-500">填写成绩后点击模拟</div>
  </div>
</template>
