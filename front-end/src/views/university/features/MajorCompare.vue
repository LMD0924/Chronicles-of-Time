<script setup>
import { ref, watch } from 'vue'
import { useUniversityData } from '@/composables/useUniversityData'
import request from '@/utils/request'

const props = defineProps({ majorId: Number, isDark: Boolean })
const { majors, loadMajors } = useUniversityData()
const majorB = ref(null)
const compare = ref(null)
const loading = ref(false)

const runCompare = async () => {
  if (!props.majorId || !majorB.value) return
  loading.value = true
  try {
    const res = await request.get('/university/major/compare', {
      majorId1: props.majorId,
      majorId2: majorB.value,
    })
    if (res.code === 200) compare.value = res.data
  } finally {
    loading.value = false
  }
}

watch([() => props.majorId, majorB], runCompare)
watch(() => props.majorId, (id) => {
  if (id && majors.value.length > 1) {
    const other = majors.value.find((m) => m.id !== id)
    if (other) majorB.value = other.id
  }
})
loadMajors()
</script>

<template>
  <div class="space-y-4">
    <div class="app-card-surface p-4 flex flex-wrap gap-4 items-center">
      <span class="text-sm text-zinc-500">对比专业 B：</span>
      <select v-model="majorB" class="px-3 py-2 rounded-lg border text-sm" :class="isDark ? 'bg-dark-surface border-dark-border' : 'border-zinc-200'">
        <option v-for="m in majors.filter(x => x.id !== majorId)" :key="m.id" :value="m.id">{{ m.name }}</option>
      </select>
      <button type="button" class="app-btn-secondary text-sm" @click="runCompare">刷新对比</button>
    </div>

    <div v-if="loading" class="text-center text-zinc-500 py-8">对比中...</div>
    <div v-else-if="compare" class="grid md:grid-cols-3 gap-4">
      <div class="app-card-surface p-4">
        <h4 class="font-semibold text-brand-600 mb-2">仅 {{ compare.majorA?.name }}</h4>
        <p class="text-2xl font-bold mb-3">{{ compare.onlyInACount }} 门</p>
        <ul class="text-xs space-y-1 max-h-48 overflow-y-auto text-zinc-600">
          <li v-for="c in compare.onlyInA" :key="c.courseCode">{{ c.name }}</li>
        </ul>
      </div>
      <div class="app-card-surface p-4 text-center">
        <h4 class="font-semibold mb-2">共同课程</h4>
        <p class="text-3xl font-bold text-brand-600">{{ compare.sharedCount }}</p>
      </div>
      <div class="app-card-surface p-4">
        <h4 class="font-semibold text-pink-600 mb-2">仅 {{ compare.majorB?.name }}</h4>
        <p class="text-2xl font-bold mb-3">{{ compare.onlyInBCount }} 门</p>
        <ul class="text-xs space-y-1 max-h-48 overflow-y-auto text-zinc-600">
          <li v-for="c in compare.onlyInB" :key="c.courseCode">{{ c.name }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>
